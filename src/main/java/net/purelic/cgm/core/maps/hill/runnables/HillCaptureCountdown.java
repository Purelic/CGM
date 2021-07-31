package net.purelic.cgm.core.maps.hill.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.maps.hill.events.HillCaptureEvent;
import net.purelic.cgm.core.maps.hill.events.HillLostEvent;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HillCaptureCountdown extends BukkitRunnable {

    private final Hill hill;
    private final int delay;
    private final double multiplier;
    private float progress;
    private int tick;

    public HillCaptureCountdown(Hill hill) {
        this.hill = hill;
        this.delay = NumberSetting.HILL_CAPTURE_DELAY.value();
        this.multiplier = NumberSetting.HILL_CAPTURE_MULTIPLIER.value();
        this.progress = this.getStartingProgress() * this.delay;
        this.tick = 0;
    }

    private float getStartingProgress() {
        // if no one controls the hill or the hill is fully reclaimed by the owner start them from 0
        if (this.hill.getControlledBy() == null || (this.hill.getControlledBy() == this.hill.getOwner() && this.hill.getProgress() == 1F))
            return 0F;
        else return this.hill.getProgress();
    }

    @Override
    public void run() {
        this.tick++;

        boolean empty = this.hill.getPlayers().isEmpty();
        boolean contested = this.hill.getTeams().size() > 1;

        // pause capturing if no one is on the hill or multiple teams are on the hill
        if (empty || contested) {
            if (empty && !ToggleSetting.CAPTURE_LOCK.isEnabled()) {
                this.hill.setCapturedBy(this.hill.isNeutral() ? null : this.hill.getOwner());
                this.hill.setControlledBy(this.hill.getCapturedBy());
                this.hill.resetProgress();
            }

            this.cancel();
            return;
        }

        MatchTeam team = this.hill.getTeams().get(0);

        // if this is the first team to start controlling the hill
        if (this.hill.getControlledBy() == null) {
            this.hill.setControlledBy(team);
        }

        // if capture lock is off reset the hill every time a new team takes control
        if (team != this.hill.getControlledBy() && !ToggleSetting.CAPTURE_LOCK.isEnabled()) {
            this.hill.setCapturedBy(this.hill.isNeutral() ? null : this.hill.getOwner());
            this.hill.setControlledBy(this.hill.getCapturedBy());
            this.hill.resetProgress();
            this.progress = 0F;
        }

        // do nothing if the team is the owner and the hill is fully reclaimed
        if (this.hill.getProgress() == 1F && team == this.hill.getOwner() && this.hill.getCapturedBy() == this.hill.getOwner()) {
            this.cancel();
            return;
        }

        // calculate bonus progress based on having multiple teammates on the hill
        double bonus = (0.1 * (this.multiplier / 100)) * (this.hill.getPlayers().size() - 1);

        // increase if controlled by this team
        // decrease if controlled by an enemy team (needs to be uncapped first)
        if (this.hill.getControlledBy() == team) this.progress += (0.1 + bonus);
        else this.progress -= (0.1 + bonus);

        float percent = Math.max(Math.min(this.progress / this.delay, 1F), 0F); // percent always min 0 and max 1

        if (percent == 0F) { // hill is uncapped
            // the event only fires if the hill previously fully capped, not reclaiming full control
            boolean lostHill = this.hill.getCapturedBy() != null
                && (this.hill.isNeutral() || this.hill.getOwner() != this.hill.getCapturedBy());

            if (lostHill) {
                Commons.callEvent(new HillLostEvent(this.hill, this.hill.getCapturedBy()));
            }

            // if a team has reclaimed a hill they own they don't need to recapture it
            if (!this.hill.isNeutral() && this.hill.getOwner() == team) {
                this.sendActionBar("Reclaimed " + this.hill.getName() + ChatColor.RESET + "!");
                this.hill.setCapturedBy(team);
                this.hill.setControlledBy(team);
                percent = 1F;
                this.cancel();
            } else {
                if (this.hill.isNeutral()) this.hill.setCapturedBy(null);
                this.hill.setControlledBy(team);
                this.progress = 0F; // normalize it in-case progress went below 0
            }

            this.hill.setProgress(percent);

            // update the scoreboard to reset the green score color (if applicable)
            ScoreboardManager.updateTeamBoard();
        } else if (percent == 1F) { // hill is fully captured

            // check required for when someone is reclaiming lost progress on a hill they've already captured
            if (this.hill.getCapturedBy() != team) {
                this.sendActionBar("Captured " + this.hill.getName() + ChatColor.RESET + "!");
                this.hill.setCapturedBy(team);
                this.hill.setProgress(percent);
                Commons.callEvent(new HillCaptureEvent(this.hill, team));
            } else {
                this.hill.setProgress(percent);
                this.sendActionBar("Reclaimed " + this.hill.getName() + ChatColor.RESET + "!");
            }

            this.cancel();
        } else { // capture in progress
            boolean sfx = this.tick % 4 == 0;
            float pitch = (1.5F * percent) + 0.5F;

            String prefix;

            if (!this.hill.isNeutral() && this.hill.getOwner() == team) {
                prefix = "Reclaiming";
            } else if (this.hill.getControlledBy() != team) {
                prefix = "Stealing";
            } else if (this.hill.getCapturedBy() == team) {
                prefix = "Reclaiming";
            } else {
                prefix = "Capturing";
            }

            String message = this.getProgressBar(prefix + " " + this.hill.getName(), percent);
            this.sendActionBar(message, sfx, pitch);
            this.hill.setProgress(percent);
        }
    }

    private void sendActionBar(String message) {
        this.sendActionBar(message, false, 0F);
    }

    private void sendActionBar(String message, boolean sfx, float pitch) {
        // it's possible players could get removed while looping so we make a copy (ConcurrentModificationException)
        List<Player> players = new ArrayList<>(this.hill.getPlayers());

        for (Player player : players) {
            // since we're running some things async, we double check if they are still in the hill and alive
            if (!this.hill.isInside(player.getLocation())
                || !MatchManager.getParticipant(player).isState(ParticipantState.ALIVE)
                || this.hill.getTeams().size() > 1) continue;

            ChatUtils.sendActionBar(player, message);

            if (sfx) player.playSound(player.getLocation(), Sound.CLICK, 1F, pitch);
        }
    }

    private String getProgressBar(String prefix, float percent) {
        char symbol = '|';
        ChatColor completed = ChatColor.GREEN;
        ChatColor incomplete = ChatColor.GRAY;
        int totalBars = 40;
        int progressBars = (int) (totalBars * percent);
        String progress = (new DecimalFormat("#.#").format(percent * 100));

        return prefix + " " +
            ChatColor.DARK_GRAY + "[" + ChatColor.RESET +
            StringUtils.repeat("" + completed + symbol, progressBars) +
            StringUtils.repeat("" + incomplete + symbol, totalBars - progressBars) +
            ChatColor.DARK_GRAY + "]" +
            ChatColor.AQUA + " " + progress + (progress.contains(".") ? "" : ".0") + "%";
    }

}
