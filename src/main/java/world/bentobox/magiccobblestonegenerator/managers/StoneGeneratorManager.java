package world.bentobox.magiccobblestonegenerator.managers;


import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


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
     *
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
     * This method clears cache and reloads all generators.
     * Unsaved changes will be lost.
     * Includes island data.
     */
    public void reload()
    {
        // on reload clear cache and load generators.

        this.generatorDataCache.clear();
        this.load();
    }


    /**
     * Creates generators cache.
     */
    public void load()
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


    /**
     * This method removes from cache and database every generator that is related to
     * given gamemode.
     * @param optional GameMode addon which generators must be removed.
     */
    public void wipeGameModeGenerators(Optional<GameModeAddon> optional)
    {
        if (!optional.isPresent())
        {
            // Done.
            return;
        }

        final String objectKey = optional.get().getDescription().getName().toLowerCase();

        List<String> keySet = new ArrayList<>(this.generatorTierCache.keySet());

        // Remove everything that starts with gamemode name.
        keySet.forEach(uniqueId -> {
            if (uniqueId.startsWith(objectKey))
            {
                this.generatorTierCache.remove(objectKey);
                this.generatorTierDatabase.deleteID(objectKey);
            }
        });
    }


    // ---------------------------------------------------------------------
    // Section: Generator related methods
    // ---------------------------------------------------------------------


    /**
     * This method returns active generator tier object for island at given location.
     * @param location Location of the block.
     * @param generatorType Generator type.
     * @return GeneratorTierObject that operates in given island or null.
     */
    public @Nullable GeneratorTierObject getGeneratorTier(Location location,
        GeneratorTierObject.GeneratorType generatorType)
    {
        Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location);

        if (!optionalIsland.isPresent())
        {
            // No islands at given location. Do not generate
            return null;
        }

        this.addIslandData(optionalIsland.get().getUniqueId());
        GeneratorDataObject data = this.generatorDataCache.get(optionalIsland.get().getUniqueId());

        // Gets biome from location.
        final Biome biome = location.getWorld().getBiome(location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());

        // Find generator from active generator list.
        Optional<GeneratorTierObject> optionalGenerator =
            data.getActiveGeneratorList().stream().
                // Map active generator id to actual object.
                map(name -> this.generatorTierCache.getOrDefault(name, null)).
                // Filter out null objects. Just in case.
                filter(Objects::nonNull).
                // Filter objects with the same generator type.
                filter(generator -> generator.getGeneratorType().equals(generatorType)).
                // Filter out objects with incorrect biomes.
                filter(generator -> generator.getRequiredBiomes().isEmpty() ||
                    generator.getRequiredBiomes().contains(biome)).
                // Get a generator that has largest priority and has required biome
                max((o1, o2) -> {
                    boolean o1HasBiome = o1.getRequiredBiomes().contains(biome);
                    boolean o2HasBiome = o2.getRequiredBiomes().contains(biome);

                    if (o1HasBiome != o2HasBiome)
                    {
                        return Boolean.compare(o1HasBiome, o2HasBiome);
                    }
                    else
                    {
                        return Integer.compare(o1.getPriority(), o2.getPriority());
                    }
                });

        return optionalGenerator.orElse(null);
    }


    /**
     * This method iterates through all world generators from given type and tries to find if someone
     * is set to be default.
     * It finds first one.
     * @param world of type World
     * @param generatorType of type GeneratorType
     * @return GeneratorTierObject
     */
    private @Nullable GeneratorTierObject findDefaultGeneratorTier(World world,
        GeneratorTierObject.GeneratorType generatorType)
    {
        String gameMode = this.addon.getPlugin().getIWM().getAddon(world).map(
            gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

        if (gameMode.isEmpty())
        {
            // If not a gamemode world then return.
            return null;
        }

        // Find default generator from cache.
        return this.generatorTierCache.values().stream().
            // Filter all default generators
            filter(GeneratorTierObject::isDefaultGenerator).
            // Filter generators with necessary type.
            filter(generator -> generator.getGeneratorType().equals(generatorType)).
            // Filter generators that starts with name.
            filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase())).
            // Return first or null.
            findFirst().orElse(null);
    }


    /**
     * This method returns all generator tiers for given world.
     * @param world World which generators must be returned.
     * @return List of generator tier objects for given world.
     */
    public List<GeneratorTierObject> getAllGeneratorTiers(World world)
    {
        String gameMode = this.addon.getPlugin().getIWM().getAddon(world).map(
            gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

        if (gameMode.isEmpty())
        {
            // If not a gamemode world then return.
            return Collections.emptyList();
        }

        // Find default generator from cache.
        return this.generatorTierCache.values().stream().
            // Filter generators that starts with name.
            filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase())).
            // Sort in order: default generators are first, followed by lowest priority,
            // generator type and then by generator name.
            sorted(Comparator.comparing(GeneratorTierObject::isDefaultGenerator).reversed().
                thenComparing(GeneratorTierObject::getPriority).
                thenComparing(GeneratorTierObject::getGeneratorType).
                thenComparing(GeneratorTierObject::getFriendlyName)).
            // Return as list collection.
            collect(Collectors.toList());
    }


    // ---------------------------------------------------------------------
    // Section: Generator Island Data
    // ---------------------------------------------------------------------


    /**
     * Load island from database into the cache or create new island data
     *
     * @param uniqueID - uniqueID to add
     */
    private void addIslandData(@NotNull String uniqueID)
    {
        if (this.generatorDataCache.containsKey(uniqueID))
        {
            return;
        }

        // The island is not in the cache
        // Check if the island exists in the database

        if (this.generatorDataDatabase.objectExists(uniqueID))
        {
            // Load player from database
            GeneratorDataObject data = this.generatorDataDatabase.loadObject(uniqueID);
            // Store in cache

            if (data != null)
            {
                this.generatorDataCache.put(uniqueID, data);
            }
            else
            {
                this.addon.logError("Could not load NULL generator data object.");
            }
        }
        else
        {
            // Create the island data
            GeneratorDataObject pd = new GeneratorDataObject();
            pd.setUniqueId(uniqueID);

            this.saveGeneratorData(pd);
            // Add to cache
            this.generatorDataCache.put(uniqueID, pd);
        }
    }


    /**
     * This method adds, validates and returns island generator data for given island.
     * @param island Island which data must be returned.
     * @return GeneratorDataObject or null if failed to create.
     */
    public @Nullable GeneratorDataObject validateIslandData(@Nullable Island island)
    {
        if (island == null || island.getOwner() == null)
        {
            return null;
        }

        this.addIslandData(island.getUniqueId());
        GeneratorDataObject dataObject = this.generatorDataCache.get(island.getUniqueId());

        if (dataObject == null)
        {
            return null;
        }

        // Validate data in generator object.

        // Remove generators which island does not qualifies anymore.
        dataObject.getUnlockedTiers().clear();

        this.getAllGeneratorTiers(island.getWorld()).forEach(generatorTier -> {
            if (generatorTier.isDefaultGenerator() ||
                dataObject.getPurchasedTiers().contains(generatorTier.getUniqueId()))
            {
                // All purchased and default tiers are available.
                dataObject.getUnlockedTiers().add(generatorTier.getUniqueId());
            }
            else if (generatorTier.getRequiredMinIslandLevel() <= this.getIslandLevel(island))
            {
                // Add only if user has all required permissions.
                if (generatorTier.getRequiredPermissions().isEmpty() ||
                    generatorTier.getRequiredPermissions().stream().allMatch(permission ->
                        User.getInstance(island.getOwner()).hasPermission(permission)))
                {
                    dataObject.getUnlockedTiers().add(generatorTier.getUniqueId());
                }
            }
        });

        // Remove locked generators from active list.
        dataObject.getActiveGeneratorList().removeIf(generator ->
            !dataObject.getUnlockedTiers().contains(generator));

        // Update max active generator count.
        int permissionSize = Utils.getPermissionValue(User.getInstance(island.getOwner()),
            Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.active-generators"),
            this.addon.getSettings().getDefaultActiveGeneratorCount());

        dataObject.setMaxGeneratorCount(Math.max(permissionSize,
            dataObject.getPurchasedActiveGeneratorCount()));

        if (dataObject.getMaxGeneratorCount() < dataObject.getActiveGeneratorList().size())
        {
            // There are more active generators then allowed.
            // Start to remove from first element.

            while (dataObject.getActiveGeneratorList().size() != dataObject.getMaxGeneratorCount())
            {
                dataObject.getActiveGeneratorList().iterator().remove();
            }
        }

        // Update max island generation range.
        int permissionRange = Utils.getPermissionValue(User.getInstance(island.getOwner()),
            Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.max-range"),
            this.addon.getSettings().getWorkingRange());

        dataObject.setWorkingRange(permissionRange);

        return dataObject;
    }


    /**
     * This method allows to get generator data for given island.
     * @param island Island which data must be returned.
     * @return instance of GeneratorDataObject.
     */
    public @Nullable GeneratorDataObject getGeneratorData(@Nullable Island island)
    {
        if (island == null)
        {
            return null;
        }

        this.addIslandData(island.getUniqueId());
        return this.generatorDataCache.get(island.getUniqueId());
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
     * This method returns long that represents given island level.
     * @param island the island.
     * @return Island level
     */
    public long getIslandLevel(Island island)
    {
        if (!this.addon.isLevelProvided())
        {
            // No level addon. Return max value.
            return Long.MAX_VALUE;
        }

        return this.addon.getLevelAddon().getIslandLevel(island.getWorld(),
            island.getOwner());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable holds Generator addon.
     */
    private final StoneGeneratorAddon addon;

    /**
     * This variable holds worlds where stone generator should work.
     */
    private final Set<World> operationWorlds;

    /**
     * Variable stores map that links String to loaded generator tier object.
     */
    private final Map<String, GeneratorTierObject> generatorTierCache;

    /**
     * Variable stores database of generator tiers objects.
     */
    private final Database<GeneratorTierObject> generatorTierDatabase;

    /**
     * Variable stores map that links String to loaded generator data object.
     */
    private final Map<String, GeneratorDataObject> generatorDataCache;

    /**
     * Variable stores database of generator data objects.
     */
    private final Database<GeneratorDataObject> generatorDataDatabase;
}
