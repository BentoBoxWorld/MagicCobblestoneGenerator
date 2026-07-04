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
 * This event is fired when user is trying to activate or deactivate generator. It is cancellable.
 */
public class GeneratorActivationEvent extends GeneratorEvent implements Cancellable
{
    /**
     * Instantiates a new Generator activation event.
     *
     * @param generator the generator
     * @param user the user
     * @param island the island
     * @param activate the activate
     */
    public GeneratorActivationEvent(GeneratorTierObject generator, User user, String island, boolean activate)
    {
        super(generator, user, island);
        this.activate = activate;
    }


    /**
     * Is activate boolean.
     *
     * @return the boolean
     */
    public boolean isActivate()
    {
        return activate;
    }


    /**
     * Sets activate.
     *
     * @param activate the activate
     */
    public void setActivate(boolean activate)
    {
        this.activate = activate;
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
        return GeneratorActivationEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return GeneratorActivationEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Boolean that indicates if generator will be activated or deactivated.
     */
    private boolean activate;

    /**
     * Boolean that indicates if event is cancelled.
     */
    private boolean cancelled;

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
