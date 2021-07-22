package net.purelic.cgm.commands.uhc;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
import net.purelic.cgm.uhc.UHCPreset;
import net.purelic.cgm.uhc.UHCScenario;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class UHCCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("uhc", "scenarios")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (!CGM.getPlaylist().isUHC()) {
                    CommandUtils.sendErrorMessage(player, "You can only use this command on UHC servers!");
                    return;
                }

                Inventory inv = Bukkit.createInventory(null, 54, "UHC Scenarios");

                // Adds the UHC preset items to the top row
                for (UHCPreset preset : UHCPreset.values()) {
                    inv.addItem(preset.getItem());
                }

                int slot = 18;

                for (UHCScenario scenario : UHCScenario.values()) {
                    inv.setItem(slot, scenario.getScenarioItem());
                    inv.setItem(slot + 9, scenario.getToggleItem());

                    slot++;

                    if (slot % 9 == 0) slot += 9;
                }

                player.openInventory(inv);
            });
    }

}
