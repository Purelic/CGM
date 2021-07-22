package net.purelic.cgm.commands.uhc;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.uhc.UHCPreset;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHCScenarioPresetCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("scenario")
            .senderType(Player.class)
            .permission(Permission.isMod(true))
            .literal("preset")
            .argument(EnumArgument.of(UHCPreset.class, "preset"))
            .handler(c -> {
                Player player = (Player) c.getSender();
                UHCPreset preset = c.get("preset");

                if (!CGM.getPlaylist().isUHC()) {
                    CommandUtils.sendErrorMessage(player, "You can only use this command on UHC servers!");
                    return;
                }

                if (MatchState.isState(MatchState.STARTED)) {
                    CommandUtils.sendErrorMessage(player, "You can't apply UHC presets during a match!");
                    return;
                }

                preset.apply();
                UHCCommand.openMenu(player);
                CommandUtils.broadcastAlertMessage(ChatColor.AQUA + preset.getName() + ChatColor.WHITE +
                    " scenario preset has been applied");
            });
    }

}
