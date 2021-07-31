package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.JumpPad;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchCycleEvent;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.utils.EntityUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class JumpPadModule implements Listener {

    private final List<Player> jumpers = new ArrayList<>();
    private List<JumpPad> jumpPads = new ArrayList<>();
    private BukkitRunnable jumpPadChecker = null;
    private BukkitRunnable particleEffects = null;

    @EventHandler
    public void onMatchCycle(MatchCycleEvent event) {
        if (!event.hasMap() || !ToggleSetting.JUMP_PADS.isEnabled()) return;

        List<JumpPad> jumpPads = event.getMap().getYaml().getJumpPads();
        this.startParticleEffects(jumpPads);
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        if (!ToggleSetting.JUMP_PADS.isEnabled()) return;

        this.jumpPads = event.getMap().getYaml().getJumpPads();
        this.startJumpPadChecker();
        this.jumpers.clear();
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        this.jumpers.clear();
        TaskUtils.cancelIfRunning(this.jumpPadChecker);
        TaskUtils.cancelIfRunning(this.particleEffects);
    }

    private void startJumpPadChecker() {
        this.jumpPadChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isActive()) {
                    this.cancel();
                    return;
                }

                for (Participant participant : MatchManager.getParticipants()) {
                    Player player = participant.getPlayer();

                    if (jumpers.contains(player) && EntityUtils.isOnGround(player)) {
                        jumpers.remove(player);
                    }

                    checkForJumpPad(participant);
                }
            }
        };

        this.jumpPadChecker.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);
    }

    private void startParticleEffects(List<JumpPad> jumpPads) {
        this.particleEffects = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isActive()) {
                    this.cancel();
                    return;
                }

                jumpPads.forEach(JumpPad::playEffect);
            }
        };

        this.particleEffects.runTaskTimerAsynchronously(CGM.get(), 0L, 5L);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player) || event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) entity;

        if (this.jumpers.contains(player)) {
            event.setCancelled(true);
            this.jumpers.remove(player);
        }

        if (MatchManager.isPlaying(player)) {
            boolean jumped = this.checkForJumpPad(MatchManager.getParticipant(player));

            if (jumped) {
                event.setCancelled(true);
            }
        }
    }

    private boolean checkForJumpPad(Participant participant) {
        Player player = participant.getPlayer();

        for (JumpPad jumpPad : this.jumpPads) {
            if (!participant.isAlive() || player.isSneaking()) continue;

            Location playerLoc = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();

            if (playerLoc.equals(jumpPad.getLocation())) {
                jumpPad.launchPlayer(player);
                this.jumpers.add(player);
                return true;
            }
        }

        return false;
    }

}
