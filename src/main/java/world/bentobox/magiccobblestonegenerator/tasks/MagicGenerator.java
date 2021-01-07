package world.bentobox.magiccobblestonegenerator.tasks;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.Random;
import java.util.TreeMap;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
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
     * @return the replaced material or null.
     */
    public @Nullable Material processBlockReplacement(@Nullable GeneratorTierObject generatorTier, Location location)
    {
        if (generatorTier == null)
        {
            Why.report(location, "Missing Generator Tier");
            // Check if generator exists.
            return null;
        }

        TreeMap<Double, Material> chanceMap = generatorTier.getBlockChanceMap();

        if (chanceMap.isEmpty())
        {
            Why.report(location, "Missing Block Chances in " + generatorTier.getUniqueId());

            // Check if any block has a chance to spawn
            return null;
        }

        Material newMaterial = this.getMaterialFromMap(chanceMap);

        if (newMaterial == null)
        {
            Why.report(location, "Cannot parse material from ChanceMap in " + generatorTier.getUniqueId());

            // Check if a material was found
            return null;
        }

        Why.report(location, "Replace with " + newMaterial + " by " + generatorTier.getUniqueId());

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

                    Why.report(location, "Dropping treasure " + drop.toString() + " by " + generatorTier.getUniqueId());

                    // drop item naturally in the location of the block
                    location.getWorld().dropItemNaturally(location, drop);
                }
            }
        }

        return newMaterial;
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
