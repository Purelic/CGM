package net.purelic.cgm.uhc.runnables;

import net.purelic.cgm.commands.toggles.ToggleAutoStartCommand;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.MapUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ChunkLoader extends BukkitRunnable {

    private static final int SIZE = 1000; // half the length of the largest border size

    private static final int REST_EVERY_NUM_OF_CHUNKS = 750;
    private static final int REST_DURATION = 20; // 1 second
    private static final int LOW_MEM_REST_DURATION = 200; // 10 seconds
    public static boolean active = false;

    private final World world;
    private final int maxChunk;
    private final int chunksToLoad;
    private int x, z;
    private int chunksLoaded;

    public ChunkLoader(World world) {
        this.world = world;
        this.maxChunk = Math.round((SIZE + 32) / 16F) + 1; // 32 block/2 chunk buffer around the max border size
        this.chunksToLoad = (2 * this.maxChunk + 1) * (2 * this.maxChunk + 1);
        this.x = -this.maxChunk;
        this.z = -this.maxChunk;
    }

    @Override
    public void run() {
        active = true;
        ChatUtils.broadcastTitle(ChatColor.AQUA + "Pre-Generating World", "(expect lag)");
        ToggleAutoStartCommand.autostart = false;
        this.pregenerate();
    }

    private void pregenerate() {
        boolean lowMem = false;

        List<Chunk> chunks = new ArrayList<>();

        while (this.x <= this.maxChunk && chunks.size() < REST_EVERY_NUM_OF_CHUNKS && !lowMem) {
            this.world.getChunkAtAsync(this.x, this.z, chunks::add);
            this.z++;

            lowMem = this.availableMemory() <= 200;

            if (this.z > this.maxChunk) {
                this.z = -this.maxChunk;
                this.x++;
            }
        }

        this.chunksLoaded += chunks.size();

        // unload and save this batch of chunks
        for (Chunk chunk : chunks) chunk.unload(true);

        // save the world
        this.world.save();

        if (this.x <= this.maxChunk) { // not done loading all chunks
            double progress = this.getProgress();
            ChatUtils.broadcastTitle("" + ChatColor.AQUA + progress + "% Pre-Generated", this.chunksLoaded + "/" + this.chunksToLoad + " chunks loaded");
            PlayerUtils.setLevelAll((int) progress, (float) (progress / 100F));
            TaskUtils.runLater(this::pregenerate, lowMem ? LOW_MEM_REST_DURATION : REST_DURATION);
        } else { // all chunks loaded
            PlayerUtils.setLevelAll(0, 0F);
            ChatUtils.broadcastTitle(ChatColor.GREEN + "Finalizing World...");

            TaskUtils.runLater(() -> {
                active = false;
                MatchManager.cycle();
                TaskUtils.runLaterAsync(MapUtils::saveUHCMap, 40L); // after 2 seconds
            }, 100L); // after 5 seconds
        }
    }

    private double getProgress() {
        double percentage = 100 * (double) this.chunksLoaded / this.chunksToLoad;
        return Math.floor(10 * percentage) / 10;
    }

    public static boolean isActive() {
        return active;
    }

    private int availableMemory() {
        Runtime rt = Runtime.getRuntime();
        return (int) ((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1048576);  // 1024 * 1024 = 1048576 (bytes in 1 MB)
    }

}
