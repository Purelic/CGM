package net.purelic.cgm.listeners;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.commons.events.PlayerRankChangeEvent;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

public class PlayerRankChange implements Listener {

    @EventHandler
    public void onPlayerRankChange(PlayerRankChangeEvent event) {
        Player player = event.getPlayer();
        Profile profile = event.getProfile();

        if (!ScoreboardManager.hasTeam(player)) {
            ScoreboardManager.setScoreboard(player);
        } else if (MatchTeam.getTeam(player) == MatchTeam.OBS) {
            Team team = player.getScoreboard().getTeam(NickUtils.getRealName(player));
            team.setPrefix(profile.getFlairs() + ChatColor.AQUA);
        }

        player.setPlayerListName(profile.getFlairs() + player.getDisplayName());
        TabManager.updateTeam(player);
    }

}
