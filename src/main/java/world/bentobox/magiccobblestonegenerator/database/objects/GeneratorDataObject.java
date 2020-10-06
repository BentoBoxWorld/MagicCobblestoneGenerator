//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.objects;


import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * This object stores generator data per island.
 * In short, it allows to easily access active and unlocked generator tiers for island.
 */
@Table(name = "GeneratorData")
public class GeneratorDataObject implements DataObject
{
	/**
	 * Default constructor.
	 */
	public GeneratorDataObject()
	{
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
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
	 * Method GeneratorDataObject#getUnlockedTiers returns the unlockedTiers of this object.
	 *
	 * @return the unlockedTiers (type Set<String>) of this object.
	 */
	public Set<String> getUnlockedTiers()
	{
		return unlockedTiers;
	}


	/**
	 * Method GeneratorDataObject#setUnlockedTiers sets new value for the unlockedTiers of this object.
	 * @param unlockedTiers new value for this object.
	 *
	 */
	public void setUnlockedTiers(Set<String> unlockedTiers)
	{
		this.unlockedTiers = unlockedTiers;
	}


	/**
	 * Method GeneratorDataObject#getActiveGeneratorList returns the activeGeneratorList of this object.
	 *
	 * @return the activeGeneratorList (type Set<String>) of this object.
	 */
	public Set<String> getActiveGeneratorList()
	{
		return activeGeneratorList;
	}


	/**
	 * Method GeneratorDataObject#setActiveGeneratorList sets new value for the activeGeneratorList of this object.
	 * @param activeGeneratorList new value for this object.
	 *
	 */
	public void setActiveGeneratorList(Set<String> activeGeneratorList)
	{
		this.activeGeneratorList = activeGeneratorList;
	}


	/**
	 * This method returns the maxGeneratorCount value.
	 * @return the value of maxGeneratorCount.
	 */
	public int getMaxGeneratorCount()
	{
		return maxGeneratorCount;
	}


	/**
	 * This method sets the maxGeneratorCount value.
	 * @param maxGeneratorCount the maxGeneratorCount new value.
	 *
	 */
	public void setMaxGeneratorCount(int maxGeneratorCount)
	{
		this.maxGeneratorCount = maxGeneratorCount;
	}


	/**
	 * This method returns the purchasedTiers value.
	 * @return the value of purchasedTiers.
	 */
	public Set<String> getPurchasedTiers()
	{
		return purchasedTiers;
	}


	/**
	 * This method sets the purchasedTiers value.
	 * @param purchasedTiers the purchasedTiers new value.
	 *
	 */
	public void setPurchasedTiers(Set<String> purchasedTiers)
	{
		this.purchasedTiers = purchasedTiers;
	}


	/**
	 * This method returns the purchasedActiveGeneratorCount value.
	 * @return the value of purchasedActiveGeneratorCount.
	 */
	public int getPurchasedActiveGeneratorCount()
	{
		return purchasedActiveGeneratorCount;
	}


	/**
	 * This method sets the purchasedGeneratorCount value.
	 * @param purchasedActiveGeneratorCount the purchasedGeneratorCount new value.
	 *
	 */
	public void setPurchasedActiveGeneratorCount(int purchasedActiveGeneratorCount)
	{
		this.purchasedActiveGeneratorCount = purchasedActiveGeneratorCount;
	}

	/**
	 * This method sets the workingRange value.
	 * @param workingRange the workingRange new value.
	 *
	 */
	public void setWorkingRange(int workingRange)
	{
		this.workingRange = workingRange;
	}


	/**
	 * This method returns the workingRange value.
	 * @return the value of workingRange.
	 */
	public int getWorkingRange()
	{
		return workingRange;
	}


	/**
	 * Gets owner bundle.
	 *
	 * @return the owner bundle
	 */
	public @Nullable String getOwnerBundle()
	{
		return ownerBundle;
	}


	/**
	 * Sets owner bundle.
	 *
	 * @param ownerBundle the owner bundle
	 */
	public void setOwnerBundle(String ownerBundle)
	{
		this.ownerBundle = ownerBundle;
	}


	/**
	 * Gets island bundle.
	 *
	 * @return the island bundle
	 */
	public @Nullable String getIslandBundle()
	{
		return islandBundle;
	}


	/**
	 * Sets island bundle.
	 *
	 * @param islandBundle the island bundle
	 */
	public void setIslandBundle(String islandBundle)
	{
		this.islandBundle = islandBundle;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * Stores unique Id for object. Mostly, island object.
	 */
	@Expose
	private String uniqueId = "";

	/**
	 * Stores a names of unlocked generator tiers.
	 */
	@Expose
	private Set<String> unlockedTiers = new HashSet<>();

	/**
	 * Stores a names of unlocked purchased tiers.
	 */
	@Expose
	private Set<String> purchasedTiers = new HashSet<>();

	/**
	 * Stores currently active generator names.
	 */
	@Expose
	private Set<String> activeGeneratorList = new HashSet<>();

	/**
	 * Stores amount of maximal active generators at the same time.
	 */
	@Expose
	private int maxGeneratorCount = 1;

	/**
	 * Stores amount of bought generators.
	 */
	@Expose
	private int purchasedActiveGeneratorCount = 0;

	/**
	 * Stores working range for current data object.
	 */
	@Expose
	private int workingRange;

	/**
	 * Stores active bundle.
	 */
	@Expose
	private @Nullable String ownerBundle;

	/**
	 * Stores active bundle.
	 */
	@Expose
	private @Nullable String islandBundle;
}
