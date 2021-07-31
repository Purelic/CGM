package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenHeadScenario implements Module {

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        Bukkit.getServer().addRecipe(this.getGoldenHeadRecipe());
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

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(killed.getName());
        skull.setItemMeta(meta);

        skull = new ItemCrafter(skull)
            .name(ChatColor.RESET + NickUtils.getDisplayName(killed) + "'s Head'")
            .setTag("killed", killed.getName())
            .setTag("killed_team", team.name())
            .craft();

        killed.getWorld().dropItem(killed.getLocation(), skull);
    }

    @SuppressWarnings("deprecation")
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

}
