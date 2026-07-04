//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This event is fired before a player buys the given generator. It is cancellable, so other plugins can add their own
 * requirements to the generator purchasing process. Cancelling this event stops the purchase before any money is
 * withdrawn and before {@link GeneratorBuyEvent} is fired.
 */
public class GeneratorPreBuyEvent extends GeneratorEvent implements Cancellable
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
        super(generator, user, island);
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
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
