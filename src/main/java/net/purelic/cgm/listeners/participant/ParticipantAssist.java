package net.purelic.cgm.listeners.participant;

import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.events.participant.ParticipantAssistEvent;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ParticipantAssist implements Listener {

    @EventHandler
    public void onParticipantAssist(ParticipantAssistEvent event) {
        KillAssist assist = event.getAssist();
        Player player = assist.getAttacker();
        Player killed = event.getKilled();
        int percent = assist.getPercentage();
        Participant participant = MatchManager.getParticipant(player);

        if (ToggleSetting.DYNAMIC_REGEN.isEnabled()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, percent / 2, 3));
        }

        if (assist.isKiller() || participant == null) return;

        participant.getStats().addAssist();

        new RewardBuilder(
                player,
                1,
                "Assist",
                "Assisted Killing " + NickUtils.getDisplayName(killed) + ChatColor.GRAY + " (" + percent + "%)")
                .reward(Sound.ORB_PICKUP);
    }

}
