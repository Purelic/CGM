package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RandomDropsScenario implements Module {

    private final Random random = new Random();
    private final Map<Material, ItemStack> drops = new HashMap<>();
    private List<Material> items;
    private BukkitRunnable resetRunnable;

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        this.resetDrops();

        this.resetRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!MatchState.isState(MatchState.STARTED)) {
                    this.cancel();
                } else {
                    resetDrops();
                }
            }
        };

        TaskUtils.runTimerAsync(this.resetRunnable, 6000L); // every 5 minutes
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        TaskUtils.cancelIfRunning(this.resetRunnable);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (UHCScenario.FLOWER_POWER.isEnabled()
            && ((FlowerPowerScenario) UHCScenario.FLOWER_POWER.getModule()).isFlower(block.getType())) {
            return;
        }

        // Always drop logs and cobblestone as normal
        if (type.name().contains("LOG") || type == Material.STONE) {
            return;
        }

        ItemStack drop;

        if (this.drops.containsKey(type)) {
            drop = this.drops.get(type);
        } else {
            int index = this.random.nextInt(this.items.size());
            Material material = this.items.get(index);

            drop = new ItemStack(material);
            this.drops.put(type, drop);

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

    private void resetDrops() {
        List<Material> items = new ArrayList<>(Arrays.asList(Material.values()));
        items.remove(Material.BEDROCK);
        items.remove(Material.STONE);
        items.remove(Material.COMMAND);
        items.remove(Material.COMMAND_MINECART);

        this.items = items;
        this.drops.clear();
    }

}
