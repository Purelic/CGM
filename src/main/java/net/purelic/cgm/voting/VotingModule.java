package net.purelic.cgm.voting;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class VotingModule implements Module {

    private final VotingManager votingManager;

    public VotingModule(VotingManager votingManager) {
        this.votingManager = votingManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // ignore events that aren't during voting match states
        if (!MatchState.isState(MatchState.VOTING)) return;

        // only look for right click actions with an item in hand
        if (!action.name().contains("RIGHT") || item == null) return;

        ItemCrafter itemCrafter = new ItemCrafter(item);

        // check if the item has voting item nbt data
        if (!itemCrafter.hasTag("voting_item")) return;

        // toggle the voting option
        this.votingManager.toggleVote(player, itemCrafter.getTag("voting_item"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (MatchState.isState(MatchState.VOTING)) {
            this.votingManager.getVotingItems(player);
        } else if (this.votingManager.shouldStartVoting()) {
            MatchState.setState(MatchState.VOTING);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // clear votes for player if offline voting is not allowed
        if (MatchState.isState(MatchState.VOTING) && !this.votingManager.getSettings().allowOfflineVotes()) {
            this.votingManager.clearVotes(player);
        }
    }

}
