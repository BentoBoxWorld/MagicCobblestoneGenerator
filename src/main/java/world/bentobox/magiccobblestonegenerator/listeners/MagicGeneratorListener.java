///
// Created by BONNe
// Copyright - 2021
///

package world.bentobox.magiccobblestonegenerator.listeners;


import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Main Generator Listener. This class contains listener that process Generator options. This class contains listener
 * and all methods that detect if custom generation can be processed.
 */
public class MagicGeneratorListener extends GeneratorListener
{
    /**
     * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
     *
     * @param addon of type StoneGeneratorAddon
     */
    public MagicGeneratorListener(StoneGeneratorAddon addon)
    {
        super(addon);
    }


    // ---------------------------------------------------------------------
    // Section: Private Methods
    // ---------------------------------------------------------------------


    /**
     * This method detects if BlockFromToEvent can be used by Magic Cobblestone Generator by checking all requirements
     * and calls custom generator if all requirements are met. It cancels this event only if a custom generator manages
     * to change material.
     *
     * @param event BlockFromToEvent which result will be overwritten.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent event)
    {
        Block eventSourceBlock = event.getBlock();

        if (!this.addon.getAddonManager().canOperateInWorld(eventSourceBlock.getWorld()))
        {
            // If not operating in world, then return as fast as possible
            return;
        }

        if (eventSourceBlock == event.getToBlock())
        {
            // We are not interested in self-flow events.
            return;
        }

        Material liquid = eventSourceBlock.getType();

        if (!eventSourceBlock.isLiquid())
        {
            // We are interested only in Water and Lava flow.
            return;
        }

        Block eventToBlock = event.getToBlock();

        if (eventToBlock.getType().equals(liquid))
        {
            // Not interested into flowing into the same type of liquid.
            return;
        }

        if (!eventToBlock.getType().equals(Material.AIR) &&
            !eventToBlock.getType().equals(Material.CAVE_AIR) &&
            !eventToBlock.getType().equals(Material.VOID_AIR) &&
            !eventToBlock.getType().equals(Material.WATER))
        {
            // We are not interested in situations when liquid flows on other blocks
            return;
        }

        Optional<Island> islandOptional = this.addon.getIslands().getIslandAt(eventSourceBlock.getLocation());

        if (islandOptional.isEmpty())
        {
            // If not operating in non-island regions.
            return;
        }

        Island island = islandOptional.get();

        if (!island.isAllowed(StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR))
        {
            // Currently addon is not working outside island protection ranges.
            return;
        }

        if (!this.isSomeoneOnline(island))
        {
            // Offline generation is not enabled and all island members are offline.
            return;
        }

        if (!this.isInRangeToGenerate(island, eventSourceBlock))
        {
            // Check if any island member is at generator range.
            return;
        }

        // Stone generator could be used to get much better elements than cobble generator
        if (this.canGenerateStone(liquid, eventToBlock))
        {
            // Return from here at any case. Even if could not manage to replace stone.

            Material material = this.generateStoneReplacement(island, eventToBlock.getLocation());

            if (material != null)
            {
                eventToBlock.setType(material);
                // sound when lava transforms to cobble
                this.playEffects(eventToBlock);
                event.setCancelled(true);
            }

            return;
        }

        if (eventToBlock.getType().equals(Material.WATER))
        {
            // We needed to check for water only in stone generator as in other cases it will be
            // replaced with cobble or obsidian

            return;
        }

        if (liquid.equals(Material.LAVA) && this.canLavaGenerateCobblestone(eventToBlock, event.getFace()))
        {
            Material material = this.generateCobblestoneReplacement(island, eventToBlock.getLocation());

            // Lava is generating cobblestone into eventToBlock place
            if (material != null)
            {
                eventToBlock.setType(material);
                // sound when lava transforms to cobble
                this.playEffects(eventToBlock);
                event.setCancelled(true);
            }
        }
        else if (liquid.equals(Material.WATER))
        {
            // Check if water finds any lava block that could be replaced with cobblestone

            Block replacedBlock = this.getWaterGeneratedCobblestone(eventToBlock, event.getFace());

            // Water flow should not be cancelled even if replacement is generated, as replacement block will
            // never be in the flow block, as it will always be next block.

            if (replacedBlock != null)
            {
                Material material = this.generateStoneReplacement(island, replacedBlock.getLocation());

                if (material != null)
                {
                    replacedBlock.setType(material);
                    // sound when lava transforms to cobble
                    this.playEffects(eventToBlock);
                }
            }
        }
    }


    /**
     * This method returns if current targetBlock could generate Stone Block. (Stone Generators).
     * <p>
     * Stone can be generated only when lava flows over water. Stone will be always generated in a block where water is
     * located.
     *
     * @param liquid Liquid that flows.
     * @param targetBlock Block in which liquid will flow
     * @return true, if below targetBlock is water and liquid is lava or up is lava and liquid is water.
     */
    private boolean canGenerateStone(Material liquid, Block targetBlock)
    {
        // Adding check if water flows under lava increases generation speed, as in next
        // ticks it will detect lava flow anyway.
        // Also it improves performance a bit, as it will run 1 event less.
        return liquid.equals(Material.LAVA) && targetBlock.getType().equals(Material.WATER) ||
            liquid.equals(Material.WATER) && targetBlock.getRelative(BlockFace.UP).getType().equals(Material.LAVA);
    }


