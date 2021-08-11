package net.purelic.cgm.listeners.modules;

import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.flag.events.FlagCaptureEvent;
import net.purelic.cgm.core.maps.hill.events.HillCaptureEvent;
import net.purelic.commons.modules.Module;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.meta.FireworkMeta;

public class CaptureEffectModule implements Module {

    @EventHandler
    public void onFlagCapture(FlagCaptureEvent event) {
        Flag flag = event.getFlag();
        if (flag.hasCarrier()) {
            final Location location = flag.getCarrier().getPlayer().getLocation();
            TaskUtils.run(() -> this.confetti(location, 2L));
        }
    }

    @EventHandler
    public void onHillCapture(HillCaptureEvent event) {
        TaskUtils.run(() -> this.confetti(event.getHill().getCenter().clone().add(0, 1, 0), 40L));
    }

    private void confetti(Location location, long delay) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
            .flicker(false)
            .trail(false)
            .with(FireworkEffect.Type.BURST)
            .withColor(Color.fromRGB(11743532), Color.fromRGB(15435844), Color.fromRGB(14602026),
                Color.fromRGB(4312372), Color.fromRGB(6719955), Color.fromRGB(8073150), Color.fromRGB(14188952))
            .build();

        meta.addEffect(effect);
        firework.setFireworkMeta(meta);
        TaskUtils.runLater(firework::detonate, delay);
    }

}
