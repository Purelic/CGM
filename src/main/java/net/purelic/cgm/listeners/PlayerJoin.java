package net.purelic.cgm.listeners;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.tab.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

//        if (CGM.isPrivate() && !CGM.hasOwner()) {
//            CGM.setOwner(player);
//        }

        CGM.get().getScoreboardManager().setScoreboard(player);
        MatchTeam.OBS.addPlayer(player, true);

        if (ServerUtils.isRanked()) {
            MatchTeam team = LeagueManager.getTeam(player);
            if (team != null) player.performCommand("join " + team.name());
        }

        if (MatchState.isActive()) {
            player.performCommand("match");
        }

        // TODO replace with "isMatchJoinable" that also takes into consideration if teams are full too
        if (MatchTeam.getTeam(player) == MatchTeam.OBS && MatchState.isState(MatchState.STARTED) && !ServerUtils.isRanked()) {
            ChatUtils.sendTitle(
                player,
                ChatColor.GOLD + MatchManager.getCurrentGameMode().getName(),
                ChatColor.AQUA + "/join" + ChatColor.WHITE + " to play!",
                60
            );
        }

        for (Participant participant : MatchManager.getParticipants()) {
            if (participant.isDead()) {
                Player dead = participant.getPlayer();
                player.hidePlayer(dead);
            }
        }

        CGM.getTabManager().enable(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (MatchState.isState(MatchState.STARTED) && MatchTeam.getTeam(player) == MatchTeam.OBS) {
                    for (Participant participant : MatchManager.getParticipants()) {
                        Player online = participant.getPlayer();
                        online.hidePlayer(player);
                    }
                }

                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(!MatchState.isState(MatchState.WAITING, MatchState.VOTING));
            }
        }.runTaskLater(CGM.get(), 1L);
    }

    public static void getItemKit(Player player) {
        player.getInventory().addItem(getSetNextItem(), getTogglesItem());
    }

    private static ItemStack getSetNextItem() {
        return new ItemCrafter(Material.ENCHANTED_BOOK)
            .name("" + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.BOLD + "Set Match" + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.GRAY + " (/setnext)")
            .addTag("setnext")
            .craft();
    }

    private static ItemStack getTogglesItem() {
        return new ItemCrafter(Material.PAPER)
            .name("" + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.BOLD + "Toggles" + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.GRAY + " (/toggles)")
            .addTag("toggles")
            .craft();
    }

}
