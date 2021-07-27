package net.purelic.cgm.core.maps.hill;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.constants.WaypointVisibility;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.region.Area;
import net.purelic.cgm.core.maps.Objective;
import net.purelic.cgm.core.maps.Waypoint;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.constants.HillModifiers;
import net.purelic.cgm.core.maps.hill.constants.HillType;
import net.purelic.cgm.core.maps.hill.events.HillLostEvent;
import net.purelic.cgm.core.maps.hill.events.HillReclaimedEvent;
import net.purelic.cgm.core.maps.hill.runnables.HillCaptureCountdown;
import net.purelic.cgm.core.maps.hill.runnables.HillChecker;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class Hill extends Objective<HillModifiers> implements Listener {

    private static final int VERTICAL_HEIGHT = 5;

    // modifiers
    private final int radius;
    private final boolean square;
    private final String rawName;
    private final String name;
    private final Material material;
    private final HillType type;
    private final boolean destructive;
    private final WaypointVisibility waypointVisibility;

    // visual
    private final ChatColor baseColor;
    private final List<BlockState> blockStates;
    private Waypoint waypoint;
    private Area area;

    // runnables
    private HillChecker checker;
    private HillCaptureCountdown captureCountdown;

    // capturing
    private final List<Player> players;
    private final List<MatchTeam> teams;
    private float progress;
    private MatchTeam capturedBy;
    private MatchTeam controlledBy;

    // utils
    private int scoreboardRow;
    private boolean locked;

    public Hill(Map<String, Object> yaml) {
        super(yaml);
        this.radius = this.get(HillModifiers.RADIUS, 3);
        this.square = !this.get(HillModifiers.CIRCLE, true);
        this.rawName = this.get(HillModifiers.NAME, "Hill");
        this.name = (this.isNeutral() ? ChatColor.WHITE : this.getOwner().getColor()) + this.rawName + ChatColor.RESET;
        this.material = this.get(HillModifiers.MATERIAL, Material.WOOL);
        this.destructive = this.get(HillModifiers.DESTRUCTIVE, true);
        this.type = this.get(HillModifiers.TYPE, HillType.KOTH_HILL);
        this.waypointVisibility = this.get(HillModifiers.WAYPOINT_VISIBILITY, WaypointVisibility.EVERYONE);
        boolean invert = this.getOwner().getColor() == ChatColor.WHITE;
        this.baseColor = this.isNeutral() ? ChatColor.WHITE : (invert ? ChatColor.BLACK : this.getOwner().getColor());
        this.blockStates = new ArrayList<>();
        this.scoreboardRow = -1;
        this.locked = false;
        this.players = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.progress = 1F;
        this.capturedBy = this.isNeutral() ? null : this.getOwner();
        this.controlledBy = this.capturedBy;
        Commons.registerListener(this);
    }

    public HillType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public ChatColor getBaseColor() {
        return this.baseColor;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.updateScoreboard();
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public List<MatchTeam> getTeams() {
        return this.teams;
    }

    public MatchTeam getCapturedBy() {
        return this.capturedBy;
    }

    @Override
    public boolean isCapturedBy(MatchTeam team) {
        return this.capturedBy == team;
    }

    public void setCapturedBy(MatchTeam team) {
        this.capturedBy = team;
    }

    public MatchTeam getControlledBy() {
        return this.controlledBy;
    }

    public void setControlledBy(MatchTeam team) {
        this.controlledBy = team;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        this.updateProgress();
        this.updateScoreboard();
    }

    public boolean isCaptured() {
        return this.capturedBy != null;
    }

    public Waypoint getWaypoint() {
        return this.waypoint;
    }

    public void setWorld(World world, int scoreboardRow, boolean active) {
        super.setWorld(world);
        this.scoreboardRow = scoreboardRow;
        this.setActive(active);
        this.area = new Area(
            this.getCenter().clone().subtract(this.radius, 0, this.radius),
            this.getCenter().clone().add(this.radius, 0, this.radius)
        );
        if (active) this.resetProgress();
    }

    public int getScoreboardRow() {
        return this.scoreboardRow;
    }

    public void activate() {
        this.setActive(true);
        this.checker = new HillChecker(this);
        this.resetProgress();
        TaskUtils.runTimerAsync(this.checker, 5);
    }

    public void resetProgress() {
        this.setProgress(1F);

        // clears the green score color if applicable
        ScoreboardManager.updateSoloBoard();
        ScoreboardManager.updateTeamBoard();
    }

    private void updateProgress() {
        ChatColor chatColor = this.controlledBy == null ? this.baseColor : this.controlledBy.getColor();

        if (this.progress == 1F) {
            if (this.waypoint == null) {
                if (this.waypointVisibility == WaypointVisibility.EVERYONE) this.waypoint = new Waypoint(this);
            } else {
                this.waypoint.update(chatColor, this.getTitle());
            }
        }

        Block center = this.getCenterBlock();
        boolean save = this.blockStates.isEmpty();

        for (int xPoint = center.getX() - this.radius; xPoint <= center.getX() + this.radius; xPoint++) {
            for (int zPoint = center.getZ() - this.radius; zPoint <= center.getZ() + this.radius; zPoint++) {
                Block block = this.getWorld().getBlockAt(xPoint, center.getY(), zPoint);

                // If the hill is not destructive, only replace blocks
                // that are the same type of material as the hill
                if (!this.destructive) {
                    Material type = block.getType();

                    if ((this.material == Material.STAINED_GLASS && type != Material.GLASS)
                        || (this.material == Material.STAINED_CLAY && type != Material.HARD_CLAY)
                        || (this.material == Material.WOOL && type != Material.WOOL)) {
                        continue;
                    }
                }

                if (save) this.blockStates.add(block.getState());

                if (this.isInside(block, this.progress)) this.updateBlockColor(block, chatColor);
                else if (this.isInside(block, 1F)) this.updateBlockColor(block, this.baseColor);
            }
        }
    }

    public boolean isInside(Location location) {
        int hillY = this.getCenter().getBlockY();

        // check if the player has a valid y-level
        if (location.getY() < hillY || location.getY() > hillY + VERTICAL_HEIGHT) {
            return false;
        }

        if (this.square) {
            return this.area.contains(location);
        } else {
            // normalize the player's (or flag's) location y-level with the hill's y-level
            Location normalized = location.clone();
            normalized.setY(hillY);
            return this.isInsideCircle(normalized, true, 1F);
        }
    }

    public boolean isInside(Block block, float percent) {
        if (this.square) return this.isInsideSquare(block, percent);
        else return this.isInsideCircle(block, percent);
    }

    private boolean isInsideCircle(Block block, float percent) {
        return this.isInsideCircle(block.getLocation().clone().add(0.5, 0, 0.5), false, percent);
    }

    private boolean isInsideCircle(Location location, boolean player, float percent) {
        // block physic events in other worlds can trigger this
        if (location.getWorld() != this.getCenter().getWorld()) return false;
        return this.getCenter().distance(location) <= (this.radius + (player ? 1.0 : 0.5)) * percent;
    }

    private boolean isInsideSquare(Block block, float percent) {
        if (percent == 1F) return true;

        int radius = (int) (this.radius * percent);

        return new Area(
            this.getCenter().clone().subtract(radius, 0, radius),
            this.getCenter().clone().add(radius, 0, radius)
        ).contains(block.getLocation());
    }

    private void updateBlockColor(Block block, ChatColor chatColor) {
        final DyeColor color = ColorConverter.getDyeColor(chatColor);
        final BlockState state = block.getState();

        TaskUtils.run(() -> {
            boolean update = false;

            if (state.getType() != this.material) {
                state.setType(this.material);
                update = true;
            }

            if (state.getRawData() != color.getWoolData()) {
                state.setRawData(color.getWoolData());
                update = true;
            }

            if (update) state.update(true, false);
        });
    }

    public void enter(Player player) {
        if (!this.players.contains(player)) { // if new player enters hill
            this.players.add(player);
            MatchTeam team = MatchTeam.getTeam(player);

            if (!this.teams.contains(team)) { // if new team enters hill
                this.teams.add(team);
                if (this.capturedBy != team || this.progress < 1F) this.startCapture();
            }
        }
    }

    public void exit(Player player) {
        // if the player was not in the hill
        if (!this.players.remove(player)) return;

        ChatUtils.clearActionBar(player);

        // removes green score color (if applicable)
        ScoreboardManager.updateSoloBoard();

        MatchTeam team = MatchTeam.getTeam(player);
        boolean teamRemoved = false;

        // if all players of this participant's team have left the hill
        if (this.players.stream().noneMatch(p -> MatchTeam.getTeam(p) == team)) {
            teamRemoved = this.teams.remove(team); // remove team from hill
        }

        // reset hill if capture lock is not enabled and the team that captured the hill is the team that was removed
        if (!ToggleSetting.CAPTURE_LOCK.isEnabled() && teamRemoved && this.capturedBy == team && (this.isNeutral() || this.getOwner() != team)) {
            if (!this.isNeutral()) Commons.callEvent(new HillReclaimedEvent(this));
            Commons.callEvent(new HillLostEvent(this, this.capturedBy));

            this.capturedBy = this.isNeutral() ? null : this.getOwner();
            this.controlledBy = this.capturedBy;
            this.resetProgress();
        }

        if (this.teams.size() == 1) this.startCapture();
    }

    private void startCapture() {
        if (TaskUtils.isRunning(this.captureCountdown)) return;
        this.captureCountdown = new HillCaptureCountdown(this);
        TaskUtils.runTimerAsync(this.captureCountdown, 2);
    }

    public List<Flag> getCollectedFlags() {
        List<Flag> flags = new ArrayList<>();

        FlagUtils.getFlags()
            .stream()
            .filter(flag ->
                !flag.hasCarrier()
                    && this.isInside(flag.getLocation())
                    && this.capturedBy != flag.getOwner())
            .forEach(flags::add);

        return flags;
    }

    public void updateScoreboard() {
        if (this.scoreboardRow == -1) return;

        String score = this.getTitle(true, true, true);
        String forceColor = "";

        if (this.locked) {
            ChatColor color = this.capturedBy == null ? ChatColor.WHITE : this.capturedBy.getColor();
            forceColor = "" + color + ChatColor.STRIKETHROUGH;
        }

        ScoreboardManager.setScore(this.scoreboardRow, score, forceColor);
    }

    public String getTitle() {
        return this.getTitle(true, false, true);
    }

    public String getTitle(boolean showSymbol, boolean showProgress, boolean showTime) {
        String symbol = showSymbol ? this.baseColor + (this.isCaptured() || !this.isNeutral() ? "⦿ " : "⦾ ") : "";
        ChatColor color = this.capturedBy == null ? ChatColor.WHITE : this.capturedBy.getColor();
        String progress = this.controlledBy != null && this.progress < 1F ? this.controlledBy.getColor() + "" + ((int) (this.progress * 100F)) + "% " : "";
        String time = showTime ? this.getScoreboardTime() : "";
        String prefix = showProgress && this.progress < 1F ? progress : symbol;
        String strike = this.locked ? ChatColor.STRIKETHROUGH + "" : "";
        return prefix + color + strike + this.rawName + ChatColor.RESET + time;
    }

    public String getScoreboardTime() {
        int seconds = 0;
        int moveInterval = NumberSetting.HILL_MOVE_INTERVAL.value();
        int collectionInterval = NumberSetting.FLAG_COLLECTION_INTERVAL.value();

        if (moveInterval > 0) {
            seconds = MatchCountdown.getElapsed() % moveInterval;
            seconds = moveInterval - seconds;
            seconds = seconds == 0 ? moveInterval : seconds;
        } else if (collectionInterval > 0 && this.type == HillType.CTF_GOAL) {
            seconds = MatchCountdown.getElapsed() % collectionInterval;
            seconds = collectionInterval - seconds;
            seconds = seconds == 0 ? collectionInterval : seconds;
        }

        boolean showSeconds = MatchState.isState(MatchState.STARTED) && seconds > 0;
        return showSeconds ? ChatColor.GRAY + " " + seconds + "s" : "";
    }

    public void destroyWaypoint() {
        if (this.waypoint != null) this.waypoint.destroy();
        this.waypoint = null;
    }

    private void remove() {
        this.blockStates.forEach(state -> state.update(true));
        this.blockStates.clear();
        this.destroyWaypoint();
    }

    public void reset(boolean remove) {
        this.players.clear();
        this.teams.clear();
        this.capturedBy = this.isNeutral() ? null : this.getOwner();
        this.controlledBy = this.capturedBy;

        TaskUtils.cancelIfRunning(this.checker);
        TaskUtils.cancelIfRunning(this.captureCountdown);

        if (remove) this.remove();
        else this.resetProgress();

        this.setActive(false);
        this.locked = false;
    }

    public void destroy() {
        this.destroyWaypoint();

        TaskUtils.cancelIfRunning(this.checker);
        TaskUtils.cancelIfRunning(this.captureCountdown);

        this.unload();
        this.setActive(false);
        this.scoreboardRow = -1;
        this.progress = 1F;
        this.blockStates.clear();
        this.players.clear();
        this.teams.clear();
        this.capturedBy = this.isNeutral() ? null : this.getOwner();
        this.controlledBy = this.capturedBy;
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (this.isLoaded() && NumberSetting.HILL_MOVE_INTERVAL.value() <= 0) this.activate();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        if (this.isActive() || this.isLoaded()) this.reset(false);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (this.isActive() || this.isLoaded()) this.destroy();
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onMatchQuit(MatchQuitEvent event) {
        this.exit(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onParticipantDeath(ParticipantDeathEvent event) {
        this.exit(event.getParticipant().getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.waypoint != null) this.waypoint.showLunarWaypoint(event.getPlayer());
    }

}
