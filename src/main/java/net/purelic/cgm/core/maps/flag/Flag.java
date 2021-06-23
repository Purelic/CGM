package net.purelic.cgm.core.maps.flag;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.Waypoint;
import net.purelic.cgm.core.maps.flag.constants.FlagDirection;
import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.core.maps.flag.runnables.*;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.utils.*;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import shaded.com.google.cloud.Timestamp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Flag implements Listener {

    private static final double TOUCH_RADIUS = 1.25;
    private static final double DROP_COOLDOWN = 2.0;
    public static final double RETURN_RADIUS = 2.0;

    private final int[] coords;
    private final String name;
    private final MatchTeam owner;
    private final FlagDirection direction;

    private boolean active;
    private boolean respawnAtHome;
    private int scoreboardRow;
    private Location home;
    private Location location;
    private Location lastLocation;
    private FlagState state;
    private Participant carrier;
    private ItemStack helmet;
    private BlockState postState;
    private BlockState baseState;
    private Waypoint flagWaypoint;
    private Waypoint carrierWaypoint;

    private Map<Participant, Timestamp> cooldowns;
    private FlagChecker checker;
    private FlagReturnChecker returnChecker;
    private FlagRespawnCountdown respawnCountdown;
    private FlagResetCountdown resetCountdown;
    private FlagTracker flagTracker;
    private BukkitRunnable returnRing;

    public Flag(Map<String, Object> map) {
        this.coords = YamlUtils.getCoords(((String) map.get("location")).split(","));
        this.name = (String) map.getOrDefault("name", "The Flag");
        this.owner = MatchTeam.valueOf((String) map.getOrDefault("owner", "SOLO"));
        this.direction = FlagDirection.valueOf((String) map.getOrDefault("direction", "NORTH"));
        this.cooldowns = new HashMap<>();
        CGM.get().registerListener(this);
    }

    public int[] getCoords() {
        return this.coords;
    }

    public MatchTeam getOwner() {
        return this.owner;
    }

    public boolean isNeutral() {
        return this.owner == MatchTeam.SOLO;
    }

    public ChatColor getFlagColor() {
        return this.isNeutral() ? ChatColor.WHITE : this.owner.getColor();
    }

    public String getColoredName() {
        return this.isNeutral() ? this.name : this.owner.getColor() + this.name + ChatColor.RESET;
    }

    public void setWorld(World world, boolean place, int scoreboardRow) {
        this.home = new Location(world, this.coords[0], this.coords[1], this.coords[2]).add(0.5, 0, 0.5);
        this.location = this.home;
        this.scoreboardRow = scoreboardRow;
        this.respawnAtHome = true;

        if (place) {
            this.active = true;
            this.setState(FlagState.RETURNED);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        if (active) this.active = true;
        else this.reset(true);
    }

    public boolean willRespawnAtHome() {
        return this.respawnAtHome;
    }

    public void setRespawnAtHome(boolean respawnAtHome) {
        this.respawnAtHome = respawnAtHome;
    }

    public boolean isLoaded() {
        return this.home != null;
    }

    public Location getLocation() {
        return this.location;
    }

    public Location getLastLocation() {
        return this.lastLocation;
    }

    public void setLastLocation(Location location) {
        this.lastLocation = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
    }

    public Location getHome() {
        return this.home;
    }

    public boolean isState(FlagState... states) {
        return Arrays.asList(states).contains(this.state);
    }

    public void setState(FlagState state) {
        this.state = state;
        this.state.callEvent(this);
        if (this.active) this.updateScoreboard();
    }

    public Participant getCarrier() {
        return this.carrier;
    }

    public boolean hasCarrier() {
        return this.carrier != null;
    }

    public void setCarrier(Participant participant) {
        this.carrier = participant;
        this.flagWaypoint.hide(); // destroy the flag waypoint
        // this.flagWaypoint = null;
        this.carrierWaypoint = new Waypoint(this, participant); // create waypoint above carrier

        Player player = participant.getPlayer();
        this.helmet = player.getInventory().getHelmet();
        if (ToggleSetting.FLAG_CARRIER_DISABLE_SPRINTING.isEnabled()) player.setFoodLevel(6);
        player.setMaxHealth(NumberSetting.FLAG_CARRIER_MAX_HEALTH.value() * 2);

        this.setLastLocation(this.location);
        this.flagTracker = new FlagTracker(this);
        this.flagTracker.runTaskTimerAsynchronously(CGM.get(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerUtils.addPermanentEffect(player, PotionEffectType.SPEED, NumberSetting.FLAG_CARRIER_SPEED.value() - 1);
                PlayerUtils.addPermanentEffect(player, PotionEffectType.SLOW, NumberSetting.FLAG_CARRIER_SLOWNESS.value() - 1);
                PlayerUtils.addPermanentEffect(player, PotionEffectType.JUMP, NumberSetting.FLAG_CARRIER_JUMP_BOOST.value() - 1);
                PlayerUtils.addPermanentEffect(player, PotionEffectType.DAMAGE_RESISTANCE, NumberSetting.FLAG_CARRIER_RESISTANCE.value() - 1);
            }
        }.runTask(CGM.get()); // run sync

        this.setState(FlagState.TAKEN);
    }

    public void clearCarrier() {
        if (this.carrier == null) return;

        final Player player = this.carrier.getPlayer();
        ChatUtils.sendActionBar(player, "");
        player.getInventory().setHelmet(this.helmet);
        player.setMaxHealth(20);
        player.setFoodLevel(20);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getActivePotionEffects().forEach(effect -> {
                    PotionEffectType type = effect.getType();

                    if (type.equals(PotionEffectType.SPEED) && NumberSetting.FLAG_CARRIER_SPEED.value() > 0) {
                        player.removePotionEffect(type);
                        PlayerUtils.addPermanentEffect(player, PotionEffectType.SPEED, NumberSetting.PLAYER_SPEED.value() - 1);
                    } else if (type.equals(PotionEffectType.SLOW) && NumberSetting.FLAG_CARRIER_SLOWNESS.value() > 0) {
                        player.removePotionEffect(type);
                    } else if (type.equals(PotionEffectType.JUMP) && NumberSetting.FLAG_CARRIER_JUMP_BOOST.value() > 0) {
                        player.removePotionEffect(type);
                        PlayerUtils.addPermanentEffect(player, PotionEffectType.JUMP, NumberSetting.PLAYER_JUMP_BOOST.value() - 1);
                    } else if (type.equals(PotionEffectType.DAMAGE_RESISTANCE) && NumberSetting.FLAG_CARRIER_RESISTANCE.value() > 0) {
                        player.removePotionEffect(type);
                        PlayerUtils.addPermanentEffect(player, PotionEffectType.DAMAGE_RESISTANCE, NumberSetting.PLAYER_RESISTANCE.value() - 1);
                    }
                });
            }
        }.runTask(CGM.get()); // run sync

        if (TaskUtils.isRunning(this.flagTracker)) this.flagTracker.cancel();
        this.flagTracker = null;

        this.carrierWaypoint.hide();
        // this.carrierWaypoint = null;
        this.carrier = null;
        this.helmet = null;
    }

    public void resetBase() {
        if (MatchState.isState(MatchState.PRE_GAME)) return;

        final Flag flag = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (flag.postState != null) {
                    flag.postState.update(true); // updates post block to original state
                    flag.postState = null;
                }

                if (flag.baseState != null) {
                    flag.baseState.update(true); // updates base block to original state
                    flag.baseState = null;
                }
            }
        }.runTask(CGM.get()); //run sync
    }

    public void startChecker() {
        this.checker = new FlagChecker(this);
        this.checker.runTaskTimerAsynchronously(CGM.get(), 0L, 1L);
    }

    public boolean isTouching(Participant participant) {
        double radius = this.canTake(participant) ? Flag.TOUCH_RADIUS : Flag.RETURN_RADIUS;
        return this.isTouching(participant, radius);
    }

    private boolean isTouching(Participant participant, double radius) {
        return !participant.isDead()
                && this.location.distance(participant.getPlayer().getLocation()) <= radius
                && (!this.cooldowns.containsKey(participant)
                    || Timestamp.now().getSeconds() - this.cooldowns.get(participant).getSeconds() >= DROP_COOLDOWN);
    }

    public boolean canTake(Participant participant) {
        return this.canTake(participant, false);
    }

    private boolean canTake(Participant participant, boolean excludeCollected) {
        return (this.isNeutral() || this.owner != MatchTeam.getTeam(participant))
                && !FlagUtils.isCarrier(participant)
                && (excludeCollected || this.getCollectedBy() != MatchTeam.getTeam(participant));
    }

    public void addCooldown(Participant participant) {
        this.cooldowns.put(participant, Timestamp.now());
    }

    public void startRespawn() {
        final Location location = this.respawnAtHome || this.lastLocation == null ? this.home : this.lastLocation;
        int delay = this.respawnAtHome || this.lastLocation == null ? NumberSetting.FLAG_RESPAWN_DELAY.value() : NumberSetting.FLAG_VOIDED_DELAY.value();

        this.respawnCountdown = new FlagRespawnCountdown(this, delay);
        this.respawnCountdown.runTaskTimerAsynchronously(CGM.get(), 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // if the match has ended and the flag has been reset,
                // this can cause a NPE since it's wrapped in a runnable
                if (location != null) setFlagWaypointLocation(location);
            }
        }.runTask(CGM.get()); // run sync
    }

    public void startReturn() {
        if (TaskUtils.isRunning(this.returnChecker)) return;

        this.returnChecker = new FlagReturnChecker(this);
        this.returnChecker.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);

        if (!this.isNeutral() && !TaskUtils.isRunning(this.returnRing)) {
            this.returnRing = ParticleUtils.getParticleCircle(this);
            this.returnRing.runTaskTimerAsynchronously(CGM.get(), 0L, 2L);
        }

        if (TaskUtils.isRunning(this.resetCountdown) || NumberSetting.FLAG_COLLECTION_INTERVAL.value() > 0) return;

        this.resetCountdown = new FlagResetCountdown(this);
        this.resetCountdown.runTaskTimerAsynchronously(CGM.get(), 0L, 20L);
    }

    public void returnFlag() {
        if (this.respawnAtHome) {
            this.place(this.home, this.direction.getBlockFace(), false);
        } else {
            // we're returning the flag to the last location - only time we don't use setState()
            // setting the state to DROPPED will allow the reset countdown to start
            this.state = FlagState.DROPPED;
            this.place(this.lastLocation, false);
        }
    }

    public void place(Location location, boolean instantRespawn) {
        this.place(location, this.getLocationBlockFace(location), instantRespawn);
    }

    private void place(Location location, BlockFace blockFace, boolean instantRespawn) {
        // update flag location
        this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);

        if (instantRespawn) {
            this.startCheckers(true);
        } else {
            // place the banner
            this.createFlag(blockFace);
        }

        // Reset respawn at home and last location
        this.respawnAtHome = true;
        this.lastLocation = null;
    }

    private void createFlag(BlockFace blockFace) {
        final Flag flag = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                // block where the banner will be placed
                Block block = flag.location.getBlock();

                // Store the block state where the banner will be placed
                flag.postState = block.getState();

                // set the block to a banner
                block.setType(Material.STANDING_BANNER);

                // update banner color
                Banner banner = (Banner) block.getState();
                banner.setBaseColor(ColorConverter.getDyeColor(flag.getFlagColor()));

                // set banner direction
                Directional directional = (Directional) banner.getData();
                directional.setFacingDirection(blockFace);

                // update banner
                banner.update();

                // store the base below the banner
                Block base = block.getRelative(BlockFace.DOWN);
                flag.baseState = base.getState();

                if (base.isLiquid()) {
                    Material type = base.getType();
                    if (type == Material.LAVA || type == Material.STATIONARY_LAVA) base.setType(Material.REDSTONE_BLOCK);
                    else base.setType(Material.ICE);
                }

                // create/update flag waypoint
                flag.setFlagWaypointLocation(flag.location);

                // start the flag and return checker
                flag.startCheckers(false);
            }
        }.runTask(CGM.get()); // run sync
    }

    private void startCheckers(boolean instantRespawn) {
        // don't activate flag checker until match starts
        if (MatchState.isState(MatchState.STARTED) && !instantRespawn) this.startChecker();

        // if not placed at home, start return
        if (this.location != this.home) {
            this.startReturn();
        }
    }

    private BlockFace getLocationBlockFace(Location location) {
        return FlagDirection.values()[Math.round(location.getYaw() / 45f) & 0x7].getBlockFace().getOppositeFace();
    }

    private String getScoreboardTime() {
        int seconds = 0;

        if (this.isState(FlagState.RESPAWNING)) {
            seconds = this.respawnCountdown.getSeconds();
        } else if (this.isState(FlagState.DROPPED) && this.resetCountdown != null) {
            seconds = this.resetCountdown.getSeconds();
        }

        return seconds == 0 ? "" : ChatColor.GRAY + " " + seconds + "s";
    }

    private MatchTeam getCollectedBy() {
        Hill hill = FlagUtils.getHill(this);
        return this.isState(FlagState.DROPPED) && hill != null ? hill.getControlledBy() : null;
    }

    private ChatColor getIconColor() {
        if (this.carrier != null) return MatchTeam.getTeam(this.carrier).getColor();
        if (this.getCollectedBy() != null) return this.getCollectedBy().getColor();
        return this.getFlagColor();
    }

    public String getTitle() {
        if (this.state == null) return "";
        ChatColor iconColor = this.getIconColor();
        String icon = this.state.getSymbol();
        String time = this.getScoreboardTime();
        return " " + iconColor + icon + " " + this.getFlagColor() + this.name + time;
    }

    public void updateScoreboard() {
        String title = this.getTitle();
        if (title.isEmpty()) return;
        ScoreboardManager.setScore(this.scoreboardRow, title);
        if (this.flagWaypoint != null && !this.isState(FlagState.TAKEN)) this.flagWaypoint.setName(title);
    }

    public void setFlagWaypointLocation(Location location) {
        if (this.flagWaypoint == null) this.flagWaypoint = new Waypoint(this, location);
        else this.flagWaypoint.setLocation(this.getTitle(), location);
    }

    public void reset() {
        this.reset(false);
    }

    private void reset(boolean deactivate) {
        this.resetBase();

        if (!deactivate) {
            this.scoreboardRow = -1;
            this.home = null;
            this.state = null;
            this.location = null;
        }

        this.active = false;
        this.respawnAtHome = true;
        this.lastLocation = null;
        this.clearCarrier();
        if (this.flagWaypoint != null) this.flagWaypoint.destroy();
        this.flagWaypoint = null;
        this.cooldowns = new HashMap<>();
        TaskUtils.cancelIfRunning(this.checker);
        TaskUtils.cancelIfRunning(this.returnChecker);
        TaskUtils.cancelIfRunning(this.resetCountdown);
        TaskUtils.cancelIfRunning(this.respawnCountdown);
        TaskUtils.cancelIfRunning(this.flagTracker);
        TaskUtils.cancelIfRunning(this.returnRing);
        this.checker = null;
        this.returnChecker = null;
        this.resetCountdown = null;
        this.respawnCountdown = null;
        this.flagTracker = null;
        this.returnRing = null;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onParticipantDeath(ParticipantDeathEvent event) {
        if (this.carrier == event.getParticipant()) this.setState(FlagState.DROPPED);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onMatchQuit(MatchQuitEvent event) {
        Participant participant = event.getParticipant();
        if (participant != null && this.carrier == participant) this.setState(FlagState.DROPPED);
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (this.active) this.startChecker();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        // TODO why did I add this? wouldnt this make multi round flag game modes not work?
        if (this.active) this.reset();
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (this.isLoaded()) this.reset();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null
                && item.getType() == Material.BANNER
                && !item.getItemMeta().getLore().isEmpty()) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            Participant participant = MatchManager.getParticipant(player);

            if (this.carrier == participant) {
                if (!FlagUtils.canPlace(this, player.getLocation())) {
                    CommandUtils.sendErrorMessage(player, "You can't drop the flag here!");
                    return;
                }

                this.addCooldown(participant);
                this.setState(FlagState.DROPPED);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.flagWaypoint != null) this.flagWaypoint.showLunarWaypoint(event.getPlayer());
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.active
            || !this.isState(FlagState.DROPPED, FlagState.RETURNED)
            || this.location == null) return;

        Location blockLoc = event.getBlock().getLocation();
        Location flagLoc = this.location.getBlock().getLocation();

        if (!blockLoc.equals(flagLoc)) return;

        Player player = event.getPlayer();
        Participant participant = MatchManager.getParticipant(player);

        if (participant != null && this.canTake(participant, true)) this.setCarrier(participant);
    }

}
