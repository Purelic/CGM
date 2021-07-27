package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleFriendlyFireCommand implements CustomCommand {

    public static boolean friendlyFire = false;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggle")
            .literal("friendly_fire", "ff")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .handler(c -> {
                friendlyFire = !friendlyFire;
                ScoreboardManager.setFriendlyFire(ToggleSetting.FRIENDLY_FIRE.isEnabled() || ToggleFriendlyFireCommand.friendlyFire);
                CommandUtils.broadcastAlertMessage("Friendly Fire is now " + TogglesCommand.getStatus(friendlyFire));
            });
    }

}
