package net.purelic.cgm.core.runnables;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.VoteManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class VotingCountdown extends BukkitRunnable {

    private static BukkitRunnable countdown;
    private static int seconds;
    private final boolean forced;

    public VotingCountdown(int seconds, boolean forced) {
        countdown = this;
        VotingCountdown.seconds = seconds;
        this.forced = forced;
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().size() <= (VoteManager.getMinPlayers() / 2) && !this.forced) {
            CommandUtils.broadcastErrorMessage("Vote canceled! Not enough players to continue.");
            ChatUtils.broadcastActionBar("");
            MatchState.setState(MatchState.WAITING);
            clearVotingItems();
            this.cancel();
            return;
        }

        if (seconds <= 0) {
            clearVotingItems();
            PlayerUtils.setLevelAll(0);

            Map<CustomMap, CustomGameMode> option = CGM.getPlugin().getVoteManager().getTopOption();
            CustomMap map = option.keySet().stream().findFirst().get();
            CustomGameMode gameMode = option.values().stream().findFirst().get();

            MatchManager.setNext(map, gameMode);

            new CycleCountdown(5, map, gameMode).runTaskTimer(CGM.getPlugin(), 0, 20);

            this.cancel();
            return;
        }

        String message = "Voting ends in " + ChatColor.AQUA + seconds +
            ChatColor.RESET + " second" + ((seconds == 1) ? "!" : "s!");
        ChatUtils.broadcastActionBar(message);
        SoundUtils.playCountdownNote(seconds);

        if (seconds % 5 == 0) {
            Bukkit.getOnlinePlayers().stream().filter(VersionUtils::isLegacy).forEach(player -> player.sendMessage(message));
        }

        PlayerUtils.setLevelAll(seconds);

        seconds--;
    }

    public static void clearVotingItems() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            for (ItemStack item : player.getInventory().getContents()) {
                if (new ItemCrafter(item).hasTag("map")) player.getInventory().remove(item);
            }
        });
    }

    public static BukkitRunnable getCountdown() {
        return countdown;
    }

    public static void setCountdown(int seconds) {
        VotingCountdown.seconds = seconds;
    }

}
