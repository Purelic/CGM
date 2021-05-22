package net.purelic.cgm.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.book.BookUtil;

import java.util.ArrayList;
import java.util.List;

public class BookGUI {

    public static void openPaginatedBook(Player player, String header, List<BaseComponent[]> entries) {
        BookGUI.openPaginatedBook(player, header, entries, new ComponentBuilder("").create());
    }

    public static void openPaginatedBook(Player player, String header, List<BaseComponent[]> entries, BaseComponent[] footer) {
        int total = entries.size();
        int i = 0;

        if (total == 0) {
            CommandUtils.sendErrorMessage(player, "There's no information to display!");
            return;
        }

        List<BaseComponent[]> pages = new ArrayList<>();
        BookUtil.PageBuilder page = new BookUtil.PageBuilder();

        for (BaseComponent[] entry : entries) {
            if (i % 10 == 0) {
                if (i != 0) pages.add(page.build());
                page = new BookUtil.PageBuilder().add(header).newLine();
            }

            int number = i + 1;
            page.newLine().add(number + ". ").add(entry);

            i++;
        }

        pages.add(page.newLine().newLine().add(footer).build());

        ItemStack book = BookUtil.writtenBook().pages(pages).build();
        BookUtil.openPlayer(player, book);
    }

}
