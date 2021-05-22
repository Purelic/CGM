package net.purelic.cgm.listeners.participant;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.damage.DamageTick;
import net.purelic.cgm.core.damage.KillAssist;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.DropType;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.DamageManger;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ShopManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.maps.shop.constants.TeamUpgrade;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.runnables.RespawnCountdown;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.participant.ParticipantAssistEvent;
import net.purelic.cgm.events.participant.ParticipantDeathEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantKillEvent;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.*;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.Title;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipantDeath implements Listener {

    @EventHandler
    public void onParticipantDeath(ParticipantDeathEvent event) {
        Participant participant = event.getParticipant();
        Player player = participant.getPlayer();
        Participant killerParticipant = event.getKiller();
        PlayerDeathEvent deathEvent = event.getDeathEvent();
        boolean combatLog = event.isCombatLog();
        boolean eliminated = event.isEliminated();
        boolean gameTypeWithScoring = event.isGameTypeWithScoring();

        // If no killer found, use the last player who damaged them
        List<DamageTick> ticks = DamageManger.getLoggedTicks(player.getUniqueId());
        List<KillAssist> assists = DamageManger.getPossibleAssists(ticks);

        if (killerParticipant == null && assists.size() > 0) {
            killerParticipant = MatchManager.getParticipant(assists.get(assists.size() - 1).getAttacker());
        }

        // Set final killer (if one) and send death message
        Player killer = killerParticipant == null ? null : killerParticipant.getPlayer();
        boolean suicide = killer == null;

        // Broadcast death message
        deathEvent.setDeathMessage(null);
        this.sendDeathMessage(player, killer, eliminated, assists.size() - 1, combatLog);

        // Award assists
        for (KillAssist assist : assists) {
            assist.setKiller(killer == assist.getAttacker());
            Commons.callEvent(new ParticipantAssistEvent(assist, player));
        }

        // Stat and score tracking
        if (!suicide) {
            // oq gm was index out of bounds
            KillAssist assist = assists.stream().filter(KillAssist::isKiller).collect(Collectors.toList()).stream().findFirst().orElse(null);
            Commons.callEvent(new ParticipantKillEvent(killerParticipant, participant, assist, eliminated, gameTypeWithScoring));
        } else if (gameTypeWithScoring) {
            participant.addScore(NumberSetting.DEATHMATCH_SUICIDE_POINTS.value());
        }

        // Drop items
        this.dropItems(player, this.getItemDrops(player, deathEvent), true, killer);

        participant.setDead(true);
        participant.getStats().addDeath(killerParticipant, eliminated);

        if (gameTypeWithScoring) {
            participant.addScore(NumberSetting.DEATHMATCH_DEATH_POINTS.value());
        }

        // Set player properties
        TaskUtils.runAsync(() -> PacketUtils.playDeathAnimation(player, eliminated));
        PlayerUtils.clearEffects(player);
        // PacketUtils.setBorder(player);
        player.setPlayerListName((eliminated ? ChatColor.RED + "\u2715 " : "") + Commons.getProfile(player).getFlairs() + ColorConverter.darken(MatchTeam.getTeam(player).getColor()) + NickUtils.getRealName(player));
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 1F, 1.0F);
        player.spigot().setCollidesWithEntities(false);
        player.spigot().setAffectsSpawning(false);
        player.setFireTicks(0);
        player.setLevel(0);
        player.setExp(0);
        player.setSaturation(20);
        player.setFoodLevel(20);
        PlayerUtils.clearInventory(player);

        if (ToggleSetting.BLACKOUT_RESPAWN.isEnabled()) {
            PlayerUtils.addPermanentEffect(player, PotionEffectType.BLINDNESS);
        }

        // Start respawn or eliminate player
        if (!eliminated) {
            PlayerUtils.hideFromAll(player);
            TabManager.updatePlayer(player);
            if (!combatLog) this.startRespawnCountdown(participant, killer, suicide);
            // PlayerUtils.hideFromAll(player);
        } else {
            TabManager.updateTeam(player);
            // PlayerUtils.hideFromPlaying(player);
            PlayerUtils.updateVisibility(player);
            Commons.callEvent(new ParticipantEliminateEvent(participant, combatLog));
        }

        // Clean up damage tracking
        DamageManger.dump(player.getUniqueId());

        // Log player death for AFK detection
        PlayerUtils.logDeathLocation(player);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onMatchQuit(MatchQuitEvent event) {
        Player player = event.getPlayer();
        this.dropItems(player, this.getItemDrops(player), false, null);
    }

    private List<ItemStack> getItemDrops(Player player, PlayerDeathEvent event) {
        List<ItemStack> drops = event.getDrops();

        drops.add(player.getItemOnCursor());

        if (player.getOpenInventory() != null) {
            drops.addAll(Arrays.asList(player.getOpenInventory().getTopInventory().getContents()));
        }

        return drops;
    }

    private List<ItemStack> getItemDrops(Player player) {
        List<ItemStack> drops = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
        drops.add(player.getItemOnCursor());

        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
            drops.addAll(Arrays.asList(player.getOpenInventory().getTopInventory().getContents()));
        }

        return drops;
    }

    private void dropItems(Player player, List<ItemStack> items, boolean deathEvent, Player killer) {
        Participant killerParticipant = MatchManager.getParticipant(killer);
        boolean isKillerDead = killerParticipant != null && (killerParticipant.isDead() || killerParticipant.isEliminated());
        boolean inVoid = player.getLastDamageCause() != null && player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID;
        boolean voided = killerParticipant != null && inVoid;

        boolean headHunter = EnumSetting.GAME_TYPE.is(GameType.HEAD_HUNTER);
        boolean bedWars = EnumSetting.GAME_TYPE.is(GameType.BED_WARS);

        List<ItemStack> remove = new ArrayList<>();
        int arrows = 0;
        int gapples = 0;
        int wool = 0;
        int emerald = 0;
        int pearl = 0;

        for (ItemStack item : items) {
            if (item == null) continue;

            Material material = item.getType();
            ItemCrafter itemCrafter = new ItemCrafter(item);

            if (bedWars) {
                if (material != Material.IRON_INGOT
                    && material != Material.GOLD_INGOT
                    && material != Material.DIAMOND
                    && material != Material.EMERALD) {
                    remove.add(item);
                } else {
                    if (voided) {
                        remove.add(item);

                        if (!isKillerDead) {
                            killer.getInventory().addItem(item);
                        }
                    }
                }

                continue;
            }

            if (inVoid) {
                remove.add(item);
                continue;
            }

            if (itemCrafter.hasTag("locked") || itemCrafter.hasTag("kit")) {
                remove.add(item);
            } else if (material == Material.ARROW) {
                arrows += item.getAmount();
                remove.add(item);
            } else if (material == Material.GOLDEN_APPLE) {
                gapples += item.getAmount();
                remove.add(item);
            } else if (material == Material.WOOL) {
                wool += item.getAmount();
                remove.add(item);
            } else if (material == Material.EMERALD) {
                emerald += item.getAmount();
                remove.add(item);
            } else if (material == Material.ENDER_PEARL) {
                pearl += item.getAmount();
                remove.add(item);
            } else if (material == Material.SKULL_ITEM && headHunter) {
                Item drop = player.getWorld().dropItemNaturally(player.getLocation(), item);
                drop.setVelocity(drop.getVelocity().multiply(3));
                remove.add(item);
            } else if (material == Material.BANNER && !item.getItemMeta().getLore().isEmpty()) {
                remove.add(item);
            }
        }

        DropType arrowDropType = EnumSetting.ARROW_DROP_TYPE.get();
        arrowDropType.dropItems(player, Material.ARROW, arrows);

        DropType gappleDropType = EnumSetting.GAPPLE_DROP_TYPE.get();
        gappleDropType.dropItems(player, Material.GOLDEN_APPLE, gapples);

        DropType woolDropType = EnumSetting.WOOL_DROP_TYPE.get();
        woolDropType.dropItems(player, Material.WOOL, wool);

        DropType emeraldDropType = EnumSetting.EMERALD_DROP_TYPE.get();
        emeraldDropType.dropItems(player, Material.EMERALD, emerald);

        DropType pearlDropType = EnumSetting.PEARL_DROP_TYPE.get();
        pearlDropType.dropItems(player, Material.ENDER_PEARL, pearl);

        items.removeAll(remove);

        if (!deathEvent) {
            for (ItemStack item : items) {
                if (item == null || item.getType() == Material.AIR) continue;
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    private void sendDeathMessage(Player player, Player killer, boolean eliminated, int assists, boolean combatLog) {
        String whiteMessage = DeathMessageUtils.getDeathMessage(player, killer, ChatColor.WHITE);
        String grayMessage = DeathMessageUtils.getDeathMessage(player, killer, ChatColor.GRAY);

        String assistMessage = assists < 1 ? "" : " assisted by " + assists + " other" + (assists == 1 ? "" : "s");
        whiteMessage += ChatColor.WHITE + assistMessage;
        grayMessage  += ChatColor.GRAY  + assistMessage;

        whiteMessage += !combatLog ? "" : " (combat log)";
        grayMessage  += !combatLog ? "" : " (combat log)";

        whiteMessage += !eliminated ? "" : "" + ChatColor.RED + ChatColor.BOLD + " ELIMINATED";
        grayMessage  += !eliminated ? "" : "" + ChatColor.RED + ChatColor.BOLD + " ELIMINATED";

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == player) {
                online.sendMessage(whiteMessage.replaceFirst(player.getName(), "You").replaceFirst(" was ", " were "));
            } else if (online == killer) {
                online.sendMessage(whiteMessage.replaceFirst(killer.getName(), "You"));
            } else {
                online.sendMessage(grayMessage);
            }
        }
    }

    private void startRespawnCountdown(Participant participant, Player killer, boolean suicide) {
        Player player = participant.getPlayer();

        double seconds = NumberSetting.RESPAWN_TIME.value();
        seconds += participant.getDeaths() * NumberSetting.RESPAWN_TIME_GROWTH.value();
        seconds += suicide ? NumberSetting.RESPAWN_SUICIDE_PENALTY.value() : 0;
        seconds = Math.min(seconds, NumberSetting.RESPAWN_MAX_TIME.value());

        if (ShopManager.hasUpgrade(player, TeamUpgrade.QUICK_RESPAWN_II)) {
            seconds  *= 0.70; // 30% faster
        } else if (ShopManager.hasUpgrade(player, TeamUpgrade.QUICK_RESPAWN_II)) {
            seconds *= 0.85; // 15% faster
        }

        if (ToggleSetting.RESPAWN_ON_DROP.isEnabled() && FlagUtils.isTeammateCarrier(player)) {
            player.setAllowFlight(true);
            FlagUtils.sendRespawnOnDropMessage(player);
            return;
        }

        if (VersionUtils.isLegacy(player)) {
            CommandUtils.sendAlertMessage(player, "You will respawn in " + ChatColor.AQUA + new DecimalFormat("#.#").format(seconds) + ChatColor.RESET + " second" + (seconds == 1 ? "" : "s"));
        } else {
            player.sendTitle(new Title(
                ChatColor.RED + "You died!",
                DeathMessageUtils.getShortDeathMessage(player, killer),
                5,
                (int) (seconds * 20),
                5));
        }

        new RespawnCountdown(seconds, participant).runTaskTimerAsynchronously(CGM.getPlugin(), 0L, 1L);
    }

}
