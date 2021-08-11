package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.utils.EntityUtils;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class RespawnCountdown extends BukkitRunnable {

    private static final DecimalFormat FORMAT = new DecimalFormat("#.#");

    private double seconds;
    private final Participant participant;
    private final Player player;
    private EntityArmorStand entity;

    public RespawnCountdown(double seconds, Participant participant) {
        this.seconds = seconds;
        this.participant = participant;
        this.player = participant.getPlayer();
        this.setFrozen();
    }

    @Override
    public void run() {
        boolean respawnOnDrop = FlagUtils.respawnOnDrop(this.player);

        if (TaskUtils.isRunning(RoundCountdown.getCountdown())
            || !MatchState.isState(MatchState.STARTED)
            || respawnOnDrop) {
            this.cancelAndClearStand();
            PlayerUtils.setLevel(this.player, 0);
            ChatUtils.sendActionBar(this.player, "");

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setAllowFlight(true);
                }
            }.runTask(CGM.get());

            if (respawnOnDrop) FlagUtils.sendRespawnOnDropMessage(this.player);

            return;
        }

        if (!this.player.isOnline() || MatchTeam.getTeam(this.player) == MatchTeam.OBS) {
            this.cancelAndClearStand();

            if (this.player.isOnline()) {
                ChatUtils.sendActionBar(this.player, "");
                PlayerUtils.setLevel(this.player, 0);
                this.player.hideTitle();
            }

            return;
        }

        if (this.seconds <= 0) {
            this.cancelAndClearStand();
            this.respawnPlayer(this.participant);
            return;
        }

        String secondsFormat = FORMAT.format(this.seconds);
        ChatUtils.sendActionBar(
            this.player,
            "Respawning in " + ChatColor.AQUA + secondsFormat + (secondsFormat.contains(".") ? "" : ".0") + ChatColor.RESET + " seconds");

        float perc = (float) (this.seconds - ((int) this.seconds));
        PlayerUtils.setLevel(this.player, (int) (this.seconds + 1), perc);

        this.seconds -= 0.05;
    }

    private void setFrozen() {
        EntityArmorStand entity = EntityUtils.spawnArmorStand(this.player, this.player.getLocation().add(0, 0.25, 0));
        this.player.setAllowFlight(true);
        EntityUtils.attachEntity(this.player, entity);
        this.entity = entity;
    }

    private void destroyStand() {
        EntityUtils.destroyArmorStand(this.player, this.entity);
    }

    private void cancelAndClearStand() {
        this.destroyStand();
        this.cancel();
    }

    private void respawnPlayer(final Participant participant) {
        ChatUtils.sendActionBar(this.player, "");
        Commons.callEvent(new ParticipantRespawnEvent(participant, false));
    }

}
