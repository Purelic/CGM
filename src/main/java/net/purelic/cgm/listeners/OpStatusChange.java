//package net.purelic.cgm.listeners;
//
//import net.purelic.cgm.CGM;
//import net.purelic.cgm.core.managers.TabManager;
//import net.purelic.commons.events.OpStatusChangeEvent;
//import net.purelic.commons.utils.CommandUtils;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//
//public class OpStatusChange implements Listener {
//
//    @EventHandler
//    public void onOpStatusChangedEvent(OpStatusChangeEvent event) {
//        Player player = event.getPlayer();
//        boolean op = event.isOp();
//
//        if (CGM.isPrivate() && !op && CGM.isOwner(player) && event.getChangedBy() != null) {
//            CommandUtils.sendErrorMessage(event.getChangedBy(), "You cannot deop the server owner!");
//            event.setCancelled(true);
//        } else {
//            TabManager.updateTeam(player);
//        }
//    }
//
//}
