package world.bentobox.magiccobblestonegenerator.managers;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
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
     * This method clears cache and reloads all generators. Unsaved changes will be lost. Includes island data.
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

        this.addon.log("Loading generator tiers from database...");

        this.generatorTierDatabase.loadObjects().forEach(this::loadGeneratorTier);

        this.addon.log("Done");
    }


    /**
     * Loads generator tiers in cache silently. Used when loading.
     *
     * @param generatorTier that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorTier(GeneratorTierObject generatorTier)
    {
        return this.loadGeneratorTier(generatorTier, true, null, true);
    }


    /**
     * Load generatorTier in the cache.
     *
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
                    user.sendMessage(Constants.MESSAGE + "skipping",
                        Constants.GENERATOR,
                        generatorTier.getFriendlyName());
                }

                return false;
            }
            else
            {
                if (!silent)
                {
                    user.sendMessage(Constants.MESSAGE + "overwriting",
                        Constants.GENERATOR,
                        generatorTier.getFriendlyName());
                }

                this.generatorTierCache.replace(generatorTier.getUniqueId(), generatorTier);
                return true;
            }
        }

        if (!silent)
        {
            user.sendMessage(Constants.MESSAGE + "loaded",
                Constants.GENERATOR,
                generatorTier.getFriendlyName());
        }

        this.generatorTierCache.put(generatorTier.getUniqueId(), generatorTier);
        return true;
    }


    /**
     * Loads generator data in cache silently. Used when loading.
     *
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
     *
     * @param generatorTier object that must be saved in database.
     */
    public void saveGeneratorTier(GeneratorTierObject generatorTier)
    {
        this.generatorTierDatabase.saveObjectAsync(generatorTier);
    }


    /**
     * This method allows to store single generatorData object.
     *
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
     * This method removes from cache and database every generator that is related to given gamemode.
     *
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
     * This method returns generator tier object from given generator Id.
     *
     * @param generatorId Generator Id that must be returned.
     * @return GeneratorTierObject with given Id, or null.
     */
    public GeneratorTierObject getGeneratorByID(String generatorId)
    {
        return this.generatorTierCache.get(generatorId);
    }


    /**
     * This method returns active generator tier object for island at given location.
     *
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

        this.addIslandData(optionalIsland.get());
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
                // Filter out generators that are not deployed.
                filter(GeneratorTierObject::isDeployed).
                // Filter objects with the same generator type.
                filter(generator -> generator.getGeneratorType().equals(generatorType)).
                // Filter out objects with incorrect biomes.
                filter(generator -> generator.getRequiredBiomes().isEmpty() ||
                    generator.getRequiredBiomes().contains(biome)).
                // Get a generator that has largest priority and has required biome
                max((o1, o2) -> {
                    // If required biomes is empty, the it works in all biomes.
                    boolean o1HasBiome = o1.getRequiredBiomes().isEmpty() ||
                        o1.getRequiredBiomes().contains(biome);
                    boolean o2HasBiome = o2.getRequiredBiomes().isEmpty() ||
                        o2.getRequiredBiomes().contains(biome);

                    if (o1HasBiome != o2HasBiome)
                    {
                        return Boolean.compare(o1HasBiome, o2HasBiome);
                    }
                    else if (o1.getPriority() != o2.getPriority())
                    {
                        // Larger priority must be in the end.
                        return Integer.compare(o1.getPriority(), o2.getPriority());
                    }
                    else
                    {
                        // Default should be placed last one.
                        return Boolean.compare(o2.isDefaultGenerator(), o1.isDefaultGenerator());
                    }
                });

        return optionalGenerator.orElse(null);
    }


    /**
     * This method iterates through all world generators from given type and tries to find if someone is set to be
     * default. It finds first one.
     *
     * @param world of type World
     * @param generatorType of type GeneratorType
     * @return GeneratorTierObject
     */
    private @Nullable
    GeneratorTierObject findDefaultGeneratorTier(World world,
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
     *
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
            // Filter out undeployed generators.
            filter(GeneratorTierObject::isDeployed).
            // Sort in order: default generators are first, followed by lowest priority,
            // generator type and then by generator name.
            sorted(Comparator.comparing(GeneratorTierObject::isDefaultGenerator).reversed().
                thenComparing(GeneratorTierObject::getPriority).
                thenComparing(GeneratorTierObject::getGeneratorType).
                thenComparing(GeneratorTierObject::getFriendlyName)).
            // Return as list collection.
            collect(Collectors.toList());
    }


    /**
     * Tis method finds all default generators in given world.
     * @param world World where generators must be searched
     * @return List with default generators.
     */
    public List<GeneratorTierObject> findDefaultGeneratorList(World world)
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
            // Filter deployed and default generators.
            filter(GeneratorTierObject::isDefaultGenerator).
            filter(GeneratorTierObject::isDeployed).
            // Return as list collection.
            collect(Collectors.toList());
    }


    // ---------------------------------------------------------------------
    // Section: Generator Island Data
    // ---------------------------------------------------------------------


    /**
     * This method checks every island in stored worlds for user and loads them in cache.
     *
     * @param uniqueId User unique id.
     */
    public void loadUserIslands(UUID uniqueId)
    {
        this.operationWorlds.stream().
            map(world -> this.addon.getIslands().getIsland(world, uniqueId)).
            filter(Objects::nonNull).
            forEach(island -> {
                if (island.getOwner() == uniqueId)
                {
                    // Owner island must be validated.
                    this.validateIslandData(island);
                }
                else
                {
                    // Members does not influence island data.
                    this.addIslandData(island);
                }
            });
    }


    /**
     * Load island from database into the cache or create new island data
     *
     * @param island - island that must be loaded
     */
    private void addIslandData(@NotNull Island island)
    {
        final String uniqueID = island.getUniqueId();

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

            pd.getActiveGeneratorList().addAll(this.findDefaultGeneratorList(island.getWorld()));
            this.saveGeneratorData(pd);

            // Add to cache
            this.generatorDataCache.put(uniqueID, pd);
        }
    }


    /**
     * This method adds, validates and returns island generator data for given island.
     *
     * @param island Island which data must be returned.
     * @return GeneratorDataObject or null if failed to create.
     */
    public @Nullable GeneratorDataObject validateIslandData(@Nullable Island island)
    {
        if (island == null || island.getOwner() == null)
        {
            return null;
        }

        this.addIslandData(island);
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
                dataObject.getPurchasedTiers().contains(generatorTier))
            {
                // All purchased and default tiers are available.
                dataObject.getUnlockedTiers().add(generatorTier);
            }
            else if (generatorTier.getRequiredMinIslandLevel() <= this.getIslandLevel(island))
            {
                // Add only if user has all required permissions and generator cost is 0 or vault
                // is not provided.

                if ((generatorTier.getRequiredPermissions().isEmpty() ||
                    generatorTier.getRequiredPermissions().stream().allMatch(permission ->
                        User.getInstance(island.getOwner()).hasPermission(permission))) &&
                    (generatorTier.getGeneratorTierCost() <= 0 ||
                        !this.addon.isVaultProvided()))
                {
                    dataObject.getUnlockedTiers().add(generatorTier);
                }
            }
        });

        // Remove locked generators from active list.
        dataObject.getActiveGeneratorList().removeIf(generator ->
            !dataObject.getUnlockedTiers().contains(generator));

        this.updateMaxGeneratorCount(island, dataObject);

        if (dataObject.getMaxGeneratorCount() < dataObject.getActiveGeneratorList().size())
        {
            // There are more active generators then allowed.
            // Start to remove from first element.

            while (dataObject.getActiveGeneratorList().size() != dataObject.getMaxGeneratorCount())
            {
                dataObject.getActiveGeneratorList().iterator().remove();
            }
        }

        this.updateMaxWorkingRange(island, dataObject);

        return dataObject;
    }


    /**
     * This method updates max working range for island.
     * @param island Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateMaxWorkingRange(@NotNull Island island, @NotNull GeneratorDataObject dataObject)
    {
        // Update max island generation range.
        int permissionRange = Utils.getPermissionValue(User.getInstance(island.getOwner()),
            Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.max-range"),
            this.addon.getSettings().getWorkingRange());

        dataObject.setWorkingRange(permissionRange);
    }


    /**
     * This method updates max active generator count.
     * @param island Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateMaxGeneratorCount(@NotNull Island island, @NotNull GeneratorDataObject dataObject)
    {
        // Update max active generator count.
        int permissionSize = Utils.getPermissionValue(User.getInstance(island.getOwner()),
            Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.active-generators"),
            this.addon.getSettings().getDefaultActiveGeneratorCount());

        dataObject.setMaxGeneratorCount(Math.max(permissionSize,
            dataObject.getPurchasedActiveGeneratorCount()));
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

        this.addIslandData(island);
        return this.generatorDataCache.get(island.getUniqueId());
    }


    /**
     * This is just a wrapper method that allows to deactivate generator.
     * @param user User who deactivates generator.
     * @param generatorData Data which will be populated.
     * @param generatorTier Generator that will be removed.
     */
    public void deactivateGenerator(@NotNull User user,
        @NotNull GeneratorDataObject generatorData,
        @NotNull GeneratorTierObject generatorTier)
    {
        user.sendMessage(Constants.MESSAGE + "generator-deactivated",
            Constants.GENERATOR, generatorTier.getFriendlyName());
        generatorData.getActiveGeneratorList().remove(generatorTier);

        // Save object.
        this.saveGeneratorData(generatorData);
    }


    /**
     * This method checks if given user can activate given generator tier.
     * This method includes money withdraw, so it is assumed, that it is used as check
     * before activating the generator tier.
     * @param user User who will pay for activating.
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be activated.
     * @return {@code true} if can activate, {@false} if cannot activate.
     */
    public boolean canActivateGenerator(@NotNull User user,
        @NotNull GeneratorDataObject generatorData,
        @NotNull GeneratorTierObject generatorTier)
    {
        if (generatorData.getActiveGeneratorList().size() >= generatorData.getMaxGeneratorCount())
        {
            // Too many generators.
            user.sendMessage(Constants.ERRORS + "active-generators-reached");
            return false;
        }

        if (!generatorData.getUnlockedTiers().contains(generatorTier))
        {
            // Generator is not unlocked. Return false.
            user.sendMessage(Constants.ERRORS + "generator-not-unlocked",
                Constants.GENERATOR, generatorTier.getFriendlyName());
            return false;
        }
        else
        {
            if (this.addon.isVaultProvided() && generatorTier.getActivationCost() > 0)
            {
                // Return true only if user has enough money and its removal was successful.
                if (this.addon.getVaultHook().has(user, generatorTier.getActivationCost()) &&
                    this.addon.getVaultHook().withdraw(user,
                        generatorTier.getActivationCost()).transactionSuccess())
                {
                    return true;
                }
                else
                {
                    user.sendMessage(Constants.ERRORS + "no-credits",
                        TextVariables.NUMBER, String.valueOf(generatorTier.getActivationCost()));
                    return false;
                }
            }
            else
            {
                // Vault is not enabled or cost is not set. Allow change.
                return true;
            }
        }
    }


    /**
     * This is just a wrapper method that allows to activate generator.
     * @param user User who activates generator.
     * @param generatorData Data which will be populated.
     * @param generatorTier Generator that will be added.
     */
    public void activateGenerator(@NotNull User user,
        @NotNull GeneratorDataObject generatorData,
        @NotNull GeneratorTierObject generatorTier)
    {
        user.sendMessage(Constants.MESSAGE + "generator-activated",
            Constants.GENERATOR, generatorTier.getFriendlyName());
        generatorData.getActiveGeneratorList().add(generatorTier);

        // Save object.
        this.saveGeneratorData(generatorData);
    }


    /**
     * This method checks if given user can purchase given generator tier.
     * This method includes money withdraw, so it is assumed, that it is used as check
     * before purchasing the generator tier.
     * @param user User who will pay for purchase.
     * @param island GeneratorData linked island.
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be purchased.
     * @return {@code true} if can purchase, {@false} if cannot purchase.
     */
    public boolean canPurchaseGenerator(@NotNull User user,
        @NotNull Island island,
        @NotNull GeneratorDataObject generatorData,
        @NotNull GeneratorTierObject generatorTier)
    {
        if (generatorData.getPurchasedTiers().contains(generatorTier))
        {
            // Generator is not unlocked. Return false.
            user.sendMessage(Constants.ERRORS + "generator-already-purchased",
                Constants.GENERATOR, generatorTier.getFriendlyName());
            return false;
        }
        else if (generatorTier.getRequiredMinIslandLevel() > this.getIslandLevel(island))
        {
            // Generator is not unlocked. Return false.
            user.sendMessage(Constants.ERRORS + "island-level-not-reached",
                Constants.GENERATOR, generatorTier.getFriendlyName(),
                TextVariables.NUMBER, String.valueOf(generatorTier.getRequiredMinIslandLevel()));
            return false;
        }
        else if (!generatorTier.getRequiredPermissions().isEmpty() &&
            !generatorTier.getRequiredPermissions().stream().allMatch(permission ->
                User.getInstance(island.getOwner()).hasPermission(permission)))
        {
            Optional<String> missingPermission =
                generatorTier.getRequiredPermissions().stream().
                    filter(permission -> !User.getInstance(island.getOwner()).hasPermission(permission)).
                    findAny();

            // Generator is not unlocked. Return false.
            user.sendMessage(Constants.ERRORS + "missing-permission",
                Constants.GENERATOR, generatorTier.getFriendlyName(),
                TextVariables.PERMISSION, missingPermission.get());
            return false;
        }
        else
        {
            if (this.addon.isVaultProvided() && generatorTier.getGeneratorTierCost() > 0)
            {
                // Return true only if user has enough money and its removal was successful.
                if (this.addon.getVaultHook().has(user, generatorTier.getGeneratorTierCost()) &&
                    this.addon.getVaultHook().withdraw(user,
                        generatorTier.getGeneratorTierCost()).transactionSuccess())
                {
                    return true;
                }
                else
                {
                    user.sendMessage(Constants.ERRORS + "no-credits",
                        TextVariables.NUMBER, String.valueOf(generatorTier.getGeneratorTierCost()));
                    return false;
                }
            }
            else
            {
                // Vault is not enabled or cost is not set. Allow change.
                return true;
            }
        }
    }


    /**
     * This method adds generator tier to purchased generators.
     * @param user User who will pays.
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be purchased.
     * @return {@code true} if can purchase, {@false} if cannot purchase.
     */
    public void purchaseGenerator(@NotNull User user,
        @NotNull GeneratorDataObject generatorData,
        @NotNull GeneratorTierObject generatorTier)
    {
        user.sendMessage(Constants.MESSAGE + "generator-purchased",
            Constants.GENERATOR, generatorTier.getFriendlyName());
        generatorData.getPurchasedTiers().add(generatorTier);

        // Save object.
        this.saveGeneratorData(generatorData);
    }


    /**
     * This method removes given data object from cache and database.
     * @param uniqueId Object that must be removed.
     */
    public void wipeGeneratorData(String uniqueId)
    {
        this.generatorDataCache.remove(uniqueId);
        this.generatorDataDatabase.deleteID(uniqueId);
    }


    /**
     * This method removes given data object from cache and database.
     * @param dataObject Object that must be removed.
     */
    public void wipeGeneratorData(GeneratorDataObject dataObject)
    {
        this.wipeGeneratorData(dataObject.getUniqueId());
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


    /**
     * This method returns long that represents given user level.
     * @param user the user.
     * @return Island level
     */
    public long getIslandLevel(User user)
    {
        if (!this.addon.isLevelProvided())
        {
            // No level addon. Return max value.
            return Long.MAX_VALUE;
        }

        return this.addon.getLevelAddon().getIslandLevel(user.getWorld(),
            user.getUniqueId());
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
