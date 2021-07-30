package net.purelic.cgm.uhc.scenarios;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CycleGraceScenario implements Module {

    private boolean active = false;
    private BukkitRunnable task;

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (this.active && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        this.active = false;
        this.scheduleTask();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        this.active = false;
        TaskUtils.cancelIfRunning(this.task);
    }

    private void scheduleTask() {
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    this.cancel();
                    return;
                }

                if (active) {
                    ChatUtils.sendMessageAll(
                        new ComponentBuilder("\n")
                            .append(" PVP ENABLED » ").color(ChatColor.GREEN).bold(true)
                            .append("Damage has now been re-enabled for 10 minutes!").reset()
                            .append("\n").reset()
                    );

                    active = false;
                } else {
                    ChatUtils.sendMessageAll(
                        new ComponentBuilder("\n")
                            .append(" PVP DISABLED » ").color(ChatColor.RED).bold(true)
                            .append("All damage is now disabled for 10 minutes!").reset()
                            .append("\n").reset()
                    );

                    active = true;
                }

                scheduleTask();
            }
        };

        TaskUtils.runLaterAsync(this.task, 20L * 60 * 10); // 10 minutes later
    }

}
