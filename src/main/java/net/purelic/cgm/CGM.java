package net.purelic.cgm;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.purelic.cgm.commands.communication.GlobalCommand;
import net.purelic.cgm.commands.controls.*;
import net.purelic.cgm.commands.info.*;
import net.purelic.cgm.commands.league.NoSpectatorsCommand;
import net.purelic.cgm.commands.league.RankCommand;
import net.purelic.cgm.commands.league.ReRollCommand;
import net.purelic.cgm.commands.league.ReadyCommand;
import net.purelic.cgm.commands.match.*;
import net.purelic.cgm.commands.preferences.ColorCommand;
import net.purelic.cgm.commands.preferences.HotbarCommand;
import net.purelic.cgm.commands.preferences.SoundCommand;
import net.purelic.cgm.commands.toggles.*;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.cgm.core.managers.LootManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.listeners.*;
import net.purelic.cgm.listeners.bed.BedBreak;
import net.purelic.cgm.listeners.flag.*;
import net.purelic.cgm.listeners.match.*;
import net.purelic.cgm.listeners.modules.*;
import net.purelic.cgm.listeners.modules.bedwars.*;
import net.purelic.cgm.listeners.modules.stats.ArrowStatsModule;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.cgm.listeners.participant.*;
import net.purelic.cgm.listeners.shop.ShopItemPurchase;
import net.purelic.cgm.listeners.shop.TeamUpgradePurchase;
import net.purelic.cgm.server.Playlist;
import net.purelic.cgm.voting.VotingManager;
import net.purelic.cgm.voting.VotingModule;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.runnables.MapLoader;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.function.Function;

public class CGM extends JavaPlugin {

    // TODO if you cancel the match cycle after voting you should be able to restart voting

    private static CGM plugin;
    private static boolean ready;
    private static boolean isPrivate;
    private static UUID owner;
    private static String serverName;

    private Playlist playlist;

    private MatchManager matchManager;
    private ScoreboardManager scoreboardManager;
    private VotingManager votingManager;

    private PaperCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        plugin = this;
        ready = false;

        // download lobby map
        TaskUtils.runAsync(new MapLoader("Lobby"));

        // download playlist
        this.playlist = new Playlist();

        // register managers, listeners, and commands
        this.registerManagers();
        this.registerModules();
        this.registerListeners();
        this.registerCommands();

        // load server/database info
        DatabaseUtils.loadServerDoc();
        serverName = ServerUtils.getName();
        if (serverName.contains("Unknown")) serverName = "PuRelic Network";

