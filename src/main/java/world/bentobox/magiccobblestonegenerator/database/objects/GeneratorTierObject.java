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


	/**
	 * Method GeneratorTierObject#getFriendlyName returns the friendlyName of this object.
	 *
	 * @return the friendlyName (type String) of this object.
	 */
	public String getFriendlyName()
	{
		return friendlyName;
	}


	/**
	 * Method GeneratorTierObject#setFriendlyName sets new value for the friendlyName of this object.
	 * @param friendlyName new value for this object.
	 *
	 */
	public void setFriendlyName(String friendlyName)
	{
		this.friendlyName = friendlyName;
	}


	/**
	 * Method GeneratorTierObject#getGeneratorIcon returns the generatorIcon of this object.
	 *
	 * @return the generatorIcon (type ItemStack) of this object.
	 */
	public ItemStack getGeneratorIcon()
	{
		return generatorIcon;
	}


	/**
	 * Method GeneratorTierObject#setGeneratorIcon sets new value for the generatorIcon of this object.
	 * @param generatorIcon new value for this object.
	 *
	 */
	public void setGeneratorIcon(ItemStack generatorIcon)
	{
		this.generatorIcon = generatorIcon;
	}


	/**
	 * Method GeneratorTierObject#getGeneratorType returns the generatorType of this object.
	 *
	 * @return the generatorType (type GeneratorType) of this object.
	 */
	public GeneratorType getGeneratorType()
	{
		return generatorType;
	}


	/**
	 * Method GeneratorTierObject#setGeneratorType sets new value for the generatorType of this object.
	 * @param generatorType new value for this object.
	 *
	 */
	public void setGeneratorType(GeneratorType generatorType)
	{
		this.generatorType = generatorType;
	}


	/**
	 * Method GeneratorTierObject#getRequiredMinIslandLevel returns the requiredMinIslandLevel of this object.
	 *
	 * @return the requiredMinIslandLevel (type long) of this object.
	 */
	public long getRequiredMinIslandLevel()
	{
		return requiredMinIslandLevel;
	}


	/**
	 * Method GeneratorTierObject#setRequiredMinIslandLevel sets new value for the requiredMinIslandLevel of this object.
	 * @param requiredMinIslandLevel new value for this object.
	 *
	 */
	public void setRequiredMinIslandLevel(long requiredMinIslandLevel)
	{
		this.requiredMinIslandLevel = requiredMinIslandLevel;
	}


	/**
	 * Method GeneratorTierObject#getRequiredBiomes returns the requiredBiomes of this object.
	 *
	 * @return the requiredBiomes (type Set<Biome>) of this object.
	 */
	public Set<Biome> getRequiredBiomes()
	{
		return requiredBiomes;
	}


	/**
	 * Method GeneratorTierObject#setRequiredBiomes sets new value for the requiredBiomes of this object.
	 * @param requiredBiomes new value for this object.
	 *
	 */
	public void setRequiredBiomes(Set<Biome> requiredBiomes)
	{
		this.requiredBiomes = requiredBiomes;
	}


	/**
	 * Method GeneratorTierObject#getRequiredPermissions returns the requiredPermissions of this object.
	 *
	 * @return the requiredPermissions (type Set<String>) of this object.
	 */
	public Set<String> getRequiredPermissions()
	{
		return requiredPermissions;
	}


	/**
	 * Method GeneratorTierObject#setRequiredPermissions sets new value for the requiredPermissions of this object.
	 * @param requiredPermissions new value for this object.
	 *
	 */
	public void setRequiredPermissions(Set<String> requiredPermissions)
	{
		this.requiredPermissions = requiredPermissions;
	}


	/**
	 * Method GeneratorTierObject#getGeneratorTierCost returns the generatorTierCost of this object.
	 *
	 * @return the generatorTierCost (type double) of this object.
	 */
	public double getGeneratorTierCost()
	{
		return generatorTierCost;
	}


	/**
	 * Method GeneratorTierObject#setGeneratorTierCost sets new value for the generatorTierCost of this object.
	 * @param generatorTierCost new value for this object.
	 *
	 */
	public void setGeneratorTierCost(double generatorTierCost)
	{
		this.generatorTierCost = generatorTierCost;
	}


	/**
	 * Method GeneratorTierObject#isDeployed returns the deployed of this object.
	 *
	 * @return the deployed (type boolean) of this object.
	 */
	public boolean isDeployed()
	{
		return deployed;
	}


	/**
	 * Method GeneratorTierObject#setDeployed sets new value for the deployed of this object.
	 * @param deployed new value for this object.
	 *
	 */
	public void setDeployed(boolean deployed)
	{
		this.deployed = deployed;
	}


	/**
	 * Method GeneratorTierObject#getBlockChanceMap returns the blockChanceMap of this object.
	 *
	 * @return the blockChanceMap (type Map<Double, Material>) of this object.
	 */
	public Map<Double, Material> getBlockChanceMap()
	{
		return blockChanceMap;
	}


	/**
	 * Method GeneratorTierObject#setBlockChanceMap sets new value for the blockChanceMap of this object.
	 * @param blockChanceMap new value for this object.
	 *
	 */
	public void setBlockChanceMap(Map<Double, Material> blockChanceMap)
	{
		this.blockChanceMap = blockChanceMap;
	}


	/**
	 * Method GeneratorTierObject#getTreasureChanceMap returns the treasureChanceMap of this object.
	 *
	 * @return the treasureChanceMap (type Map<Double, Material>) of this object.
	 */
	public Map<Double, Material> getTreasureChanceMap()
	{
		return treasureChanceMap;
	}


	/**
	 * Method GeneratorTierObject#setTreasureChanceMap sets new value for the treasureChanceMap of this object.
	 * @param treasureChanceMap new value for this object.
	 *
	 */
	public void setTreasureChanceMap(Map<Double, Material> treasureChanceMap)
	{
		this.treasureChanceMap = treasureChanceMap;
	}


	/**
	 * Method GeneratorTierObject#getTreasureChance returns the treasureChance of this object.
	 *
	 * @return the treasureChance (type double) of this object.
	 */
	public double getTreasureChance()
	{
		return treasureChance;
	}


	/**
	 * Method GeneratorTierObject#setTreasureChance sets new value for the treasureChance of this object.
	 * @param treasureChance new value for this object.
	 *
	 */
	public void setTreasureChance(double treasureChance)
	{
		this.treasureChance = treasureChance;
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

