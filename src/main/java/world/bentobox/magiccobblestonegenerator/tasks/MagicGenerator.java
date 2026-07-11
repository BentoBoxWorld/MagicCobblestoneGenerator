package world.bentobox.magiccobblestonegenerator.tasks;


import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.events.GeneratorTreasureDropEvent;
import world.bentobox.magiccobblestonegenerator.utils.CustomBlocks;
import world.bentobox.magiccobblestonegenerator.utils.Why;


/**
 * This class process given block transforming to random object from input configuration.
 */
public class MagicGenerator
{
    /**
     * Default constructor. Inits Generator once.
     *
     * @param addon Magic Cobblestone Generator addon.
     */
    public MagicGenerator(StoneGeneratorAddon addon)
    {
        this.addon = addon;
    }


    /**
     * This method tries to replace block from chance map and returns if it was successful.
     *
     * @param generatorTier Object that contains all possible chances.
     * @param location Location of the block that need to be replaced.
     * @return the picked block ID (vanilla material name or provider-prefixed custom block ID) or null.
     */
    public @Nullable String processBlockReplacement(@Nullable GeneratorTierObject generatorTier, Location location)
    {
        return this.processBlockReplacement(generatorTier, location, null);
    }


    /**
     * This method tries to replace block from chance map and returns if it was successful.
     *
     * @param generatorTier Object that contains all possible chances.
     * @param location Location of the block that need to be replaced.
     * @param island Island on which the block is processed, or null if unknown. Used to provide context for the
     *     {@link GeneratorTreasureDropEvent}.
     * @return the picked block ID (vanilla material name or provider-prefixed custom block ID) or null.
     */
    public @Nullable String processBlockReplacement(@Nullable GeneratorTierObject generatorTier, Location location,
        @Nullable Island island)
    {
        if (generatorTier == null)
        {
            Why.report(location, "Missing Generator Tier");
            // Check if generator exists.
            return null;
        }

        // Check if the block is within the generator's global height range
        int blockY = location.getBlockY();
        if (blockY < generatorTier.getMinHeight() || blockY > generatorTier.getMaxHeight())
        {
            Why.report(location, "Block outside global height range: " + blockY + 
                " (min: " + generatorTier.getMinHeight() + ", max: " + generatorTier.getMaxHeight() + ")");
            return null;
        }

        TreeMap<Double, String> chanceMap = generatorTier.getBlockChanceMap();

        if (chanceMap.isEmpty())
        {
            Why.report(location, "Missing Block Chances in " + generatorTier.getUniqueId());

            // Check if any block has a chance to spawn
            return null;
        }

        String newBlockId = this.getMaterialFromMap(chanceMap);

        if (newBlockId == null)
        {
            Why.report(location, "Cannot parse material from ChanceMap in " + generatorTier.getUniqueId());

            // Check if a material was found
            return null;
        }

        // Check if this material has specific height restrictions
        int[] materialHeightRange = generatorTier.getMaterialHeightRange(newBlockId);
        if (materialHeightRange != null)
        {
            int materialMinHeight = materialHeightRange[0];
            int materialMaxHeight = materialHeightRange[1];

            if (blockY < materialMinHeight || blockY > materialMaxHeight)
            {
                Why.report(location, "Material " + newBlockId + " outside its specific height range: " + blockY +
                    " (min: " + materialMinHeight + ", max: " + materialMaxHeight + ")");

                // Try to find another material that can be generated at this height
                String alternativeMaterial = findMaterialForHeight(generatorTier, blockY);
                if (alternativeMaterial != null)
                {
                    Why.report(location, "Using alternative material " + alternativeMaterial + " for height " + blockY);
                    newBlockId = alternativeMaterial;
                }
                else
                {
                    // If no suitable material found, don't replace the block
                    return null;
                }
            }
        }

        if (CustomBlocks.isCustom(newBlockId))
        {
            if (!CustomBlocks.canPlace(this.addon, newBlockId))
            {
                Why.report(location, "Custom block " + newBlockId + " cannot be placed (plugin missing, ID not " +
                    "registered, or BentoBox hook lacks placement support) in " + generatorTier.getUniqueId());
                return null;
            }
        }
        else if (CustomBlocks.matchVanilla(newBlockId) == null)
        {
            Why.report(location, "Unknown material " + newBlockId + " in " + generatorTier.getUniqueId());
            return null;
        }

        Why.report(location, "Replace with " + newBlockId + " by " + generatorTier.getUniqueId());

        if (generatorTier.getMaxTreasureAmount() > 0 &&
            generatorTier.getTreasureChance() > 0 &&
            !generatorTier.getTreasureItemChanceMap().isEmpty())
        {
            // Random check on getting treasure.
            if (this.random.nextDouble() <= generatorTier.getTreasureChance())
            {
                // Use the same variables for treasures.
                TreeMap<Double, ItemStack> treasureMap = generatorTier.getTreasureItemChanceMap();
                ItemStack itemStack = this.getMaterialFromMap(treasureMap);

                // Double check, in general it should always be a material.
                if (itemStack != null)
                {
                    ItemStack drop = itemStack.clone();
                    drop.setAmount(this.random.nextInt(generatorTier.getMaxTreasureAmount() + 1) + 1);

                    // Fire a cancellable event so other plugins can intercept, modify or veto the treasure drop.
                    GeneratorTreasureDropEvent treasureEvent = new GeneratorTreasureDropEvent(generatorTier,
                        island == null ? null : island.getUniqueId(),
                        location,
                        drop);
                    Bukkit.getPluginManager().callEvent(treasureEvent);

                    if (!treasureEvent.isCancelled() && treasureEvent.getItemStack() != null)
                    {
                        ItemStack finalDrop = treasureEvent.getItemStack();
                        Location dropLocation = treasureEvent.getLocation();

                        // A listener may have nulled the location (or its world); guard against it so we do
                        // not throw and break block generation.
                        if (dropLocation == null || dropLocation.getWorld() == null)
                        {
                            Why.report(location, "Treasure drop skipped: listener supplied an invalid drop location for " +
                                generatorTier.getUniqueId());
                        }
                        else
                        {
                            Why.report(location, "Dropping treasure " + finalDrop + " by " + generatorTier.getUniqueId());

                            // drop item naturally in the location of the block
                            dropLocation.getWorld().dropItemNaturally(dropLocation, finalDrop);
                        }
                    }
                    else
                    {
                        Why.report(location, "Treasure drop cancelled by " + generatorTier.getUniqueId());
                    }
                }
            }
        }

        return newBlockId;
    }

