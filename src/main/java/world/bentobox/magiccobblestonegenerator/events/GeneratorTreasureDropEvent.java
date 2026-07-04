//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This event is fired just before a generator drops a treasure item at the generated block location. It is cancellable
 * and the dropped {@link ItemStack} can be modified, so other plugins can intercept, change or veto treasure drops.
 * <p>
 * The magic generator produces blocks from a {@code BlockFormEvent} (lava/water flow) rather than from a player mining a
 * block, so there is no player or broken block state associated with the drop. This dedicated event is therefore used
 * instead of Bukkit's {@code BlockDropItemEvent}, which requires both.
 */
public class GeneratorTreasureDropEvent extends BentoBoxEvent implements Cancellable
{
    /**
     * Instantiates a new Generator treasure drop event.
     *
     * @param generator the generator tier that produced the treasure
     * @param islandUUID the island unique id, or null if unknown
     * @param location the location where the treasure will be dropped
     * @param itemStack the treasure item that will be dropped
     */
    public GeneratorTreasureDropEvent(GeneratorTierObject generator,
        String islandUUID,
        Location location,
        ItemStack itemStack)
    {
        this.generator = generator.getFriendlyName();
        this.generatorID = generator.getUniqueId();

        this.islandUUID = islandUUID;
        this.location = location;
        this.itemStack = itemStack;
    }


    /**
     * Gets island uuid.
     *
     * @return the island uuid, or null if unknown
     */
    public String getIslandUUID()
    {
        return islandUUID;
    }


    /**
     * Sets island uuid.
     *
     * @param islandUUID the island uuid
     */
    public void setIslandUUID(String islandUUID)
    {
        this.islandUUID = islandUUID;
    }


    /**
     * Gets generator.
     *
     * @return the generator
     */
    public String getGenerator()
    {
        return generator;
    }


    /**
     * Sets generator.
     *
     * @param generator the generator
     */
    public void setGenerator(String generator)
    {
        this.generator = generator;
    }


    /**
     * Gets generator id.
     *
     * @return the generator id
     */
    public String getGeneratorID()
    {
        return generatorID;
    }


    /**
     * Sets generator id.
     *
     * @param generatorID the generator id
     */
    public void setGeneratorID(String generatorID)
    {
        this.generatorID = generatorID;
    }


    /**
     * Gets the location where the treasure will be dropped.
     *
     * @return the location
     */
    public Location getLocation()
    {
        return location;
    }


    /**
     * Sets the location where the treasure will be dropped.
     *
     * @param location the location
     */
    public void setLocation(Location location)
    {
        this.location = location;
    }


    /**
     * Gets the treasure item that will be dropped.
     *
     * @return the item stack
     */
    public ItemStack getItemStack()
    {
        return itemStack;
    }


    /**
     * Sets the treasure item that will be dropped. Allows listeners to change the treasure.
     *
     * @param itemStack the item stack
     */
    public void setItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }


    /**
     * Gets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
     * pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }


    /**
     * Sets the cancellation state of this event. A cancelled event will not drop the treasure, but will still pass to
     * other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }


// ---------------------------------------------------------------------
// Section: Handler methods
// ---------------------------------------------------------------------


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    @Override
    public HandlerList getHandlers()
    {
        return GeneratorTreasureDropEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return GeneratorTreasureDropEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Island Id.
     */
    private String islandUUID;

    /**
     * Friendly name for generator.
     */
    private String generator;

    /**
     * Generator ID.
     */
    private String generatorID;

    /**
     * Location where the treasure will be dropped.
     */
    private Location location;

    /**
     * The treasure item that will be dropped.
     */
    private ItemStack itemStack;

    /**
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
