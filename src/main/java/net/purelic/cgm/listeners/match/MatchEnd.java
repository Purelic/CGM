package net.purelic.cgm.listeners.match;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.runnables.CycleCountdown;
import net.purelic.cgm.core.stats.MatchPlacement;
import net.purelic.cgm.core.stats.constants.MatchResult;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.listeners.modules.stats.MatchStatsModule;
import net.purelic.cgm.utils.PlaceUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class MatchEnd implements Listener {

    public static final List<MatchTeam> ELIMINATED_TEAMS = new ArrayList<>();
    public static final List<UUID> ELIMINATED_PLAYERS = new ArrayList<>();

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        TaskUtils.run(() -> {
            PlayerUtils.showEveryone();

            for (Participant participant : MatchManager.getParticipants()) {
                Player player = participant.getPlayer();
                player.setAllowFlight(true);
                player.spigot().setCollidesWithEntities(false);
                PlayerUtils.clearInventory(player);
                PlayerUtils.clearEffects(player);
                player.setLevel(0);
                player.setExp(0);
            }
        });

        this.sendMatchResults(event.isForced());
        MatchManager.addMatch();

        ScoreboardManager.setNameVisibility(true);
        ScoreboardManager.setFriendlyFire(false);

        TaskUtils.runTimer(new CycleCountdown(), 20L);
    }

    private void sendMatchResults(boolean forced) {
        Map<UUID, MatchPlacement> placements = new HashMap<>();

        if (forced) {
            this.sendPlacement(Bukkit.getOnlinePlayers(), ChatColor.YELLOW + "Force Ended!");
        } else {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                List<Participant> ordered = MatchManager.getOrderedParticipants(true);
                Participant topParticipant = MatchManager.getTopParticipant(true);

                // Send match result to spectators
                if (topParticipant == null) {
                    this.sendPlacement(MatchTeam.OBS.getPlayers(), ChatColor.YELLOW + "Draw!");
                } else {
                    this.sendPlacement(MatchTeam.OBS.getPlayers(), NickUtils.getDisplayName(topParticipant.getPlayer()) + ChatColor.RESET + " won the match!");
                }

                // Calculate match placements for all online participants
                boolean tied = false;
                int previousPlace = 1;
                int previousScore = ordered.size() == 0 ? 0 : ordered.get(0).getFinalScore();

                for (int i = 0; i < ordered.size(); i++) {
                    Participant participant = ordered.get(i);

                    int place = i + 1;
                    int score = participant.getFinalScore();

                    // Tied with the previous player
                    if (i - 1 >= 0) {
                        tied = previousScore == score;
                        place = tied ? previousPlace : place;
                    }

                    // Tied with the next player
                    if (i + 1 < ordered.size()) {
                        int nextScore = ordered.get(i + 1).getFinalScore();
                        tied = tied || nextScore == score;
                    }

                    String placeStr = (tied ? "Tied for " : "") + ChatColor.AQUA + PlaceUtils.getPlace(place);
                    this.sendPlacement(participant.getPlayer(), placeStr);

                    MatchResult result = MatchResult.getResult(topParticipant != null, place, ordered.size() >= 8);
                    MatchPlacement placement = this.sendPostMatchResults(participant, place, tied, result);
                    placements.put(participant.getPlayer().getUniqueId(), placement);

                    tied = false;
                    previousPlace = place;
                    previousScore = score;
                }
            } else {
                if (NumberSetting.ROUNDS.value() > 1) {
                    List<MatchTeam> ordered = MatchTeam.getOrderedTeams(true);
                    MatchTeam topTeam = MatchTeam.getTopTeam(true);
                    MatchStatsModule.getCurrent().setWinner(topTeam);

                    // Send match result to spectators
                    if (topTeam == null) {
                        this.sendPlacement(MatchTeam.OBS.getPlayers(), ChatColor.YELLOW + "Draw!");
                    } else {
                        this.sendPlacement(MatchTeam.OBS.getPlayers(), topTeam.getColoredName() + ChatColor.RESET + " won the match!");
                    }

                    // Calculate match placements for all the teams
                    boolean tied = false;
                    int previousPlace = 1;
                    int previousRoundsWon = ordered.get(0).getRoundsWon();

                    for (int i = 0; i < ordered.size(); i++) {
                        MatchTeam team = ordered.get(i);

                        int place = i + 1;
                        int roundsWon = team.getRoundsWon();

                        // Tied with the previous team
                        if (i - 1 >= 0) {
                            tied = previousRoundsWon == roundsWon;
                            place = tied ? previousPlace : place;
                        }

                        // Tied with the next team
                        if (i + 1 < ordered.size()) {
                            int nextRoundsWon = ordered.get(i + 1).getRoundsWon();
                            tied = tied || nextRoundsWon == roundsWon;
                        }

                        String placeStr = EnumSetting.TEAM_TYPE.is(TeamType.TEAMS) ?
                            (tied ? ChatColor.YELLOW + "Draw!" :
                                (place == 1 ? ChatColor.GREEN + "Match Won!" : ChatColor.RED + "Match Lost!")) :
                            (tied ? "Tied for " : "") +
                                ChatColor.AQUA + PlaceUtils.getPlace(place);

                        this.sendPlacement(team.getPlayers(), placeStr);

                        for (Player player : team.getPlayers()) {
                            if (placeStr.contains("Won") || placeStr.contains("1st")) {
                                SoundUtils.SFX.ALL_STAR.play(player);
                            }

                            Participant participant = MatchManager.getParticipant(player);
                            MatchResult result = MatchResult.getResult(topTeam != null, place, false);
                            MatchPlacement placement = this.sendPostMatchResults(participant, place, tied, result);
                            placements.put(participant.getPlayer().getUniqueId(), placement);
                        }

                        tied = false;
                        previousPlace = place;
                        previousRoundsWon = roundsWon;
                    }
                } else {
                    Map<MatchTeam, Integer> ordered = MatchTeam.getOrderedScores(false);
                    MatchTeam winner = MatchTeam.getTopTeam(ordered);
                    MatchStatsModule.getCurrent().setWinner(winner);

                    List<MatchTeam> teams = new ArrayList<>(ordered.keySet());
                    List<Integer> scores = new ArrayList<>(ordered.values());

                    // Send match result to spectators
                    if (winner == null) {
                        this.sendPlacement(MatchTeam.OBS.getPlayers(), ChatColor.YELLOW + "Draw!");
                    } else {
                        this.sendPlacement(MatchTeam.OBS.getPlayers(), winner.getColoredName() + ChatColor.RESET + " won the match!");
                    }

                    boolean tied = false;
                    int previousPlace = 1;
                    int previousScore = scores.get(0);

                    for (int i = 0; i < scores.size(); i++) {
                        MatchTeam team = teams.get(i);

                        int place = i + 1;
                        int score = scores.get(i);

                        // Tied with the previous team
                        if (i - 1 >= 0) {
                            tied = previousScore == score;
                            place = tied ? previousPlace : place;
                        }

                        // Tied with the next team
                        if (i + 1 < ordered.size()) {
                            int nextScore = scores.get(i + 1);
                            tied = tied || nextScore == score;
                        }

                        String placeStr = EnumSetting.TEAM_TYPE.is(TeamType.TEAMS) ?
                            (tied ? ChatColor.YELLOW + "Draw!" :
                                (place == 1 ? ChatColor.GREEN + "Match Won!" : ChatColor.RED + "Match Lost!")) :
                            (tied ? "Tied for " : "") +
                                ChatColor.AQUA + PlaceUtils.getPlace(place);

                        this.sendPlacement(team.getPlayers(), placeStr);

                        for (Player player : team.getPlayers()) {
                            if (placeStr.contains("Won") || placeStr.contains("1st")) {
                                SoundUtils.SFX.ALL_STAR.play(player);
                            }

                            Participant participant = MatchManager.getParticipant(player);
                            MatchResult result = MatchResult.getResult(winner != null, place, false);
                            MatchPlacement placement = this.sendPostMatchResults(participant, place, tied, result);
                            placements.put(player.getUniqueId(), placement);
                        }

                        tied = false;
                        previousPlace = place;
                        previousScore = score;
                    }
                }
            }
        }

        MatchStatsModule.getCurrent().setPlacements(placements);

        ELIMINATED_TEAMS.clear();
        ELIMINATED_PLAYERS.clear();
    }

    private MatchPlacement sendPostMatchResults(Participant participant, int place, boolean tied, MatchResult result) {
        Player player = participant.getPlayer();

        ComponentBuilder stats = new ComponentBuilder("")
            .append(" ??? Kills: " + ChatColor.GREEN + participant.getKills())
            .append("\n ??? Deaths: " + ChatColor.RED + participant.getDeaths())
            .append("\n ??? Final Killstreak: " + ChatColor.AQUA + participant.getKillstreak() + ChatColor.GRAY + " (best " + participant.getBestKillstreak() + ")")
            .append("\n ??? Longest Shot: " + ChatColor.AQUA + participant.getLongestShot())
            .append("\n ??? Personal Score: " + ChatColor.AQUA + participant.getTotalScore());

        if (!EnumSetting.TEAM_TYPE.is(TeamType.SOLO) && !NumberSetting.ROUNDS.isDefault()) {
            stats.append("\n ??? Rounds Won: " + ChatColor.AQUA + participant.getRoundsWon() + ChatColor.GRAY + " (of " + MatchManager.getRound() + ")");
        }

        stats.append("\n ??? Place: " + (tied ? "Tied for " : "") + ChatColor.AQUA + PlaceUtils.getPlace(place));

        ChatUtils.sendMessage(player, ChatUtils.getHeader("Post-Match Stats", ChatColor.AQUA, ChatColor.WHITE));
        ChatUtils.sendMessage(player, stats);

        return new MatchPlacement(participant, place, tied, result);
    }

    @EventHandler
    public void onMatchTeamEliminate(MatchTeamEliminateEvent event) {
        if (NumberSetting.ROUNDS.value() == 1) ELIMINATED_TEAMS.add(event.getTeam());
    }

    @EventHandler
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        if (NumberSetting.ROUNDS.value() == 1) ELIMINATED_PLAYERS.add(event.getParticipant().getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        ELIMINATED_TEAMS.clear();
        ELIMINATED_PLAYERS.clear();
    }

    private void sendPlacement(Collection<? extends Player> players, String subtitle) {
        for (Player player : players) this.sendPlacement(player, subtitle);
    }

    private void sendPlacement(Player player, String subtitle) {
        ChatUtils.sendTitle(player, "Game Over", subtitle, 60);

        ChatUtils.sendMessage(
            player,
            new ComponentBuilder("\n")
                .append(" GAME OVER ?? ").color(ChatColor.WHITE).bold(true)
                .append(subtitle).reset()
                .append("\n").reset()
        );
    }

}
