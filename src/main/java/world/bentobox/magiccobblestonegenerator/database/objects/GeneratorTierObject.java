//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.objects;


import com.google.gson.annotations.Expose;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * This class manages generator tier storing.
 */
@Table(name = "Generator")
public class GeneratorTierObject implements DataObject
{
	/**
	 * Constructor GeneratorTierObject creates a new GeneratorTierObject instance.
	 */
	public GeneratorTierObject()
	{
		// Empty constructor
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * @return the uniqueId
	 */
	@Override
	public String getUniqueId()
	{
		return this.uniqueId;
	}


	/**
	 * @param uniqueId - unique ID the uniqueId to set
	 */
	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Minecraft has 3 generator types:
	 *  - cobblestone
	 *  - stone
	 *  - basalt
	 */
	public enum GeneratorType
	{
		COBBLESTONE,
		STONE,
		BASALT
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Unique Id of the generator tier.
	 */
	@Expose
	private String uniqueId;

	/**
	 * Stores display name for generator
	 */
	@Expose
	private String friendlyName;

	/**
	 * Stores display icon for current generator tier.
	 */
	@Expose
	private ItemStack generatorIcon = new ItemStack(Material.STONE);

	/**
	 * Stores type of the generator for this tier.
	 */
	@Expose
	private GeneratorType generatorType = GeneratorType.COBBLESTONE;

	// Requirements for unlocking.

	/**
	 * Stores minimal island level for this generator to work.
	 */
	@Expose
	private long requiredMinIslandLevel = 0;

	/**
	 * Stores biomes where this generator operates.
	 */
	@Expose
	private Set<Biome> requiredBiomes = Collections.emptySet();

	/**
	 * List of permissions for this generator to work.
	 */
	@Expose
	private Set<String> requiredPermissions = Collections.emptySet();

	/**
	 * Cost to do buy current generator.
	 */
	@Expose
	private double generatorTierCost = 0.0;

	/**
	 * Ability to disable generator tier for everyone.
	 */
	@Expose
	private boolean deployed = true;

	// Rewards section

	/**
	 * Map that stores different blocks and their chance for generating.
	 */
	@Expose
	private Map<Double, Material> blockChanceMap = Collections.emptyMap();

	/**
	 * Map that stores different extra treasures and their change for being dropped.
	 */
	@Expose
	private Map<Double, Material> treasureChanceMap = Collections.emptyMap();

	/**
	 * This stores a value of dropping treasure from treasure chance map.
	 */
	@Expose
	private double treasureChance = 0.0;
}

