//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.level.events.IslandLevelCalculatedEvent;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * The type Level listener.
 */
public class IslandLevelListener implements Listener
{
	/**
	 * Instantiates a new Level listener.
	 *
	 * @param addon the addon
	 */
	public IslandLevelListener(StoneGeneratorAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * On level up.
	 *
	 * @param event the event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLevelCalculated(IslandLevelCalculatedEvent event)
	{
		// Process event in Addon Manager.
		this.addon.getAddonManager().checkGeneratorUnlockStatus(
			event.getIsland(),
			User.getInstance(event.getTargetPlayer()),
			event.getLevel());
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Instance of addon for event.
	 */
	private final StoneGeneratorAddon addon;
}
