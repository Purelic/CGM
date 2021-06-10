package net.purelic.cgm.listeners.match;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.github.paperspigot.Title;

public class RoundEnd implements Listener {

    private final MatchManager matchManager;

    public RoundEnd() {
        this.matchManager = CGM.getPlugin().getMatchManager();
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        MatchTeam winningTeam = event.getWinnerTeam();
        Participant winner = event.getWinner();

        this.updateParticipantStates();

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO) && winner != null) {
            this.matchManager.setRoundWinner(MatchTeam.SOLO);
            winner.addRoundWin();
        } else {
            this.matchManager.setRoundWinner(winningTeam);
        }

        MatchCountdown.getCountdown().cancel();
        // MatchUtils.updateTabAll();
        TabManager.updateRounds();

        PlayerUtils.clearEffectsAll();
        // PlayerUtils.showEveryone();

        if ((!NumberSetting.ROUNDS.isDefault() && winningTeam != null && winningTeam.getRoundsWon() > (NumberSetting.ROUNDS.value() / 2))
            || (NumberSetting.ROUNDS.value() <= MatchManager.getRound())) {
            MatchState.setState(MatchState.ENDED);
        } else {
            new RoundCountdown().runTaskTimer(CGM.getPlugin(), 0, 20);

            Bukkit.getOnlinePlayers().forEach(player -> {
                MatchTeam team = MatchTeam.getTeam(player);
                PlayerUtils.clearInventory(player);
                player.setAllowFlight(true);

                if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                    if (winner == null) {
                        player.sendTitle(new Title(
                                new TextComponent("Round Over"),
                                new TextComponent(ChatColor.YELLOW + "Draw!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND OVER » ").color(ChatColor.WHITE).bold(true)
                                .append(ChatColor.YELLOW + "Draw!").reset()
                                .append("\n").reset()
                        );
                    } else if (team == MatchTeam.OBS) {
                        player.sendTitle(new Title(
                                new TextComponent("Round Over"),
                                new TextComponent(NickUtils.getDisplayName(winner.getPlayer()) + ChatColor.RESET + " won the round!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND OVER » ").color(ChatColor.WHITE).bold(true)
                                .append(NickUtils.getDisplayName(winner.getPlayer()) + ChatColor.RESET + " won the round!").reset()
                                .append("\n").reset()
                        );
                    } else if (winner.getPlayer() == player) {
                        player.sendTitle(new Title(
                                new TextComponent(ChatColor.GREEN + "Round Won"),
                                new TextComponent(""),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND WON » ").color(ChatColor.WHITE).bold(true)
                                .append(ChatColor.GREEN + "You won the round!").reset()
                                .append("\n").reset()
                        );

                        SoundUtils.SFX.MATCH_WON.play(player);
                    } else {
                        player.sendTitle(new Title(
                                new TextComponent(ChatColor.RED + "Round Lost"),
                                new TextComponent(NickUtils.getDisplayName(winner.getPlayer()) + ChatColor.RESET + " won the round!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND LOST » ").color(ChatColor.WHITE).bold(true)
                                .append(NickUtils.getDisplayName(winner.getPlayer()) + ChatColor.RESET + " won the round!").reset()
                                .append("\n").reset()
                        );
                    }
                } else {
                    if (winningTeam == null) {
                        player.sendTitle(new Title(
                                new TextComponent("Round Over"),
                                new TextComponent(ChatColor.YELLOW + "Draw!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND OVER » ").color(ChatColor.WHITE).bold(true)
                                .append(ChatColor.YELLOW + "Draw!").reset()
                                .append("\n").reset()
                        );
                    } else if (team == MatchTeam.OBS) {
                        player.sendTitle(new Title(
                                new TextComponent("Round Over"),
                                new TextComponent(winningTeam.getColoredName() + ChatColor.RESET + " won the round!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND OVER » ").color(ChatColor.WHITE).bold(true)
                                .append(winningTeam.getColoredName() + ChatColor.RESET + " won the round!").reset()
                                .append("\n").reset()
                        );
                    } else if (team == winningTeam) {
                        player.sendTitle(new Title(
                                new TextComponent(ChatColor.GREEN + "Round Won"),
                                new TextComponent(""),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND WON » ").color(ChatColor.WHITE).bold(true)
                                .append(ChatColor.GREEN + "You won the round!").reset()
                                .append("\n").reset()
                        );

                        SoundUtils.SFX.MATCH_WON.play(player);
                    } else {
                        player.sendTitle(new Title(
                                new TextComponent(ChatColor.RED + "Round Lost"),
                                new TextComponent(winningTeam.getColoredName() + ChatColor.RESET + " won the round!"),
                                5,
                                40,
                                5
                        ));

                        ChatUtils.sendMessage(
                            player,
                            new ComponentBuilder("\n")
                                .append(" ROUND LOST » ").color(ChatColor.WHITE).bold(true)
                                .append(winningTeam.getColoredName() + ChatColor.RESET + " won the round!").reset()
                                .append("\n").reset()
                        );
                    }
                }
            });
        }
    }

    // Set all participant states back to ALIVE on round end
    // This is mainly important for updating player visibility
    // E.g. RESPAWNING participants when the round ends might not get shown to others
    private void updateParticipantStates() {
        for (Participant participant : MatchManager.getParticipants()) {
            participant.setState(ParticipantState.ALIVE);
        }
    }

}
