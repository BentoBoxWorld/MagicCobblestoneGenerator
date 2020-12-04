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
@Table(name = "GeneratorTier")
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
		return generatorIcon.clone();
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
	public TreeMap<Double, Material> getBlockChanceMap()
	{
		return blockChanceMap;
	}


	/**
	 * Method GeneratorTierObject#setBlockChanceMap sets new value for the blockChanceMap of this object.
	 * @param blockChanceMap new value for this object.
	 *
	 */
	public void setBlockChanceMap(TreeMap<Double, Material> blockChanceMap)
	{
		this.blockChanceMap = blockChanceMap;
	}


	/**
	 * Method GeneratorTierObject#getTreasureChanceMap returns the treasureChanceMap of this object.
	 *
	 * @return the treasureChanceMap (type Map<Double, Material>) of this object.
	 */
	public TreeMap<Double, Material> getTreasureChanceMap()
	{
		return treasureChanceMap;
	}


	/**
	 * Method GeneratorTierObject#setTreasureChanceMap sets new value for the treasureChanceMap of this object.
	 * @param treasureChanceMap new value for this object.
	 *
	 */
	public void setTreasureChanceMap(TreeMap<Double, Material> treasureChanceMap)
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
		this.treasureChance = Math.max(0, treasureChance);
	}


	/**
	 * Method GeneratorTierObject#getDescription returns the description of this object.
	 *
	 * @return the description (type List<String>) of this object.
	 */
	public List<String> getDescription()
	{
		return description;
	}


	/**
	 * Method GeneratorTierObject#setDescription sets new value for the description of this object.
	 * @param description new value for this object.
	 *
	 */
	public void setDescription(List<String> description)
	{
		this.description = description;
	}


	/**
	 * Method GeneratorTierObject#getMaxTreasureAmount returns the maxTreasureAmount of this object.
	 *
	 * @return the maxTreasureAmount (type int) of this object.
	 */
	public int getMaxTreasureAmount()
	{
		return maxTreasureAmount;
	}


	/**
	 * Method GeneratorTierObject#setMaxTreasureAmount sets new value for the maxTreasureAmount of this object.
	 * @param maxTreasureAmount new value for this object.
	 *
	 */
	public void setMaxTreasureAmount(int maxTreasureAmount)
	{
		// Non-negative values only.
		this.maxTreasureAmount = Math.max(0, maxTreasureAmount);
	}


	/**
	 * Method GeneratorTierObject#isDefaultGenerator returns the defaultGenerator of this object.
	 *
	 * @return the defaultGenerator (type boolean) of this object.
	 */
	public boolean isDefaultGenerator()
	{
		return defaultGenerator;
	}


	/**
	 * Method GeneratorTierObject#setDefaultGenerator sets new value for the defaultGenerator of this object.
	 * @param defaultGenerator new value for this object.
	 *
	 */
	public void setDefaultGenerator(boolean defaultGenerator)
	{
		this.defaultGenerator = defaultGenerator;
	}


	/**
	 * Method GeneratorTierObject#getPriority returns the priority of this object.
	 *
	 * @return the priority (type int) of this object.
	 */
	public int getPriority()
	{
		return priority;
	}


	/**
	 * Method GeneratorTierObject#setPriority sets new value for the priority of this object.
	 * @param priority new value for this object.
	 *
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}


	/**
	 * This method returns the activationCost value.
	 * @return the value of activationCost.
	 */
	public double getActivationCost()
	{
		return activationCost;
	}


	/**
	 * This method sets the activationCost value.
	 * @param activationCost the activationCost new value.
	 *
	 */
	public void setActivationCost(double activationCost)
	{
		this.activationCost = activationCost;
	}


	/**
	 * Gets treasure item chance map.
	 *
	 * @return the treasure item chance map
	 */
	public TreeMap<Double, ItemStack> getTreasureItemChanceMap()
	{
		return treasureItemChanceMap;
	}


	/**
	 * Sets treasure item chance map.
	 *
	 * @param treasureItemChanceMap the treasure item chance map
	 */
	public void setTreasureItemChanceMap(TreeMap<Double, ItemStack> treasureItemChanceMap)
	{
		this.treasureItemChanceMap = treasureItemChanceMap;
	}


	/**
	 * Gets locked icon.
	 *
	 * @return the locked icon
	 */
	public ItemStack getLockedIcon()
	{
		return lockedIcon.clone();
	}


	/**
	 * Sets locked icon.
	 *
	 * @param lockedIcon the locked icon
	 */
	public void setLockedIcon(ItemStack lockedIcon)
	{
		this.lockedIcon = lockedIcon;
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * Creates and returns a copy of this object.  The precise meaning of "copy" may depend on the class of the object.
	 * @return a clone of this instance.
	 */
	@Override
	public GeneratorTierObject clone()
	{
		GeneratorTierObject clone = new GeneratorTierObject();

		clone.setUniqueId(this.uniqueId);
		clone.setFriendlyName(this.friendlyName);
		clone.setGeneratorIcon(this.generatorIcon.clone());
		clone.setLockedIcon(this.lockedIcon.clone());
		clone.setDescription(new ArrayList<>(this.description));
		clone.setGeneratorType(this.generatorType);
		clone.setDefaultGenerator(this.defaultGenerator);
		clone.setPriority(this.priority);
		clone.setRequiredMinIslandLevel(this.requiredMinIslandLevel);
		clone.setRequiredBiomes(new HashSet<>(this.requiredBiomes));
		clone.setRequiredPermissions(new HashSet<>(this.requiredPermissions));
		clone.setGeneratorTierCost(this.generatorTierCost);
		clone.setActivationCost(this.activationCost);
		clone.setDeployed(this.deployed);
		clone.setBlockChanceMap(new TreeMap<>(this.blockChanceMap));

		if (treasureChanceMap != null)
		{
			clone.setTreasureChanceMap(new TreeMap<>(this.treasureChanceMap));
		}
		else
		{
			clone.setTreasureChanceMap(null);
		}

		// Cloning must be done like this
		TreeMap<Double, ItemStack> cloneMap = new TreeMap<>();
		this.treasureItemChanceMap.forEach((chance, item) -> {
			cloneMap.put(chance, item.clone());
		});
		clone.setTreasureItemChanceMap(cloneMap);

		clone.setTreasureChance(this.treasureChance);
		clone.setMaxTreasureAmount(this.maxTreasureAmount);

		return clone;
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
		COBBLESTONE(1),
		STONE(2),
		BASALT(4),
		COBBLESTONE_OR_STONE(3),
		BASALT_OR_COBBLESTONE(5),
		BASALT_OR_STONE(6),
		ANY(7);

		GeneratorType(int id)
		{
			this.id = id;
		}


		/**
		 * This method returns if given generator type is included by current generator.
		 * @param type Generator type that must be checked. Most likely it is just basic
		 *             basalt, cobblestone or stone.
		 * @return {@code true} if current generator is compatible with given generator
		 * type, {@code false} otherwise.
		 */
		public boolean includes(GeneratorType type)
		{
			return (this.id & type.id) != 0;
		}


		/**
		 * ID of the generator.
		 */
		private final int id;
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
	 * Stores description for generator.
	 */
	@Expose
	private List<String> description = Collections.emptyList();

	/**
	 * Stores display icon for current generator tier.
	 */
	@Expose
	private ItemStack generatorIcon = new ItemStack(Material.STONE);

	/**
	 * Stores display icon for current generator tier.
	 */
	@Expose
	private ItemStack lockedIcon = new ItemStack(Material.BARRIER);

	/**
	 * Stores type of the generator for this tier.
	 */
	@Expose
	private GeneratorType generatorType = GeneratorType.COBBLESTONE;

	/**
	 * Indicates if current generator is default.
	 * Only one generator per type can be default.
	 */
	@Expose
	private boolean defaultGenerator = false;

	/**
	 * Stores generator priority. Larger priority means generator will be used over other generators.
	 */
	@Expose
	private int priority = 0;

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
	 * Cost to do activate current generator.
	 */
	@Expose
	private double activationCost = 0.0;

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
	private TreeMap<Double, Material> blockChanceMap = new TreeMap<>();

	/**
	 * Map that stores different extra treasures and their change for being dropped.
	 */
	@Deprecated
	@Expose
	private TreeMap<Double, Material> treasureChanceMap = new TreeMap<>();

	/**
	 * Map that stores different extra treasures and their change for being dropped.
	 */
	@Expose
	private TreeMap<Double, ItemStack> treasureItemChanceMap = new TreeMap<>();

	/**
	 * This stores a value of dropping treasure from treasure chance map.
	 */
	@Expose
	private double treasureChance = 0.0;

	/**
	 * This variable stores max amount of treasure item to be dropped. From 1... till it.
	 */
	@Expose
	private int maxTreasureAmount = 1;
}

