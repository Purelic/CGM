package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.analytics.GameModeDownloadedEvent;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.DatabaseUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import shaded.com.google.cloud.firestore.DocumentReference;
import shaded.com.google.cloud.firestore.FieldValue;
import shaded.com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.UUID;

public class DownloadGameModeCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("download")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .literal("gamemode")
            .argument(StringArgument.of("player"))
            .argument(StringArgument.greedy("game mode"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                String playerArg = c.get("player");
                String gameModeArg = c.get("game mode");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        UUID uuid = Fetcher.getUUIDOf(playerArg);

                        if (uuid == null) {
                            CommandUtils.sendNoPlayerMessage(player, playerArg);
                            return;
                        }

                        QueryDocumentSnapshot data = DatabaseUtils.getGameMode(uuid.toString(), gameModeArg, false);

                        if (data == null) {
                            data = DatabaseUtils.getGameMode(uuid.toString(), gameModeArg, true);

                            if (data == null) {
                                CommandUtils.sendErrorMessage(player, Fetcher.getNameOf(uuid) + " has not created a game mode named \"" + gameModeArg + "\"!");
                                return;
                            }
                        }

                        CustomGameMode gameMode = new CustomGameMode(data);

                        if (!gameMode.isPublic()) {
                            CommandUtils.sendErrorMessage(player, Fetcher.getNameOf(uuid) + " has not published this game mode!");
                            return;
                        }

                        CustomGameMode gameModeByName = CGM.getPlaylist().getGameModeByName(gameMode.getName());
                        CustomGameMode gameModeByAlias = CGM.getPlaylist().getGameModeByAlias(gameMode.getAlias());

                        if (gameModeByName != null) {
                            CommandUtils.sendErrorMessage(player, "There's already a game mode downloaded with that name!");
                            return;
                        }

                        if (gameModeByAlias != null) {
                            CommandUtils.sendErrorMessage(player, "There's already a game mode downloaded with that alias!");
                            return;
                        }

                        CGM.getPlaylist().loadGameMode(gameMode);
                        incrementGameModeDownloads(data.getId());
                        CommandUtils.sendSuccessMessage(player, "Successfully downloaded \"" + gameMode.getName() + "\" by " + Fetcher.getNameOf(uuid) + "!");
                        new GameModeDownloadedEvent(player, gameMode).track();
                    }
                }.runTaskAsynchronously(CGM.get());
            });
    }

    private void incrementGameModeDownloads(String docId) {
        DocumentReference docRef = DatabaseUtils.getFirestore().collection("game_modes").document(docId);
        docRef.update("downloads", FieldValue.increment(1));
    }

}
