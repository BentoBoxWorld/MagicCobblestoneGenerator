package world.bentobox.magiccobblestonegenerator.tasks;


import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * This class process given block transforming to random object from input configuration.
 */
public class MagicGenerator
{
    /**
     * Default constructor. Inits Generator once.
     * @param addon Magic Cobblestone Generator addon.
     */
    public MagicGenerator(StoneGeneratorAddon addon)
    {
        this.addon = addon;
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isCobblestoneReplacementGenerated(Block block)
    {
        TreeMap<Double, Material> chanceMap =
            (TreeMap<Double, Material>) this.addon.getManager().getMaterialChanceMap(
                this.addon.getManager().getIslandLevel(block.getLocation()), block.getWorld());

        if (chanceMap.isEmpty())
        {
            return false;
        }

        Material newMaterial;

        if (chanceMap.size() == 1)
        {
            // no needs to calculate. It is our material.
            newMaterial = chanceMap.get(chanceMap.firstKey());
        }
        else
        {
            double rand = this.random.nextDouble() * chanceMap.lastKey();
            newMaterial = chanceMap.ceilingEntry(rand).getValue();
        }

        // ask config if physics should be used
        block.setType(newMaterial, this.addon.getSettings().usePhysics());

        return true;
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isStoneReplacementGenerated(Block block)
    {
        TreeMap<Double, Material> chanceMap =
            (TreeMap<Double, Material>) this.addon.getManager().getMaterialChanceMap(
                this.addon.getManager().getIslandLevel(block.getLocation()), block.getWorld());

        if (chanceMap.isEmpty())
        {
            return false;
        }

        Material newMaterial;

        if (chanceMap.size() == 1)
        {
            // no needs to calculate. It is our material.
            newMaterial = chanceMap.get(chanceMap.firstKey());
        }
        else
        {
            double rand = this.random.nextDouble() * chanceMap.lastKey();
            newMaterial = chanceMap.ceilingEntry(rand).getValue();
        }

        // ask config if physics should be used
        block.setType(newMaterial, this.addon.getSettings().usePhysics());

        return true;
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isBasaltReplacementGenerated(Block block)
    {
        TreeMap<Double, Material> chanceMap =
            (TreeMap<Double, Material>) this.addon.getManager().getMaterialChanceMap(
                this.addon.getManager().getIslandLevel(block.getLocation()), block.getWorld());

        if (chanceMap.isEmpty())
        {
            return false;
        }

        Material newMaterial;

        if (chanceMap.size() == 1)
        {
            // no needs to calculate. It is our material.
            newMaterial = chanceMap.get(chanceMap.firstKey());
        }
        else
        {
            double rand = this.random.nextDouble() * chanceMap.lastKey();
            newMaterial = chanceMap.ceilingEntry(rand).getValue();
        }

        // ask config if physics should be used
        block.setType(newMaterial, this.addon.getSettings().usePhysics());

        return true;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds stone generator addon object.
     */
    private StoneGeneratorAddon addon;


    /**
     * Random for generator
     */
    private final Random random = new Random(System.currentTimeMillis());
}
