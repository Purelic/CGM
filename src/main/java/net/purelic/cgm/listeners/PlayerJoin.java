package net.purelic.cgm.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.toggles.ToggleVotingCommand;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.LeagueManager;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.managers.VoteManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.ServerUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

//        if (CGM.isPrivate() && !CGM.hasOwner()) {
//            CGM.setOwner(player);
//        }

        CGM.getPlugin().getScoreboardManager().setScoreboard(player);
        MatchTeam.OBS.addPlayer(player, true);

        if (MatchState.isState(MatchState.VOTING)) {
            VoteManager voteManager = CGM.getPlugin().getVoteManager();

            if (TaskUtils.isRunning(voteManager.getVotingCountdown())) {
                VoteManager.getVotingItems(player);
            }
        } else if (MatchState.isState(MatchState.WAITING)
                && Bukkit.getOnlinePlayers().size() == VoteManager.getMinPlayers()
                && !VoteManager.isCanceled()
                && !ServerUtils.isRanked()
                && ToggleVotingCommand.voting) {
            MatchState.setState(MatchState.VOTING);
        }

        if (ServerUtils.isRanked()) {
            MatchTeam team = LeagueManager.getTeam(player);
            if (team != null) player.performCommand("join " + team.name());
        }

        if (MatchState.isActive()) {
            player.performCommand("match");
        }

        // TODO replace with "isMatchJoinable" that also takes into consideration if teams are full too
        if (MatchTeam.getTeam(player) == MatchTeam.OBS && MatchState.isState(MatchState.STARTED) && !ServerUtils.isRanked()) {
            player.sendTitle(new Title(
                new TextComponent(ChatColor.GOLD + MatchManager.getCurrentGameMode().getName()),
                new TextComponent(ChatColor.AQUA + "/join" + ChatColor.WHITE + " to play!"),
                5,
                60,
                5
            ));
        }

        for (Participant participant : MatchManager.getParticipants()) {
            if (participant.isDead()) {
                Player dead = participant.getPlayer();
                player.hidePlayer(dead);
            }
        }

        TabManager.enable(player);

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
        }.runTaskLater(CGM.getPlugin(), 1L);
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
