package net.purelic.cgm.listeners.match;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantRespawnEvent;
import net.purelic.cgm.listeners.participant.ParticipantKill;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundStart implements Listener {

    private final MatchManager matchManager;

    public RoundStart() {
        this.matchManager = CGM.get().getMatchManager();
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        ParticipantKill.firstBlood = true;

        if (EnumSetting.GAME_TYPE.is(GameType.UHC)) {
            SpawnUtils.spread(MatchManager.getParticipants());
        }

        if (NumberSetting.ROUNDS.value() <= MatchManager.getRound()) {
            MatchState.setState(MatchState.ENDED);
        } else {
            this.matchManager.addRound();
            new MatchCountdown(event.isForced()).runTaskTimer(CGM.get(), 0, 20);

            MatchManager.getParticipants().forEach(Participant::resetScore);
            ScoreboardManager.updateSoloBoard();

            if (!EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                for (MatchTeam team : MatchTeam.values()) {
                    team.resetScore();
                }
            }
        }

        // Clear dropped item entities each round
        MatchManager.getCurrentMap().getWorld().getEntities()
            .stream().filter(entity -> entity.getType() == EntityType.DROPPED_ITEM || entity.getType() == EntityType.ARROW)
            .forEach(Entity::remove);

        if (NumberSetting.ROUNDS.value() > 1) {
            if (MatchManager.getRound() > 1) {
                for (Participant participant : MatchManager.getParticipants()) {
                    participant.resetLives();
                    participant.setState(ParticipantState.ALIVE);
                    Commons.callEvent(new ParticipantRespawnEvent(participant, true));
                }
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (MatchTeam.getTeam(player) == MatchTeam.OBS) {
                    ChatUtils.sendTitle(
                        player,
                        "Round " + MatchManager.getRound(),
                        MatchManager.getRoundsString()
                    );
                } else {
                    Participant participant = MatchManager.getParticipant(player);

                    ChatUtils.sendTitle(
                        participant.getPlayer(),
                        "Round " + MatchManager.getRound(),
                        MatchManager.getRound() == 1 ? MatchUtils.getObjectiveString() : MatchManager.getRoundsString()
                    );

                    if (NumberSetting.LIVES_PER_ROUND.value() > 0) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                int lives = participant.getLives();

                                if (lives != -1) {
                                    ChatUtils.sendTitle(
                                        participant.getPlayer(),
                                        "",
                                        ChatColor.AQUA + "" + lives + ChatColor.RESET + " " +
                                            (lives == 1 ? "Life" : "Lives") + " Remaining"
                                    );
                                }
                            }
                        }.runTaskLater(CGM.get(), 50);
                    }
                }
            });

            TabManager.updateTeams();
            return;
        }

        if (NumberSetting.LIVES_PER_ROUND.value() > 0) {
            MatchManager.getParticipants().forEach(participant -> {
                int lives = participant.getLives();

                if (lives != -1) {
                    ChatUtils.sendTitle(
                        participant.getPlayer(),
                        MatchUtils.hasRounds() ? "Round " + MatchManager.getRound() : "",
                        ChatColor.AQUA + "" + lives + ChatColor.RESET + " " + (lives == 1 ? "Life" : "Lives") + " Remaining"
                    );
                }
            });
        }

        if (MatchUtils.isElimination()) {
            TeamType teamType = EnumSetting.TEAM_TYPE.get();
            teamType.getTeams().stream()
                .filter(team -> team.getPlayers().size() == 0)
                .forEach(team -> Commons.callEvent(new MatchTeamEliminateEvent(team)));
        }

        TabManager.updateTeams();
    }

}
