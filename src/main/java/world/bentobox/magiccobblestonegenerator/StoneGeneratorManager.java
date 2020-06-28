package world.bentobox.magiccobblestonegenerator;


import java.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


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
        this.operationWorlds = new HashSet<>();

        this.generatorTierDatabase = new Database<>(addon, GeneratorTierObject.class);
        this.generatorTierCache = new HashMap<>();

        this.generatorDataDatabase = new Database<>(addon, GeneratorDataObject.class);
        this.generatorDataCache = new HashMap<>();

        this.load();
    }


    /**
     * Adds given world to operation worlds where generator will work.
     * @param world List of game mode names where this addon should work.
     */
    public void addWorld(@Nullable World world)
    {
        if (world != null)
        {
            this.operationWorlds.add(world);
        }
    }


    // ---------------------------------------------------------------------
    // Section: Database related methods
    // ---------------------------------------------------------------------


    /**
     * Creates generators cache.
     */
    private void load()
    {
        this.generatorTierCache.clear();

        this.addon.getLogger().info("Loading generator tiers...");

        this.generatorTierDatabase.loadObjects().forEach(this::loadGeneratorTier);

        this.addon.getLogger().info("Done");
    }


    /**
     * Loads generator tiers in cache silently. Used when loading.
     * @param generatorTier that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorTier(GeneratorTierObject generatorTier)
    {
        return this.loadGeneratorTier(generatorTier, true, null, true);
    }


    /**
     * Load generatorTier in the cache.
     * @param generatorTier - generatorTier that must be stored.
     * @param overwrite - true if previous biomes should be overwritten
     * @param user - user making the request
     * @param silent - if true, no messages are sent to user
     * @return - true if imported
     */
    public boolean loadGeneratorTier(GeneratorTierObject generatorTier, boolean overwrite, User user, boolean silent)
    {
        if (this.generatorTierCache.containsKey(generatorTier.getUniqueId()))
        {
            if (!overwrite)
            {
                if (!silent)
                {
                    user.sendMessage("stonegenerator.messages.skipping",
                        "[generator]",
                        generatorTier.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage("stonegenerator.messages.overwriting",
                        "[generator]",
                        generatorTier.getFriendlyName());
                }

                this.generatorTierCache.replace(generatorTier.getUniqueId(), generatorTier);
                return true;
            }
        }

        if (!silent)
        {
            user.sendMessage("stonegenerator.messages.imported",
                "[generator]",
                generatorTier.getFriendlyName());
        }

        this.generatorTierCache.put(generatorTier.getUniqueId(), generatorTier);
        return true;
    }


    /**
     * Loads generator data in cache silently. Used when loading.
     * @param generatorData that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorData(GeneratorDataObject generatorData)
    {
        this.generatorDataCache.put(generatorData.getUniqueId(), generatorData);
        return true;
    }


    /**
     * This method allows to store single generatorTier object.
     * @param generatorTier object that must be saved in database.
     */
    public void saveGeneratorTier(GeneratorTierObject generatorTier)
    {
        this.generatorTierDatabase.saveObjectAsync(generatorTier);
    }


    /**
     * This method allows to store single generatorData object.
     * @param generatorData object that must be saved in database.
     */
    public void saveGeneratorData(GeneratorDataObject generatorData)
    {
        this.generatorDataDatabase.saveObjectAsync(generatorData);
    }


    /**
     * Save generator tiers from cache into database
     */
    public void save()
    {
        this.generatorTierCache.values().forEach(this::saveGeneratorTier);
        this.generatorDataCache.values().forEach(this::saveGeneratorData);
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
        return this.operationWorlds.contains(world);
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
        if (!this.addon.isLevelProvided())
        {
            // No level addon. Return 0.
            return 0L;
        }

        Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location);

        return optionalIsland.map(island ->
            this.addon.getLevelAddon().getIslandLevel(location.getWorld(), island.getOwner())).orElse(0L);
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
     * This variable holds worlds where stone generator should work.
     */
    private Set<World> operationWorlds;

    /**
     * Variable stores map that links String to loaded generator tier object.
     */
    private Map<String, GeneratorTierObject> generatorTierCache;

    /**
     * Variable stores database of generator tiers objects.
     */
    private Database<GeneratorTierObject> generatorTierDatabase;

    /**
     * Variable stores map that links String to loaded generator data object.
     */
    private Map<String, GeneratorDataObject> generatorDataCache;

    /**
     * Variable stores database of generator data objects.
     */
    private Database<GeneratorDataObject> generatorDataDatabase;
}
