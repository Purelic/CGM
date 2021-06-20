package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.analytics.MapDownloadedEvent;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.MapUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DownloadMapCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("download")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .literal("map")
            .argument(StringArgument.of("player"))
            .argument(StringArgument.greedy("map"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                String playerArg = c.get("player");
                String mapName = c.get("map");

                if (CGM.getPlaylist().getMapByName(mapName) != null) {
                    CommandUtils.sendErrorMessage(player, "There's already a map downloaded with that name!");
                    return;
                }

                // TODO prev started async runnable here

                UUID uuid = Fetcher.getUUIDOf(playerArg);

                if (uuid == null) {
                    CommandUtils.sendNoPlayerMessage(player, playerArg);
                    return;
                }

                String downloaded = MapUtils.downloadPublishedMap(uuid, mapName);

                if (downloaded == null) {
                    CommandUtils.sendErrorMessage(player, Fetcher.getNameOf(uuid) + " has not published a map named \"" + mapName + "\"!");
                    return;
                }

                MapYaml yaml = new MapYaml(MapUtils.getMapYaml(downloaded));
                CustomMap map = new CustomMap(downloaded, yaml);
                CGM.getPlaylist().loadMap(map);

                CommandUtils.sendSuccessMessage(player, "Successfully downloaded \"" + downloaded + "\" by " + Fetcher.getNameOf(uuid) + "!");
                new MapDownloadedEvent(player, map).track();
            });
    }

}
