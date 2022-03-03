//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.objects;


import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * This object stores generator data per island. In short, it allows to easily access active and unlocked generator
 * tiers for island.
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
     *
     * @param unlockedTiers new value for this object.
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
     *
     * @param activeGeneratorList new value for this object.
     */
    public void setActiveGeneratorList(Set<String> activeGeneratorList)
    {
        this.activeGeneratorList = activeGeneratorList;
    }


    /**
     * This method returns the purchasedTiers value.
     *
     * @return the value of purchasedTiers.
     */
    public Set<String> getPurchasedTiers()
    {
        return purchasedTiers;
    }


    /**
     * This method sets the purchasedTiers value.
     *
     * @param purchasedTiers the purchasedTiers new value.
     */
    public void setPurchasedTiers(Set<String> purchasedTiers)
    {
        this.purchasedTiers = purchasedTiers;
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


    /**
     * Gets owner active generator count.
     *
     * @return the owner active generator count
     */
    public int getOwnerActiveGeneratorCount()
    {
        return ownerActiveGeneratorCount;
    }


    /**
     * Sets owner active generator count.
     *
     * @param ownerActiveGeneratorCount the owner active generator count
     */
    public void setOwnerActiveGeneratorCount(int ownerActiveGeneratorCount)
    {
        this.ownerActiveGeneratorCount = ownerActiveGeneratorCount;
    }


    /**
     * Gets owner working range.
     *
     * @return the owner working range
     */
    public int getOwnerWorkingRange()
    {
        return ownerWorkingRange;
    }


    /**
     * Sets owner working range.
     *
     * @param ownerWorkingRange the owner working range
     */
    public void setOwnerWorkingRange(int ownerWorkingRange)
    {
        this.ownerWorkingRange = ownerWorkingRange;
    }


    /**
     * Gets island active generator count.
     *
     * @return the island active generator count
     */
    public int getIslandActiveGeneratorCount()
    {
        return islandActiveGeneratorCount;
    }


    /**
     * Sets island active generator count.
     *
     * @param islandActiveGeneratorCount the island active generator count
     */
    public void setIslandActiveGeneratorCount(int islandActiveGeneratorCount)
    {
        this.islandActiveGeneratorCount = islandActiveGeneratorCount;
    }


    /**
     * Gets island working range.
     *
     * @return the island working range
     */
    public int getIslandWorkingRange()
    {
        return islandWorkingRange;
    }


    /**
     * Sets island working range.
     *
     * @param islandWorkingRange the island working range
     */
    public void setIslandWorkingRange(int islandWorkingRange)
    {
        this.islandWorkingRange = islandWorkingRange;
    }


// ---------------------------------------------------------------------
// Section: Processing Methods
// ---------------------------------------------------------------------


    /**
     * This method returns bundle for current object. If owner has a specific bundle, then return ownerBundle, otherwise
     * return islandBundle.
     *
     * @return Bundle name for current island object to work.
     */
    public String getBundle()
    {
        return this.ownerBundle != null ? this.ownerBundle : this.islandBundle;
    }


    /**
     * If owner has permission that defines infinite working range (-1) or has a specific range then return the owner
     * range, otherwise return island working range.
     *
     * @return Range for generators where they will work.
     */
    public int getRange()
    {
        return this.ownerWorkingRange == -1 || this.ownerWorkingRange > 0 ?
            this.ownerWorkingRange : this.islandWorkingRange;
    }


    /**
     * If owner has a permission that defines infinite generator count or has a specific amount then return
     * ownerActiveGeneratorCount, otherwise return islandActiveGeneratorCount
     *
     * @return ActiveGeneratorCount for this object.
     */
    public int getActiveGeneratorCount()
    {
        return this.ownerActiveGeneratorCount == -1 || this.ownerActiveGeneratorCount > 0 ?
            this.ownerActiveGeneratorCount : this.islandActiveGeneratorCount;
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
    private Set<String> activeGeneratorList = new LinkedHashSet<>();

    /**
     * Stores maximum allowed active generator count for island object.
     */
    @Expose
    private int ownerActiveGeneratorCount = 0;

    /**
     * Stores working range for generators on current island defined by owner.
     */
    @Expose
    private int ownerWorkingRange = -1;

    /**
     * Stores active bundle.
     */
    @Expose
    private @Nullable String ownerBundle = null;

    /**
     * Stores maximum allowed active generator count for island object.
     */
    @Expose
    private int islandActiveGeneratorCount = 0;

    /**
     * Stores working range for generators on current island.
     */
    @Expose
    private int islandWorkingRange = -1;

    /**
     * Stores active bundle.
     */
    @Expose
    private @Nullable String islandBundle = null;
}
