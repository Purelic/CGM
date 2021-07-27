package net.purelic.cgm.listeners.modules.bedwars;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.ArmorPiece;
import net.purelic.cgm.core.gamemodes.constants.ArmorType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.maps.shop.ShopItem;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemDowngradeModule implements Listener {

    private final Map<UUID, Map<ItemStack, Integer>> items = new HashMap<>();
    private final Map<UUID, ArmorType> armor = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onParticipantDeath(ParticipantDeathEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;
        Participant participant = event.getParticipant();
        this.downgradeItems(participant);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchQuit(MatchQuitEvent event) {
        if (!EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) return;
        Participant participant = event.getParticipant();
        this.downgradeItems(participant);
    }

    private void downgradeItems(Participant participant) {
        Player player = participant.getPlayer();
        UUID uuid = player.getUniqueId();
        Inventory inventory = participant.getPlayer().getInventory();

        this.items.put(uuid, new HashMap<>());

        for (int i = 0; i < inventory.getSize() - 1; i++) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack == null) continue;

            ItemCrafter ic = new ItemCrafter(itemStack);

            if (ic.hasTag("child_id")) {
                ShopItem child = ShopManager.getShopItem(ic.getTag("child_id"));
                if (child != null) this.items.get(uuid).put(child.getItemStack(), i);
            } else if (ic.hasTag("parent_id")) {
                if (itemStack.getType() != Material.STONE_SWORD) this.items.get(uuid).put(itemStack, i);
            }
        }

        ArmorType type = ArmorType.getCurrentArmorType(player, ArmorPiece.BOOTS);
        if (type != ArmorType.LEATHER) this.armor.put(uuid, type);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onParticipantRespawn(ParticipantRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (this.items.containsKey(uuid)) {
            Inventory inventory = player.getInventory();

            this.items.get(uuid).forEach((item, slot) -> {
                if (inventory.getItem(slot) != null
                    && inventory.getItem(slot).getType().name().contains("_SWORD")
                    && item.getType().name().contains("_SWORD")) {
                    inventory.setItem(slot, item);
                } else if (inventory.getItem(slot) == null) {
                    inventory.setItem(slot, item);
                } else {
                    inventory.addItem(item);
                }
            });

            this.items.remove(uuid);
        }

        if (this.armor.containsKey(uuid)) {
            ArmorType type = this.armor.get(uuid);
            type = type == ArmorType.GOLD ? ArmorType.CHAINMAIL : type;

            PlayerInventory inventory = player.getInventory();
            ItemStack[] currentArmor = inventory.getArmorContents();

            ItemStack[] newArmor = new ItemStack[]{
                new ItemCrafter(type.getBoots()).setTag("locked", "true").setUnbreakable().craft(),
                new ItemCrafter(type.getLeggings()).setTag("locked", "true").setUnbreakable().craft(),
                currentArmor[2], // chestplate
                currentArmor[3], // helmet
            };

            inventory.setArmorContents(newArmor);
            player.updateInventory();

            this.armor.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchStart(MatchStartEvent event) {
        this.items.clear();
        this.armor.clear();
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        this.items.clear();
        this.armor.clear();
    }

}
