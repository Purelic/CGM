package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.listeners.modules.bedwars.BlastProofModule;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.HillUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockProtectionModule implements Listener {

    private static final Set<Block> blocks = new HashSet<>();
    private Entity explosion = null;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);
        Block block = event.getBlock();
        Material material = block.getType();

        if (MatchState.isState(MatchState.STARTED)
            && EnumSetting.GAME_TYPE.is(GameType.SURVIVAL_GAMES)
            && (material == Material.VINE
                || material == Material.LEAVES
                || material == Material.RED_MUSHROOM
                || material == Material.BROWN_MUSHROOM
                || material == Material.WEB
                || material == Material.WATER_LILY)
        ) {
            return;
        }

        if (!this.canBreakBlocks() && !canPlaceBlocks()) {
            event.setCancelled(true);
            return;
        }

        if (!withinBuildLimits(block)) {
            CommandUtils.sendErrorMessage(player, this.getBuildLimitMessage());
            event.setCancelled(true);
            return;
        }

        if (isSpawnProtected(block)) {
            CommandUtils.sendErrorMessage(player, "This area is spawn protected!");
            event.setCancelled(true);
            return;
        }

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())
            || (!this.canBreakBlocks() && !blocks.contains(block))) {
            event.setCancelled(true);
            return;
        }

        if (FlagUtils.getFlags().stream()
            .filter(Flag::isLoaded)
            .anyMatch(flag ->
                flag.getLocation().getBlock().getLocation().equals(block.getLocation())
                    || flag.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().equals(block.getLocation())
                    || flag.getHome().getBlock().getRelative(BlockFace.DOWN).getLocation().equals(block.getLocation()))) {
            event.setCancelled(true);
            CommandUtils.sendErrorMessage(player, "You can't break the flag!");
            return;
        }

        if (HillUtils.getHills().stream().filter(Hill::isActive).anyMatch(hill -> hill.isInside(block, 1F) && block.getY() == hill.getCenter().getBlockY())) {
            event.setCancelled(true);
            CommandUtils.sendErrorMessage(player, "You can't break the hill!");
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) {
            event.setCancelled(true);
            return;
        }

        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (this.isObs(player)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntered() instanceof Player) {
            Player player = (Player) event.getEntered();
            if (this.isObs(player)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) {
            event.setCancelled(true);
            return;
        }

        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (this.isObs(player)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleUpdate(final VehicleUpdateEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) {
            event.getVehicle().setVelocity(new Vector());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {

            if (MatchState.isState(MatchState.WAITING)
                && event.getAction().name().contains("RIGHT_CLICK")
                && player.getItemInHand() != null
                && player.getItemInHand().getType() == Material.WRITTEN_BOOK) {
                // let players open books in the lobby
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!this.canBreakBlocks()) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!this.canBreakBlocks() && (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof ArmorStand || event.getEntity() instanceof Painting)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if ((!this.canBreakBlocks() && event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) || this.isObs(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!this.canBreakBlocks() || this.isObs(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!MatchState.isState(MatchState.STARTED)
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();

        if (HillUtils.getHills().stream().filter(Hill::isActive).anyMatch(hill -> hill.isInside(block, 1F))) {
            event.setCancelled(true);
            return;
        }

        if (this.canBreakBlocks()
            || material == Material.DISPENSER
            || material == Material.DROPPER
            || material == Material.HOPPER
            || material == Material.SAND
            || material == Material.ANVIL
            || material == Material.GRAVEL
            || material == Material.FIRE
            || material.name().contains("DOOR")
            || material.name().contains("DIODE")
            || material.name().contains("REDSTONE")
            || material.name().contains("PISTON")) return;

        if (!canPlaceBlocks()) event.setCancelled(true);
        else if (!block.isLiquid()
            && material != Material.BED_BLOCK
            && material != Material.LADDER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());

        if (!canPlaceBlocks()) {
            event.setCancelled(true);
            return;
        }

        if (!withinBuildLimits(block)) {
            CommandUtils.sendErrorMessage(player, this.getBuildLimitMessage());
            event.setCancelled(true);
            return;
        }

        if (isSpawnProtected(block)) {
            CommandUtils.sendErrorMessage(player, "This area is spawn protected!");
            event.setCancelled(true);
            return;
        }

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        blocks.add(block);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);
        Block block = event.getBlockClicked();

        if (!canPlaceBlocks()) {
            event.setCancelled(true);
            return;
        }

        if (!withinBuildLimits(block)) {
            CommandUtils.sendErrorMessage(player, this.getBuildLimitMessage());
            event.setCancelled(true);
            return;
        }

        if (isSpawnProtected(block)) {
            CommandUtils.sendErrorMessage(player, "This area is spawn protected!");
            event.setCancelled(true);
            return;
        }

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        blocks.add(block);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);
        Block block = event.getBlock();

        if (!canPlaceBlocks()) {
            event.setCancelled(true);
            return;
        }

        if (!withinBuildLimits(block)) {
            CommandUtils.sendErrorMessage(player, this.getBuildLimitMessage());
            event.setCancelled(true);
            return;
        }

        if (isSpawnProtected(block)) {
            CommandUtils.sendErrorMessage(player, "This area is spawn protected!");
            event.setCancelled(true);
            return;
        }

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        if (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER) && block.getType() == Material.SKULL) {
            event.setCancelled(true);
            CommandUtils.sendErrorMessage(player, "You can't place heads!");
            return;
        }

        blocks.add(block);
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        blocks.forEach(block -> block.setType(Material.AIR));
        blocks.clear();
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        blocks.clear();
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        Participant participant = MatchManager.getParticipant(player);

        if (team == MatchTeam.OBS
            || !MatchState.isState(MatchState.STARTED)
            || (participant != null && participant.isDead())
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
            return;
        }

        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();

        if (itemStack.getType() == Material.WOOL) {
            event.setCancelled(true);
            item.remove();
            player.getInventory().addItem(
                new ItemStack(Material.WOOL, itemStack.getAmount(), ColorConverter.getDyeColor(team.getColor()).getData())
            );
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.2F, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onParticipantRespawn(ParticipantRespawnEvent event) {
        if (!ToggleSetting.SPAWN_PROTECTION.isEnabled()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();

                if (NumberSetting.PLAYER_HASTE.value() == 0 && canPlaceBlocks()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 2));
                }

                if (NumberSetting.PLAYER_RESISTANCE.value() == 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 200));
                }

                if (blocks.size() == 0) return;

                Location loc = player.getLocation().add(0, 1, 0);
                int radius = 3;

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block block = loc.getWorld().getBlockAt(
                                loc.getBlockX() + x,
                                loc.getBlockY() + y,
                                loc.getBlockZ() + z);
                            if (blocks.contains(block)) block.setType(Material.AIR);
                        }
                    }
                }
            }
        }.runTask(CGM.getPlugin());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        this.explosion = event.getEntity();
        Set<Block> toRemove = new HashSet<>();

        if (!this.canBreakBlocks()) {
            toRemove.addAll(event.blockList().stream()
                .filter(block -> !blocks.contains(block))
                .collect(Collectors.toList()));
        }

        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            Set<Block> blastProofBlocks = BlastProofModule.getBlastProofBlocks(event.getLocation().getBlock());

            for (Block block : event.blockList()) {
                Location blockLoc = block.getLocation();

                for (Block blastProof : blastProofBlocks) {
                    if (blockLoc.equals(blastProof.getLocation())) {
                        toRemove.add(block);
                    }
                }
            }
        }

        for (Block block : event.blockList()) {
            Location loc = block.getLocation();

            if (loc.distance(this.explosion.getLocation()) >= 1.5D) {
                if (BlastProofModule.touchingBlastProofBlocks(block, 2)) toRemove.add(block);
            }
        }

        event.blockList().removeAll(toRemove);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> toRemove = new ArrayList<>();

        if (this.explosion instanceof Fireball) {
            toRemove.addAll(event.blockList().stream()
                .filter(block -> block.getType() == Material.ENDER_STONE)
                .collect(Collectors.toList()));
        }

        if (!this.canBreakBlocks()) {
            toRemove.addAll(event.blockList().stream()
                .filter(block -> !blocks.contains(block))
                .collect(Collectors.toList()));
        }

        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            Set<Block> blastProofBlocks = BlastProofModule.getBlastProofBlocks(event.getBlock());

            for (Block block : event.blockList()) {
                Location blockLoc = block.getLocation();

                for (Block blastProof : blastProofBlocks) {
                    if (blockLoc.equals(blastProof.getLocation())) {
                        toRemove.add(block);
                    }
                }
            }
        }

        for (Block block : event.blockList()) {
            Location loc = block.getLocation();

            if (loc.distance(this.explosion.getLocation()) >= 1.5D) {
                if (BlastProofModule.touchingBlastProofBlocks(block, 2)) toRemove.add(block);
            }
        }

        this.explosion = null;
        event.blockList().removeAll(toRemove);
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!MatchState.isState(MatchState.STARTED)
            || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (!ToggleSetting.LEAVES_DECAY.isEnabled()) event.setCancelled(true);
    }

    public static boolean isSpawnProtected(Block block) {
        if (!ToggleSetting.SPAWN_PROTECTION.isEnabled()) return false;

        TeamType teamType = EnumSetting.TEAM_TYPE.get();

        for (MatchTeam team : teamType.getTeams()) {
            Location location = SpawnUtils.getInitialSpawn(team);

            if (block.getWorld() != location.getWorld()) continue;

            if (block.getLocation().distance(location.getBlock().getLocation()) <= 4.5) {
                return true;
            }
        }

        return false;
    }

    public static boolean withinBuildLimits(Block block) {
        CustomMap map = MatchManager.getCurrentMap();

        if (map == null) return false;

        MapYaml yaml = map.getYaml();
        int y = block.getY();
        return y <= yaml.getMaxBuildLimit() && y >= yaml.getMinBuildLimit();
    }

    private boolean canBreakBlocks() {
        CustomMap map = MatchManager.getCurrentMap();
        if (map == null) return false;
        return map.getYaml().canBreakBlocks();
    }

    public static boolean canPlaceBlocks() {
        CustomMap map = MatchManager.getCurrentMap();
        if (map == null) return false;
        return map.getYaml().canPlaceBlocks();
    }

    private String getBuildLimitMessage() {
        CustomMap map = MatchManager.getCurrentMap();

        if (map == null) return "You can't build here!";

        MapYaml yaml = map.getYaml();
        return "You can only build between Y = " + yaml.getMinBuildLimit() + " - " + yaml.getMaxBuildLimit() + "!";
    }

    private boolean isObs(Player player) {
        return MatchTeam.getTeam(player) == MatchTeam.OBS ||
            (MatchManager.isPlaying(player) && (MatchManager.getParticipant(player).isDead() || MatchManager.getParticipant(player).isEliminated()));
    }

    public static void addBreakableBlock(Block block) {
        BlockProtectionModule.blocks.add(block);
    }

}