    /**
     * This method returns if lava can generate cobblestone.
     * <p>
     * Lava is generating cobblestone in situations if next block where it flows (air block) is adjacent to block that
     * contains water. If that is true than airBlock will be replaced with cobblestone.
     *
     * @param airBlock Air Block that will be replaced with cobblestone
     * @param flowDirection Lava flow direction.
     * @return true, if lava will generate cobblestone
     */
    private boolean canLavaGenerateCobblestone(Block airBlock, BlockFace flowDirection)
    {
        return switch (flowDirection) {
            case NORTH, EAST, SOUTH, WEST ->
                    // Since waterlogged blocks, it is also necessary to check block above.
                    // Check if block in flow direction is water
                    // Check if block on the left side is water
                    // Check if block on the right side is water

                    MagicGeneratorListener.containsWater(airBlock.getRelative(BlockFace.UP)) ||
                            MagicGeneratorListener.containsWater(airBlock.getRelative(flowDirection)) ||
                            MagicGeneratorListener.containsWater(airBlock
                                    .getRelative(MagicGeneratorListener.getClockwiseDirection(flowDirection))) ||
                            MagicGeneratorListener.containsWater(airBlock
                                    .getRelative(MagicGeneratorListener.getCounterClockwiseDirection(flowDirection)));
            case DOWN ->
                    // If lava flows down then we should search for water in horizontally adjacent blocks.

                    MagicGeneratorListener.containsWater(airBlock.getRelative(BlockFace.NORTH)) ||
                            MagicGeneratorListener.containsWater(airBlock.getRelative(BlockFace.EAST)) ||
                            MagicGeneratorListener.containsWater(airBlock.getRelative(BlockFace.SOUTH)) ||
                            MagicGeneratorListener.containsWater(airBlock.getRelative(BlockFace.WEST));
            default -> false;
        };
    }


    /**
     * This method checks if water can generate cobblestone.
     * <p>
     * Water is generating cobblestone in situations when it is directly adjacent to lava block, and this block is not a
     * source block.
     * <p>
     * Node: by default Minecraft logic, lava is replaced with cobblestone/obsidian only if the water is trying to flow
     * on lava. It allows creating situations where lava and water hit each other at max flow distance without creating
     * cobblestone. This overwrite it as it checks next blocks to air block. It is done on intentional!
     *
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

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(MagicGeneratorListener.getClockwiseDirection(flowDirection));

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(MagicGeneratorListener.getCounterClockwiseDirection(flowDirection));

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                return null;
            case DOWN:
                // If lava water flows down then we should search for lava under it and in horizontally adjacent blocks.
                // If lava level is 0, then it will be transformed to obsidian. Not processed by current listener.

                checkBlock = airBlock.getRelative(flowDirection);

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.NORTH);

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.EAST);

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.SOUTH);

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                checkBlock = airBlock.getRelative(BlockFace.WEST);

                if (MagicGeneratorListener.isFlowingLavaBlock(checkBlock))
                {
                    return checkBlock;
                }

                return null;
            default:
                return null;
        }
    }


// ---------------------------------------------------------------------
// Section: Static methods
// ---------------------------------------------------------------------


    /**
     * This method returns if target block is water block or contains water.
     *
     * @param block Block that must be checked.
     * @return true if block type is water or it is waterlogged.
     */
    private static boolean containsWater(Block block)
    {
        // Block Data contains information about the water logged status.
        // If block type is not water, we need to check if it is waterlogged.
        return block.getType().equals(Material.WATER) ||
            block.getBlockData().getAsString().contains("waterlogged=true");
    }


    /**
     * This method returns if given block contains lava and is not a source block.
     *
     * @param block Block that must be checked.
     * @return true if block contains lava and is not source block.
     */
    private static boolean isFlowingLavaBlock(Block block)
    {
        // Block Data contains information about the liquid level.
        // In our situation, we need to check if the level is not 0 when it is counted as
        // a source block.
        return block.getType().equals(Material.LAVA) &&
            !block.getBlockData().getAsString().contains("level=0");
    }


    /**
     * This method transforms input block face to next BlockFace by 90 degree in clockwise direction. Only on horizontal
     * pane for NORTH,EAST,SOUTH,WEST directions.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 90 degree from input BlockFace in clockwise direction
     */
    private static BlockFace getClockwiseDirection(BlockFace face)
    {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default ->
                    // Not interested in other directions
                    face;
        };
    }


    /**
     * This method transforms input block face to next BlockFace by 90 degree in counter clockwise direction. Only on
     * horizontal pane for NORTH,EAST,SOUTH,WEST directions.
     *
     * @param face input BlockFace
     * @return Output BlockFace that is 90 degree from input BlockFace in counter clockwise direction
     */
    private static BlockFace getCounterClockwiseDirection(BlockFace face)
    {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case EAST -> BlockFace.NORTH;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            default ->
                    // Not interested in other directions
                    face;
        };
    }
}