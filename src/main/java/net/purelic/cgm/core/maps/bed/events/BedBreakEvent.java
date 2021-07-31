package net.purelic.cgm.core.maps.bed.events;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.Broadcastable;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class BedBreakEvent extends BedEvent implements Broadcastable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Participant participant;
    private final boolean suddenDeath;

    public BedBreakEvent(Bed bed, boolean suddenDeath) {
        super(bed);
        this.player = null;
        this.participant = null;
        this.suddenDeath = suddenDeath;
    }

    public BedBreakEvent(Bed bed, Player player) {
        super(bed);
        this.player = player;
        this.participant = MatchManager.getParticipant(player);
        this.suddenDeath = false;
    }

    public Bed getBed() {
        return this.bed;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public boolean isSuddenDeath() {
        return this.suddenDeath;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public BaseComponent[] getBroadcastMessage() {
        MatchTeam owner = this.bed.getOwner();

        if (this.suddenDeath) {
            return new ComponentBuilder("\n")
                .append(" BED DESTROYED » ")
                .color(owner.getColor())
                .bold(true)
                .append(owner.getName() + "'s")
                .color(owner.getColor())
                .append(" bed was destroyed!")
                .reset()
                .append(" (sudden death)")
                .color(ChatColor.GRAY)
                .append("\n")
                .reset()
                .create();
        } else if (this.player != null) {
            return new ComponentBuilder("\n")
                .append(" BED DESTROYED » ")
                .color(owner.getColor())
                .bold(true)
                .append(owner.getName() + "'s")
                .color(owner.getColor())
                .append(" bed was destroyed by ")
                .reset()
                .append(NickUtils.getDisplayName(player))
                .append("!\n")
                .reset()
                .create();
        } else {
            return new ComponentBuilder("\n")
                .append(" BED DESTROYED » ")
                .color(owner.getColor())
                .bold(true)
                .append(owner.getName() + "'s")
                .color(owner.getColor())
                .append(" bed was destroyed!")
                .reset()
                .append(" (team quit)")
                .color(ChatColor.GRAY)
                .append("\n")
                .reset()
                .create();
        }
    }

}
