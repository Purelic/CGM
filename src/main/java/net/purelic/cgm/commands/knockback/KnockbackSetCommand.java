package net.purelic.cgm.commands.knockback;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.listeners.modules.KnockbackModule;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KnockbackSetCommand implements CustomCommand {
    
    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("knockback", "kb")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .literal("set")
            .argument(DoubleArgument.of("ground horizontal multiplier"))
            .argument(DoubleArgument.of("ground vertical multiplier"))
            .argument(DoubleArgument.of("air horizontal multiplier"))
            .argument(DoubleArgument.of("air vertical multiplier"))
            .argument(DoubleArgument.of("sprint horizontal multiplier"))
            .argument(DoubleArgument.of("sprint vertical multiplier"))
            .argument(DoubleArgument.of("sprint yaw factor"))
            .handler(c -> {
                Player player = (Player) c.getSender();

                KnockbackModule module = KnockbackModule.get();
                module.setGroundHorizontal(c.get("ground horizontal multiplier"));
                module.setGroundVertical(c.get("ground vertical multiplier"));
                module.setAirHorizontal(c.get("air horizontal multiplier"));
                module.setAirVertical(c.get("air vertical multiplier"));
                module.setSprintHorizontal(c.get("sprint horizontal multiplier"));
                module.setSprintVertical(c.get("sprint vertical multiplier"));
                module.setSprintYawFactor(c.get("sprint yaw factor"));

                CommandUtils.sendSuccessMessage(player, "Custom knockback values have been set!");
            });
    }

}
