package world.bentobox.magiccobblestonegenerator.listeners;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;


import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Main Generator Listener. This class contains listener that process Generator options.
 */
public class MainGeneratorListener implements Listener
{
	/**
	 * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
	 *
	 * @param addon of type StoneGeneratorAddon
	 */
	public MainGeneratorListener(StoneGeneratorAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * This method overwrites stone and cobblestone generation from lava and water.
	 *
	 * @param event BlockFromToEvent which result will be overwritten.
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockFromToEvent(BlockFromToEvent event)
	{
		Block eventSourceBlock = event.getBlock();

		if (!this.addon.getManager().canOperateInWorld(eventSourceBlock.getWorld()))
		{
			// If not operating in world, then return as fast as possible
			return;
		}

		if (!this.addon.getManager().isMembersOnline(eventSourceBlock.getLocation()))
		{
			// If island members are not online then do not continue
			return;
		}

		Material liquid = eventSourceBlock.getType();

		if (!liquid.equals(Material.WATER) && !liquid.equals(Material.LAVA))
		{
			// We are interested only in Water and Lava flow.
			return;
		}

		Block eventToBlock = event.getToBlock();

		if (!eventToBlock.getType().equals(Material.AIR) &&
			!eventToBlock.getType().equals(Material.CAVE_AIR) &&
			!eventToBlock.getType().equals(Material.VOID_AIR) &&
			!eventToBlock.getType().equals(Material.WATER))
		{
			// We are not interested in situations when liquid flows on other blocks
			return;
		}

		// Stone generator could be used to get much better elements than cobble generator
		boolean stoneGenerator = this.canGenerateStone(liquid, eventToBlock);

		if (stoneGenerator)
		{
			// This is block that should be replaced by Stone
			event.setCancelled(this.addon.getGenerator().isReplacementGenerated(eventToBlock, true));
		}

		if (eventToBlock.getType().equals(Material.WATER))
		{
			// We needed to check for water only in stone generator
			return;
		}

		if (liquid.equals(Material.LAVA) && this.canLavaGenerateCobblestone(eventToBlock, event.getFace()))
		{
			// Lava is generating cobblestone into eventToBlock place
			event.setCancelled(this.addon.getGenerator().isReplacementGenerated(eventToBlock));
		}
		else if (liquid.equals(Material.WATER))
		{
			// Check if water finds any lava block that could be replaced with cobblestone

			Block replacedBlock = this.getWaterGeneratedCobblestone(eventToBlock, event.getFace());

			if (replacedBlock != null)
			{
				event.setCancelled(this.addon.getGenerator().isReplacementGenerated(replacedBlock));
			}
		}

		// End of process... no generation for you!!
	}


	// ---------------------------------------------------------------------
	// Section: Private Methods
	// ---------------------------------------------------------------------


	/**
	 * This method returns if current targetBlock could generate Stone Block. (Stone Generators).
	 * @param liquid Liquid that flows.
	 * @param targetBlock Block in which liquid will flow
	 * @return true, if below targetBlock is water and liquid is lava or up is lava and liquid is water.
	 */
	private boolean canGenerateStone(Material liquid, Block targetBlock)
	{
		// Stone can be generated only if lava flows on water or water flows under lava;
		// Second part is a faster stone generation as it will anyway generate stone and fall under first part
		// after processing lava flow.
		return liquid.equals(Material.LAVA) && targetBlock.getType().equals(Material.WATER) ||
			liquid.equals(Material.WATER) && targetBlock.getRelative(BlockFace.UP).getType().equals(Material.LAVA);

	}


	/**
	 * This method returns if lava can generate cobblestone
	 * @param airBlock Air Block that will be replaced with cobblestone
	 * @param flowDirection Lava flow direction.
	 * @return true, if lava will generate cobblestone
	 */
	private boolean canLavaGenerateCobblestone(Block airBlock, BlockFace flowDirection)
	{
		switch (flowDirection)
		{
			case NORTH:
			case EAST:
			case SOUTH:
			case WEST:
				// Check if block in flow direction is water
				// Check if block on the left side is water
				// Check if block on the right side is water

				return airBlock.getRelative(flowDirection).getType().equals(Material.WATER) ||
					airBlock.getRelative(this.getClockwiseDirection(flowDirection)).getType().equals(Material.WATER) ||
					airBlock.getRelative(this.getCounterClockwiseDirection(flowDirection)).getType().equals(Material.WATER);

			case DOWN:
				// If lava flows down then we should search for water in horizontally adjacent blocks.

				return airBlock.getRelative(BlockFace.NORTH).getType().equals(Material.WATER) ||
					airBlock.getRelative(BlockFace.EAST).getType().equals(Material.WATER) ||
					airBlock.getRelative(BlockFace.SOUTH).getType().equals(Material.WATER) ||
					airBlock.getRelative(BlockFace.WEST).getType().equals(Material.WATER);
			default:
				return false;
		}
	}


	/**
	 * This method checks and finds lava blocks that can be replaced with cobble stone.
	 * @param airBlock Air Block that will be replaced with water
	 * @param flowDirection Water flow direction.
	 * @return Lava block that will be replaced with water or null, if there is no lava near air block.
	 */
	private Block getWaterGeneratedCobblestone(Block airBlock, BlockFace flowDirection)
	{
		Block checkBlock;

		switch (flowDirection)
		{
			case NORTH:
			case EAST:
			case SOUTH:
			case WEST:
				// Check if block in flow direction after airBlock is lava
				// Check if block on the left side of airBlock is lava
				// Check if block on the right side of airBlock is lava
				// If lava level is 0, then it will be transformed to obsidian. Not processed by current listener.

				checkBlock = airBlock.getRelative(flowDirection);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(this.getClockwiseDirection(flowDirection));

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(this.getCounterClockwiseDirection(flowDirection));

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				return null;
			case DOWN:
				// If lava water flows down then we should search for lava under it and in horizontally adjacent blocks.
				// If lava level is 0, then it will be transformed to obsidian. Not processed by current listener.

				checkBlock = airBlock.getRelative(flowDirection);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(BlockFace.NORTH);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(BlockFace.EAST);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(BlockFace.SOUTH);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				checkBlock = airBlock.getRelative(BlockFace.WEST);

				if (checkBlock.getType().equals(Material.LAVA) && !checkBlock.getBlockData().getAsString().contains("level=0"))
				{
					return checkBlock;
				}

				return null;
			default:
				return null;
		}
	}


	/**
	 * This method transforms input block face to next BlockFace by 90 degree in clockwise direction. Only on
	 * horizontal pane for NORTH,EAST,SOUTH,WEST directions.
	 * @param face input BlockFace
	 * @return Output BlockFace that is 90 degree from input BlockFace in clockwise direction
	 */
	private BlockFace getClockwiseDirection(BlockFace face)
	{
		switch (face)
		{
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			default:
				// Not interested in other directions
				return face;
		}
	}


	/**
	 * This method transforms input block face to next BlockFace by 90 degree in counter clockwise direction.
	 * Only on horizontal pane for NORTH,EAST,SOUTH,WEST directions.
	 * @param face input BlockFace
	 * @return Output BlockFace that is 90 degree from input BlockFace in counter clockwise direction
	 */
	private BlockFace getCounterClockwiseDirection(BlockFace face)
	{
		switch (face)
		{
			case NORTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.NORTH;
			case SOUTH:
				return BlockFace.EAST;
			case WEST:
				return BlockFace.SOUTH;
			default:
				// Not interested in other directions
				return face;
		}
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * Main addon class.
	 */
	private StoneGeneratorAddon addon;
}
