package net.purelic.cgm.core.match;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;

public class Round {

    private boolean complete;
    private MatchTeam wonBy;

    public Round() {
        this.complete = false;
        this.wonBy = null;
    }

    public void setComplete(MatchTeam wonBy) {
        this.complete = true;
        this.wonBy = wonBy;
    }

    private ChatColor getColor() {
        if (complete) {
            if (this.wonBy == null) {
                return ChatColor.WHITE;
            } else {
                return this.wonBy.getColor();
            }
        } else {
            return ChatColor.WHITE;
        }
    }

    @Override
    public String toString() {
        return this.getColor() + (this.complete && wonBy == null ? "⬜" : "⬛");
    }

}
