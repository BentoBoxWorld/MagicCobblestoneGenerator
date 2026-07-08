//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * Abstract base for all generator events. Holds the data common to every generator event: the target player, the
 * island and the generator (friendly name and id), together with their accessors.
 * <p>
 * Concrete events still declare their own {@code HandlerList} as required by Bukkit, and cancellable events implement
 * {@link org.bukkit.event.Cancellable} themselves.
 */
public abstract class GeneratorEvent extends BentoBoxEvent
{
    /**
     * Instantiates a new Generator event.
     *
     * @param generator the generator
     * @param user the user, or null if there is no associated user
     * @param islandUUID the island unique id, or null if the island context is unknown
     */
    protected GeneratorEvent(GeneratorTierObject generator, @Nullable User user, @Nullable String islandUUID)
    {
        this.generator = generator.getFriendlyName();
        this.generatorID = generator.getUniqueId();

        this.targetPlayer = user == null ? null : user.getUniqueId();
        this.islandUUID = islandUUID;
    }


    /**
     * Gets target player.
     *
     * @return the target player, or null if there is no associated user
     */
    @Nullable
    public UUID getTargetPlayer()
    {
        return targetPlayer;
    }


    /**
     * Sets target player.
     *
     * @param targetPlayer the target player, or null if there is no associated user
     */
    public void setTargetPlayer(@Nullable UUID targetPlayer)
    {
        this.targetPlayer = targetPlayer;
    }


    /**
     * Gets island uuid.
     *
     * @return the island uuid, or null if the island context is unknown
     */
    @Nullable
    public String getIslandUUID()
    {
        return islandUUID;
    }


    /**
     * Sets island uuid.
     *
     * @param islandUUID the island uuid, or null if the island context is unknown
     */
    public void setIslandUUID(@Nullable String islandUUID)
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Player who triggered the event, or null.
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
}
