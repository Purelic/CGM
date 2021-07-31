package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.Medal;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.events.participant.ParticipantKillEvent;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.ItemCrafter;
import net.purelic.commons.utils.NickUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HeadModule implements Listener {

    @EventHandler
    public void onParticipantKill(ParticipantKillEvent event) {
        Participant participant = event.getParticipant();
        Player killer = participant.getPlayer();
        Player killed = event.getKilled().getPlayer();
        MatchTeam killerTeam = MatchTeam.getTeam(killer);
        MatchTeam killedTeam = MatchTeam.getTeam(killed);

        if (EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER) && ToggleSetting.PLAYERS_DROP_HEADS.isEnabled()) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(killed.getName());
            skull.setItemMeta(meta);

            skull = new ItemCrafter(skull)
                .name(ChatColor.RESET + NickUtils.getDisplayName(killed) + " killed by " + NickUtils.getDisplayName(killer))
                .setTag("killer", killer.getName())
                .setTag("killed", killed.getName())
                .setTag("killer_team", killerTeam.name())
                .setTag("killed_team", killedTeam.name())
                .craft();

            killed.getWorld().dropItem(killed.getLocation(), skull);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled()
            || !EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER)
            || !ToggleSetting.COLLECT_HEADS_INSTANTLY.isEnabled()) return;

        Player player = event.getPlayer();
        player.getInventory().addItem(event.getItem().getItemStack());

        event.setCancelled(true);
        event.getItem().remove();
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.2F, 1);

        Participant participant = MatchManager.getParticipant(player);
        HeadModule.scoreHeads(participant);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRoundStart(RoundStartEvent event) {
        int interval = NumberSetting.HEAD_COLLECTION_INTERVAL.value();
        if (interval > 0) {
            long delay = NumberSetting.ROUNDS.value() > 1 ? 50L : 0L;
            delay += NumberSetting.LIVES_PER_ROUND.value() > 0 ? 50L : 0L;
            TaskUtils.runLaterAsync(() -> ChatUtils.broadcastTitle("", "Heads collect every " +
                ChatColor.AQUA + interval + "s" + ChatColor.RESET + "!"), delay);
        }
    }

    public static void scoreHeads(Participant participant) {
        Player player = participant.getPlayer();
        Set<ItemStack> skulls = HeadModule.getSkulls(player);

        if (skulls.size() == 0) return;

        int points = 0;
        int collected = 0;
        int stolen = 0;
        int recovered = 0;

        for (ItemStack skull : skulls) {
            ItemCrafter itemCrafter = new ItemCrafter(skull);
            String killed = itemCrafter.getTag("killed");
            String killer = itemCrafter.getTag("killer");
            String killerTeam = itemCrafter.getTag("killer_team");
            int amount = skull.getAmount();

            collected += amount;

            if (player.getName().equals(killed)) {
                points += (NumberSetting.HEAD_RECOVERED_POINTS.value() * amount);
                recovered += amount;
            } else if (killed.isEmpty() || killer.isEmpty() || killerTeam.isEmpty() || player.getName().equals(killer) || MatchTeam.getTeam(player) == MatchTeam.valueOf(killerTeam)) {
                points += (NumberSetting.HEAD_COLLECTED_POINTS.value() * amount);
            } else {
                points += (NumberSetting.HEAD_STOLEN_POINTS.value() * amount);
                stolen += amount;
            }

            player.getInventory().remove(skull);
        }

        if (points == 0) return;

        participant.addScore(points);

        RewardBuilder reward = new RewardBuilder(player, points, collected + " Head" + (collected == 1 ? "" : "s") + " Collected");
        reward.addMedal(Medal.HEAD_RECOVERED, recovered);
        reward.addMedal(Medal.HEAD_STOLEN, stolen);
        if (collected >= 15) reward.addMedal(Medal.HEAD_MASTER);
        else if (collected >= 5) reward.addMedal(Medal.BOUNTY_HUNTER);
        reward.reward();

        participant.getStats().addHeadsCollected(collected);
        participant.getStats().addHeadsStolen(stolen);
        participant.getStats().addHeadsRecovered(recovered);
    }

    public static void displayParticles() {
        for (Participant participant : MatchManager.getParticipants()) {
            Player player = participant.getPlayer();
            int amount = HeadModule.getTotalSkulls(player);
            if (amount == 0) continue;

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) continue;

                online.spigot().playEffect(
                    player.getLocation().clone().add(0, 1, 0),
                    Effect.FLAME,
                    0,
                    0,
                    0.5F,
                    0.5F,
                    0.5F,
                    0.1F,
                    amount,
                    30);
            }
        }
    }

    public static Set<ItemStack> getSkulls(Player player) {
        return Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() == Material.SKULL_ITEM)
            .collect(Collectors.toSet());
    }

    public static int getTotalSkulls(Player player) {
        return HeadModule.getTotalSkulls(HeadModule.getSkulls(player));
    }

    public static int getTotalSkulls(Set<ItemStack> skulls) {
        return skulls.stream().mapToInt(ItemStack::getAmount).sum();
    }

}
