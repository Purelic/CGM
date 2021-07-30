package net.purelic.cgm.listeners.modules;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.scoreboards.ScoreboardTimer;
import net.purelic.cgm.utils.TimeUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GracePeriodModule implements DynamicModule {

    private static final ScoreboardTimer TIMER = ScoreboardTimer.GRACE;
    private static boolean active = false;
    private BukkitRunnable gracePeriodTask;
    private static int seconds = -1;

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        active = true;
        this.scheduleGracePeriodTask();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        active = false;
        TaskUtils.cancelIfRunning(this.gracePeriodTask);
        seconds = -1;
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
                String score = ChatColor.LIGHT_PURPLE + "Grace: " + TimeUtils.getFormattedTime(seconds, ChatColor.WHITE);
                int slot = ScoreboardManager.getMatchScoreboard().getTimerSlot(TIMER);
                ScoreboardManager.setScore(slot, score);

                if (seconds == 0) {
                    ChatUtils.sendMessageAll(
                        new ComponentBuilder("\n")
                            .append(" GRACE PERIOD » ").color(ChatColor.LIGHT_PURPLE).bold(true)
                            .append("Grace period has now ended! Everyone has been healed.").reset()
                            .append("\n").reset()
                    );

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setHealth(player.getMaxHealth());
                    }

                    ScoreboardManager.getMatchScoreboard().removeTimer(ScoreboardTimer.GRACE);
                    active = false;
                    this.cancel();
                }
            }
        };

        int minutes = NumberSetting.GRACE_PERIOD.value();
        seconds = minutes * 60;

        TaskUtils.runTimerAsync(this.gracePeriodTask, 20L);
    }

    @Override
    public boolean isValid() {
        return NumberSetting.GRACE_PERIOD.value() > 0;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!active) return;

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) return;

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

    public static boolean isActive() {
        return active;
    }

    public static int getSeconds() {
        return seconds;
    }

}
