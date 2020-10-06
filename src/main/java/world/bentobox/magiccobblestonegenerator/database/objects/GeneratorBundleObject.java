//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.objects;


import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * The type Generator bundle object.
 */
@Table(name = "GeneratorBundle")
public class GeneratorBundleObject implements DataObject
{
	/**
	 * Instantiates a new Generator bundle object.
	 */
	public GeneratorBundleObject()
	{
	}


// ---------------------------------------------------------------------
// Section: Getters and Setters
// ---------------------------------------------------------------------


	/**
	 * Gets uniqueId
	 *
	 * @return the unique id
	 */
	@Override
	public String getUniqueId()
	{
		return uniqueId;
	}


	/**
	 * Sets the uniqueId
	 * @param uniqueId the uniqueId
	 */
	@Override
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}


	/**
	 * Gets friendly name.
	 *
	 * @return the friendly name
	 */
	public String getFriendlyName()
	{
		return friendlyName;
	}


	/**
	 * Sets friendly name.
	 *
	 * @param friendlyName the friendly name
	 */
	public void setFriendlyName(String friendlyName)
	{
		this.friendlyName = friendlyName;
	}


	/**
	 * Gets description.
	 *
	 * @return the description
	 */
	public List<String> getDescription()
	{
		return description;
	}


	/**
	 * Sets description.
	 *
	 * @param description the description
	 */
	public void setDescription(List<String> description)
	{
		this.description = description;
	}


	/**
	 * Gets generator icon.
	 *
	 * @return the generator icon
	 */
	public ItemStack getGeneratorIcon()
	{
		return generatorIcon;
	}


	/**
	 * Sets generator icon.
	 *
	 * @param generatorIcon the generator icon
	 */
	public void setGeneratorIcon(ItemStack generatorIcon)
	{
		this.generatorIcon = generatorIcon;
	}


	/**
	 * Gets generator tiers.
	 *
	 * @return the generator tiers
	 */
	public Set<String> getGeneratorTiers()
	{
		return generatorTiers;
	}


	/**
	 * Sets generator tiers.
	 *
	 * @param generatorTiers the generator tiers
	 */
	public void setGeneratorTiers(Set<String> generatorTiers)
	{
		this.generatorTiers = generatorTiers;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * The Unique id.
	 */
	@Expose
	private String uniqueId;

	/**
	 * The Friendly name.
	 */
	@Expose
	private String friendlyName;

	/**
	 * The Description.
	 */
	@Expose
	private List<String> description = Collections.emptyList();

	/**
	 * The Generator icon.
	 */
	@Expose
	private ItemStack generatorIcon = new ItemStack(Material.STONE);

	/**
	 * The Generator tiers.
	 */
	@Expose
	private Set<String> generatorTiers = new HashSet<>();
}
