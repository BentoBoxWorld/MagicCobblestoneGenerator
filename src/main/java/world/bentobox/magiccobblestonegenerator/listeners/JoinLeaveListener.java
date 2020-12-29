//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import world.bentobox.bentobox.api.events.island.IslandEvent;
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


    /**
     * This method handles player join event. When player joins it loads all its islands into local cache.
     *
     * @param event PlayerJoinEvent instance.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Load player into cache
        this.addon.getAddonManager().loadUserIslands(event.getPlayer().getUniqueId());
    }


    /**
     * This method handles Island Created, Resetted and Registered events.
     *
     * @param event Event that must be handled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewIsland(IslandEvent event)
    {
        if (event.getReason().equals(IslandEvent.Reason.CREATED) ||
            event.getReason().equals(IslandEvent.Reason.RESETTED) ||
            event.getReason().equals(IslandEvent.Reason.REGISTERED))
        {
            this.addon.getAddonManager().validateIslandData(event.getIsland());
        }
    }


    /**
     * This method handles island deletion. On island deletion it should remove generator data too.
     *
     * @param event IslandDeletedEvent instance.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(IslandEvent.IslandDeleteEvent event)
    {
        this.addon.getAddonManager().wipeGeneratorData(event.getIsland().getUniqueId());
    }


    /**
     * stores addon instance
     */
    private final StoneGeneratorAddon addon;
}