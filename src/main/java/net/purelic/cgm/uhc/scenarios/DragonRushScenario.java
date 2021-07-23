package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.commons.Commons;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class DragonRushScenario implements Module {

    private final Random random = new Random();
    private boolean endEntered = false;

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.endEntered = false;

        List<Block> portalBlocks = new ArrayList<>();
        Location portalLoc = this.getPortalLocation();

        portalBlocks.add(portalLoc.clone().add(1, 0, 2).getBlock());
        portalBlocks.add(portalLoc.clone().add(0, 0, 2).getBlock());
        portalBlocks.add(portalLoc.clone().add(-1, 0, 2).getBlock());

        portalBlocks.add(portalLoc.clone().add(-2, 0, 1).getBlock());
        portalBlocks.add(portalLoc.clone().add(-2, 0, 0).getBlock());
        portalBlocks.add(portalLoc.clone().add(-2, 0, -1).getBlock());

        portalBlocks.add(portalLoc.clone().add(1, 0, -2).getBlock());
        portalBlocks.add(portalLoc.clone().add(0, 0, -2).getBlock());
        portalBlocks.add(portalLoc.clone().add(-1, 0, -2).getBlock());

        portalBlocks.add(portalLoc.clone().add(2, 0, 1).getBlock());
        portalBlocks.add(portalLoc.clone().add(2, 0, 0).getBlock());
        portalBlocks.add(portalLoc.clone().add(2, 0, -1).getBlock());

        int i = 0;
        BlockFace blockFace = BlockFace.NORTH;

        for (Block block : portalBlocks) {
            block.setType(Material.ENDER_PORTAL_FRAME);
            this.setEndPortalFrameOrientation(block, blockFace);

            int num = this.random.nextInt(2) + 1;

            if (num == 1) { // 50%
                this.setEye(block, true);
            }

            i++;
            if (i == 3) {
                i = 0;
                if (blockFace == BlockFace.NORTH) {
                    blockFace = BlockFace.EAST;
                } else if (blockFace == BlockFace.EAST) {
                    blockFace = BlockFace.SOUTH;
                } else if (blockFace == BlockFace.SOUTH) {
                    blockFace = BlockFace.WEST;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON
            || event.getEntity().getKiller() == null) return;

        Player killer = event.getEntity().getKiller();

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
            Commons.callEvent(new RoundEndEvent(MatchManager.getParticipant(killer)));
        } else {
            Commons.callEvent(new RoundEndEvent(MatchTeam.getTeam(killer)));
        }

        TaskUtils.runLater(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(killer);
            }
        }, 60L); // teleport everyone after 3 seconds
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            ChatUtils.broadcastAlert("A player has just entered the end dimension!");
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        World end = Bukkit.getWorld("uhc_the_end");
        Location to = new Location(end, -42, 48, -18);

        this.createEndSpawnAir(to);
        this.createEndSpawnObsidian(to);

        event.setTo(to);

        this.endEntered = true;
    }

    private Location getPortalLocation() {
        World world = MatchManager.getCurrentMap().getWorld();
        int portalY = 0;

        for (int x = -4; x < 4; x++) {
            for (int z = -4; z < 4; z++) {
                int y = getHighestBlock(world, x, z);
                if (y > portalY) {
                    portalY = y;
                }
            }
        }

        return new Location(world, 0, portalY + 1, 0);
    }

    private int getHighestBlock(World world, int x, int z) {
        int y = 250;

        while (world.getBlockAt(x, y, z).getType() == Material.AIR) {
            y--;
        }

        return y;
    }

    private void setEndPortalFrameOrientation(Block block, BlockFace blockFace) {
        byte data = -1;
        switch (blockFace) {
            case NORTH:
                data = 2;
                break;
            case EAST:
                data = 3;
                break;
            case SOUTH:
                data = 0;
                break;
            case WEST:
                data = 1;
                break;
        }

        block.setData(data);
    }

    private void setEye(Block block, boolean eye) {
        byte data = block.getData();

        if (eye && data < 4) {
            data += 4;
        } else if (!eye && data > 3) {
            data -= 4;
        }

        block.setData(data);
    }

    private void createEndSpawnAir(Location loc) {
        int topBlockX = (-41);
        int bottomBlockX = (-44);
        int topBlockY = (50);
        int bottomBlockY = (48);
        int topBlockZ = (-17);
        int bottomBlockZ = (-20);

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void createEndSpawnObsidian(Location loc) {
        int topBlockX = (-41);
        int bottomBlockX = (-44);
        int topBlockY = (47);
        int bottomBlockY = (47);
        int topBlockZ = (-17);
        int bottomBlockZ = (-20);

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc.getWorld().getBlockAt(x, y, z);
                    block.setType(Material.OBSIDIAN);
                }
            }
        }
    }

    public boolean isEndEntered() {
        return this.endEntered;
    }

    public void setEndEntered(boolean endEntered) {
        this.endEntered = endEntered;
    }

}
