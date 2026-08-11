//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.utils;


import java.lang.reflect.Method;
import java.util.Optional;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Helper that detects if AcidIsland is going to undo a block that this addon is about to replace.
 * <p>
 * AcidIsland water is acid, so its LavaCheck listener turns stone that vanilla creates when lava
 * pours into water back into water again. It does that by checking, one tick later, if the block is
 * stone. If this addon replaces the forming stone with a generator block, that check no longer
 * matches and the block survives, which lets a single lava bucket turn an entire ocean into
 * generator blocks.
 * <p>
 * AcidIsland is only a soft dependency, so the game mode is recognised by its name and the acid
 * damage value is read reflectively. That also keeps this working if AcidIsland is not installed at
 * all.
 *
 * @since 2.10.0
 */
public final class AcidIslandHelper
{
    /**
     * Private constructor. This is a utility class.
     */
    private AcidIslandHelper()
    {
        // Utility class.
    }


    /**
     * This method returns if AcidIsland manages the given world and will revert stone that is formed
     * inside its acid water back to water.
     *
     * @param addon Instance of this addon.
     * @param world World where the block is formed.
     * @return {@code true} if AcidIsland will revert the formed stone, {@code false} otherwise.
     */
    public static boolean revertsStoneFormedInWater(@NotNull StoneGeneratorAddon addon, @NotNull World world)
    {
        Optional<GameModeAddon> gameMode = addon.getPlugin().getIWM().getAddon(world);

        if (gameMode.isEmpty() || !ACID_ISLAND.equals(gameMode.get().getDescription().getName()))
        {
            // Not an AcidIsland world.
            return false;
        }

        // AcidIsland reverts the stone only if acid actually does damage.
        return getAcidDamage(gameMode.get().getWorldSettings()) > 0;
    }


    /**
     * This method returns the acid damage value from AcidIsland world settings.
     *
     * @param worldSettings World settings of the AcidIsland game mode.
     * @return Acid damage value or 0 if it could not be read.
     */
    private static int getAcidDamage(@Nullable WorldSettings worldSettings)
    {
        if (worldSettings == null)
        {
            return 0;
        }

        Method method = getAcidDamageMethod(worldSettings.getClass());

        if (method == null)
        {
            return 0;
        }

        try
        {
            return ((Number) method.invoke(worldSettings)).intValue();
        }
        catch (ReflectiveOperationException | ClassCastException | NullPointerException e)
        {
            return 0;
        }
    }


    /**
     * This method returns the cached acid damage getter for the given world settings class.
     *
     * @param settingsClass Class of the AcidIsland world settings.
     * @return The getter method or {@code null} if the class does not have one.
     */
    @Nullable
    private static Method getAcidDamageMethod(@NotNull Class<?> settingsClass)
    {
        if (settingsClass.equals(cachedSettingsClass))
        {
            return cachedAcidDamageMethod;
        }

        Method method;

        try
        {
            method = settingsClass.getMethod(ACID_DAMAGE_GETTER);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            method = null;
        }

        cachedSettingsClass = settingsClass;
        cachedAcidDamageMethod = method;

        return method;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Name of the AcidIsland game mode addon.
     */
    private static final String ACID_ISLAND = "AcidIsland";

    /**
     * Name of the method that returns player acid damage in AcidIsland settings.
     */
    private static final String ACID_DAMAGE_GETTER = "getAcidDamage";

    /**
     * Class for which the acid damage getter is cached.
     */
    private static Class<?> cachedSettingsClass;

    /**
     * Cached acid damage getter.
     */
    private static Method cachedAcidDamageMethod;
}
