package net.purelic.cgm.tab;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TabItem {

    private final int index;
    private final boolean legacy;
    private final String name;
    private final UUID uuid;
    private GameProfile profile;
    private EntityPlayer player;
    private String value;

    public TabItem(int index, boolean legacy) {
        this.index = index;
        this.legacy = legacy;
        this.name = TabUtils.getTabName(index, legacy);
        this.uuid = UUID.nameUUIDFromBytes(this.name.getBytes());
        this.profile = TabUtils.createProfile(this.uuid, this.name);
        this.player = TabUtils.createPlayer(this.profile, true);
        this.value = null;
    }

    public List<Packet<? extends PacketListenerPlayOut>> setPlayer(Player player, Player viewer) {
        if (this.legacy) return setPlayerName(NickUtils.getListName(player), viewer);

        // Show a crossed out name to staff if player is nicked
        String listName = Commons.getProfile(viewer).isStaff() && NickUtils.isNicked(player) ?
            this.strikeName(player) : NickUtils.getListName(player);

        if (listName.equals(this.value)) return new ArrayList<>();
        else this.value = listName;

        List<Packet<? extends PacketListenerPlayOut>> packets = new ArrayList<>();
        packets.add(this.getRemovePacket());

        this.profile = TabUtils.createProfile(this.uuid, this.name, player);
        this.player = TabUtils.createPlayer(this.profile, listName, false);

        // Teleport fake player far away
        this.player.setLocation(0, Integer.MAX_VALUE, 0, 0, 0);

        // Required to enable the "hat" layer of the skins in tab
        DataWatcher watcher = this.player.getDataWatcher();
        watcher.watch(10, (byte) 127);

        packets.add(this.getAddPacket()); // add entry to tab
        packets.add(new PacketPlayOutNamedEntitySpawn(this.player)); // spawn fake player
        packets.add(new PacketPlayOutEntityMetadata(this.player.getId(), watcher, true)); // enable skin parts on fake player
        packets.add(new PacketPlayOutEntityStatus(this.player, (byte) 3)); // kill the fake player

        return packets;
    }

    public List<Packet<? extends PacketListenerPlayOut>> setName(String name, TabSkin skin, Player viewer) {
        if (this.legacy) return this.setPlayerName(name, viewer);

        if (name.equals(this.value)) return new ArrayList<>();
        else this.value = name;

        List<Packet<? extends PacketListenerPlayOut>> packets = new ArrayList<>();
        packets.add(this.getRemovePacket());
        this.profile = TabUtils.createProfile(this.uuid, this.name, skin);
        this.player = TabUtils.createPlayer(this.profile, name, true);
        packets.add(this.getAddPacket());
        return packets;
    }

    private List<Packet<? extends PacketListenerPlayOut>> setPlayerName(String name, Player viewer) {
        return this.setPlayerName(name, null, viewer);
    }

    public List<Packet<? extends PacketListenerPlayOut>> setPlayerName(String name, Player player, Player viewer) {
        if (this.legacy) {
            ScoreboardManager.setEntry(TabUtils.getLegacyName(this.index), name);
            return new ArrayList<>();
        } else {
            // Show a crossed out name to staff if player is nicked
            String listName = player != null && Commons.getProfile(viewer).isMod() && NickUtils.isNicked(player) ?
                this.strikeName(player) : name;

            if (listName.equals(this.value)) return new ArrayList<>();
            else this.value = listName;

            this.player.listName = new ChatMessage(listName);

            return Collections.singletonList(this.getUpdatePacket());
        }
    }

    public PacketPlayOutPlayerInfo getAddPacket() {
        return this.getPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
    }

    public PacketPlayOutPlayerInfo getRemovePacket() {
        return this.getPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
    }

    public PacketPlayOutPlayerInfo getUpdatePacket() {
        return this.getPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
    }

    private PacketPlayOutPlayerInfo getPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action) {
        return new PacketPlayOutPlayerInfo(action, this.player);
    }

    private String strikeName(Player player) {
        return this.strikeName(NickUtils.getRealName(player), player.getPlayerListName());
    }

    private String strikeName(String realName, String newName) {
        return newName.replaceAll(realName, ChatColor.STRIKETHROUGH + realName);
    }

}
