package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.info.GameModesCommand;
import net.purelic.cgm.commands.info.MapsCommand;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.GameModeManager;
import net.purelic.cgm.core.managers.MapManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Optional;

public class SetNextCommand implements CustomCommand {

    private final MapManager mapManager;
    private final GameModeManager gameModeManager;
    private final MatchManager matchManager;

    public SetNextCommand() {
        this.mapManager = CGM.getPlugin().getMapManager();
        this.gameModeManager = CGM.getPlugin().getGameModeManager();
        this.matchManager = CGM.getPlugin().getMatchManager();
    }

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("setnext", "sn")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .argument(StringArgument.optional("map", StringArgument.StringMode.QUOTED))
            .argument(StringArgument.optional("game mode", StringArgument.StringMode.QUOTED))
            .handler(c -> {
                Player player = (Player) c.getSender();
                Optional<String> mapArg = c.getOptional("map");
                Optional<String> gameModeArg = c.getOptional("game mode");

                if (!mapArg.isPresent()) {
                    MapsCommand.openMapsBook(player, "/setnext \"%MAP%\"");
                } else if (!gameModeArg.isPresent()) {
                    CustomMap map = MapManager.getMapByName(mapArg.get());

                    if (map == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find map \"" + mapArg.get() + "\"!");
                        return;
                    }

                    GameModesCommand.openGameModesBook(player, "/setnext \"" + map.getName() + "\" \"%GM%\"", new ArrayList<>(MapManager.getRepo().get(map)));
                } else {
                    CustomMap map = MapManager.getMapByName(mapArg.get());
                    CustomGameMode gameMode = GameModeManager.getGameModeByNameOrAlias(gameModeArg.get());

                    if (map == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find map \"" + mapArg.get() + "\"!");
                        return;
                    }

                    if (gameMode == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find game mode \"" + gameModeArg.get() + "\"!");
                        return;
                    }

                    if (MapManager.getRepo().get(map).contains(gameMode)) {
                        if (MatchManager.getNextMap() != null && MatchManager.getNextMap().getWorld() == null) {
                            CommandUtils.sendErrorMessage(player, "Please wait and try again in a moment!");
                            return;
                        }

                        if (MatchManager.getNextMap() == map && this.matchManager.getNextGameMode() == gameMode) {
                            CommandUtils.sendErrorMessage(player, "The next match is already set to that!");
                            return;
                        }

                        MatchManager.setNext(map, gameMode);
                        CommandUtils.sendSuccessMessage(player, "You successfully set the next map! Use /cycle when you're ready to cycle to the map");
                    } else {
                        CommandUtils.sendErrorMessage(player, map.getName() + " does not support " + gameMode.getName() + "!");
                    }
                }
            });
    }

}