    /**
     * Finds a block ID from the generator's block chance map that can be generated at the specified height.
     *
     * @param generatorTier The generator tier object containing the material configurations
     * @param blockY The Y coordinate to check against
     * @return A block ID that can be generated at the specified height, or null if none found
     */
    private String findMaterialForHeight(GeneratorTierObject generatorTier, int blockY)
    {
        TreeMap<Double, String> chanceMap = generatorTier.getBlockChanceMap();

        for (String blockId : chanceMap.values())
        {
            int[] heightRange = generatorTier.getMaterialHeightRange(blockId);

            // If this material has no specific height range, it can be generated anywhere within the generator's global range
            if (heightRange == null)
            {
                return blockId;
            }

            // Check if the block Y is within this material's height range
            if (blockY >= heightRange[0] && blockY <= heightRange[1])
            {
                return blockId;
            }
        }

        return null;
    }


    /**
     * This method returns a random material from given tree map.
     *
     * @param chanceMap Map that contains all objects with their chance to drop.
     * @return <T> from map or null.
     */
    private <T> T getMaterialFromMap(TreeMap<Double, T> chanceMap)
    {
        if (chanceMap.isEmpty())
        {
            return null;
        }

        if (chanceMap.size() == 1)
        {
            // no needs to calculate. It is our material.
            return chanceMap.get(chanceMap.firstKey());
        }
        else
        {
            double rand = this.random.nextDouble() * chanceMap.lastKey();
            return chanceMap.ceilingEntry(rand).getValue();
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds stone generator addon object.
     */
    private final StoneGeneratorAddon addon;

    /**
     * Random for generator
     */
    private final Random random = new Random(System.currentTimeMillis());
}
