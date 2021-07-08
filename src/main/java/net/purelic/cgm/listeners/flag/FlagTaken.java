package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.FlagItem;
import net.purelic.cgm.core.maps.flag.constants.FlagPattern;
import net.purelic.cgm.core.maps.flag.events.FlagTakenEvent;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagTaken implements Listener {

    @EventHandler
    public void onFlagTaken(FlagTakenEvent event) {
        event.broadcast();

        Flag flag = event.getFlag();
        flag.resetBase();

        Participant participant = flag.getCarrier();
        Player player = participant.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);
        FlagItem flagItem = new FlagItem(flag, FlagPattern.random());
        player.getInventory().setHelmet(flagItem.getItemStack());

        if (team == MatchTeam.SOLO) SoundUtils.SFX.FLAG_TAKEN.play(player);
        else SoundUtils.SFX.FLAG_TAKEN.play(team);

        if (!flag.isNeutral()) SoundUtils.SFX.FLAG_STOLEN.play(flag.getOwner());

        ChatUtils.sendTitle(player, "", "Carrying " + flag.getTitle().trim());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (flag.getCarrier() != participant) {
                    this.cancel();
                    return;
                }

                Hill hill = FlagUtils.getHill(flag);
                boolean inGoal = hill != null && hill.getControlledBy() == team;

                if (inGoal) {
                    ChatUtils.sendTitle(player, "", ChatColor.BOLD + "REMOVE HELMET TO DROP FLAG");
                } else {
                    ChatUtils.sendActionBar(player, "Carrying " + flag.getTitle().trim());
                }
            }
        }.runTaskTimerAsynchronously(CGM.get(), 0L, 20L);
    }

}
