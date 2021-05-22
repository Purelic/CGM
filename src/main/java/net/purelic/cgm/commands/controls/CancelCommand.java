package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.VoteManager;
import net.purelic.cgm.core.runnables.CycleCountdown;
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
            // .flag(CommandFlag.newBuilder("forced").withAliases("f"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                // boolean forced = c.flags().isPresent("forced");
                if (CycleCountdown.getCountdown() != null
                    && Bukkit.getScheduler().isQueued(CycleCountdown.getCountdown().getTaskId())) {
                    CycleCountdown.getCountdown().cancel();
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" canceled the match cycle")
                    );
                    PlayerUtils.setLevelAll(0);
                } else if (MatchState.isState(MatchState.VOTING)) {
                    MatchState.setState(MatchState.WAITING);
                    CommandUtils.broadcastAlertMessage(
                        Fetcher.getFancyName(player),
                        new TextComponent(" canceled the voting countdown")
                    );
                    VoteManager.setCanceled(true);
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

}
