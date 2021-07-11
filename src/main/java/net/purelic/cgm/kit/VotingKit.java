package net.purelic.cgm.kit;

import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.voting.VotingManager;
import net.purelic.cgm.voting.VotingOption;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VotingKit implements Kit {

    private final VotingManager votingManager;
    private final String nbtTag;

    public VotingKit(VotingManager votingManager, String nbtTag) {
        this.votingManager = votingManager;
        this.nbtTag = nbtTag;
    }

    @Override
    public void apply(Player player) {
        int start = 4 - (this.votingManager.getSettings().getVotingOptions() / 2);
        int slot = start; // starting slot index (centers items in hotbar)

        for (VotingOption option : this.votingManager.getSelected()) {
            // if random option is enabled and it's the last voting option
            boolean random = this.votingManager.getSettings().hasRandomOption() && slot == (start + this.votingManager.getSelected().size() - 1);
            player.getInventory().setItem(slot++, this.getVotingItem(player, option, random));
        }
    }

    private ItemStack getVotingItem(Player player, VotingOption option, boolean random) {
        boolean uhc = option.getGameMode().getGameType() == GameType.UHC;
        String name = option.getGameMode().getColoredName() + (uhc ? "" : " on " + option.getMap().getColoredName());
        if (random) name = ChatColor.YELLOW + "Random";
        Material material = option.voted(player) ? this.votingManager.getSettings().getVotedItem() : this.votingManager.getSettings().getVoteItem();
        return new ItemCrafter(material).name(name).setTag(this.nbtTag, option.getId()).craft();
    }

    public void remove(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (new ItemCrafter(item).hasTag(this.nbtTag)) {
                player.getInventory().remove(item);
            }
        }
    }

}
