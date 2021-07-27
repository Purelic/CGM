package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.shop.ShopItem;
import net.purelic.cgm.core.maps.shop.events.ShopItemPurchaseEvent;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ShopModule implements Listener {

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            ShopManager.loadBedWarsShop();
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (!this.isShopEntity(entity) || !MatchState.isState(MatchState.STARTED)) return;

        Player player = event.getPlayer();
        Participant participant = MatchManager.getParticipant(player);

        if (!MatchManager.isPlaying(player) || participant.isDead() || participant.isEliminated()) return;

        event.setCancelled(true);
        ShopManager.openShop(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.isShopEntity(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item == null) return;

        ItemCrafter ic = new ItemCrafter(item);

        if (!ic.hasTag("shop_item")) return;

        String id = ic.getTag("shop_item");
        ShopItem shopItem = ShopManager.getShopItem(id);
        event.setCancelled(true);

        if (shopItem == null) return;

        Player player = (Player) event.getWhoClicked();
        int price = shopItem.getPrice();
        int amount = this.getAmount(player, shopItem);

        if (amount < price) {
            int difference = price - amount;
            String currency = shopItem.getCurrency().name().replaceAll("_", " ").toLowerCase();
            currency += difference == 1 ? "" : "s";
            SoundUtils.SFX.SHOP_PURCHASE_FAILED.play(player);
            player.sendMessage(ChatColor.RED + "You need " + difference + " more " + currency + " to buy this!");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            CommandUtils.sendErrorMessage(player, "Your inventory is currently full!");
            return;
        }

        if (shopItem.isLimited() && shopItem.hasItem(player)) {
            CommandUtils.sendErrorMessage(player, "You've already purchased this " + (shopItem.isUpgrade() ? "upgrade" : "item") + "!");
            return;
        }

        if (!shopItem.hasParent() && shopItem.hasChild() && shopItem.hasItem(player)) {
            CommandUtils.sendErrorMessage(player, "You've already purchased the max tier!");
            return;
        }

        Commons.callEvent(new ShopItemPurchaseEvent(player, shopItem));
    }

    private boolean isShopEntity(Entity entity) {
        return EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && entity instanceof Villager;
    }

    private int getAmount(Player player, ShopItem shopItem) {
        return this.getAmount(player, shopItem.getCurrency());
    }

    private int getAmount(Player player, Material material) {
        int amount = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) amount += item.getAmount();
        }

        return amount;
    }

}
