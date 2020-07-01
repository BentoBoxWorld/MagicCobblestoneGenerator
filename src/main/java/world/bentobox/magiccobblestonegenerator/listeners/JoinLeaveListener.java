//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * This listener loads player islands in cache when they login.
 */
public class JoinLeaveListener implements Listener
{
	/**
	 * @param addon - addon
	 */
	public JoinLeaveListener(StoneGeneratorAddon addon)
	{
		this.addon = addon;
	}


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		// Load player into cache
		this.addon.getAddonManager().loadUserIslands(e.getPlayer().getUniqueId());
	}


	/**
	 * stores addon instance
	 */
	private final StoneGeneratorAddon addon;
}