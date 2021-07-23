package net.purelic.cgm.uhc.scenarios;

import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.events.match.MatchStartEvent;
import net.purelic.commons.modules.Module;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.reflect.Method;
import java.util.Arrays;

public class NetherScenario implements Module {

    private boolean netherEntered = false;

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        this.netherEntered = false;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;

        Location loc = event.getFrom();

        try {
            Class<TravelAgent> travelAgent = TravelAgent.class;
            Method getPortalTravelAgent = this.getMethod(event.getClass(), "getPortalTravelAgent");
            Method findOrCreate = this.getMethod(travelAgent, "findOrCreate", Location.class);
            Object travelAgentInstance = getPortalTravelAgent.invoke(event);

            if (event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
                loc.setWorld(MatchManager.getCurrentMap().getWorld());
                loc.setX(loc.getX() * 2D);
                loc.setZ(loc.getZ() * 2D);
                Location to = (Location) findOrCreate.invoke(travelAgentInstance, loc);
                Validate.notNull(to, "TravelAgent returned null location!");
                event.setTo(to);
            } else {
                loc.setWorld(Bukkit.getWorld("uhc_nether"));
                loc.setX(loc.getX() / 2D);
                loc.setZ(loc.getZ() / 2D);
                Location to = (Location) findOrCreate.invoke(travelAgentInstance, loc);
                Validate.notNull(to, "TravelAgent returned null location!");
                event.setTo(to);
                this.netherEntered = true;
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    public Method getMethod(Class<?> c, String name, Class<?>... argTypes) throws ReflectiveOperationException{

        for (Method method : c.getMethods()){
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), argTypes)){
                method.setAccessible(true);
                return method;
            }
        }

        for (Method method : c.getDeclaredMethods()){
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), argTypes)){
                method.setAccessible(true);
                return method;
            }
        }

        throw new ReflectiveOperationException("Method " + name + " not found in " + c.getName() + " with arguments: " + Arrays.toString(argTypes));
    }

    public boolean isNetherEntered() {
        return this.netherEntered;
    }

    public void setNetherEntered(boolean netherEntered) {
        this.netherEntered = netherEntered;
    }

}
