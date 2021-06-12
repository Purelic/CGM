package net.purelic.cgm.core.maps.hill;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.Area;
import net.purelic.cgm.core.maps.Waypoint;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.constants.HillType;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.listeners.modules.HeadModule;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.cgm.utils.YamlUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Hill implements Listener {

    private final int[] coords;
    private final MatchTeam owner;
    private final int radius;
    private final boolean square;
    private final String name;
    private final Material material;
    private final HillType type;

    private int scoreboardRow;
    private World world;
    private Location center;
    private boolean active;
    private Set<BlockState> blocks;
    private Area area;
    private Waypoint waypoint;

    private Set<Participant> participants;
    private Set<MatchTeam> teams;
    private MatchTeam capturedByTeam;
    private Participant capturedByParticipant;
    private boolean captured;
    private boolean contested;
    private float captureProgress;
    private ChatColor capturingColor;

    private BukkitRunnable capturing;
    private BukkitRunnable checker;

    public Hill(Map<String, Object> map) {
        this.coords = YamlUtils.getCoords(((String) map.get("location")).split(","));
        this.owner = MatchTeam.valueOf((String) map.getOrDefault("owner", "SOLO"));
        this.radius = (int) map.getOrDefault("radius", 3);
        this.square = !((boolean) map.getOrDefault("circle", true));
        this.name = (String) map.getOrDefault("name", "The Hill");
        this.material = Material.valueOf((String) map.getOrDefault("material", "WOOL"));
        this.type = HillType.valueOf((String) map.getOrDefault("type", "KOTH_HILL"));
        this.reset();
        CGM.getPlugin().registerListener(this);
    }

    public int[] getCoords() {
        return this.coords;
    }

    public HillType getType() {
        return this.type;
    }

    public MatchTeam getOwner() {
        return this.owner;
    }

    public MatchTeam getControlledBy() {
        return this.captured ? this.capturedByTeam : this.getOwner();
    }

    public Material getMaterial() {
        return this.material;
    }

    public boolean isNeutral() {
        return this.owner == MatchTeam.SOLO;
    }

    public String getName() {
        return this.name;
    }

    public String getColoredName() {
        return (this.isNeutral() ? ChatColor.WHITE : this.owner.getColor()) + this.name + ChatColor.RESET;
    }

    public Location getCenter() {
        return this.center;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isLoaded() {
        return this.world != null;
    }

    public boolean isEmpty() {
        return this.participants.isEmpty();
    }

    public boolean hasNoEnemies() {
        return !this.isNeutral() && this.participants.stream().noneMatch(participant -> MatchTeam.getTeam(participant) != this.owner);
    }

    public boolean isCaptured() {
        return this.captured;
    }

    public Participant getCapturedByParticipant() {
        return this.capturedByParticipant;
    }

    public MatchTeam getCapturedByTeam() {
        return this.capturedByTeam;
    }

    public Waypoint getWaypoint() {
        return this.waypoint;
    }

    public void setWorld(int scoreboardRow, World world, boolean active) {
        this.scoreboardRow = scoreboardRow;
        this.world = world;
        this.center = new Location(world, this.coords[0], this.coords[1], this.coords[2]).add(0.5, 0, 0.5);
        this.active = active;

        if (active) {
            this.resetColor();
        }
    }

    public ChatColor getColor() {
        boolean invert = this.owner.getColor() == ChatColor.WHITE;
        return !this.isNeutral() ? (invert ? ChatColor.BLACK : this.owner.getColor()) : ChatColor.WHITE;
    }

    private void resetColor() {
        this.setColor(this.getColor(), 1F);

        // Clears the green score color if applicable
        ScoreboardManager.updateSoloBoard();
    }

    private void clear() {
        this.blocks.forEach(state -> state.update(true));
        this.blocks.clear();
        this.waypoint.destroy();
        this.waypoint = null;
    }

    private void setColor(ChatColor chatColor, float percent) {
        if (percent == 1F) {
            if (this.waypoint == null) {
                final Hill hill = this;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        waypoint = new Waypoint(hill);
                    }
                }.runTask(CGM.getPlugin());
               //  this.waypoint = new Waypoint(this);
            } else {
                this.waypoint.update(chatColor, this.getTitle());
            }
        }

        this.updateScoreboard();

        DyeColor color = ColorConverter.getDyeColor(chatColor);
        Block center = this.center.getBlock();
        boolean save = this.blocks.isEmpty();

        for (int xPoint = center.getX() - this.radius; xPoint <= center.getX() + this.radius; xPoint++) {
            for (int zPoint = center.getZ() - this.radius; zPoint <= center.getZ() + this.radius; zPoint++) {
                Block block = center.getWorld().getBlockAt(xPoint, center.getY(), zPoint);
                if (save) this.blocks.add(block.getState());
                if (this.isInside(block, percent)) this.updateBlockColor(block, color);
            }
        }
    }

    private void updateBlockColor(Block block, DyeColor color) {
        if (block.getType() != this.material) block.setType(this.material);

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setData(color.getWoolData());
            }
        }.runTask(CGM.getPlugin());
    }

    public boolean isInside(Block block, float percent) {
        if (this.square) return this.isInsideSquare(block, percent);
        else return this.isInsideCircle(block, percent);
    }

    public boolean isInside(Player player) {
        return this.isInside(player.getLocation());
    }

    public boolean isInside(Location loc) {
        // TODO temporary, not critical but sometimes this gets called after the match ends and center has been reset to null
        if (this.center == null) return false;

        Location location = loc.clone();
        int yLevel = this.center.getBlockY();

        // check if the player has a correct y-level
        if (location.getY() < yLevel || location.getY() > yLevel + 5) {
            return false;
        }

        if (this.square) {
            if (this.area == null) {
                this.area = new Area(
                        this.center.clone().subtract(this.radius, 0, this.radius),
                        this.center.clone().add(this.radius, 0, this.radius)
                );
            }

            return this.area.contains(location);
        } else {
            // normalize the player's y-level with the hill's y-level
            location.setY(yLevel);
            return this.isInsideCircle(location, true, 1F);
        }
    }

    private boolean isInsideCircle(Block block, float percent) {
        return this.isInsideCircle(block.getLocation().clone().add(0.5, 0, 0.5), false, percent);
    }

    private boolean isInsideCircle(Location location, boolean player, float percent) {
        if (this.center.getWorld() != location.getWorld()) return false; // todo idk why/how this happens
        return this.center.distance(location) <= (this.radius + (player ? 1.0 : 0.5)) * percent;
    }

    private boolean isInsideSquare(Block block, float percent) {
        if (percent == 1F) return true;

        int radius = (int) (this.radius * percent);

        Area area = new Area(
                this.center.clone().subtract(radius, 0, radius),
                this.center.clone().add(radius, 0, radius)
        );

        return area.contains(block.getLocation());
    }

    public void activate() {
        this.active = true;
        this.resetColor();

        this.checker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Participant participant : MatchManager.getParticipants()) {
                    Player player = participant.getPlayer();
                    boolean inside = !participant.isDead() && isInside(player);

                    if (!ToggleSetting.PERMANENT_HILLS.isEnabled()) {
                        if (inside) addParticipant(participant);
                        else removeParticipant(participant);
                    }

                    objectiveCheck(participant, inside);
                }
            }
        };

        this.checker.runTaskTimerAsynchronously(CGM.getPlugin(), 0, 2);
    }

    private void addParticipant(Participant participant) {
        Player player = participant.getPlayer();

        if (this.participants.add(participant)) { // if new player enters hill
            MatchTeam team = MatchTeam.getTeam(player);

            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                if (this.participants.size() == 1) {
                    if (this.capturedByParticipant != participant) this.startCapture(participant); // only player in hill
                } else {
                    this.contested = true; // not only player in hill
                }
            } else {
                if (this.teams.add(team)) { // if new team enters hill
                    if (this.teams.size() == 1) {
                        if (this.isNeutral() || this.owner != team) {
                            if (this.capturedByTeam != team) this.startCapture(participant); // only team in hill and hill is neutral or enemy hill
                        } // else they're in their own hill - do nothing
                    } else {
                        this.contested = true; // other teams are inside
                    }
                }
            }
        }

        if (this.contested) ChatUtils.sendActionBar(player, this.getColoredName() + " is contested!");
    }

    private void removeParticipant(Participant participant) {
        if (this.participants.remove(participant)) { // if the player was removed from the hill
            Player player = participant.getPlayer();
            MatchTeam team = MatchTeam.getTeam(player);
            boolean teamRemoved = false;

            ChatUtils.sendActionBar(player, "");

            // if all players of this participant's team have left the hill
            if (this.participants.stream().filter(p -> MatchTeam.getTeam(p.getPlayer()) == team).toArray().length == 0) {
                this.teams.remove(team); // remove team from hill
                teamRemoved = true;
            }

            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                this.contested = this.participants.size() > 1; // check if multiple players are present
            } else {
                this.contested = this.teams.size() > 1; // check if multiple teams are present
            }

            // reset hill if capture lock is not enabled and hill is currently captured and empty
            if (!ToggleSetting.CAPTURE_LOCK.isEnabled() && this.captured && (this.isEmpty() || !this.contested)) {
                if (teamRemoved && this.capturedByTeam == team) { // reset if the team that captured the hill is the team that was removed
                    if (this.isNeutral()) Bukkit.broadcastMessage(" ⦿ " + this.getColoredName() + " is now uncontested!");
                    else Bukkit.broadcastMessage(" ⦿ " + this.getColoredName() + " has been reclaimed!");

                    // clear action bar message
                    this.participants.forEach(p -> ChatUtils.sendActionBar(p.getPlayer(), ""));

                    this.captured = false;
                    this.contested = false;
                    this.capturedByTeam = null;
                    this.capturedByParticipant = null;
                    this.captureProgress = 1F;

                    this.resetColor();

                    // clears green scoreboard color
                    ScoreboardManager.updateTeamBoard();
                    // ScoreboardManager.updateTeamBoard(team);

                    return;
                }
            }

            // if hill is not contested
            if (!this.contested && !this.isEmpty()) {
                Participant capturedBy = this.participants.stream().findFirst().get();
                MatchTeam capturedByTeam = MatchTeam.getTeam(capturedBy.getPlayer());

                // if hill is not captured by participant or their team and hill is neutral or not owned by participant
                if (this.capturedByParticipant != capturedBy && (this.isNeutral() || this.owner != capturedByTeam)) {
                    if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO) || this.capturedByTeam != capturedByTeam) {
                        if (!ToggleSetting.CAPTURE_LOCK.isEnabled()) {
                            MatchTeam previouslyCapturedBy = this.capturedByTeam;

                            this.captured = false;
                            this.capturedByTeam = null;
                            this.capturedByParticipant = null;
                            this.captureProgress = 1F;
                            this.resetColor();

                            // clears green scoreboard color
                            ScoreboardManager.updateTeamBoard();
                        }

                        this.startCapture(capturedBy);
                    }
                } else {
                    // clear action bar message
                    this.participants.forEach(p -> ChatUtils.sendActionBar(p.getPlayer(), ""));
                }
            }
        }
    }

    private void objectiveCheck(Participant participant, boolean insideHill) {
        MatchTeam team = participant.getTeam();
        boolean captured = EnumSetting.TEAM_TYPE.is(TeamType.SOLO) ?
                (this.capturedByParticipant == participant)
                : (this.isNeutral() || this.captured ? this.capturedByTeam == team : this.owner == team);

        if (insideHill && captured) {
            if (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER)) {
                HeadModule.scoreHeads(participant);
            } else if (EnumSetting.GAME_TYPE.is(GameType.CAPTURE_THE_FLAG)) {
                FlagUtils.captureFlag(participant);
            }
        }
    }

    public Set<Flag> getCollectedFlags() {
        Set<Flag> flags = new HashSet<>();

        FlagUtils.getFlags()
                .stream()
                .filter(flag ->
                        !flag.hasCarrier()
                                && this.isInside(flag.getLocation())
                                && this.getControlledBy() != flag.getOwner())
                .forEach(flags::add);

        return flags;
    }

    private void startCapture(Participant participant) {
        if (TaskUtils.isRunning(this.capturing)) return;

        Player player = participant.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        boolean solo = EnumSetting.TEAM_TYPE.is(TeamType.SOLO);
        String capturedBy = (solo ? NickUtils.getDisplayName(player) : team.getColoredName()) + ChatColor.RESET;
        Bukkit.broadcastMessage(" ⦿ " + this.getColoredName() + " is now being captured by " + capturedBy + "!");
        this.capturingColor = team.getColor();

        this.capturing = new BukkitRunnable() {

            private final int delay = NumberSetting.HILL_CAPTURE_DELAY.value();
            private float progress = 0;
            private int tick = 0;

            @Override
            public void run() {
                this.tick++;

                if (isEmpty() || contested || hasNoEnemies()) {
                    captureProgress = 1F;

                    if (!captured) resetColor();
                    else setColor(capturedByTeam.getColor(), 1F);

                    this.cancel();
                } else if (this.progress >= this.delay) {
                    // sound effects
                    if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                        if (capturedByParticipant != null) {
                            SoundUtils.SFX.HILL_LOST.play(capturedByParticipant.getPlayer());
                        }

                        SoundUtils.SFX.HILL_CAPTURED.play(player);
                    } else {
                        if (capturedByTeam != null) {
                            SoundUtils.SFX.HILL_LOST.play(capturedByTeam.getPlayers());
                        }

                        SoundUtils.SFX.HILL_CAPTURED.play(team.getPlayers());
                    }

                    MatchTeam previouslyCapturedBy = capturedByTeam;

                    captured = true;
                    capturedByTeam = team;
                    capturedByParticipant = participant;
                    captureProgress = 1F;

                    // clears green scoreboard color
                    if (previouslyCapturedBy != null) {
                        ScoreboardManager.updateTeamBoard();
                        // ScoreboardManager.updateTeamBoard(previouslyCapturedBy);
                    }

                    Bukkit.broadcastMessage(" ⦿ " + getColoredName() + " has been captured by " + capturedBy + "!");
                    setColor(team.getColor(), 1F);
                    this.cancel();
                } else {
                    double multiplier = NumberSetting.HILL_CAPTURE_MULTIPLIER.value();
                    double bonus = (0.1 * (multiplier / 100)) * (participants.size() - 1);
                    this.progress += (0.1 + bonus);
                    Set<Participant> participantCopy = new HashSet<>(participants); // it's possible players could get removed (ConcurrentModificationException)

                    for (Participant participant : participantCopy) {
                        Player pl = participant.getPlayer();

                        if (!isInside(pl) || participant.isDead()) continue; // check if player is still in hill

                        if (this.progress < this.delay) {
                            float percent = this.progress / this.delay;
                            captureProgress = percent;

                            ChatUtils.sendActionBar(pl, getProgressBar());
                            setColor(team.getColor(), percent);

                            if (this.tick % 4 == 0) { // sound effect
                                float pitch = (1.5F * percent) + 0.5F;
                                pl.playSound(pl.getLocation(), Sound.CLICK, 1F, pitch);
                            }
                        } else {
                            ChatUtils.sendActionBar(pl, team.getColor() + "You" + ChatColor.RESET + " captured " + getColoredName() + "!");
                        }
                    }
                }
            }

        };

        this.capturing.runTaskTimerAsynchronously(CGM.getPlugin(), 0L, 2L);
    }

    private String getProgressBar() {
        char symbol = '|';
        ChatColor completed = ChatColor.GREEN;
        ChatColor incomplete = ChatColor.GRAY;
        int totalBars = 40;
        int progressBars = (int) (totalBars * this.captureProgress);
        String progress = (new DecimalFormat("#.#").format(this.captureProgress * 100));

        return "Capturing " + this.getColoredName() + " " +
                ChatColor.DARK_GRAY + "[" + ChatColor.RESET +
                StringUtils.repeat("" + completed + symbol, progressBars) +
                StringUtils.repeat("" + incomplete + symbol, totalBars - progressBars) +
                ChatColor.DARK_GRAY + "]" +
                ChatColor.AQUA + " " + progress + (progress.contains(".") ? "" : ".0") + "%";
    }

    public void updateScoreboard() {
        if (this.scoreboardRow == -1) return;

        String symbol = this.captured || !this.isNeutral() ? "⦿" : "⦾";
        ChatColor iconColor = this.captured ? this.capturedByTeam.getColor() : this.getColor();
        String icon = this.captureProgress == 1F ? iconColor + symbol + " " : "";

        String progress = this.captureProgress < 1F ? this.capturingColor + "" + ((int) (this.captureProgress * 100F)) + "% " : "";
        String score = " " + progress + icon + this.getColor() + this.name + this.getScoreboardTime();

        ScoreboardManager.setScore(this.scoreboardRow, score);
    }

    public int getScoreboardRow() {
        return this.scoreboardRow;
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

    public String getTitle() {
        String symbol = this.captured || !this.isNeutral() ? "⦿ " : "⦾ ";
        ChatColor iconColor = this.captured ? this.capturedByTeam.getColor() : this.getColor();
        return iconColor + symbol + this.getColoredName() + this.getScoreboardTime();
    }

    public void cancel(boolean clear) {
        this.participants.clear();
        this.teams.clear();
        this.capturedByTeam = null;
        this.capturedByParticipant = null;
        this.captured = false;
        this.contested = false;
        this.captureProgress = 1F;

        if (this.checker != null) this.checker.cancel();
        if (this.capturing != null) this.capturing.cancel();

        if (clear) this.clear();
        else this.resetColor();

        this.active = false;
    }

    public void reset() {
        this.scoreboardRow = -1;
        this.world = null;
        this.center = null;
        this.active = false;
        this.blocks = new HashSet<>();
        this.area = null;
        this.waypoint = null;

        this.participants = new HashSet<>();
        this.teams = new HashSet<>();
        this.capturedByTeam = null;
        this.capturedByParticipant = null;
        this.captured = false;
        this.contested = false;
        this.captureProgress = 1F;
        this.capturingColor = null;

        if (this.checker != null) this.checker.cancel();
        if (this.capturing != null) this.capturing.cancel();
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (this.isLoaded() && NumberSetting.HILL_MOVE_INTERVAL.value() <= 0) this.activate();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        if (this.isActive() || this.isLoaded()) this.cancel(false);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        if (this.isActive() || this.isLoaded()) this.reset();
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onMatchQuit(MatchQuitEvent event) {
        this.removeParticipant(event.getParticipant());
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onParticipantDeath(ParticipantDeathEvent event) {
        this.removeParticipant(event.getParticipant());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.waypoint != null) this.waypoint.showLunarWaypoint(event.getPlayer());
    }

}
