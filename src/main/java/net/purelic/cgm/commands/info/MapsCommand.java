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
import net.purelic.cgm.core.managers.MapManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.utils.BookGUI;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.MapUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MapsCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("maps")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(StringArgument.optional("player"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                Optional<String> playerArg = c.getOptional("player");

                if (!playerArg.isPresent()) {
                    MapsCommand.openMapsBook(player, "/map %MAP%");
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
                        String command = "/download map " + playerName + " ";

                        List<BaseComponent[]> entries = new ArrayList<>();
                        Set<String> maps = MapUtils.listPublishedMaps(uuid); // these might be alphabetical already

                        for (String map : maps) {
                            boolean canDownload = MapManager.getMapByExactName(map) == null;

                            entries.add(new ComponentBuilder(map.length() >= 20 ? map.substring(0, 15) + ".." : map)
                                .color(canDownload ? ChatColor.BLACK : ChatColor.GRAY)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + map))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    canDownload
                                        ? new ComponentBuilder("Click to Download")
                                        .append("\n" + ChatColor.GRAY + ChatColor.ITALIC + command + map)
                                        .create()
                                        : new ComponentBuilder("Can't download - conflicting map name!").color(ChatColor.GRAY).create()
                                )).create()
                            );
                        }

                        BookGUI.openPaginatedBook(player, "Maps by " + playerName + ":", entries);
                    }
                }.runTaskAsynchronously(CGM.getPlugin());
            });
    }

    public static void openMapsBook(Player player, String command) {
        List<BaseComponent[]> entries = new ArrayList<>();
        Map<String, CustomMap> maps = MapManager.getMaps();

        for (Map.Entry<String, CustomMap> entry : maps.entrySet()) {
            CustomMap map = entry.getValue();
            String name = map.getName();
            String author = Fetcher.getNameOf(map.getYaml().getAuthors().get(0));
            Set<CustomGameMode> gameModes = MapManager.getGameModes(map);
            String cmd = command.replaceAll("%MAP%", name);

            entries.add(new ComponentBuilder(name.length() >= 20 ? name.substring(0, 15) + "..." : name)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(map.getColoredName() + ChatColor.GRAY + " by " + MapsCommand.getAuthorComponent(author))
                        .append("\nSupports " + ChatColor.AQUA + gameModes.size() + ChatColor.RESET + " Game Modes")
                        .append("\n\n" + ChatColor.GRAY + ChatColor.ITALIC + cmd)
                        .create())).create());
        }

        BookGUI.openPaginatedBook(player, "Select a map:", entries);
    }

    public static String getAuthorComponent(String name) {
        Player author = Bukkit.getPlayer(name);
        if (author != null) return Commons.getProfile(author).getFlairs() + NickUtils.getDisplayName(author);
        else return ChatColor.DARK_AQUA + name;
    }

}
