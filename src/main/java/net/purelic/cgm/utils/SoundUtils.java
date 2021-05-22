package net.purelic.cgm.utils;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SoundUtils {

    public enum SFX {

        // dirt = piano
        // wood = bass
        // glass = sticks
        // sand = snare
        // stone = bass drum

        DEFAULT_SFX(new SoundBuilder(Sound.ORB_PICKUP)),

        TEAM_ELIMINATED(new SoundBuilder(Sound.ENDERDRAGON_GROWL)),

        ROUND_WON(
                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.G4)
                        .addSound(SoundBuilder.Key.AS3, 4)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.G4, 4)
                        .addSound(SoundBuilder.Key.AS3, 6)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.G4, 4)
                        .addSound(SoundBuilder.Key.CS4, 2)
                        .addSound(SoundBuilder.Key.E4, 2)
                        .addSound(SoundBuilder.Key.G4, 2)
                        .addSound(SoundBuilder.Key.B4, 2)
                        .addSound(SoundBuilder.Key.A4, 4),

                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.AS3)
                        .addSound(SoundBuilder.Key.D4, 4)
                        .addSound(SoundBuilder.Key.G3, 2)
                        .addSound(SoundBuilder.Key.AS3, 4)
                        .addSound(SoundBuilder.Key.D4, 6)
                        .addSound(SoundBuilder.Key.G3, 2)
                        .addSound(SoundBuilder.Key.AS3, 4)
                        .addSound(SoundBuilder.Key.E4, 2)
                        .addSound(SoundBuilder.Key.A3, 2)
                        .addSound(SoundBuilder.Key.CS4, 2)
                        .addSound(SoundBuilder.Key.E4, 2)
                        .addSound(SoundBuilder.Key.CS4, 4),

                new SoundBuilder(Sound.NOTE_BASS, SoundBuilder.Key.AS3)
                        .addSound(SoundBuilder.Key.G4, 6)
                        .addSound(SoundBuilder.Key.AS3, 6)
                        .addSound(SoundBuilder.Key.G4, 6)
                        .addSound(SoundBuilder.Key.A3, 6)
                        .addSound(SoundBuilder.Key.G4, 6)
                        .addSound(SoundBuilder.Key.A3, 6)
                        .addSound(SoundBuilder.Key.G4, 6),

                new SoundBuilder(Sound.NOTE_STICKS, SoundBuilder.Key.FS5, 6) // 8
                        .addSound(SoundBuilder.Key.FS5, 10)
                        .addSound(SoundBuilder.Key.FS5, 12)
                        .addSound(SoundBuilder.Key.FS5, 6)
                        .addSound(SoundBuilder.Key.FS5, 2)
                        .addSound(SoundBuilder.Key.FS5, 2)
                        .addSound(SoundBuilder.Key.FS5, 2)
        ),

        HILL_CAPTURED(
                new SoundBuilder(Sound.FIREWORK_TWINKLE)
        ),

        HILL_LOST(
                new SoundBuilder(Sound.FIREWORK_LAUNCH)
        ),

        TRAP_TRIGGERED(
                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.A4)
                        .addSound(SoundBuilder.Key.D5, 2)
                        .addSound(SoundBuilder.Key.A4, 2)
                        .addSound(SoundBuilder.Key.D5, 2)
                        .addSound(SoundBuilder.Key.A4, 2)
                        .addSound(SoundBuilder.Key.D5, 2)
                        .addSound(SoundBuilder.Key.A4, 2)
                        .addSound(SoundBuilder.Key.D5, 2)
                        .addSound(SoundBuilder.Key.A4, 2)
                        .addSound(SoundBuilder.Key.D5, 2)
                        .addSound(SoundBuilder.Key.D5, 2),

                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.D4)
                        .addSound(SoundBuilder.Key.FS4, 2)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.FS4, 2)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.FS4, 2)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.FS4, 2)
                        .addSound(SoundBuilder.Key.D4, 2)
                        .addSound(SoundBuilder.Key.FS4, 2)
                        .addSound(SoundBuilder.Key.F4, 2),

                new SoundBuilder(Sound.NOTE_BASS, SoundBuilder.Key.E4)
                        .addSound(SoundBuilder.Key.C4, 2)
                        .addSound(SoundBuilder.Key.B3, 6)
                        .addSound(SoundBuilder.Key.A3, 6)
                        .addSound(SoundBuilder.Key.G3, 6),

                new SoundBuilder(Sound.NOTE_STICKS, SoundBuilder.Key.FS5, 2) // 4
                        .addSound(SoundBuilder.Key.FS5, 4)
                        .addSound(SoundBuilder.Key.FS5, 2)
                        .addSound(SoundBuilder.Key.FS5, 6)
                        .addSound(SoundBuilder.Key.FS5, 4)
                        .addSound(SoundBuilder.Key.FS5, 2)
        ),

        MATCH_WON( // Crab Rave
                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.A3)
                        .addSound(SoundBuilder.Key.F4, 4)
                        .addSound(SoundBuilder.Key.D4, 4)
                        .addSound(SoundBuilder.Key.D4, 4)
                        .addSound(SoundBuilder.Key.A3, 2)
                        .addSound(SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.E4, 2)
                        .addSound(SoundBuilder.Key.C4, 4)
                        .addSound(SoundBuilder.Key.C4, 4)
                        .addSound(SoundBuilder.Key.A3, 2)
                        .addSound(SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.E4, 2)
                        .addSound(SoundBuilder.Key.C4, 4)
                        .addSound(SoundBuilder.Key.C4, 4)
                        .addSound(SoundBuilder.Key.G3, 2)
                        .addSound(SoundBuilder.Key.G3, 4)
                        .addSound(SoundBuilder.Key.B3, 2)
                        .addSound(SoundBuilder.Key.B3, 4)
                        .addSound(SoundBuilder.Key.C4, 2)
                        .addSound(SoundBuilder.Key.B3, 2),

                new SoundBuilder(Sound.NOTE_BASS, SoundBuilder.Key.D4)
                        .addSound(SoundBuilder.Key.D4, 8)
                        .addSound(SoundBuilder.Key.C4, 8)
                        .addSound(SoundBuilder.Key.C4, 8)
                        .addSound(SoundBuilder.Key.A3, 8)
                        .addSound(SoundBuilder.Key.A3, 8)
                        .addSound(SoundBuilder.Key.G3, 8)
                        .addSound(SoundBuilder.Key.G3, 8),

                new SoundBuilder(Sound.NOTE_BASS_DRUM, SoundBuilder.Key.A3)
                        .addSound(SoundBuilder.Key.A3, 16)
                        .addSound(SoundBuilder.Key.A3, 16)
                        .addSound(SoundBuilder.Key.A3, 16),

                new SoundBuilder(Sound.NOTE_SNARE_DRUM, SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.E4, 4)
                        .addSound(SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.A3, 8)
                        .addSound(SoundBuilder.Key.E4, 4)
                        .addSound(SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.A3, 8)
                        .addSound(SoundBuilder.Key.E4, 4)
                        .addSound(SoundBuilder.Key.A3, 4)
                        .addSound(SoundBuilder.Key.A3, 8)
                        .addSound(SoundBuilder.Key.E4, 4)
                        .addSound(SoundBuilder.Key.A3, 4)
        ),

        ALL_STAR  (
                new SoundBuilder(Sound.NOTE_PIANO, SoundBuilder.Key.FS4)
                        .addSound(SoundBuilder.Key.CS5, 8)
                        .addSound(SoundBuilder.Key.AS4, 4)
                        .addSound(SoundBuilder.Key.AS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 8)
                        .addSound(SoundBuilder.Key.FS4, 4)
                        .addSound(SoundBuilder.Key.FS4, 4)
                        .addSound(SoundBuilder.Key.B4, 4)
                        .addSound(SoundBuilder.Key.AS4, 8)
                        .addSound(SoundBuilder.Key.AS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 4)
                        .addSound(SoundBuilder.Key.FS4, 4)
                        .addSound(SoundBuilder.Key.FS4, 8)
                        .addSound(SoundBuilder.Key.CS5, 4)
                        .addSound(SoundBuilder.Key.AS4, 4)
                        .addSound(SoundBuilder.Key.AS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 4)
                        .addSound(SoundBuilder.Key.FS4, 4)
                        .addSound(SoundBuilder.Key.FS4, 4)
                        .addSound(SoundBuilder.Key.DS4, 4)
                        .addSound(SoundBuilder.Key.CS4, 8),

                new SoundBuilder(Sound.NOTE_BASS, SoundBuilder.Key.FS3, 8)
                        .addSound(SoundBuilder.Key.C4, 12)
                        .addSound(SoundBuilder.Key.CS4, 4)
                        .addSound(SoundBuilder.Key.DS4, 12)
                        .addSound(SoundBuilder.Key.GS3, 4)
                        .addSound(SoundBuilder.Key.AS3, 12)
                        .addSound(SoundBuilder.Key.B3, 4)
                        .addSound(SoundBuilder.Key.CS4, 12)
                        .addSound(SoundBuilder.Key.FS3, 4)
                        .addSound(SoundBuilder.Key.C4, 12)
                        .addSound(SoundBuilder.Key.CS4, 4)
                        .addSound(SoundBuilder.Key.DS4, 12)
                        .addSound(SoundBuilder.Key.GS3, 4),

                new SoundBuilder(Sound.NOTE_STICKS, SoundBuilder.Key.CS4, 16)
                        .addSound(SoundBuilder.Key.DS4, 12)
                        .addSound(SoundBuilder.Key.DS4, 4)
                        .addSound(SoundBuilder.Key.DS4, 16)
                        .addSound(SoundBuilder.Key.CS4, 12)
                        .addSound(SoundBuilder.Key.CS4, 8)
                        .addSound(SoundBuilder.Key.DS4, 12)
                        .addSound(SoundBuilder.Key.DS4, 12)
                        .addSound(SoundBuilder.Key.DS4, 4),

                new SoundBuilder(Sound.NOTE_STICKS, SoundBuilder.Key.FS4, 16)
                        .addSound(SoundBuilder.Key.GS4, 12)
                        .addSound(SoundBuilder.Key.GS4, 4)
                        .addSound(SoundBuilder.Key.GS4, 16)
                        .addSound(SoundBuilder.Key.FS4, 12)
                        .addSound(SoundBuilder.Key.FS4, 8)
                        .addSound(SoundBuilder.Key.FS4, 12)
                        .addSound(SoundBuilder.Key.GS4, 12)
                        .addSound(SoundBuilder.Key.GS4, 4)
        ),

        FLAG_TAKEN(new SoundBuilder(Sound.ENDERMAN_TELEPORT)),

        // FLAG_DROPPED(new SoundBuilder(Sound.WOLF_WHINE)),

        FLAG_RETURNED(),

        FLAG_CAPTURED(new SoundBuilder(Sound.FIREWORK_TWINKLE)),

        BED_DESTROYED(new SoundBuilder(Sound.WITHER_DEATH)),

        ENEMY_BED_DESTROYED(new SoundBuilder(Sound.FIREWORK_TWINKLE)),

        TEAM_DESTROYED_BED(new SoundBuilder(Sound.FIREWORK_LAUNCH)),

        SHOP_ITEM_PURCHASED(new SoundBuilder(Sound.NOTE_PLING, 2F, 0)),

        SHOP_PURCHASE_FAILED(new SoundBuilder(Sound.NOTE_PLING, 0.5F, 0)),

        TEAM_UPGRADE_PURCHASED(new SoundBuilder(Sound.ANVIL_USE)),

        ENDER_PEARL_TELEPORT(new SoundBuilder(Sound.ENDERMAN_TELEPORT)),

        JUMP_PAD_LAUNCH(new SoundBuilder(Sound.WITHER_SHOOT, 0.5F, 0.5F)),

        // capturing hill
        // hill captured
        // hill lost
        // teammate death
        // enemy death
        // respawn
        // hill contested
        // round win
        // round lost
        // match win
        // match lost
        // round start
        ;

        private final List<SoundBuilder> sfx;

        SFX(SoundBuilder... soundBuilders) {
            this.sfx = Arrays.asList(soundBuilders);
        }

        public void play(MatchTeam team) {
            this.play(team.getPlayers());
        }

        public void play(Collection<? extends Player> players) {
            players.forEach(this::play);
        }

        public void play(Participant participant) {
            this.play(participant.getPlayer());
        }

        public void play(Player player) {
            this.sfx.forEach(sfx -> sfx.play(player));
        }

        public void playAll() {
            this.sfx.forEach(SoundBuilder::playAll);
        }

        public void playAll(Location location) {
            this.sfx.forEach(sfx -> sfx.playAll(location));
        }

    }

    public static void playSound(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void playSoundAll(Sound sound, float volume, float pitch) {
        SoundUtils.playSound(Bukkit.getOnlinePlayers(), sound, volume, pitch);
    }

    public static void playCountdownNote(int countdown) {
        if (countdown % 10 == 0) playSoundAll(Sound.NOTE_PLING, 10.0F, 0.0F);
        else if (countdown == 1) playSoundAll(Sound.NOTE_PLING, 10.0F, 2.0F);
        else if (countdown <= 5) playSoundAll(Sound.NOTE_PLING, 10.0F, 1.0F);
    }

}
