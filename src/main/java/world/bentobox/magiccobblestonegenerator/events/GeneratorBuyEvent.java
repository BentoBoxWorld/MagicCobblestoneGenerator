//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.events;


import org.bukkit.event.HandlerList;
import java.util.UUID;

import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This event is called after player bought the given generator.
 */
public class GeneratorBuyEvent extends BentoBoxEvent
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
	 * Player who bought generator Id.
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
	 * Event listener list for current
	 */
	private static final HandlerList handlers = new HandlerList();
}
