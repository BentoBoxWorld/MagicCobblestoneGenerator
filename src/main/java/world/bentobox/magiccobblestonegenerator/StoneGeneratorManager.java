package world.bentobox.magiccobblestonegenerator;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.config.Settings;


/**
 * This class provides some helper methods to clean up StoneGeneratorAddon class.
 */
public class StoneGeneratorManager
{
    /**
     * Default constructor.
     *
     * @param addon of type StoneGeneratorAddon
     */
    public StoneGeneratorManager(StoneGeneratorAddon addon)
    {
        this.addon = addon;
        this.hookedGameModes = new HashSet<>();
    }


    /**
     * Adds given gameModes to current GameMode processing.
     * @param gameModes List of game mode names where this addon should work.
     */
    protected void addGameModes(List<String> gameModes)
    {
        this.hookedGameModes.addAll(gameModes);
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method returns if Stone generator should operate in given world.
     * @param world World object that must be checked.
     * @return <code>true</code> if addon should work in given world.
     */
    public boolean canOperateInWorld(World world)
    {
        Optional<GameModeAddon> addon = this.addon.getPlugin().getIWM().getAddon(world);

        return addon.isPresent() && this.hookedGameModes.contains(addon.get().getDescription().getName());
    }


    /**
     * This method returns true if offline generation is enabled or at least one member of island is online.
     * @param location Location of the generated block.
     * @return true if offline generation is enabled or at least one member is online.
     */
    public boolean isMembersOnline(Location location)
    {
        if (this.addon.getSettings().isOfflineGeneration())
        {
            return true;
        }

        Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location);

        if (optionalIsland.isPresent())
        {
            for (UUID member : optionalIsland.get().getMemberSet())
            {
                if (User.getInstance(member).isOnline())
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * This method returns long that represents island level in given location.
     * @param location Location of the island.
     * @return Island level
     */
    public long getIslandLevel(Location location)
    {
    	Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location);
    	
    	if (StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_OWN_LEVEL.isSetForWorld(location.getWorld())) {
    		return optionalIsland.map(island -> 
    			this.addon.getLevelsData(island.getOwner()).getGeneratorLevel()).orElse(0L);
    	} else {
    		if (!this.addon.isLevelProvided())
            {
                // No level addon. Return 0.
                return 0L;
            }

            return optionalIsland.map(island ->
            this.addon.getLevelAddon().getIslandLevel(location.getWorld(), island.getOwner())).orElse(0L);
    	}
    }


    /**
     * This method finds and sorts all Generator Tiers for given world.
     * @param world World where tiers must be found.
     * @return List with generator tiers.
     */
    public List<Settings.GeneratorTier> getAllGeneratorTiers(World world)
    {
        String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName()).orElse(null);
        if (name == null) return Collections.emptyList();

        Map<String, Settings.GeneratorTier> defaultTiers = this.addon.getSettings().getDefaultGeneratorTierMap();
        Map<String, Settings.GeneratorTier> customAddonTiers = this.addon.getSettings().getAddonGeneratorTierMap(name);

        List<Settings.GeneratorTier> tierList;

        if (customAddonTiers.isEmpty())
        {
            tierList = new ArrayList<>(defaultTiers.values());
        }
        else
        {
            // Collect all unique IDs
            Set<String> uniqueIDSet = new HashSet<>(customAddonTiers.keySet());
            uniqueIDSet.addAll(defaultTiers.keySet());
            tierList = new ArrayList<>(uniqueIDSet.size());

            // Populate list with correct generator tiers.
            uniqueIDSet.forEach(id -> tierList.add(customAddonTiers.getOrDefault(id, defaultTiers.get(id))));
        }

        if (tierList.isEmpty())
        {
            // Something goes wrong!
            return Collections.emptyList();
        }

        // List will be sorted from smallest to largest.
        tierList.sort(Comparator.comparingInt(Settings.GeneratorTier::getMinLevel));

        return tierList;
    }


    /**
     * This method returns Generator Tier for generating ores.
     * @param islandLevel Level of island.
     * @param world World where generation happens.
     * @return Generator Tier that will be applied or null, if tier not found.
     */
    public Settings.GeneratorTier getGeneratorTier(long islandLevel, World world)
    {
        List<Settings.GeneratorTier> tierList = this.getAllGeneratorTiers(world);

        if (tierList.isEmpty())
        {
            // There does not exist any tiers. Return null.
            return null;
        }

        Settings.GeneratorTier generatorTier = tierList.get(0);

        if (generatorTier.getMinLevel() < 0)
        {
            // Negative min level mean that it will be always set as chance.
            return generatorTier;
        }

        // Optimized for cycle
        for (int i = 1; i < tierList.size(); i++)
        {
            if (islandLevel >= tierList.get(i).getMinLevel())
            {
                generatorTier = tierList.get(i);
            }
            else
            {
                break;
            }
        }

        // Something goes wrong. Returning empty map.
        return generatorTier;
    }


    /**
     * This method returns chance map for generating ores.
     * @param islandLevel Level of island.
     * @param world World where generation happens.
     * @return Map that contains materials and its chance to be generated.
     */
    public Map<Double, Material> getMaterialChanceMap(long islandLevel, World world)
    {
        Settings.GeneratorTier generatorTier = this.getGeneratorTier(islandLevel, world);

        return generatorTier == null ? Collections.emptyMap() : generatorTier.getBlockChanceMap();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable holds Generator addon.
     */
    private StoneGeneratorAddon addon;

    /**
     * This variable holds addon names where stone generator should work.
     */
    private Set<String> hookedGameModes;
}
