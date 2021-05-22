package net.purelic.cgm.commands.preferences;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.commands.parsers.CustomCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoundCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("sfx")
                .senderType(Player.class)
                .argument(EnumArgument.of(SoundUtils.SFX.class, "sound"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    SoundUtils.SFX sfx = c.get("sound");
                    sfx.play(player);
                });
    }

}
