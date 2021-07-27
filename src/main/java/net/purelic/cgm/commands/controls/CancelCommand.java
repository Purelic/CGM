package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.uhc.runnables.ChunkLoader;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("cancel")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (ChunkLoader.isActive()) {
                    CommandUtils.sendErrorMessage(player, "You can't cancel UHC maps being pre-generated! Please wait until it's finished.");
                    return;
                }

                if (MatchState.isState(MatchState.VOTING)) {
                    if (this.isCycleCountdownRunning()) {
                        CycleCountdown.getCountdown().cancel();
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" canceled the match cycle")
                        );
                    } else {
                        CommandUtils.broadcastAlertMessage(
                            Fetcher.getFancyName(player),
                            new TextComponent(" canceled the voting countdown")
                        );
                    }

                    CGM.getVotingManager().setCanceled(true);
                    MatchState.setState(MatchState.WAITING);
                } else if (this.isCycleCountdownRunning()) {
                    CycleCountdown.getCountdown().cancel();
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" canceled the match cycle")
                    );
                    PlayerUtils.setLevelAll(0);
                } else if (MatchState.isState(MatchState.STARTING)) {
                    MatchState.setState(MatchState.PRE_GAME);
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" canceled the match countdown")
                    );
                    PlayerUtils.setLevelAll(0);
                } else {
                    CommandUtils.sendErrorMessage(player, "There are no countdowns to cancel right now!");
                    return;
                }

                ChatUtils.broadcastActionBar("");
            });
    }

    private boolean isCycleCountdownRunning() {
        return CycleCountdown.getCountdown() != null
            && Bukkit.getScheduler().isQueued(CycleCountdown.getCountdown().getTaskId());
    }

}
