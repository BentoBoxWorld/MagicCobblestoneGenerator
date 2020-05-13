package world.bentobox.magiccobblestonegenerator.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandEvent.IslandDeleteEvent;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;

/**
 * Listener that get Island destruction to remove entry from DataBase
 *
 */
public class IslandChangeListener implements Listener {

	private StoneGeneratorAddon addon;
	
	public IslandChangeListener(StoneGeneratorAddon addon) {
		this.addon = addon;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onIslandDeleteEvent(IslandDeleteEvent e) {
		// Remove from cache without save
		this.addon.uncacheIsland(e.getPlayerUUID(), false);
		// Remove from DataBase
		this.addon.getHandler().deleteID(e.getPlayerUUID().toString());
	}
	
}
