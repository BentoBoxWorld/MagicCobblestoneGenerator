//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.objects;


import com.google.gson.annotations.Expose;

import java.util.Collections;
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
	 * Mostly stores generators that are purchased via Upgrades API.
	 */
	@Expose
	private Set<String> unlockedTiers = Collections.emptySet();

	/**
	 * Stores currently active generator names.
	 */
	@Expose
	private Set<String> activeGeneratorList = Collections.emptySet();
}
