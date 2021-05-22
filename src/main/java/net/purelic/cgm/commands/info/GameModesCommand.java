package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.GameModeManager;
import net.purelic.cgm.utils.BookGUI;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.Fetcher;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.*;

public class GameModesCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("gamemodes", "gms")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(StringArgument.optional("player"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                Optional<String> playerArg = c.getOptional("player");

                if (!playerArg.isPresent()) {
                    openGameModesBook(player, "/gm %GM%");
                    return;
                }

                // TODO look into the chained commands example to allow for two different commands
                // with different permissions
//                if (!CommandUtils.hasPermission(player, PermissionType.MAP_DEV, true)) {
//                    return;
//                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // start async runnable
                        String playerName = playerArg.get();

                        UUID uuid = Fetcher.getUUIDOf(playerName);

                        if (uuid == null) {
                            CommandUtils.sendNoPlayerMessage(player, playerName);
                            return;
                        }

                        playerName = Fetcher.getNameOf(uuid);
                        String command = "/download gamemode " + playerName + " ";

                        List<BaseComponent[]> entries = new ArrayList<>();
                        List<QueryDocumentSnapshot> gameModes = DatabaseUtils.getGameModes(uuid); // these might be alphabetical already

                        for (QueryDocumentSnapshot documentSnapshot : gameModes) {
                            CustomGameMode gameMode = new CustomGameMode(documentSnapshot);

                            if (!gameMode.isPublic()) continue;

                            String name = gameMode.getName();
                            CustomGameMode gameModeByName = GameModeManager.getGameModeByExactName(name);
                            CustomGameMode gameModeByAlias = GameModeManager.getGameModeByAlias(gameMode.getAlias());

                            boolean canDownload = gameModeByName == null && gameModeByAlias == null;

                            entries.add(new ComponentBuilder(name.length() >= 20 ? name.substring(0, 15) + ".." : name)
                                .color(canDownload ? ChatColor.BLACK : ChatColor.GRAY)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + name))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    canDownload
                                        ? new ComponentBuilder("Click to Download")
                                        .append("\n" + ChatColor.GRAY + ChatColor.ITALIC + command + name)
                                        .create()
                                        : new ComponentBuilder("Can't download - conflicting game mode name!").color(ChatColor.GRAY).create()
                                )).create()
                            );
                        }

                        BookGUI.openPaginatedBook(player, "Game modes by " + playerName + ":", entries);
                    }
                }.runTaskAsynchronously(CGM.getPlugin());
            });
    }

    public static void openGameModesBook(Player player, String command) {
        Collection<CustomGameMode> gameModes = GameModeManager.getGameModes();
        GameModesCommand.openGameModesBook(player, command, new ArrayList<>(gameModes));
    }

    public static void openGameModesBook(Player player, String command, List<CustomGameMode> gameModes) {
        List<BaseComponent[]> entries = new ArrayList<>();

        gameModes.sort(Comparator.comparing(CustomGameMode::getName));

        for (CustomGameMode gameMode : gameModes) {
            String name = gameMode.getName();
            String author = Fetcher.getNameOf(gameMode.getAuthor());
            TeamType teamType = TeamType.valueOf(gameMode.getEnumSetting(EnumSetting.TEAM_TYPE));
            String teamSizes = teamType == TeamType.SOLO ? "" : " (" + teamType.getTeams().size() + " teams of " + MatchUtils.getMaxTeamPlayers(gameMode) + ")";
            String cmd = command.replaceAll("%GM%", name);
            String description = WordUtils.wrap(gameMode.getDescription(), 30, "\n", true);

            entries.add(new ComponentBuilder(name.length() >= 20 ? name.substring(0, 15) + "..." : name)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(gameMode.getColoredNameWithAlias() + ChatColor.GRAY + " by " + MapsCommand.getAuthorComponent(author))
                        .append("\n" + description)
                        .append("\n\nGame Type: " + ChatColor.AQUA + gameMode.getGameType().getName())
                        .append("\nTeam Type: " + ChatColor.AQUA + teamType.getName() + ChatColor.GRAY + teamSizes)
                        .append("\n\n" + ChatColor.GRAY + ChatColor.ITALIC + cmd)
                        .create())).create());
        }

        BookGUI.openPaginatedBook(player, "Select a game mode:", entries);
    }

}
