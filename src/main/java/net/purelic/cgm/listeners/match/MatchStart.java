package net.purelic.cgm.listeners.match;

import net.purelic.cgm.commands.match.SpectateCommand;
import net.purelic.cgm.commands.toggles.ToggleAutoJoinCommand;
import net.purelic.cgm.commands.toggles.ToggleFriendlyFireCommand;
import net.purelic.cgm.core.constants.JoinState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class MatchStart implements Listener {

    private static List<Set<Player>> parties = new ArrayList<>();

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        TabManager.blockUpdates();

        ScoreboardManager.setNameVisibility(ToggleSetting.PLAYER_NAME_VISIBLE.isEnabled());
        ScoreboardManager.setFriendlyFire(ToggleSetting.FRIENDLY_FIRE.isEnabled() || ToggleFriendlyFireCommand.friendlyFire);

        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        TeamSize teamSize = EnumSetting.TEAM_SIZE.get();

        if (JoinState.isState(JoinState.LOCKED) && teamType != TeamType.SOLO && teamSize != TeamSize.SINGLES && ToggleAutoJoinCommand.autoJoin) {
            JoinState.setState(JoinState.PARTY_PRIORITY);
            int maxTeamSize = MatchUtils.getMaxTeamPlayers();
            int toJoin = (int) Bukkit.getOnlinePlayers().stream().filter(player -> !SpectateCommand.SPECTATORS.contains(player)).count();
            Set<Player> shuffled = new HashSet<>();

            if (toJoin < Math.min(6, maxTeamSize + (maxTeamSize / 2))) {
                parties.forEach(party -> party.forEach(player -> {
                    CommandUtils.sendAlertMessage(player, "Not enough online players to support parties - shuffling your party...");
                    shuffled.add(player);
                }));
            } else {
                for (Set<Player> party : parties) {
                    if (party.size() > maxTeamSize || party.size() > 4) {
                        party.forEach(player ->
                                    CommandUtils.sendAlertMessage(player, "Your party is too big for this match - shuffling your party..."));
                        continue;
                    }

                    for (MatchTeam team : teamType.getTeams()) {
                        int size = team.getPlayers().size();
                        int available = maxTeamSize - size;

                        if (size == 0 && available >= party.size()) {
                            for (Player player : party) {
                                CommandUtils.sendAlertMessage(player, "Sending your party to " + team.getColoredName() + " team...");
                                player.performCommand("join " + team.name());
                            }
                            break;
                        }
                    }
                }

                parties.forEach(party -> party.stream()
                        .filter(player -> MatchTeam.getTeam(player) == MatchTeam.OBS && !shuffled.contains(player))
                        .forEach(player ->
                                CommandUtils.sendAlertMessage(player, "Too many parties already joined this match - shuffling your party...")));
            }
        }

        JoinState.setState(JoinState.EVERYONE);
        MatchJoin.setAddParticipants(false);

        List<Player> premiumPlayers = Bukkit.getOnlinePlayers().stream()
            .filter(player -> CommandUtils.isOp(player) || (!Commons.getProfile(player).isStaff() && Commons.getProfile(player).isDonator()))
            .collect(Collectors.toList());

        for (Player player : premiumPlayers) {
            if (MatchTeam.getTeam(player) == MatchTeam.OBS && !SpectateCommand.SPECTATORS.contains(player)) {
                if (ToggleAutoJoinCommand.autoJoin) player.performCommand("join");
            }
        }

        List<Player> shuffled = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(shuffled);

        for (Player player : shuffled) {
            if (MatchTeam.getTeam(player) == MatchTeam.OBS && !SpectateCommand.SPECTATORS.contains(player)) {
                if (ToggleAutoJoinCommand.autoJoin) player.performCommand("join");
            }
        }

        MatchJoin.setAddParticipants(true);

        for (MatchTeam team : teamType.getTeams()) {
            if (team == MatchTeam.OBS) continue;
            for (Player player : team.getPlayers()) {
                MatchManager.addParticipant(player, true, false);
            }
            TabManager.updateTeam(team);
        }

        PlayerUtils.hideObs();

        ChatUtils.broadcastActionBar("The match has begun!");
        Commons.callEvent(new RoundStartEvent(event.isForced()));

        TabManager.allowUpdates();

        // DatabaseUtils.updateStatus(ServerStatus.STARTED, MatchManager.getCurrentMap().getName(), MatchManager.getCurrentGameMode().getName());
    }

    public static void updateParties() {
        Map<String, Set<Player>> parties = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Commons.getProfile(player);
            String partyId = profile.getPartyId();

            if (partyId != null) {
                parties.putIfAbsent(partyId, new HashSet<>());
                parties.get(partyId).add(player);
            }
        }

        MatchStart.parties = orderParties(parties.values().stream().filter(party -> party.size() > 1).collect(Collectors.toList()));
    }

    private static List<Set<Player>> orderParties(List<Set<Player>> parties) {
        int maxTeamSize = MatchUtils.getMaxTeamPlayers();
        List<Set<Player>> highPriority = new ArrayList<>();
        List<Set<Player>> lowPriority = new ArrayList<>();

        for (Set<Player> party : parties) {
            if (party.size() % maxTeamSize == 0 && party.size() >= maxTeamSize) highPriority.add(party);
            else lowPriority.add(party);
        }

        lowPriority.sort(Comparator.comparingInt(Set::size));
        Collections.reverse(lowPriority);

        highPriority.sort(Comparator.comparingInt(Set::size));
        Collections.reverse(highPriority);
        highPriority.addAll(lowPriority);

        return highPriority;
    }

}
