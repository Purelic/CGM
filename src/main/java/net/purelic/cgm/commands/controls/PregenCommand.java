package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PregenCommand implements CustomCommand {

    enum WorldType {

        NORMAL(org.bukkit.WorldType.NORMAL),
        AMPLIFIED(org.bukkit.WorldType.AMPLIFIED),
        LARGE_BIOMES(org.bukkit.WorldType.LARGE_BIOMES),
        ;

        private final org.bukkit.WorldType type;

        WorldType(org.bukkit.WorldType type) {
            this.type = type;
        }

        public org.bukkit.WorldType bukkit() {
            return this.type;
        }

    }

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("pregen")
            .senderType(Player.class)
            .permission(Permission.isMapDev())
            .argument(EnumArgument.optional(WorldType.class, "type", WorldType.NORMAL))
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (!MatchState.isState(MatchState.WAITING)) {
                    CommandUtils.sendErrorMessage(player, "You can't pregen a world right now!");
                    return;
                }

                WorldType worldType = c.get("type");
                MatchManager.startPregen(worldType.bukkit());
            });
    }

}
