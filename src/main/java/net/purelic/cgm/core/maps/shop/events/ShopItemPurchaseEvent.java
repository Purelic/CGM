package net.purelic.cgm.core.maps.shop.events;

import net.purelic.cgm.core.maps.shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopItemPurchaseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ShopItem item;

    public ShopItemPurchaseEvent(Player player, ShopItem item) {
        this.player = player;
        this.item = item;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ShopItem getItem() {
        return this.item;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
