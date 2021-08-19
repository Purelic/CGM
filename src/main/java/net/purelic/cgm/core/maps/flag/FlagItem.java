package net.purelic.cgm.core.maps.flag;

import net.purelic.cgm.core.maps.flag.constants.FlagState;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.commons.profile.preferences.FlagPattern;
import net.purelic.commons.utils.ItemCrafter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class FlagItem {

    private final Flag flag;
    private final DyeColor baseColor;
    private final FlagPattern pattern;

    public FlagItem(Flag flag, FlagPattern pattern) {
        this.flag = flag;
        this.baseColor = ColorConverter.getDyeColor(flag.getFlagColor());
        this.pattern = pattern;
    }

    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        meta.setBaseColor(this.baseColor);
        if (this.pattern != FlagPattern.BLANK) meta.setPatterns(this.pattern.getPatterns());
        item.setItemMeta(meta);

        ItemCrafter itemCrafter = new ItemCrafter(item);
        itemCrafter.setTag("ctf_flag", 1);
        itemCrafter.name(FlagState.RETURNED.getSymbol() + " " + this.flag.getColoredName());
        itemCrafter.lore(ChatColor.BOLD + "Click to Drop Flag");
        itemCrafter.flag(ItemFlag.HIDE_POTION_EFFECTS);

        return itemCrafter.craft();
    }

}
