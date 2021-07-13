package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.runnables.ChunkLoader;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CycleCommand implements CustomCommand {

    private final MatchManager matchManager;

    public CycleCommand() {
        this.matchManager = CGM.get().getMatchManager();
    }

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("cycle")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (ChunkLoader.isActive()) {
                    CommandUtils.sendErrorMessage(player, "You can't cycle while UHC maps are being pre-generated!");
                    return;
                }

                if (TaskUtils.isRunning(CycleCountdown.getCountdown())) {
                    CycleCountdown.setCountdown(0);
                } else {
                    if (MatchState.isState(MatchState.WAITING, MatchState.VOTING)
                        && MatchManager.getNextMap() == null) {
                        CommandUtils.sendErrorMessage(player, "There's no match to cycle to! Use /setnext");
                    } else if (MatchState.isState(MatchState.STARTED)) {
                        MatchState.setState(MatchState.ENDED, true);
                        CycleCountdown.getCountdown().cancel();
                        MatchManager.cycle();
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" force ended the match and cycled")
                        );
                    } else if (MatchManager.getNextMap() != null && MatchManager.getNextMap().getNextWorld() == null) {
                        CommandUtils.sendErrorMessage(player, "The next map hasn't fully loaded yet! Please try again in a moment.");
                        return;
                    } else {
                        MatchManager.cycle();
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" forced the match cycle")
                        );
                    }
                }

                ChatUtils.broadcastActionBar("");
            });
    }

}
