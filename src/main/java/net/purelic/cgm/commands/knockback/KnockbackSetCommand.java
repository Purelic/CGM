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
            .argument(DoubleArgument.<CommandSender>newBuilder("ground horizontal multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("ground vertical multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("air horizontal multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("air vertical multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("sprint horizontal multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("sprint vertical multiplier").withMin(0).withMax(2))
            .argument(DoubleArgument.<CommandSender>newBuilder("sprint yaw factor").withMin(0).withMax(1))
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
