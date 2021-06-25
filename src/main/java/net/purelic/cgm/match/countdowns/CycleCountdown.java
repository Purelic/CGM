package net.purelic.cgm.match.countdowns;

import net.purelic.cgm.CGM;

public class CycleCountdown extends Countdown {

    public CycleCountdown() {
        super(10, ""); // don't need to set a prefix since it's updated every tick
    }

    @Override
    public void tick() {
        // We update the prefix every tick in case the next match is changed mid-countdown
        if (CGM.getMatchManager2().hasNextMatch()) {
            this.setActionPrefix("Cycling to " + CGM.getMatchManager2().getNextMatch().getMatchTitle());
        } else {
            this.setActionPrefix("Returning to lobby");
        }
    }

    @Override
    public void complete() {
        CGM.getMatchManager2().cycle();
    }

}
