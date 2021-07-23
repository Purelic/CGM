package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GracePeriodModule implements DynamicModule {

    private boolean active = false;
    private BukkitRunnable gracePeriodTask;
    private int seconds;

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        this.active = true;
        this.scheduleGracePeriodTask();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        this.active = false;
        TaskUtils.cancelIfRunning(this.gracePeriodTask);
    }

    private void scheduleGracePeriodTask() {
        this.gracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    active = false;
                    this.cancel();
                    return;
                }

                seconds--;

                if (seconds == 0) {
                    ChatUtils.sendMessageAll(
                        new ComponentBuilder("\n")
                            .append(" GRACE PERIOD » ").color(ChatColor.YELLOW).bold(true)
                            .append("Grace period has now ended!").reset()
                            .append("\n").reset()
                    );

                    active = false;
                    this.cancel();
                }
            }
        };

        int minutes = NumberSetting.GRACE_PERIOD.value();
        this.seconds = minutes * 60;

        TaskUtils.runTimerAsync(this.gracePeriodTask, 20L);
    }

    @EventHandler
    public boolean isValid() {
        return NumberSetting.GRACE_PERIOD.value() > 0;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!this.active) return;

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(entity instanceof  Player)) return;

        if (damager instanceof Player) {
            event.setCancelled(true);
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player) {
                projectile.remove();
                event.setCancelled(true);
            }
        }
    }

}
