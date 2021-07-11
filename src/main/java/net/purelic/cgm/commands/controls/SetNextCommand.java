package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.info.GameModesCommand;
import net.purelic.cgm.commands.info.MapsCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
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

    private final MatchManager matchManager;

    public SetNextCommand() {
        this.matchManager = CGM.get().getMatchManager();
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
                    CustomMap map = CGM.getPlaylist().getMap(mapArg.get());

                    if (map == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find map \"" + mapArg.get() + "\"!");
                        return;
                    }

                    GameModesCommand.openGameModesBook(player, "/setnext \"" + map.getName() + "\" \"%GM%\"", new ArrayList<>(CGM.getPlaylist().getRepo().get(map)));
                } else {
                    CustomMap map = CGM.getPlaylist().getMap(mapArg.get());
                    CustomGameMode gameMode = CGM.getPlaylist().getGameMode(gameModeArg.get());

                    if (map == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find map \"" + mapArg.get() + "\"!");
                        return;
                    }

                    if (map.getName().equals("UHC") && !MatchState.isState(MatchState.WAITING)) {
                        String error = "You can only set UHC matches while waiting in the lobby!";

                        if (MatchState.isState(MatchState.VOTING)) {
                            error = "You can't set UHC matches right now! Please cancel the voting countdowns first, then try again.";
                        }

                        CommandUtils.sendErrorMessage(player, error);
                        return;
                    }

                    if (gameMode == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find game mode \"" + gameModeArg.get() + "\"!");
                        return;
                    }

                    if (CGM.getPlaylist().getRepo().get(map).contains(gameMode)) {
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
