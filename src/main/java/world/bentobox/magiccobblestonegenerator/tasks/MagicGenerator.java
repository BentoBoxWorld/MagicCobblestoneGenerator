package world.bentobox.magiccobblestonegenerator.tasks;


import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.function.ToIntFunction;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


public class MagicGenerator
{
	public MagicGenerator(StoneGeneratorAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * This method tries to replace given block with new object and returns true if it was successful.
	 * @param block Block that should be replaced.
	 * @return <code>true</code> if replacing block was successful.
	 */
	public boolean isReplacementGenerated(Block block)
	{
		return this.isReplacementGenerated(block, false);
	}


	/**
	 * This method tries to replace given block with new object and returns true if it was successful.
	 * @param block Block that should be replaced.
	 * @param improved Boolean that indicate if current process used stone generation.
	 * @return <code>true</code> if replacing block was successful.
	 */
	public boolean isReplacementGenerated(Block block, boolean improved)
	{
		Map<Material, Integer> chanceMap = this.addon.getManager().getMaterialChanceMap(
			this.addon.getManager().getIslandLevel(block.getLocation()),
			block.getWorld());

		if (chanceMap.isEmpty())
		{
			return false;
		}

		Material newMaterial;

		if (chanceMap.size() == 1)
		{
			// no needs to calculate. It is our material.
			newMaterial = chanceMap.keySet().iterator().next();
		}
		else
		{
			// Fun Begins. Calculate total sum of all chances.
			int normalizedValue = chanceMap.values().stream().mapToInt(chance -> chance).sum();

			// Put all materials in list and order it by element chance and name.
			List<Material> materialList = new ArrayList<>(chanceMap.keySet());
			materialList.sort(Comparator.comparingInt((ToIntFunction<Material>) chanceMap::get).thenComparing(material -> material));

			// Get random index +1 to avoid issues with getting 0 index.
			int index = new Random().nextInt(normalizedValue) + 1;
			int sum = 0;
			int i = 0;

			// Find element from chance range.
			while (sum < index)
			{
				sum += chanceMap.get(materialList.get(i++));
			}

			// Return Material that is in this value range.
			newMaterial = materialList.get(i - 1);
		}

		try
		{
			// Now try to replace block with given material.

			block.setType(newMaterial);
			return true;
		}
		catch (Exception e)
		{
			this.addon.logError("Something went wrong when change block material in Magic Cobblestone Generator.");
		}

		return false;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable holds stone generator addon object.
	 */
	private StoneGeneratorAddon addon;
}
