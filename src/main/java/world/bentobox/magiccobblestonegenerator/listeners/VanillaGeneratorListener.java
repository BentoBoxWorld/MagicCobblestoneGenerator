//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Optional;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.utils.Why;


/**
 * Vanilla Generator Listener. This class contains listener that process Generator options. This listener uses only
 * vanilla minecraft generator and replaces end block after a tick.
 */
public class VanillaGeneratorListener extends GeneratorListener
{
    /**
     * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
     *
     * @param addon of type StoneGeneratorAddon
     */
    public VanillaGeneratorListener(StoneGeneratorAddon addon)
    {
        super(addon);
    }


    /**
     * Handles magic generation when a block is formed
     *
     * @param event - event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event)
    {
        Block eventSourceBlock = event.getBlock();

        if (!eventSourceBlock.isLiquid())
        {
            // We are interested only in blocks that were created from lava or water
            return;
        }

        if (!this.addon.getAddonManager().canOperateInWorld(eventSourceBlock.getWorld()))
        {
            // If not operating in world, then return as fast as possible
            return;
        }

        Optional<Island> islandOptional = this.addon.getIslands().getIslandAt(eventSourceBlock.getLocation());

        if (!islandOptional.isPresent())
        {
            // If not operating in non-island regions.
            return;
        }

        Island island = islandOptional.get();

        if (!island.isAllowed(StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR))
        {
            // Currently addon is not working outside island protection ranges.
            // New flag is required for enabling this feature.
            Why.report(island, eventSourceBlock.getLocation(), "MCG is disabled by MAGIC_COBBLESTONE_GENERATOR flag!");
            return;
        }

        if (!this.isSomeoneOnline(island))
        {
            // Offline generation is not enabled and all island members are offline.
            // TODO: probably need another option to disable generation if members are not on the island :)
            Why.report(island, eventSourceBlock.getLocation(), "All island members are offline!");
            return;
        }

        if (!this.isInRangeToGenerate(island, eventSourceBlock))
        {
            // Check if any island member is at generator range.
            Why.report(island, eventSourceBlock.getLocation(), "No players in range!");
            return;
        }

        // All edge cases are processed. Not deal with block generation.
        // Use new block state to detect which generator to use.

        if (event.getNewState().getType() == Material.COBBLESTONE)
        {
            Material material = this.generateCobblestoneReplacement(island, eventSourceBlock.getLocation());

            if (material != null)
            {
                // Replace new state with a proper material.
                event.getNewState().setType(material);
            }
        }
        else if (event.getNewState().getType() == Material.STONE)
        {
            Material material = this.generateStoneReplacement(island, eventSourceBlock.getLocation());

            if (material != null)
            {
                // Replace new state with a proper material.
                event.getNewState().setType(material);
            }
        }
        else if (event.getNewState().getType() == Material.BASALT)
        {
            Material material = this.generateBasaltReplacement(island, eventSourceBlock.getLocation());

            if (material != null)
            {
                // Replace new state with a proper material.
                event.getNewState().setType(material);
            }
        }
    }
}
