//package net.purelic.cgm.commands.controls;
//
//import cloud.commandframework.Command;
//import cloud.commandframework.bukkit.BukkitCommandManager;
//import net.purelic.cgm.CGM;
//import net.purelic.commons.Commons;
//import net.purelic.commons.commands.parsers.CustomCommand;
//import org.bukkit.Bukkit;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//public class ShutdownCommand implements CustomCommand {
//
//    @Override
//    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
//        return mgr.commandBuilder("shutdown")
//            .senderType(Player.class)
//            .handler(c -> {
//                Player player = (Player) c.getSender();
//
//                if ((CGM.isPrivate() && CGM.isOwner(player)) || Commons.getProfile(player).isAdmin()) {
//                    Bukkit.shutdown();
//                }
//            });
//    }
//
//}
