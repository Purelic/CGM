package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.commons.modules.Module;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RandomDropsScenario implements Module {

    private final Random random = new Random();
    private final Map<Material, ItemStack> drops = new HashMap<>();
    private List<Material> items;

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.items = new ArrayList<>(Arrays.asList(Material.values()));
        this.drops.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (UHCScenario.FLOWER_POWER.isEnabled()
            && ((FlowerPowerScenario) UHCScenario.FLOWER_POWER.getModule()).isFlower(block.getType())) {
            return;
        }

        ItemStack drop;

        if (this.drops.containsKey(block.getType())) {
            drop = this.drops.get(block.getType());
        } else {
            int index = this.random.nextInt(this.items.size());
            Material material = this.items.get(index);

            drop = new ItemStack(material);
            this.drops.put(block.getType(), drop);

            this.items.remove(material);
        }

        event.setCancelled(true);
        block.setType(Material.AIR);

        Location dropLocation = block.getLocation().add(.5, 0, .5);
        dropLocation.getWorld().dropItemNaturally(dropLocation, drop);

        Player player = event.getPlayer();
        ItemStack tool = player.getItemInHand();

        if (tool != null && tool.hasItemMeta() && tool.getDurability() > 1) {
            tool.setDurability((short) (tool.getDurability() - 1));
            player.setItemInHand(tool);
        }
    }

}
