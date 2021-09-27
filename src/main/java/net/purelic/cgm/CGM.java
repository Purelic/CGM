package net.purelic.cgm;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.purelic.cgm.commands.communication.GlobalCommand;
import net.purelic.cgm.commands.controls.*;
import net.purelic.cgm.commands.discord.LTPCommand;
import net.purelic.cgm.commands.info.*;
import net.purelic.cgm.commands.knockback.KnockbackDebugNormalCommand;
import net.purelic.cgm.commands.knockback.KnockbackDebugSprintCommand;
import net.purelic.cgm.commands.knockback.KnockbackSetCommand;
import net.purelic.cgm.commands.league.RankCommand;
import net.purelic.cgm.commands.league.ReRollCommand;
import net.purelic.cgm.commands.league.ReadyCommand;
import net.purelic.cgm.commands.match.*;
import net.purelic.cgm.commands.preferences.HotbarCommand;
import net.purelic.cgm.commands.preferences.SoundCommand;
import net.purelic.cgm.commands.toggles.*;
import net.purelic.cgm.commands.uhc.UHCCommand;
import net.purelic.cgm.commands.uhc.UHCScenarioPresetCommand;
import net.purelic.cgm.commands.uhc.UHCScenarioToggleCommand;
import net.purelic.cgm.core.managers.LootManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.hill.HillModule;
import net.purelic.cgm.core.maps.region.RegionModule;
import net.purelic.cgm.league.LeagueModule;
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
import net.purelic.cgm.uhc.UHCModule;
import net.purelic.cgm.voting.VotingManager;
import net.purelic.cgm.voting.VotingModule;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.runnables.MapDownloader;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class CGM extends JavaPlugin {

    private static CGM plugin;
    private static boolean ready;

    private Playlist playlist;

    private MatchManager matchManager;
    private ScoreboardManager scoreboardManager;
    private VotingManager votingManager;

    private PaperCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        plugin = this;
        ready = false;

        // download playlist
        this.playlist = new Playlist();

        // download lobby map
        TaskUtils.runAsync(new MapDownloader(Commons.getLobbyPreference(), true));

        // register managers, listeners, and commands
        this.registerManagers();
        this.registerModules();
        this.registerListeners();
        this.registerCommands();

        // load server/database info
        DatabaseUtils.loadServerDoc();

        this.setReady();
    }

    public static CGM get() {
        return plugin;
    }

    public static boolean isReady() {
        return ready;
    }

    private void setReady() {
        ready = true;

        if (ServerUtils.isRanked()) {
            LeagueModule leagueModule = new LeagueModule();
            leagueModule.loadListenerRegistration();
            Commons.registerListener(leagueModule);
        }

        System.out.println("The server is now ready!");
        Commons.setServerReady();

        // Cache all the author names
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
        Commons.registerListener(new HillModule());
        Commons.registerListener(new CaptureEffectModule());
        Commons.registerListener(new GearModule());
        Commons.registerListener(new RegionModule());
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
        this.registerListener(new MatchCycle());
        this.registerListener(new SpectatorJoin());

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
        this.registerListener(new StraightArrowModule());
        this.registerListener(new ArrowSpawnModule());
        this.registerListener(new ArrowTrailModule());
        this.registerListener(new AutoLapisModule());
        this.registerListener(new BlockProtectionModule());
        this.registerListener(new DamageTrackerModule());
        this.registerListener(new ExplosionModule());
        this.registerListener(new GappleSpawnModule());
        this.registerListener(new HeadModule());
        this.registerListener(new HotbarModule());
        this.registerListener(new InstantKillModule());
        this.registerListener(new JumpPadModule());
        new KnockbackModule().register();
        this.registerListener(new LootChestRefillModule());
        this.registerListener(new NoHungerModule());
        this.registerListener(new NoSleepingModule());
        this.registerListener(new RegenerationModule());
        this.registerListener(new RespawnModule());
        new RodDurabilityModule().register();
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
        this.registerListener(new EntityTarget());
        // this.registerListener(new OpStatusChange());
        this.registerListener(new PlayerChat());
        this.registerListener(new ItemLockModule());
        this.registerListener(new PlayerJoin());
        this.registerListener(new PlayerQuit());
        this.registerListener(new PlayerRankChange());
        this.registerListener(new ServerListPing());

        // Dynamic Modules
        new DynamicModuleModule().register();
        DynamicModuleModule.add(new CraftingRepairModule());
        DynamicModuleModule.add(new UHCModule());
        DynamicModuleModule.add(new GracePeriodModule());
        DynamicModuleModule.add(new CompassTrackingModule());
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void registerCommandManager() {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
            CommandExecutionCoordinator.simpleCoordinator();

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
        this.registerCommand(new LTPCommand(this.getConfig()));
        this.registerCommand(new PregenCommand());
        this.registerCommand(new RematchCommand());
        this.registerCommand(new SetNextCommand());
        this.registerCommand(new StartCommand());

        // Info
        this.registerCommand(new GameModeCommand());
        this.registerCommand(new GameModesCommand());
        this.registerCommand(new MapCommand());
        this.registerCommand(new MapsCommand());
        this.registerCommand(new MatchCommand());
        this.registerCommand(new RoundsCommand());
        this.registerCommand(new ScoreLimitCommand());
        this.registerCommand(new SeedCommand());
        this.registerCommand(new TimeCommand());
        this.registerCommand(new WorldBorderCommand());

        // Knockback
        Commons.registerCommand(new KnockbackDebugNormalCommand());
        Commons.registerCommand(new KnockbackDebugSprintCommand());
        Commons.registerCommand(new KnockbackSetCommand());

        // Match
        this.registerCommand(new ForceCommand());
        this.registerCommand(new JoinCommand());
        this.registerCommand(new LivesCommand());
        this.registerCommand(new QuitCommand());
        this.registerCommand(new ReadyCommand());
        this.registerCommand(new RankCommand());
        this.registerCommand(new ReRollCommand());
        this.registerCommand(new RenameCommand());
        this.registerCommand(new ShuffleCommand());
        this.registerCommand(new SpectateCommand());
        this.registerCommand(new TeleportCommand());

        // Preferences
        this.registerCommand(new HotbarCommand());
        this.registerCommand(new SoundCommand());

        // Toggles
        this.registerCommand(new ToggleAutoJoinCommand());
        this.registerCommand(new ToggleAutoStartCommand());
        this.registerCommand(new ToggleFriendlyFireCommand());
        this.registerCommand(new ToggleGameModeCommand());
        this.registerCommand(new ToggleJoinLockCommand());
        this.registerCommand(new ToggleKnockbackCommand());
        this.registerCommand(new TogglesCommand());
        this.registerCommand(new ToggleSpectatorsCommand());
        this.registerCommand(new ToggleVotingCommand(this.votingManager));

        // UHC
        this.registerCommand(new UHCCommand());
        this.registerCommand(new UHCScenarioPresetCommand());
        this.registerCommand(new UHCScenarioToggleCommand());
    }

    public void registerCommand(CustomCommand customCommand) {
        this.commandManager.command(customCommand.getCommandBuilder(this.commandManager).build());
    }

}
