//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import org.bukkit.event.HandlerList;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This event is called after player bought the given generator.
 */
public class GeneratorBuyEvent extends GeneratorEvent
{
    /**
     * Instantiates a new Generator buy event.
     *
     * @param generator the generator
     * @param user the user
     * @param island the island
     */
    public GeneratorBuyEvent(GeneratorTierObject generator, User user, String island)
    {
        super(generator, user, island);
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
        return GeneratorBuyEvent.handlers;
    }


    /**
     * Gets handlers.
     *
     * @return the handlers
     */
    public static HandlerList getHandlerList()
    {
        return GeneratorBuyEvent.handlers;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Event listener list for current
     */
    private static final HandlerList handlers = new HandlerList();
}
