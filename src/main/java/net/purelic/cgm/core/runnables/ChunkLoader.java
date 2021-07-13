package net.purelic.cgm.core.runnables;

import net.purelic.cgm.commands.toggles.ToggleAutoStartCommand;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.MapUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkLoader extends BukkitRunnable {

    private static final int MAX_SIZE = 1000; // half the length of the largest border size

    private static final int REST_EVERY_NUM_OF_CHUNKS = 250;
    private static final int REST_DURATION = 15;
    public static boolean active = false;

    private final World world;
    private final int maxChunk;
    private final int totalChunksToLoad;
    private int x, z;
    private int chunksLoaded;

    public ChunkLoader(World world) {
        this.world = world;
        this.maxChunk = Math.round(MAX_SIZE / 16F) + 1;
        this.totalChunksToLoad = (2 * this.maxChunk + 1) * (2 * this.maxChunk + 1);
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
        int loaded = 0;

        while (this.x <= this.maxChunk && loaded < REST_EVERY_NUM_OF_CHUNKS) {
            final World.ChunkLoadCallback callback = chunk -> { };
            this.world.getChunkAtAsync(this.x, this.z, callback);

            loaded++;
            this.z++;

            if (this.z > this.maxChunk) {
                this.z = -this.maxChunk;
                this.x++;
            }
        }

        this.chunksLoaded += loaded;

        if (this.x <= this.maxChunk) { // not done loading all chunks
            double progress = this.getProgress();
            ChatUtils.broadcastTitle("" + ChatColor.AQUA + progress + "% Pre-Generated",this.chunksLoaded + "/" + this.totalChunksToLoad + " chunks loaded");
            PlayerUtils.setLevelAll((int) progress, (float) (progress / 100F));
            TaskUtils.runLater(this::pregenerate, REST_DURATION);
        } else { // all chunks loaded
            this.world.save();
            MapUtils.saveUHCMap();

            TaskUtils.runLater(() -> {
                active = false;
                MatchManager.cycle();
            }, 100L);
        }
    }

    private double getProgress() {
        double percentage = 100 * (double) this.chunksLoaded / this.totalChunksToLoad;
        return Math.floor(10 * percentage) / 10;
    }

    public static boolean isActive() {
        return active;
    }

}
