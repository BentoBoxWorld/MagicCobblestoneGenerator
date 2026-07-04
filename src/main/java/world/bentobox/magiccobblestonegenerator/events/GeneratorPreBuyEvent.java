//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This event is fired before a player buys the given generator. It is cancellable, so other plugins can add their own
 * requirements to the generator purchasing process. Cancelling this event stops the purchase before any money is
 * withdrawn and before {@link GeneratorBuyEvent} is fired.
 */
public class GeneratorPreBuyEvent extends BentoBoxEvent implements Cancellable
{
    /**
     * Instantiates a new Generator pre buy event.
     *
     * @param generator the generator
     * @param user the user
     * @param island the island
     */
    public GeneratorPreBuyEvent(GeneratorTierObject generator, User user, String island)
    {
        this.generator = generator.getFriendlyName();
        this.generatorID = generator.getUniqueId();

        this.targetPlayer = user.getUniqueId();
        this.islandUUID = island;
    }


    /**
     * Gets target player.
     *
     * @return the target player
     */
    public UUID getTargetPlayer()
    {
        return targetPlayer;
    }


    /**
     * Sets target player.
     *
     * @param targetPlayer the target player
     */
    public void setTargetPlayer(UUID targetPlayer)
    {
        this.targetPlayer = targetPlayer;
    }


    /**
     * Gets island uuid.
     *
     * @return the island uuid
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
     * Sets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
     * pass to other plugins.
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
        return GeneratorPreBuyEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return GeneratorPreBuyEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Player who buys generator Id.
     */
    private UUID targetPlayer;

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
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
