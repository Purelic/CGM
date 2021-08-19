package net.purelic.cgm.listeners.participant;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.kit.KitType;
import net.purelic.cgm.listeners.modules.BlockProtectionModule;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.*;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticipantRespawn implements Listener {

    // TODO any way to run this async and just do the necessary things sync?

    @EventHandler
    public void onParticipantRespawn(ParticipantRespawnEvent event) {
        Participant participant = event.getParticipant();
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);

        player.setPlayerListName(Commons.getProfile(player).getFlairs() + team.getColor() + NickUtils.getRealName(player));

        if ((NumberSetting.LIVES_PER_ROUND.value() > 0 && participant.isQueued() && !CGM.getPlaylist().isUHC()) || TaskUtils.isRunning(RoundCountdown.getCountdown())) {
            CommandUtils.sendAlertMessage(player, "You will join at the beginning of the next round");
            participant.setLives(-1);
            participant.setDead(true);
            participant.setState(ParticipantState.QUEUED);
            player.setPlayerListName(ChatColor.RED + "\u2715 " + Commons.getProfile(player).getFlairs() + ColorConverter.darken(MatchTeam.getTeam(player).getColor()) + NickUtils.getRealName(player));
            // TODO run async?
            TabManager.updateTeam(team, false);
            return;
        } else {
            // TODO run async?
            TabManager.updatePlayer(player);
        }

        if (!NumberSetting.LIVES_PER_ROUND.isDefault() && !event.isRoundStart() && !EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) {
            int lives = participant.getLives();
            String text = ChatColor.AQUA + "" + lives + ChatColor.RESET + " " + (lives == 1 ? "Life" : "Lives") + " Remaining";

            ChatUtils.sendTitle(
                player,
                "",
                text,
                20
            );

            CommandUtils.sendAlertMessage(player, text);
        }

        participant.setDead(false);
        participant.setState(ParticipantState.ALIVE);

        if (MatchState.isState(MatchState.STARTED)) {
            MatchManager.getCurrentGameMode().getKit(KitType.DEFAULT).apply(player);
        }

        SpawnUtils.teleportRandom(player, event.isRoundStart());
        player.setHealth(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setExp(0);
        player.setLevel(0);
        player.spigot().setAffectsSpawning(true);
        player.spigot().setCollidesWithEntities(true);
        player.setGameMode(GameMode.SURVIVAL);
        PlayerUtils.logRespawnLocation(player);
        // removes stuck arrows
        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerUtils.updateVisibility(player);
                PlayerUtils.clearEffects(player);
                PlayerUtils.applyDefaultEffects(player);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setSneaking(false);
                player.setSprinting(false);

                if (NumberSetting.PLAYER_HASTE.value() == 0 && BlockProtectionModule.canPlaceBlocks()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 2));
                }

                if (NumberSetting.PLAYER_RESISTANCE.value() == 0) {
                    int duration = EnumSetting.GAME_TYPE.is(GameType.UHC) && event.isRoundStart() ? 600 : 60;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 200));
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 200));

                // fix legacy players sometimes still being able to fly after their game mode is updated
                if (VersionUtils.isLegacy(player)) {
                    TaskUtils.runLater(() -> {
                        if (MatchState.isState(MatchState.STARTED)) {
                            if (!player.getAllowFlight()) {
                                player.setFlying(false);
                            }
                        }
                    }, 20L);
                }
            }
        }.runTask(CGM.get());
    }

}
