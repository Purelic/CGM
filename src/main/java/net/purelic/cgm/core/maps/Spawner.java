package net.purelic.cgm.core.maps;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class Spawner implements Listener {

    private final String id;
    private final Location location;
    private final Material material;
    private final int amount;
    private final int delay;
    private final boolean infinite;
    private final boolean display;
    private final boolean startSpawned;
    private final boolean destructive;
    private final ChatColor color;
    private World world;
    private BlockState replaced;
    private ArmorStand stand;
    private BukkitRunnable runnable;
    private int countdown;
    private boolean active;
    private int totalSpawned;

    public Spawner(Map<String, Object> map) {
        this.id = UUID.randomUUID().toString();
        int[] coords = this.getCoords(((String) map.get("location")).split(","));
        this.location = new Location(null, coords[0], coords[1], coords[2]);
        this.material = Material.valueOf((String) map.getOrDefault("material", Material.GOLDEN_APPLE.name()));
        this.amount = (int) map.getOrDefault("amount", 1);
        this.delay = (int) map.getOrDefault("delay", 20);
        this.infinite = (boolean) map.getOrDefault("infinite", false);
        this.display = (boolean) map.getOrDefault("hologram", true);
        this.startSpawned = (boolean) map.getOrDefault("start_spawned", false);
        this.destructive = (boolean) map.getOrDefault("destructive", true);
        this.color = this.getColor();
        this.totalSpawned = 0;
        CGM.get().registerListener(this);
    }

    private int[] getCoords(String[] args) {
        int[] coords = new int[3];

        for (int i = 0; i < args.length; i++) {
            coords[i] = Integer.parseInt(args[i]);
        }

        return coords;
    }

    public Material getMaterial() {
        return this.material;
    }

    public Location getLocation() {
        return this.location;
    }

    private ChatColor getColor() {
        if (this.material == Material.GOLDEN_APPLE) return ChatColor.YELLOW;
        else if (this.material == Material.DIAMOND) return ChatColor.AQUA;
        else if (this.material == Material.EMERALD) return ChatColor.GREEN;
        else if (this.material == Material.TNT) return ChatColor.RED;
        else if (this.material == Material.SKULL_ITEM) return ChatColor.AQUA;
            // else if (this.material == Material.FIREWORK_CHARGE) return ChatColor.GOLD;
        else return ChatColor.WHITE;
    }

    public void create(World world) {
        this.world = world;
        this.location.setWorld(this.world);
        this.createBlock();
        if (this.display) this.createStand();
    }

    private void createBlock() {
        Block block = this.world.getBlockAt(this.location);

        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && this.destructive) {
            block.setType(Material.MOB_SPAWNER);
        }

        BlockState state = block.getState();
        this.replaced = state;

        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && this.destructive) {
            CreatureSpawner spawner = (CreatureSpawner) state;
            spawner.setSpawnedType(EntityType.FALLING_BLOCK);
            spawner.setDelay(Integer.MAX_VALUE);
        }
    }

    private void createStand() {
        Location standLoc = this.location.clone();
        this.stand = (ArmorStand) this.world.spawnEntity(standLoc.add(0.5, 2, 0.5), EntityType.ARMOR_STAND);
        this.stand.setSmall(true);
        this.stand.setCanPickupItems(false);
        this.stand.setMarker(true);
        this.stand.setGravity(false);
        this.stand.setVisible(false);
        this.stand.setRemoveWhenFarAway(false);
        this.stand.setBasePlate(false);
        this.stand.setLeftLegPose(new EulerAngle(Math.PI, 0.0D, 0.0D));
        this.stand.setRightLegPose(new EulerAngle(Math.PI, 0.0D, 0.0D));
        this.stand.setCustomName(this.color + "" + this.delay + "s" + (this.amount == 1 ? "" : " (x" + this.amount + ")"));
        this.stand.setCustomNameVisible(true);

        if (this.material == Material.ARROW) this.stand.setHelmet(new ItemStack(Material.IRON_BLOCK));
        else if (this.material == Material.GOLDEN_APPLE) this.stand.setHelmet(new ItemStack(Material.GOLD_BLOCK));
        else if (this.material == Material.DIAMOND) this.stand.setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
        else if (this.material == Material.EMERALD) this.stand.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
        else this.stand.setHelmet(new ItemStack(this.material));
    }

    public void start() {
        this.runnable = new BukkitRunnable() {

            @Override
            public void run() {
                if (countdown == 0) {
                    spawnItem();
                    this.cancel();
                } else {
                    if (stand != null)
                        stand.setCustomName(color + "" + countdown + "s" + (amount == 1 ? "" : " (x" + amount + ")"));
                    countdown--;
                }
            }

        };

        this.countdown = this.delay;
        this.runnable.runTaskTimer(CGM.get(), 0, 20);
    }

    private void spawnItem() {
        Location itemLoc = this.location.clone().add(0.5, 1.5, 0.5);
        ItemStack item = new ItemCrafter(this.material).amount(this.amount).setTag("spawner_id", this.id).craft();
        Item drop = this.world.dropItem(itemLoc, item);
        drop.setVelocity(new Vector(0, 0, 0));
        this.totalSpawned += this.amount;
        this.world.playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 1);
        this.stop(false);
        if (this.infinite && !this.isMaxSpawnedItems()) this.start();
    }

    private boolean isMaxSpawnedItems() {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return false;
        if (this.material == Material.EMERALD && this.totalSpawned >= 4) return true;
        else if (this.material == Material.DIAMOND && this.totalSpawned >= 8) return true;
        else if (this.material == Material.GOLD_INGOT && this.totalSpawned >= 16) return true;
        else return this.material == Material.IRON_INGOT && this.totalSpawned >= 64;
    }

    public void stop(boolean destroy) {
        if (TaskUtils.isRunning(this.runnable)) {
            this.runnable.cancel();
            if (this.stand != null)
                this.stand.setCustomName(this.color + "" + this.delay + "s" + (this.amount == 1 ? "" : " (x" + this.amount + ")"));
        }

        if (destroy) this.destroy();
    }

    private void destroy() {
        this.replaced.update(true);
        if (this.stand != null) stand.remove();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchStart(MatchStartEvent event) {
        this.active = event.getMap().getWorld() == this.world;
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (this.active) {
            this.active = false;
            this.stop(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRoundStart(RoundStartEvent event) {
        if (this.active) {
            this.totalSpawned = 0;

            if (this.startSpawned) this.spawnItem();
            else this.start();
        }
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        if (this.active) this.stop(false);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();

        if (new ItemCrafter(itemStack).getTag("spawner_id").equals(this.id)) {
            itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
            if (!this.infinite || this.isMaxSpawnedItems()) this.start();
            this.totalSpawned = 0;
            this.splitItem(itemStack, event.getPlayer());
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (new ItemCrafter(itemStack).getTag("spawner_id").equals(this.id)) {
            event.setCancelled(true);
        }
    }

    // Handles entities being exploded and not restarting the spawn timer
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.DROPPED_ITEM) return;

        Item item = (Item) event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (new ItemCrafter(itemStack).getTag("spawner_id").equals(this.id)) {
            if (!this.infinite || this.isMaxSpawnedItems()) this.start();
            this.totalSpawned = 0;
        }
    }

    private void splitItem(ItemStack item, Player player) {
        // only applies to bed wars game types
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;

        Material material = item.getType();

        // only split iron and gold ingots
        if (material != Material.IRON_INGOT && material != Material.GOLD_INGOT) return;

        // get team mates within 2 blocks of the player that picked up the item and split
        Location playerLoc = player.getLocation();
        MatchTeam.getTeam(player).getPlayers()
            .stream().filter(team -> team.getLocation().distance(playerLoc) <= 2.0D)
            .forEach(team -> {
                if (team != player) {
                    Participant participant = MatchManager.getParticipant(team);
                    if (!participant.isDead() && !participant.isEliminated()) {
                        team.getInventory().addItem(item);
                    }
                }
            });
    }

}
