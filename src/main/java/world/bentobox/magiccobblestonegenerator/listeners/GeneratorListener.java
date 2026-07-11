//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.listeners;


import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.CustomBlocks;
import world.bentobox.magiccobblestonegenerator.utils.Why;


/**
 * This is main generator listener class.
 */
public abstract class GeneratorListener implements Listener
{
    /**
     * Constructor MainGeneratorListener creates a new MainGeneratorListener instance.
     *
     * @param addon of type StoneGeneratorAddon
     */
    public GeneratorListener(StoneGeneratorAddon addon)
    {
        this.addon = addon;
        this.random = new Random(System.currentTimeMillis());
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method returns if someone from the island is online. The method do nothing, if offline
     * offline generation is enabled.
     * @param island Island that must be checked.
     * @return {@code true} if someone is online or offline generation is enabled, {@code false} otherwise.
     */
    protected boolean isSomeoneOnline(Island island)
    {
        return island.isSpawn() ||
            addon.getSettings().isOfflineGeneration() ||
            island.getMemberSet().stream().
                map(User::getInstance).
                filter(Objects::nonNull).
                anyMatch(User::isOnline);
    }


    /**
     * This method returns there is an island member in range to active of "custom" block generation
     *
     * @param block Block that must be checked.
     * @return true if there is a player in the set range
     */
    protected boolean isInRangeToGenerate(Island island, Block block)
    {
        // If settings value is 0, then assume that it is not set at all.
        int workingRange = this.addon.getSettings().getDefaultWorkingRange();

        // Check if any island member is near block.
        if (workingRange > 0)
        {
            GeneratorDataObject data = this.addon.getAddonManager().getGeneratorData(island);

            if (data == null)
            {
                // If data object is not found, return false.
                return false;
            }

            // Use range from island object.
            int range = data.getRange();

            // If range is -1 or 0, the ignore it.
            if (range == -1 || range == 0)
            {
                return true;
            }

            return block.getWorld().getNearbyEntities(block.getLocation(),
                range,
                range,
                range).
                stream().
                filter(Player.class::isInstance).
                anyMatch(e -> island.getMemberSet().contains(e.getUniqueId()));
        }
        else
        {
            return true;
        }
    }


    /**
     * This method plays sound effect and adds particles to new block.
     *
     * @param block block placement where particle must be generated.
     */
    protected void playEffects(Block block)
    {
        final double blockX = block.getX();
        final double blockY = block.getY();
        final double blockZ = block.getZ();

        // Run everything in new task
        Bukkit.getScheduler().runTask(this.addon.getPlugin(), () -> {
            // Play sound for spawning block
            block.getWorld().playSound(block.getLocation(),
                Sound.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.5F,
                2.6F + (this.random.nextFloat() * 2 - 1) * 0.8F);

            // This spawns 8 large smoke particles.
            for (int counter = 0; counter < 8; ++counter)
            {
                block.getWorld().spawnParticle(Particle.SMOKE,
                    blockX + Math.random(),
                    blockY + 1 + Math.random(),
                    blockZ + Math.random(),
                    1,
                    0,
                    0,
                    0,
                    0);
            }
        });
    }


    /**
     * This method applies a block ID picked by the generator to the given block. Vanilla IDs are set
     * immediately; custom (hook-provided) IDs first set a vanilla placeholder and are replaced by the
     * hook placement one tick later, because hook APIs set blocks directly rather than through a state.
     *
     * @param blockId Block ID picked by the generator (vanilla material name or provider-prefixed custom ID).
     * @param block Block that must be replaced.
     * @param placeholder Vanilla material to leave in place while a custom placement is pending.
     * @return {@code true} if a replacement was applied or scheduled.
     */
    protected boolean applyBlockId(String blockId, Block block, Material placeholder)
    {
        Material material = CustomBlocks.matchVanilla(blockId);

        if (material != null)
        {
            if (!material.isBlock())
            {
                return false;
            }

            block.setType(material);
            return true;
        }

        if (CustomBlocks.isCustom(blockId))
        {
            block.setType(placeholder);
            this.scheduleCustomBlockPlacement(blockId, block.getLocation());
            return true;
        }

        return false;
    }


    /**
     * This method schedules placement of a custom block via its BentoBox hook on the next tick.
     *
     * @param blockId Provider-prefixed custom block ID.
     * @param location Location where the block must be placed.
     */
    protected void scheduleCustomBlockPlacement(String blockId, Location location)
    {
        Bukkit.getScheduler().runTask(this.addon.getPlugin(), () ->
        {
            if (!CustomBlocks.place(this.addon, blockId, location))
            {
                Why.report(location, "Custom block " + blockId + " could not be placed.");
            }
        });
    }


    /**
     * This method returns block ID of new block if generator manages to replace cobblestone to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block location that need to be replaced.
     * @return Block ID of replaced block or null, if block was not replaced.
     */
    protected @Nullable String generateCobblestoneReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.COBBLESTONE);

        if (island != null && generatorTier != null && !this.addon.getAddonManager().canGenerateBlock(island, generatorTier))
        {
            // Generator tier is exhausted and is currently on cooldown.
            return null;
        }

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location, island);
    }


    /**
     * This method returns block ID of new block if generator manages to replace stone to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block that need to be replaced.
     * @return Block ID of replaced block or null, if block was not replaced.
     */
    protected @Nullable String generateStoneReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.STONE);

        if (island != null && generatorTier != null && !this.addon.getAddonManager().canGenerateBlock(island, generatorTier))
        {
            // Generator tier is exhausted and is currently on cooldown.
            return null;
        }

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location, island);
    }


    /**
     * This method returns block ID of new block if generator manages to replace basalt to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block that need to be replaced.
     * @return Block ID of replaced block or null, if block was not replaced.
     */
    protected @Nullable String generateBasaltReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.BASALT);

        if (island != null && generatorTier != null && !this.addon.getAddonManager().canGenerateBlock(island, generatorTier))
        {
            // Generator tier is exhausted and is currently on cooldown.
            return null;
        }

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location, island);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Main addon class.
     */
    protected final StoneGeneratorAddon addon;

    /**
     * Instance of Random.
     */
    protected final Random random;
}
