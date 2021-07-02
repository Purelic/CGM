package net.purelic.cgm.core.maps.region;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.commons.modules.Module;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegionModule implements Module {

    private static final String WARNING_SYMBOL = "\u26A0"; // ⚠
    private static final String WARNING_PREFIX = ChatColor.YELLOW + " " + WARNING_SYMBOL + " " + ChatColor.RED;

    private List<Region> regions = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo().getBlock().getLocation();
        Location from = event.getFrom().getBlock().getLocation();
        MatchTeam team = MatchTeam.getTeam(player);

        if (this.ignoreEvent(player)) return;

        for (Region region : this.regions) {
            boolean toInRegion = region.contains(to);
            boolean fromInRegion = region.contains(from);
            boolean canBypass = region.isOwner(team);

            // TODO all region filters should check for dependency control not just teleporters
            if (toInRegion && region.canTeleport(player)) {
                region.teleport(player);
                break;
            }

            if (canBypass) continue;

            if (!region.canEnter() && toInRegion) {
                event.setCancelled(true);
                this.sendWarningMessage(player, region, RegionModifiers.ENTER);
            } else if (!region.canLeave() && fromInRegion && !toInRegion) {
                event.setCancelled(true);
                this.sendWarningMessage(player, region, RegionModifiers.LEAVE);
            } else if (region.isInstantDeath() && toInRegion) {
                MatchManager.getParticipant(player).setState(ParticipantState.RESPAWNING);
                player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, 0));
                player.damage(10000D);
                break;
            }

            if (event.isCancelled()) {
                this.resetPosition(event);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation().clone().add(0.5, 0, 0.5);
        MatchTeam team = MatchTeam.getTeam(player);

        if (this.ignoreEvent(player)) return;

        for (Region region : this.regions) {
            if (!region.isOwner(team) && !region.canBreakBlocks() && region.contains(location)) {
                this.sendWarningMessage(player, region, RegionModifiers.BREAK_BLOCKS);
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        MatchTeam team = MatchTeam.getTeam(player);

        if (this.ignoreEvent(player)) return;

        for (Region region : this.regions) {
            if (!region.isOwner(team) && !region.canPlaceBlocks() && region.contains(block)) {
                this.sendWarningMessage(player, region, RegionModifiers.PLACE_BLOCKS);
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        Location location = player.getLocation();
        MatchTeam team = MatchTeam.getTeam(player);

        if (this.ignoreEvent(player)) return;

        for (Region region : this.regions) {
            if (region.isOwner(team) && !region.canTakeDamage() && region.contains(location)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) return;

        Player player = null;
        Projectile projectile = null;

        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }

        if (player == null || this.ignoreEvent(player)) return;

        Location location = player.getLocation();
        MatchTeam team = MatchTeam.getTeam(player);

        for (Region region : this.regions) {
            if (!region.isOwner(team) && !region.canPvP() && region.contains(location)) {
                this.sendWarningMessage(player, region, RegionModifiers.PVP);
                event.setCancelled(true);
                if (projectile != null) projectile.remove();
                break;
            }
        }
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();
        List<Region> regions = this.regions.stream()
            .filter(region ->
                !region.isNeutral()
                    && region.hasDependency()
                    && region.isTeleporter())
            .collect(Collectors.toList());

        for (Region region : regions) {
            for (MatchTeam team : teams) {
                if (region.getOwner() == team) {
                    if (region.canTeleport(team)) region.show();
                    else region.hide();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.regions = event.getMap().getYaml().getRegions();

        for (Region region : this.regions) {
            if (region.isTeleporter()) region.show();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockFromToEvent event) {
        if (this.regions.isEmpty() || !MatchState.isActive()) return;

        Block block = event.getBlock();

        if (!block.isLiquid()) return;

        for (Region region : this.regions) {
            if (region.isTeleporter() && region.contains(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean ignoreEvent(Player player) {
        return this.regions.isEmpty()
            || !MatchState.isActive()
            || !MatchManager.isPlaying(player)
            || !MatchManager.getParticipant(player).isState(ParticipantState.ALIVE);
    }

    private void sendWarningMessage(Player player, Region region, RegionModifiers modifier) {
        if (modifier == RegionModifiers.ENTER) {
            this.sendWarningMessage(player, region, "enter");
        } else if (modifier == RegionModifiers.LEAVE) {
            this.sendWarningMessage(player, region, "leave");
        } else if (modifier == RegionModifiers.PLACE_BLOCKS) {
            this.sendWarningMessage(player, region, "place blocks in");
        } else if (modifier == RegionModifiers.BREAK_BLOCKS) {
            this.sendWarningMessage(player, region, "break blocks in");
        } else if (modifier == RegionModifiers.PVP) {
            this.sendWarningMessage(player, region, "hurt that player in");
        }
    }

    private void sendWarningMessage(Player player, Region region, String warning) {
        player.sendMessage(WARNING_PREFIX + "You can't " + warning + " " + region.getName() + ChatColor.RED + "!");
        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1F, 0.75F);
    }

    private void resetPosition(final PlayerMoveEvent event) {
        Location newLoc;
        double yValue = event.getFrom().getY();

        if (yValue <= 0 || event instanceof PlayerTeleportEvent) {
            newLoc = event.getFrom();
        } else {
            newLoc = this.center(event.getFrom()).subtract(new Vector(0, 0.5, 0));

            if (newLoc.getBlock() != null) {
                switch (newLoc.getBlock().getType()) {
                    case STEP:
                    case WOOD_STEP:
                        newLoc.add(new Vector(0, 0.5, 0));
                        break;
                    default:
                        break;
                }
            }
        }

        newLoc.setPitch(event.getTo().getPitch());
        newLoc.setYaw(event.getTo().getYaw());
        event.setCancelled(false);
        event.setTo(newLoc);
    }

    private Location center(Location location) {
        Location center = location.clone();
        center.setX(center.getBlockX() + 0.5);
        center.setY(center.getBlockY() + 0.5);
        center.setZ(center.getBlockZ() + 0.5);
        return center;
    }

}
