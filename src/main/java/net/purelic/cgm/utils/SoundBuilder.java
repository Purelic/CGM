package net.purelic.cgm.utils;

import net.purelic.cgm.CGM;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SoundBuilder {

    // TODO add a repeat and volume option

    private static final float DEFAULT_VOLUME = 10F;
    private static final float DEFAULT_PITCH = 1F; // 0.5 = half speed, 1 = normal speed, 2 = double speed

    private final SoundBuilder builder;
    private final List<CustomSound> sounds;
    private Sound sound;

    public SoundBuilder(Sound sound) {
        this(sound, DEFAULT_PITCH);
    }

    public SoundBuilder(Sound sound, Key key) {
        this(sound, key.getPitch());
    }

    public SoundBuilder(Sound sound, Key key, long delay) {
        this(sound, key.getPitch(), delay);
    }

    public SoundBuilder(Sound sound, float pitch) {
        this(sound, pitch, 0L);
    }

    public SoundBuilder(Sound sound, float pitch, float volume) {
        this(sound, pitch, volume, 0L);
    }

    public SoundBuilder(Sound sound, float pitch, long delay) {
        this(sound, pitch, DEFAULT_VOLUME, delay);
    }

    public SoundBuilder(Sound sound, float pitch, float volume, long delay) {
        this.builder = this;
        this.sounds = new ArrayList<>();
        this.sound = sound;
        this.sounds.add(new CustomSound(sound, volume, pitch, delay));
    }

    public SoundBuilder addSound(long delay) {
        return this.addSound(this.sound, delay);
    }

    public SoundBuilder addSound(Key key, long delay) {
        return this.addSound(this.sound, key, delay);
    }

    public SoundBuilder addSound(Sound sound, long delay) {
        return this.addSound(sound, DEFAULT_PITCH, delay);
    }

    public SoundBuilder addSound(Sound sound, Key key, long delay) {
        return this.addSound(sound, key.getPitch(), delay);
    }

    public SoundBuilder addSound(Sound sound, float pitch, long delay) {
        this.sounds.add(new CustomSound(sound, DEFAULT_VOLUME, pitch, delay));
        this.sound = sound;
        return builder;
    }

    public void play(Player player) {
        this.play(player, 0);
    }

    public void playAll() {
        this.playAll(null);
    }

    public void playAll(Location location) {
        this.playAll(0, location);
    }

    private void play(Player player, int index) {
        if (index >= this.sounds.size()) return;

        CustomSound sound = this.sounds.get(index);

        new BukkitRunnable() {
            @Override
            public void run() {
                sound.playSound(player);
                play(player, index + 1);
            }
        }.runTaskLater(CGM.getPlugin(), sound.getDelay());
    }

    private void playAll(int index, Location location) {
        if (index >= this.sounds.size()) return;

        CustomSound sound = this.sounds.get(index);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (location == null) sound.playSoundAll();
                else sound.playSoundAll(location);
                playAll(index + 1, location);
            }
        }.runTaskLater(CGM.getPlugin(), sound.getDelay());
    }

    public enum Key {

        FS3,
        G3,
        GS3,
        A3,
        AS3,
        B3,
        C4,
        CS4,
        D4,
        DS4,
        E4,
        F4,
        FS4,
        G4,
        GS4,
        A4,
        AS4,
        B4,
        C5,
        CS5,
        D5,
        DS5,
        E5,
        F5,
        FS5,
        ;

        private final float pitch;

        Key() {
            this.pitch = (float) Math.pow(2, (this.ordinal() - 12D) / 12);;
        }

        public float getPitch() {
            return this.pitch;
        }

    }

    private static class CustomSound {

        private final Sound sound;
        private final float volume;
        private final float pitch;
        private final long delay;

        public CustomSound(Sound sound, float volume, float pitch, long delay) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delay = delay;
        }

        public long getDelay() {
            return this.delay;
        }

        public void playSound(Player player) {
            this.playSound(player, player.getLocation());
        }

        public void playSound(Player player, Location location) {
            player.playSound(location, this.sound, this.volume, this.pitch);
        }

        public void playSoundAll() {
            Bukkit.getOnlinePlayers().forEach(this::playSound);
        }

        public void playSoundAll(Location location) {
            Bukkit.getOnlinePlayers().forEach(player -> this.playSound(player, location));
        }

    }

}
