package net.purelic.cgm.core.gamemodes.constants;

import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public enum DropType {

    ALL, NONE, HALF;

    public void dropItems(Player player, Material material, int amount) {
        if (this == NONE || amount == 0) return;
        int total = this == ALL ? amount : amount == 1 ? 1 : amount / 2;
        player.getWorld().dropItemNaturally(player.getLocation(), (new ItemCrafter(material)).amount(total).craft());
    }

}
