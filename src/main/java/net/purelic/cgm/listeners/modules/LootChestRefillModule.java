package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.LootType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.chest.LootChest;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.utils.TimeUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class LootChestRefillModule implements Listener {

    private BukkitRunnable refillTask;
    private TeamType teamType;
    private int minutes;
    private int seconds;
    private int teams;

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES)
            || NumberSetting.REFILL_EVENT.value() == 0
            || EnumSetting.LOOT_TYPE.is(LootType.CUSTOM)) return;

        this.scheduleRefillTask();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        TaskUtils.cancelIfRunning(this.refillTask);
    }

    private void scheduleRefillTask() {
        this.refillTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    this.cancel();
                    return;
                }

                seconds--;

                ScoreboardManager.setScore(teamType == TeamType.SOLO ? 0 : teams + 1, "Refill in " + TimeUtils.getFormattedTime(seconds));

                if (seconds <= 0) {
                    MatchManager.getCurrentMap().getYaml().getLootChests().forEach(LootChest::refill);

                    ChatUtils.sendMessageAll(
                        new ComponentBuilder("\n")
                            .append(" CHESTS REFILLED » ").color(ChatColor.YELLOW).bold(true)
                            .append("All chests have been refilled!").reset()
                            .append("\n").reset()
                    );

                    seconds = minutes * 60;

                    if (MatchCountdown.getSeconds() <= seconds) {
                        this.cancel();

                        ScoreboardManager.resetScore(teamType == TeamType.SOLO ? 0 : teams + 1);
                        ScoreboardManager.resetScore(teamType == TeamType.SOLO ? 1 : teams);
                    }
                }
            }
        };

        this.minutes = NumberSetting.REFILL_EVENT.value();
        this.seconds = minutes * 60;
        this.teamType = EnumSetting.TEAM_TYPE.get();
        this.teams = this.teamType.getTeams().size();

        if (this.teamType == TeamType.SOLO) {
            ScoreboardManager.setScore(1, "");
        } else {
            ScoreboardManager.setScore(this.teams, "");
        }

        this.refillTask.runTaskTimerAsynchronously(CGM.get(), 0L, 20L);
    }

}