        this.setReady();
    }

    public static CGM getPlugin() {
        return plugin;
    }

    public static boolean isReady() {
        return ready;
    }

    private void setReady() {
        ready = true;
        DatabaseUtils.setServerOnline();
        isPrivate = ServerUtils.isPrivate();
        if (ServerUtils.isRanked()) {
            LeagueManager.loadListenerRegistration();
        }
        System.out.println("The server is now ready!");

        TaskUtils.runAsync(() -> playlist.getMaps().values().forEach(map -> map.getYaml().getAuthors().forEach(Fetcher::getNameOf)));
    }

    public static Playlist getPlaylist() {
        return plugin.playlist;
    }

    public static VotingManager getVotingManager() {
        return plugin.votingManager;
    }

    public MatchManager getMatchManager() {
        return this.matchManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    private void registerManagers() {
        this.matchManager = new MatchManager();
        this.scoreboardManager = new ScoreboardManager();
        this.votingManager = new VotingManager(this.playlist);
        LootManager.setLootItems();
    }

    private void registerModules() {
        Commons.registerListener(new VotingModule(this.votingManager));
    }

    private void registerListeners() {
        // Bed
        this.registerListener(new BedBreak());

        // Stats
        this.registerListener(new ArrowStatsModule());
        this.registerListener(new MatchStatsModule());

        // Flag
        this.registerListener(new FlagCapture());
        this.registerListener(new FlagDrop());
        this.registerListener(new FlagRespawn());
        this.registerListener(new FlagReturn());
        this.registerListener(new FlagsCollected());
        this.registerListener(new FlagTaken());

        // Match
        this.registerListener(new MapLoad());
        this.registerListener(new MatchEnd());
        this.registerListener(new MatchJoin());
        this.registerListener(new MatchQuit());
        this.registerListener(new MatchStart());
        this.registerListener(new MatchStateChange());
        this.registerListener(new RoundEnd());
        this.registerListener(new RoundStart());

        // Modules - Bed Wars
        this.registerListener(new BlastProofModule());
        this.registerListener(new BridgeEggModule());
        this.registerListener(new EnderChestModule());
        this.registerListener(new EnderPearlModule());
        this.registerListener(new FireballModule());
        this.registerListener(new InstantTNTModule());
        this.registerListener(new ItemDowngradeModule());
        this.registerListener(new NoBedDropModule());
        this.registerListener(new NoBottleModule());
        this.registerListener(new NoCraftingModule());
        this.registerListener(new ShopModule());
        this.registerListener(new TeamChestModule());
        this.registerListener(new TNTWaypointModule());
        this.registerListener(new TrapModule());
        this.registerListener(new VoidResourcesModule());

        // Modules
        this.registerListener(new ArmorLockModule());
        this.registerListener(new StraightArrowModule());
        this.registerListener(new ArrowSpawnModule());
        this.registerListener(new ArrowTrailModule());
        this.registerListener(new AutoLapisModule());
        this.registerListener(new BlockProtectionModule());
        this.registerListener(new CompassTrackingModule());
        this.registerListener(new DamageTrackerModule());
        this.registerListener(new ExplosionModule());
        this.registerListener(new GappleSpawnModule());
        this.registerListener(new HeadModule());
        this.registerListener(new HotbarModule());
        this.registerListener(new InstantKillModule());
        this.registerListener(new JumpPadModule());
        this.registerListener(new LootChestRefillModule());
        this.registerListener(new NoHungerModule());
        this.registerListener(new NoSleepingModule());
        this.registerListener(new RegenerationModule());
        this.registerListener(new RespawnModule());
        this.registerListener(new SpawnerModule());
        this.registerListener(new TeamChatModule());
        this.registerListener(new WeatherModule());
        this.registerListener(new WorldBorderModule());
        this.registerListener(new WorldSettingModule());

        // Participant
        this.registerListener(new ParticipantAssist());
        this.registerListener(new ParticipantDeath());
        this.registerListener(new ParticipantEliminate());
        this.registerListener(new ParticipantKill());
        this.registerListener(new ParticipantRespawn());
        this.registerListener(new ParticipantScore());

        // Shop
        this.registerListener(new ShopItemPurchase());
        this.registerListener(new TeamUpgradePurchase());

        // General
        this.registerListener(new EntityDamage());
        // this.registerListener(new OpStatusChange());
        this.registerListener(new PlayerChat());
        this.registerListener(new ItemLockModule());
        this.registerListener(new PlayerInteract());
        this.registerListener(new PlayerJoin());
        this.registerListener(new PlayerQuit());
        this.registerListener(new PlayerRankChange());
        this.registerListener(new ServerListPing());
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void registerCommandManager() {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                CommandExecutionCoordinator.simpleCoordinator();

//        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
//            AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();

        try {
            this.commandManager = new PaperCommandManager<>(
                /* Owning plugin */ this,
                /* Coordinator function */ executionCoordinatorFunction,
                /* Command Sender -> C */ mapperFunction,
                /* C -> Command Sender */ mapperFunction
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command manager");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        if (this.commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.commandManager.registerAsynchronousCompletions();
        }
    }

    private void registerCommands() {
        this.registerCommandManager();

        // Communication
        this.registerCommand(new GlobalCommand());

        // Controls
        this.registerCommand(new CancelCommand());
        this.registerCommand(new CycleCommand());
        this.registerCommand(new DownloadGameModeCommand());
        this.registerCommand(new DownloadMapCommand());
        this.registerCommand(new EndCommand());
        this.registerCommand(new RematchCommand());
        this.registerCommand(new SetNextCommand());
        // this.registerCommand(new ShutdownCommand());
        this.registerCommand(new StartCommand());

        // Info
        this.registerCommand(new GameModeCommand());
        this.registerCommand(new GameModesCommand());
        this.registerCommand(new MapCommand());
        this.registerCommand(new MapsCommand());
        this.registerCommand(new MatchCommand());
        this.registerCommand(new RoundsCommand());
        this.registerCommand(new ScoreLimitCommand());
        this.registerCommand(new TimeCommand());
        this.registerCommand(new WorldBorderCommand());

        // Match
        this.registerCommand(new JoinCommand());
        this.registerCommand(new LivesCommand());
        this.registerCommand(new QuitCommand());
        this.registerCommand(new ReadyCommand());
        this.registerCommand(new RankCommand());
        this.registerCommand(new ReRollCommand());
        this.registerCommand(new RenameCommand());
        this.registerCommand(new ShuffleCommand());
        this.registerCommand(new NoSpectatorsCommand());
        this.registerCommand(new SpectateCommand());
        this.registerCommand(new TeleportCommand());

        // Preferences
        this.registerCommand(new ColorCommand());
        this.registerCommand(new HotbarCommand());
        this.registerCommand(new SoundCommand());

        // Toggles
        this.registerCommand(new ToggleAutoJoinCommand());
        this.registerCommand(new ToggleAutoStartCommand());
        this.registerCommand(new ToggleGameModeCommand());
        this.registerCommand(new TogglesCommand());
        this.registerCommand(new ToggleSpectatorsCommand());
        this.registerCommand(new ToggleVotingCommand());
    }

    public void registerCommand(CustomCommand customCommand) {
        this.commandManager.command(customCommand.getCommandBuilder(this.commandManager).build());
    }

}
