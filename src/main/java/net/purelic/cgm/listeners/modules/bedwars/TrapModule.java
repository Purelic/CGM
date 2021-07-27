package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.cgm.core.maps.shop.events.TrapPurchaseEvent;
import net.purelic.cgm.core.maps.shop.events.TrapTriggeredEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TrapModule implements Listener {

    private final Map<MatchTeam, TeamUpgrade> traps = new HashMap<>();
    private final Map<MatchTeam, BukkitRunnable> checkers = new HashMap<>();

    @EventHandler
    public void onTrapPurchase(TrapPurchaseEvent event) {
        MatchTeam team = event.getTeam();
        TeamUpgrade upgrade = event.getUpgrade();

        // set their trap type to the most recent kind
        this.traps.put(team, upgrade);

        if (this.checkers.containsKey(team)) return; // trap already activated

        Bed bed = BedUtils.getBed(team);

        if (bed == null) return;

        BukkitRunnable checker = this.getChecker(bed);
        checker.runTaskTimer(CGM.get(), 0L, 2L);
        this.checkers.put(team, checker);
    }

    private BukkitRunnable getChecker(final Bed bed) {
        return new BukkitRunnable() {

            private final Location bedLoc = bed.getLocation();
            private final MatchTeam owner = bed.getOwner();

            @Override
            public void run() {
                Player closest = PlayerUtils.getClosestEnemy(this.owner);

                if (closest == null) return;

                double distance = closest.getLocation().distance(this.bedLoc);

                if (distance <= 15D && !this.hasImmunity(closest)) {
                    Commons.callEvent(new TrapTriggeredEvent(bed, closest));
                    this.cancel();
                }
            }

            private boolean hasImmunity(Player player) {
                return player.hasPotionEffect(PotionEffectType.NIGHT_VISION) || player.hasPotionEffect(PotionEffectType.SLOW_DIGGING);
            }
        };
    }

    @EventHandler
    public void onTrapTriggered(TrapTriggeredEvent event) {
        Bed bed = event.getBed();
        MatchTeam owner = bed.getOwner();
        TeamUpgrade upgrade = this.traps.get(owner);
        Player player = event.getTriggeredBy();

        switch (upgrade) {
            case TRAP_I:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 160, 0));
                break;
            case TRAP_II:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 160, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0));
                break;
            case TRAP_III:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 160, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                break;
        }

        String playerMsg = "" + ChatColor.RED + ChatColor.BOLD + "You set off " + owner.getColoredName() + ChatColor.RED + ChatColor.BOLD + "'s trap!";

        if (VersionUtils.isLegacy(player)) {
            player.sendMessage(playerMsg);
        } else {
            ChatUtils.sendActionBar(player, playerMsg);
        }

        String teamMsg = ChatColor.BOLD + NickUtils.getDisplayName(player) + ChatColor.RED + ChatColor.BOLD + " set off your trap!";

        owner.getPlayers().forEach(pl -> {
            if (VersionUtils.isLegacy(pl)) {
                pl.sendMessage(teamMsg);
            } else {
                ChatUtils.sendActionBar(pl, teamMsg);
            }
        });

        SoundUtils.SFX.TRAP_TRIGGERED.play(player);
        SoundUtils.SFX.TRAP_TRIGGERED.play(owner);

        this.clearTeam(owner);
        ShopManager.getUpgrades().get(owner).remove(TeamUpgrade.TRAP_I);
        ShopManager.getUpgrades().get(owner).remove(TeamUpgrade.TRAP_II);
        ShopManager.getUpgrades().get(owner).remove(TeamUpgrade.TRAP_III);
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.clearAll();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        this.clearAll();
    }

    @EventHandler
    public void onMatchTeamEliminate(MatchTeamEliminateEvent event) {
        this.clearTeam(event.getTeam());
    }

    private void clearAll() {
        Arrays.asList(MatchTeam.values()).forEach(this::clearTeam);
    }

    private void clearTeam(MatchTeam team) {
        if (this.checkers.containsKey(team)) {
            BukkitRunnable checker = this.checkers.get(team);
            if (TaskUtils.isRunning(checker)) checker.cancel();
        }

        this.traps.remove(team);
        this.checkers.remove(team);
    }

}
