package net.purelic.cgm.listeners.participant;

import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.events.participant.ParticipantAssistEvent;
import net.purelic.cgm.utils.MatchUtils;
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
            int duration = percent / 2;

            // Check if the player already has regen and combine the effect durations
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType() == PotionEffectType.REGENERATION) {
                    // For now, we only combine the durations if it's of the same amplifier
                    if (effect.getAmplifier() == 3) duration += effect.getDuration();
                    break;
                }
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 3));
        }

        if (assist.isKiller() || participant == null) return;

        int assistPoints = NumberSetting.DEATHMATCH_ASSIST_POINTS.value();
        boolean scoreAssist = percent >= 50 && MatchUtils.hasKillScoring() && assistPoints > 0;

        if (scoreAssist) {
            participant.addScore(assistPoints);
        }

        participant.getStats().addAssist();

        new RewardBuilder(
            player,
            scoreAssist ? assistPoints : 1,
            scoreAssist ? "Point" : "Assist",
            "Assisted Killing " + NickUtils.getDisplayName(killed) + ChatColor.GRAY + " (" + percent + "%)")
            .reward(Sound.ORB_PICKUP);
    }

}
