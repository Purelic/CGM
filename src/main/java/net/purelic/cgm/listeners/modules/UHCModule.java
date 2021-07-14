package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.CompassTrackingType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchEndEvent;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UHCModule implements DynamicModule {

    private final ShapedRecipe trackingCompass;
    private final ShapedRecipe goldenHead;

    public UHCModule() {
        this.trackingCompass = this.getTrackingCompassRecipe();
        this.goldenHead = this.getGoldenHeadRecipe();
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        World world = event.getMap().getWorld();

        // When a UHC world is loaded it's set to peaceful to improve
        // performance when loading a potentially large world. So, when
        // the match starts we want to set the difficulty back to hard.
        world.setDifficulty(Difficulty.HARD);

        // Enable the player tracking compass recipe
        if (ToggleSetting.PLAYER_COMPASS_ENABLED.isEnabled() && EnumSetting.PLAYER_COMPASS_TYPE.is(CompassTrackingType.PLAYER)) {
            Bukkit.getServer().addRecipe(this.trackingCompass);
        }

        // TODO make scenario toggle
        Bukkit.getServer().addRecipe(this.goldenHead);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Bukkit.getServer().resetRecipes(); // removes any custom crafting recipes
    }

    @EventHandler
    public void onPlayerPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.GOLDEN_APPLE && new ItemCrafter(event.getItem()).hasTag("golden_head")) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
        }
    }

    @EventHandler
    public void onParticipantDeath(ParticipantDeathEvent event) {
        Participant participant = event.getParticipant();
        Player killed = participant.getPlayer();
        MatchTeam team = MatchTeam.getTeam(killed);

        if (EnumSetting.GAME_TYPE.is(GameType.UHC)) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(killed.getName());
            skull.setItemMeta(meta);

            skull = new ItemCrafter(skull)
                .name(ChatColor.RESET + NickUtils.getDisplayName(killed) + "'s Head'")
                .setTag("killed", killed.getName())
                .setTag("killed_team", team.name())
                .craft();

            System.out.println("Dropping head"); // TODO this is firing twice...
            killed.getWorld().dropItem(killed.getLocation(), skull);
        }
    }

    @Override
    public boolean isValid() {
        return EnumSetting.GAME_TYPE.is(GameType.UHC);
    }

    private ShapedRecipe getGoldenHeadRecipe() {
        ItemCrafter item = new ItemCrafter(Material.GOLDEN_APPLE)
            .name(ChatColor.YELLOW + "Golden Player Head")
            .lore(ChatColor.GRAY + "Heals" + ChatColor.RED + " 4 " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "+2" + ChatColor.GRAY + ") Hearts")
            .addTag("golden_head");

        ShapedRecipe recipe = new ShapedRecipe(item.craft());

        recipe.shape(
            "@@@",
            "@#@",
            "@@@"
        );

        recipe.setIngredient('@', Material.GOLD_INGOT);
        recipe.setIngredient('#', Material.SKULL_ITEM, SkullType.PLAYER.ordinal());

        return recipe;
    }

    private ShapedRecipe getTrackingCompassRecipe() {
        ItemCrafter item = new ItemCrafter(Material.COMPASS)
            .name("" + ChatColor.RESET + ChatColor.BOLD + "Tracking Compass")
            .lore("R-Click to track the closest enemy!")
            .addTag("tracking_compass");

        ShapedRecipe recipe = new ShapedRecipe(item.craft());

        recipe.shape(
            "*@*",
            "@#@",
            "*@*"
        );

        recipe.setIngredient('*', Material.AIR);
        recipe.setIngredient('@', Material.GOLD_INGOT);
        recipe.setIngredient('#', Material.REDSTONE);

        return recipe;
    }

}
