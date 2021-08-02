package net.purelic.cgm.utils.tab;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.*;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamSize;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.Rank;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.ServerUtils;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class TabUtils {

    public static final int COLUMNS = 4;
    public static final int ROWS = 20;
    public static final int MAX_SLOTS = COLUMNS * ROWS;

    public static int getLegacyIndex(int index) {
        int colIndex = index / ROWS;
        int colOffset = colIndex * ROWS;
        int rowIndex = index - colOffset;
        int rowOffset = rowIndex * COLUMNS;
        return rowOffset + colIndex;
    }

    public static String getTabName(int index, boolean legacy) {
        return legacy ?
            getLegacyName(index) : // 1.7 displays name in tab, so we need to use unique invisible characters
            String.format("%02d", index); // 1.8+ displays list name (updated separately), so we can use "00" - "79"
    }

    public static String getLegacyName(int index) {
        return LegacyTabName.get(index);
    }

    public static GameProfile createProfile(UUID uuid, String name) {
        return createProfile(uuid, name, TabSkin.BLANK);
    }

    public static GameProfile createProfile(UUID uuid, String name, TabSkin skin) {
        GameProfile profile = new GameProfile(uuid, name);
        setSkin(profile, skin);
        return profile;
    }

    public static GameProfile createProfile(UUID uuid, String name, Player player) {
        GameProfile profile = new GameProfile(uuid, name);
        Property property = ((CraftPlayer) player).getHandle().getProfile().getProperties().get("textures").iterator().next();
        setSkin(profile, new Property("textures", property.getValue(), property.getSignature()));
        return profile;
    }

    public static void setSkin(GameProfile profile, TabSkin skin) {
        setSkin(profile, skin.getProperty());
    }

    public static void setSkin(GameProfile profile, Property property) {
        profile.getProperties().put("textures", property);
    }

    public static EntityPlayer createPlayer(GameProfile profile, boolean hidePing) {
        return createPlayer(profile, "", hidePing);
    }

    public static EntityPlayer createPlayer(GameProfile profile, String name, boolean hidePing) {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer world = server.getWorldServer(0);
        PlayerInteractManager manager = new PlayerInteractManager(world);
        EntityPlayer player = new EntityPlayer(server, world, profile, manager);
        player.listName = new ChatMessage(name);
        if (hidePing) player.ping = -1;
        return player;
    }

    public static void sendUpdates(Player player, List<Packet<? extends PacketListenerPlayOut>> packets) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (Packet<? extends PacketListenerPlayOut> packet : packets) connection.sendPacket(packet);
    }

    public static String getTeamSlot(MatchTeam team, Player player, boolean legacy) {
        return getTeamSlot(team, MatchTeam.getTeam(player) == team, legacy);
    }

    private static String getTeamSlot(MatchTeam team, boolean sameTeam, boolean legacy) {
        return "" + team.getColor() + ChatColor.BOLD + team.getName() +
            ChatColor.RESET + "  " + ChatColor.WHITE + team.playing() +
            (team != MatchTeam.OBS ? ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + MatchUtils.getMaxTeamPlayers() : "") +
            (team != MatchTeam.OBS && sameTeam && !legacy ? " " + ChatColor.WHITE + "(You)" : "");
    }

    private static int maxTeamsPerCol(TeamType teamType) {
        TeamSize teamSize = EnumSetting.TEAM_SIZE.get();
        int totalTeams = teamType.getTeams().size();

        switch (teamSize) {
            case SINGLES:
                switch (teamType) {
                    case SOLO:
                    case MULTI_TEAM:
                    case TEAMS:
                        return totalTeams;
                    case SQUADS:
                        return totalTeams / 2;
                }
            case DOUBLES:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                    case MULTI_TEAM:
                        return totalTeams;
                    case SQUADS:
                        return totalTeams / 2;
                }
            case TRIOS:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                    case MULTI_TEAM:
                        return totalTeams;
                    case SQUADS:
                        return totalTeams / 2;
                }
            case MINI:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                    case MULTI_TEAM:
                        return totalTeams;
                    case SQUADS:
                        return totalTeams / 2;
                }
            case NORMAL:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                        return totalTeams;
                    case MULTI_TEAM:
                    case SQUADS:
                        return totalTeams / 2;
                }
            case BIG:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                        return 1;
                    case MULTI_TEAM:
                        return 2;
                    case SQUADS:
                        return 3;
                }
            case MEGA:
                switch (teamType) {
                    case SOLO:
                    case TEAMS:
                    case MULTI_TEAM:
                        return 1;
                    case SQUADS:
                        return 2;
                }
            default:
                return totalTeams;
        }
    }

    public static Map<MatchTeam, TabBox> getTeamBoxes(boolean legacy) {
        Map<MatchTeam, TabBox> boxes = new HashMap<>();

        TeamType teamType = EnumSetting.TEAM_TYPE.get();
        List<MatchTeam> teams = teamType.getTeams();

        int maxTeamSize = MatchUtils.getMaxTeamPlayers();
        int playerBoxSize = Math.min(maxTeamSize, 16);
        int colsPerTeam = maxTeamSize / playerBoxSize;
        int maxBoxSize = playerBoxSize + 2;
        int maxTeamsPerCol = maxTeamsPerCol(teamType);

        int rowOffset = 0;
        int colOffset = 0;

        // Create tab box for each team
        for (MatchTeam team : teams) {
            int col1 = colOffset;
            int row1 = rowOffset + 1; // offset for the team header
            int col2 = colOffset + colsPerTeam - 1; // get the index
            int row2 = rowOffset + playerBoxSize; // offset for the header

            boxes.put(team, new TabBox(team, col1, row1, col2, row2));

            rowOffset += maxBoxSize; // padding for team header and blank space below

            if (rowOffset >= TabUtils.ROWS || boxes.size() % maxTeamsPerCol == 0) {
                rowOffset = 0;
                colOffset += colsPerTeam;
            }
        }

        if (colOffset < COLUMNS) {
            // Add spectator team last
            boxes.put(MatchTeam.OBS, new TabBox(MatchTeam.OBS, colOffset, 1, COLUMNS - 1, ROWS - (legacy ? 3 : 1))); // 10 : 6 for stats
        }

        return boxes;
    }

    public static List<Player> sort(List<Player> players) {
        players.sort(new TabComparator());
        return players;
    }

    public static class TabComparator implements Comparator<Player> {

        public int compare(Player p1, Player p2) {
            boolean p1Alive = true;
            boolean p2Alive = true;

            if (MatchManager.isPlaying(p1)) {
                Participant participant = MatchManager.getParticipant(p1);
                p1Alive = participant.getLives() > 0;
            }

            if (MatchManager.isPlaying(p2)) {
                Participant participant = MatchManager.getParticipant(p2);
                p2Alive = participant.getLives() > 0;
            }

            // Sort alive players first (compared to eliminated players)
            int aliveResult = Boolean.compare(p2Alive, p1Alive);
            if (aliveResult != 0) return aliveResult;

            // Sort OP players first
            int opResult = Boolean.compare(CommandUtils.isOp(p2), CommandUtils.isOp(p1));
            if (opResult != 0) return opResult;

            Profile p1Profile = Commons.getProfile(p1);
            Profile p2Profile = Commons.getProfile(p2);

            // Sort by league rating (if ranked server)
            if (ServerUtils.isRanked()) {
                int ratingResult = p2Profile.getRating() - p1Profile.getRating();
                if (ratingResult != 0) return ratingResult;
            }

            // Sort by additional ranks
            for (Rank rank : Rank.values()) {
                int rankResult = Boolean.compare(
                    !NickUtils.isNicked(p2) && p2Profile.hasRank(rank), !NickUtils.isNicked(p1) && p1Profile.hasRank(rank));
                if (rankResult != 0) return rankResult;
            }

            // Lastly, sort alphabetically (case insensitive)
            return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
        }

    }

}
