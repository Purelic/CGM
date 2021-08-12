package net.purelic.cgm.commands.knockback;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.listeners.modules.KnockbackModule;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackDebugSprintCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("knockback", "kb")
            .senderType(Player.class)
            .permission(Permission.isMapDev(true))
            .literal("debug")
            .literal("sprint")
            .handler(c -> {
                Player player = (Player) c.getSender();
                KnockbackModule module = KnockbackModule.get();
                player.setVelocity(new Vector(
                    1.0D * module.getGroundHorizontal() * module.getSprintHorizontal(),
                    1.0D * module.getGroundVertical() * module.getSprintVertical(),
                    0.0D
                ));
            });
    }

}
