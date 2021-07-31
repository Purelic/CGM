package net.purelic.cgm.scoreboards;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.LootType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.listeners.modules.GracePeriodModule;
import net.purelic.cgm.listeners.modules.LootChestRefillModule;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.HillUtils;
import net.purelic.cgm.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class MatchScoreboard {

    protected int objectives = 0;
    protected List<ScoreboardTimer> timers = new ArrayList<>();
    protected int slots = 16;
    protected int size = 0;
    protected int start = 0;

    abstract void update();

    public void init() {
        // add objective slots
        this.objectives = 0;

        int hills = MatchManager.getCurrentMap().getLoadedHills().size();
        this.objectives += !ToggleSetting.PERMANENT_HILLS.isEnabled() && hills > 0 ? (NumberSetting.HILL_MOVE_INTERVAL.value() > 0 ? 1 : hills) : 0;

        int flags = MatchManager.getCurrentMap().getLoadedFlags().size();
        this.objectives += flags > 0 ? (ToggleSetting.MOVING_FLAG.isEnabled() ? 1 : flags) : 0;

        // start of player/team slots
        this.start = this.objectives == 0 ? 0 : (this.objectives + 1);

        // add timer slots
        if (EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES)
            && NumberSetting.REFILL_EVENT.value() > 0
            && !EnumSetting.LOOT_TYPE.is(LootType.CUSTOM)) {
            this.timers.add(ScoreboardTimer.REFILL);
        }

        if (NumberSetting.GRACE_PERIOD.value() > 0) {
            this.timers.add(ScoreboardTimer.GRACE);
        }

        if (NumberSetting.WB_SHRINK_SPEED.value() > 0 && NumberSetting.WB_SHRINK_DELAY.value() == 0) {
            this.timers.add(ScoreboardTimer.BORDER);
        }

        // calculate how many slots we have for players/teams
        this.slots = 16;

        if (this.objectives > 0) {
            this.slots -= this.objectives + 1; // extra for blank line between objectives
            ScoreboardManager.setScore(this.objectives, "");
        }

        if (this.timers.size() > 0) {
            this.slots -= this.timers.size() + 1; // extra for blank line between timers
        }

        // set the scoreboard size
        this.size = 16 - this.slots;

        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        if (teamType != TeamType.SOLO) {
            this.size += teamType.getTeams().size();
        }

        // add timer slots
        this.updateTimers();

        // update the scoreboard
        this.update();

        // reset unused rows
        ScoreboardManager.resetScores(this.size);
    }

    protected String getScoreColor(MatchTeam team) {
        if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)
            && NumberSetting.FLAG_CARRIER_POINTS.value() > 0
            && FlagUtils.hasCarrier(team)) {
            return ChatColor.GREEN.toString();
        } else if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)
            && !EnumSetting.TEAM_TYPE.is(TeamType.SOLO)
            && HillUtils.hasCaptured(team)) {
            return ChatColor.GREEN.toString();
        }

        return "";
    }

    protected String getScoreColor(Participant participant) {
        if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)
            && NumberSetting.FLAG_CARRIER_POINTS.value() > 0
            && FlagUtils.isCarrier(participant)) {
            return ChatColor.GREEN.toString();
        } else if (EnumSetting.GAME_TYPE.is(GameType.KING_OF_THE_HILL)
            && EnumSetting.TEAM_TYPE.is(TeamType.SOLO)
            && HillUtils.hasCaptured(participant.getPlayer())) {
            return ChatColor.GREEN.toString();
        }

        return "";
    }

    public void addTimer(ScoreboardTimer timer) {
        if (this.timers.contains(timer)) return;

        if (this.timers.size() >= 1) {
            // lose one player slot for a timer
            this.slots--;
            this.size++;
        } else {
            // lose two player slots, one for a timer and one for a blank line between the sections
            this.slots -= 2;
            this.size += 2;
        }

        this.timers.add(timer);
        this.updateTimers();
        this.update();
    }

    public void removeTimer(ScoreboardTimer timer) {
        if (!this.timers.contains(timer)) return;

        this.timers.remove(timer);

        if (this.timers.size() == 0) {
            // lose two timer slots, one for a timer and one for a blank line between the sections
            this.slots += 2;
            this.size -= 2;
        } else {
            // lose one timer slot
            this.slots++;
            this.size--;
        }

        this.updateTimers();
        this.update();
    }

    public int getTimerSlot(ScoreboardTimer timer) {
        return this.size - this.timers.indexOf(timer) - 1;
    }

    public void updateTimers() {
        if (this.timers.size() > 0) {
            // blank row
            ScoreboardManager.setScore(this.size - this.timers.size() - 1, "");

            if (this.timers.contains(ScoreboardTimer.BORDER)) {
                int border = (int) (MatchManager.getCurrentMap().getWorld().getWorldBorder().getSize() / 2);
                String score = ChatColor.DARK_AQUA + "Border: " + ChatColor.WHITE + "±" + border;
                ScoreboardManager.setScore(this.getTimerSlot(ScoreboardTimer.BORDER), score);
            }

            if (this.timers.contains(ScoreboardTimer.GRACE)) {
                int slot = ScoreboardManager.getMatchScoreboard().getTimerSlot(ScoreboardTimer.GRACE);
                int seconds = GracePeriodModule.getSeconds() == -1 ? NumberSetting.GRACE_PERIOD.value() * 60 : GracePeriodModule.getSeconds();
                String score = ChatColor.LIGHT_PURPLE + "Grace: " + TimeUtils.getFormattedTime(seconds, ChatColor.WHITE);
                ScoreboardManager.setScore(slot, score);
            }

            if (this.timers.contains(ScoreboardTimer.REFILL)) {
                int slot = ScoreboardManager.getMatchScoreboard().getTimerSlot(ScoreboardTimer.REFILL);
                int seconds = LootChestRefillModule.getSeconds() == -1 ? NumberSetting.REFILL_EVENT.value() * 60 : LootChestRefillModule.getSeconds();
                String score = ChatColor.GOLD + "Refill: " + TimeUtils.getFormattedTime(seconds, ChatColor.WHITE);
                ScoreboardManager.setScore(slot, score);
            }
        }
    }

}
