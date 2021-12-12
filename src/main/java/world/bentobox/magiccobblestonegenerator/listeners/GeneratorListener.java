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
        return addon.getSettings().isOfflineGeneration() ||
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
                block.getWorld().spawnParticle(Particle.SMOKE_LARGE,
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
     * This method returns material of new block if generator manages to replace cobblestone to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block location that need to be replaced.
     * @return Material of replaced block or null, if block was not replaced.
     */
    protected @Nullable Material generateCobblestoneReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.COBBLESTONE);

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location);
    }


    /**
     * This method returns material of new block if generator manages to replace stone to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block that need to be replaced.
     * @return Material of replaced block or null, if block was not replaced.
     */
    protected @Nullable Material generateStoneReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.STONE);

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location);
    }


    /**
     * This method returns material of new block if generator manages to replace basalt to a new magic block.
     *
     * @param island Island on which block is processed.
     * @param location Block that need to be replaced.
     * @return Material of replaced block or null, if block was not replaced.
     */
    protected @Nullable Material generateBasaltReplacement(@Nullable Island island, Location location)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            island,
            location,
            GeneratorTierObject.GeneratorType.BASALT);

        return this.addon.getGenerator().processBlockReplacement(generatorTier, location);
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
