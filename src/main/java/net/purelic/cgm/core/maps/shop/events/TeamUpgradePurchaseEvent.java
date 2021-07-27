package net.purelic.cgm.core.maps.shop.events;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamUpgradePurchaseEvent extends Event implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MatchTeam team;
    private final TeamUpgrade upgrade;

    public TeamUpgradePurchaseEvent(Player player, TeamUpgrade upgrade) {
        this.player = player;
        this.team = MatchTeam.getTeam(player);
        this.upgrade = upgrade;
    }

    public Player getPlayer() {
        return this.player;
    }

    public MatchTeam getTeam() {
        return this.team;
    }

    public TeamUpgrade getUpgrade() {
        return this.upgrade;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public SoundUtils.SFX getSFX() {
        return SoundUtils.SFX.TEAM_UPGRADE_PURCHASED;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        return new ComponentBuilder("")
            // .append(" TEAM UPGRADE » ").color(this.team.getColor()).bold(true)
            //.append("").reset()
            .append(NickUtils.getNick(this.player))
            .append(" purchased ").reset()
            .append(this.upgrade.getName()).color(ChatColor.AQUA)
            .append("!").reset()
            .create();
    }

}
