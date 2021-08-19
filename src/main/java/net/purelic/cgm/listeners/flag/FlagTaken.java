package net.purelic.cgm.listeners.flag;

import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.FlagItem;
import net.purelic.cgm.core.maps.flag.events.FlagTakenEvent;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.preferences.FlagPattern;
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

        Profile profile = Commons.getProfile(player);
        String colorPref = (String) profile.getPreference(Preference.FLAG_PATTERN, FlagPattern.BLANK.name());
        FlagPattern flagPattern;

        if (FlagPattern.contains(colorPref)) {
            flagPattern = FlagPattern.valueOf(colorPref.toUpperCase());
        } else {
            flagPattern = FlagPattern.BLANK;
        }

        FlagItem flagItem = new FlagItem(flag, flagPattern);
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

                // only show the drop flag title if flag collection is turned on
                if (inGoal && NumberSetting.FLAG_COLLECTION_INTERVAL.value() > 0) {
                    ChatUtils.sendTitle(player, "", ChatColor.BOLD + "REMOVE HELMET TO DROP FLAG");
                } else {
                    ChatUtils.sendActionBar(player, "Carrying " + flag.getTitle().trim());
                }
            }
        }.runTaskTimerAsynchronously(CGM.get(), 0L, 20L);
    }

}
