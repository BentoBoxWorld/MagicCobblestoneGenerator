package world.bentobox.magiccobblestonegenerator.managers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.data.Money;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.events.GeneratorActivationEvent;
import world.bentobox.magiccobblestonegenerator.events.GeneratorBuyEvent;
import world.bentobox.magiccobblestonegenerator.events.GeneratorUnlockEvent;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;

/**
 * This class provides some helper methods to clean up StoneGeneratorAddon
 * class.
 */
public class StoneGeneratorManager {
    /**
     * Default constructor.
     *
     * @param addon of type StoneGeneratorAddon
     */
    public StoneGeneratorManager(StoneGeneratorAddon addon) {
	this.addon = addon;
	this.operationWorlds = new HashSet<>();

	this.generatorTierDatabase = new Database<>(addon, GeneratorTierObject.class);
	this.generatorTierCache = new HashMap<>();

	this.generatorDataDatabase = new Database<>(addon, GeneratorDataObject.class);
	this.generatorDataCache = new HashMap<>();

	this.generatorBundleDatabase = new Database<>(addon, GeneratorBundleObject.class);
	this.generatorBundleCache = new HashMap<>();
    }

    /**
     * Adds given world to operation worlds where generator will work.
     *
     * @param world List of game mode names where this addon should work.
     */
    public void addWorld(@Nullable World world) {
	if (world != null) {
	    this.operationWorlds.add(world);
	}
    }

    // ---------------------------------------------------------------------
    // Section: Database related methods
    // ---------------------------------------------------------------------

    /**
     * This method clears cache and reloads all generators. Unsaved changes will be
     * lost. Includes island data.
     */
    public void reload() {
	// on reload clear cache and load generators.

	this.generatorDataCache.clear();
	this.load();
    }

    /**
     * Creates generators cache.
     */
    public void load() {
	this.generatorTierCache.clear();
	this.generatorBundleCache.clear();

	this.addon.log("Loading generator tiers from database...");

	this.generatorTierDatabase.loadObjects().forEach(this::loadGeneratorTier);
	this.generatorBundleDatabase.loadObjects().forEach(this::loadGeneratorBundle);

	this.addon.log("Done");
    }

    /**
     * Loads generator tiers in cache silently. Used when loading.
     *
     * @param generatorTier that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorTier(GeneratorTierObject generatorTier) {
	return this.loadGeneratorTier(generatorTier, true, null);
    }

    /**
     * Load generatorTier in the cache.
     *
     * @param generatorTier - generatorTier that must be stored.
     * @param overwrite     - true if previous biomes should be overwritten
     * @param user          - user making the request
     * @return - true if imported
     */
    public boolean loadGeneratorTier(GeneratorTierObject generatorTier, boolean overwrite, User user) {
	if (this.generatorTierCache.containsKey(generatorTier.getUniqueId())) {
	    if (!overwrite) {
		return false;
	    } else {
		this.generatorTierCache.replace(generatorTier.getUniqueId(), generatorTier);
		return true;
	    }
	}

	if (user != null) {
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-loaded", Constants.GENERATOR,
		    generatorTier.getFriendlyName()));
	}

	// Migrate generator tier object.
	this.migrateGeneratorTier(generatorTier);

	this.generatorTierCache.put(generatorTier.getUniqueId(), generatorTier);
	return true;
    }

    /**
     * Loads generator bundles in cache silently. Used when loading.
     *
     * @param generatorBundle that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorBundle(GeneratorBundleObject generatorBundle) {
	return this.loadGeneratorBundle(generatorBundle, true, null);
    }

    /**
     * Load generatorBundle in the cache.
     *
     * @param generatorBundle - generatorBundle that must be stored.
     * @param overwrite       - true if previous biomes should be overwritten
     * @param user            - user making the request
     * @return - true if imported
     */
    public boolean loadGeneratorBundle(GeneratorBundleObject generatorBundle, boolean overwrite, User user) {
	if (this.generatorBundleCache.containsKey(generatorBundle.getUniqueId())) {
	    if (!overwrite) {
		return false;
	    } else {
		this.generatorBundleCache.replace(generatorBundle.getUniqueId(), generatorBundle);
		return true;
	    }
	}

	if (user != null) {
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "bundle-loaded", Constants.BUNDLE,
		    generatorBundle.getFriendlyName()));
	}

	this.generatorBundleCache.put(generatorBundle.getUniqueId(), generatorBundle);
	return true;
    }

    /**
     * Loads generator data in cache silently. Used when loading.
     *
     * @param generatorData that must be stored.
     * @return true if successful
     */
    private boolean loadGeneratorData(GeneratorDataObject generatorData) {
	this.generatorDataCache.put(generatorData.getUniqueId(), generatorData);
	return true;
    }

    /**
     * This method allows to store single generatorTier object.
     *
     * @param generatorTier object that must be saved in database.
     * @return CompletableFuture<Boolean> to indicate if it is done
     */
    public CompletableFuture<Boolean> saveGeneratorTier(GeneratorTierObject generatorTier) {
	return this.generatorTierDatabase.saveObjectAsync(generatorTier);
    }

    /**
     * This method allows to store single generatorBundle object.
     *
     * @param generatorBundle object that must be saved in database.
     * @return CompletableFuture<Boolean> to indicate if it is done
     */
    public CompletableFuture<Boolean> saveGeneratorBundle(GeneratorBundleObject generatorBundle) {
	return this.generatorBundleDatabase.saveObjectAsync(generatorBundle);
    }

    /**
     * This method allows to store single generatorData object.
     *
     * @param generatorData object that must be saved in database.
     * @return CompletableFuture<Boolean> to indicate if it is done
     */
    public CompletableFuture<Boolean> saveGeneratorData(GeneratorDataObject generatorData) {
	return this.generatorDataDatabase.saveObjectAsync(generatorData);
    }

    /**
     * Save generator tiers from cache into database
     * 
     * @return CompletableFuture<Boolean> to indicate if it is done
     */
    public CompletableFuture<Boolean> save() {
	List<CompletableFuture<Boolean>> futures = this.generatorTierCache.values().stream()
		.map(this::saveGeneratorTier).collect(Collectors.toList());
	futures.addAll(this.generatorBundleCache.values().stream().map(this::saveGeneratorBundle)
		.collect(Collectors.toList()));
	futures.addAll(
		this.generatorDataCache.values().stream().map(this::saveGeneratorData).collect(Collectors.toList()));

	return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v ->
	// Return true if all futures completed successfully
	futures.stream().allMatch(CompletableFuture::join));
    }

    /**
     * This method removes from cache and database every generator that is related
     * to given gamemode.
     *
     * @param gameMode GameMode addon which generators must be removed.
     */
    public void wipeGameModeGenerators(GameModeAddon gameMode) {
	final String objectKey = gameMode.getDescription().getName().toLowerCase();

	// Collect all generators
	List<String> keySet = new ArrayList<>(this.generatorTierCache.keySet());

	// Remove everything that starts with gamemode name.
	keySet.forEach(uniqueId -> {
	    if (uniqueId.startsWith(objectKey)) {
		this.generatorTierCache.remove(uniqueId);
		this.generatorTierDatabase.deleteID(uniqueId);
	    }
	});

	this.addon.log("All generators for " + objectKey + " are removed!");

	// Collect all bundles
	keySet = new ArrayList<>(this.generatorBundleCache.keySet());

	// Remove everything that starts with gamemode name.
	keySet.forEach(uniqueId -> {
	    if (uniqueId.startsWith(objectKey)) {
		this.generatorBundleCache.remove(uniqueId);
		this.generatorBundleDatabase.deleteID(uniqueId);
	    }
	});

	this.addon.log("All bundles for " + objectKey + " are removed!");
    }

    /**
     * This method removes from cache and database every island data that is related
     * to given gamemode.
     *
     * @param gameModeAddon GameMode addon which generators must be removed.
     */
    public void wipeIslandData(GameModeAddon gameModeAddon) {

	final String objectKey = gameModeAddon.getDescription().getName();

	List<String> keySet = new ArrayList<>(this.generatorDataCache.keySet());

	// Remove everything that starts with gamemode name.
	keySet.forEach(uniqueId -> {
	    if (uniqueId.startsWith(objectKey)) {
		this.generatorDataCache.remove(uniqueId);
		this.generatorDataDatabase.deleteID(uniqueId);
	    }
	});

	this.addon.log("All island data for " + objectKey + " are removed!");
    }

    // ---------------------------------------------------------------------
    // Section: Generator related methods
    // ---------------------------------------------------------------------

    /**
     * This method migrated generator tier to a newer version.
     *
     * @param generator Generator that must be migrated.
     */
    private void migrateGeneratorTier(GeneratorTierObject generator) {
	if (generator.getTreasureChanceMap() != null && !generator.getTreasureChanceMap().isEmpty()) {
	    generator.setTreasureItemChanceMap(new TreeMap<>());

	    generator.getTreasureChanceMap().forEach(
		    (chance, material) -> generator.getTreasureItemChanceMap().put(chance, new ItemStack(material)));

	    generator.setTreasureChanceMap(null);
	    this.saveGeneratorTier(generator);
	}
    }

    /**
     * This method removes given generator tier from database.
     *
     * @param generatorTier generator tier that must be removed.
     */
    public void wipeGeneratorTier(GeneratorTierObject generatorTier) {
	if (this.generatorTierCache.containsKey(generatorTier.getUniqueId())) {
	    this.generatorTierCache.remove(generatorTier.getUniqueId());
	    this.generatorTierDatabase.deleteID(generatorTier.getUniqueId());

	    // remove generator tier from all bundles.
	    this.generatorBundleCache.values().forEach(bundle -> {
		if (bundle.getGeneratorTiers().remove(generatorTier.getUniqueId())) {
		    // If removing was successfully, save bundle.
		    this.saveGeneratorBundle(bundle);
		}
	    });
	}
    }

    /**
     * This method returns generator tier object from given generator Id.
     *
     * @param generatorId Generator Id that must be returned.
     * @return GeneratorTierObject with given Id, or null.
     */
    public GeneratorTierObject getGeneratorByID(String generatorId) {
	return this.generatorTierCache.get(generatorId);
    }

    /**
     * This method returns active generator tier object for island at given
     * location.
     *
     * @param island        Island on which generation is happening.
     * @param location      Location of the block.
     * @param generatorType Generator type.
     * @return GeneratorTierObject that operates in given island or null.
     */
    public @Nullable GeneratorTierObject getGeneratorTier(@Nullable Island island, Location location,
	    GeneratorTierObject.GeneratorType generatorType) {
	// Gets biome from location.
	final Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockY(),
		location.getBlockZ());

	if (island == null) {
	    // No islands at given location, find and use default generator tier.
	    return this.findDefaultGeneratorTier(location.getWorld(), generatorType, biome);
	}

	this.addIslandData(island);
	GeneratorDataObject data = this.generatorDataCache.get(island.getUniqueId());

	// TODO: It is necessary to reset user cache when import new generators are don.
	// I changed implementation for faster work, so it gets challenges on island
	// data loading,
	// but it cache it inside island data. So when someone overwrites generators in
	// database,
	// but player data are not resetted, then all their generators are locally
	// different then
	// used in database.

	// Find generator from active generator list.
	Optional<GeneratorTierObject> optionalGenerator = data.getActiveGeneratorList().stream().
	// Map generator id with proper generator object.
		map(this.addon.getAddonManager()::getGeneratorByID).
		// Remove generators that are apparently removed from database.
		filter(Objects::nonNull).
		// Filter out generators that are not deployed.
		filter(GeneratorTierObject::isDeployed).
		// Filter objects with the same generator type.
		filter(generator -> generator.getGeneratorType().includes(generatorType)).
		// Filter out objects with incorrect biomes.
		filter(generator -> generator.getRequiredBiomes().isEmpty()
			|| generator.getRequiredBiomes().contains(biome))
		.
		// Get a generator that has largest priority and has required biome
		max((o1, o2) -> {
		    // If required biomes is empty, the it works in all biomes.
		    boolean o1HasBiome = o1.getRequiredBiomes().isEmpty() || o1.getRequiredBiomes().contains(biome);
		    boolean o2HasBiome = o2.getRequiredBiomes().isEmpty() || o2.getRequiredBiomes().contains(biome);

		    if (o1HasBiome != o2HasBiome) {
			return Boolean.compare(o1HasBiome, o2HasBiome);
		    } else if (o1.getPriority() != o2.getPriority()) {
			// Larger priority must be in the end.
			return Integer.compare(o1.getPriority(), o2.getPriority());
		    } else if (o1.isDefaultGenerator() || o2.isDefaultGenerator()) {
			// Default should be placed last one.
			return Boolean.compare(o2.isDefaultGenerator(), o1.isDefaultGenerator());
		    } else if (o1.getGeneratorType() != o2.getGeneratorType()) {
			// Compare by type. Generators which are more specified should be first.
			return o1.getGeneratorType().compareTo(o2.getGeneratorType());
		    } else {
			// Compare by unique id.
			return o1.getUniqueId().compareTo(o2.getUniqueId());
		    }
		});

	return optionalGenerator.orElse(this.findDefaultGeneratorTier(location.getWorld(), generatorType, biome));
    }

    /**
     * This method iterates through all world generators from given type and tries
     * to find if someone is set to be default. It finds first one.
     *
     * @param world         of type World
     * @param generatorType of type GeneratorType
     * @param biome         of the generator location.
     * @return GeneratorTierObject generator tier object
     */
    private @Nullable GeneratorTierObject findDefaultGeneratorTier(World world,
	    GeneratorTierObject.GeneratorType generatorType, Biome biome) {
	String gameMode = this.addon.getPlugin().getIWM().getAddon(world)
		.map(gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

	if (gameMode.isEmpty()) {
	    // If not a gamemode world then return.
	    return null;
	}

	// Find default generator from cache.
	// Filter all default generators
	// Filter generators with necessary type.
	// Filter generators that starts with name.
	// Get a generator that has largest priority and has required biome
	// Return return the generator tier with max value or null
	return this.generatorTierCache.values().stream().filter(GeneratorTierObject::isDefaultGenerator)
		.filter(generator -> generator.getGeneratorType().includes(generatorType))
		.filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase()))
		.filter(generator -> generator.getRequiredBiomes().isEmpty()
			|| generator.getRequiredBiomes().contains(biome))
		.max((o1, o2) -> {
		    // If required biomes is empty, the it works in all biomes.
		    boolean o1HasBiome = o1.getRequiredBiomes().isEmpty() || o1.getRequiredBiomes().contains(biome);
		    boolean o2HasBiome = o2.getRequiredBiomes().isEmpty() || o2.getRequiredBiomes().contains(biome);

		    if (o1HasBiome != o2HasBiome) {
			return Boolean.compare(o1HasBiome, o2HasBiome);
		    } else if (o1.getPriority() != o2.getPriority()) {
			// Larger priority must be in the end.
			return Integer.compare(o1.getPriority(), o2.getPriority());
		    } else if (o1.getGeneratorType() != o2.getGeneratorType()) {
			// Compare by type. Generators which are more specified should be first.
			return o1.getGeneratorType().compareTo(o2.getGeneratorType());
		    } else {
			// Compare by unique id.
			return o1.getUniqueId().compareTo(o2.getUniqueId());
		    }
		}).orElse(null);
    }

    /**
     * This method returns all generator tiers for given world.
     *
     * @param world World which generators must be returned.
     * @return List of generator tier objects for given world.
     */
    public List<GeneratorTierObject> getAllGeneratorTiers(World world) {
	String gameMode = this.addon.getPlugin().getIWM().getAddon(world)
		.map(gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

	if (gameMode.isEmpty()) {
	    // If not a gamemode world then return.
	    return Collections.emptyList();
	}

	// Find default generator from cache.
	return this.generatorTierCache.values().stream().
	// Filter generators that starts with name.
		filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase())).
		// Sort in order: default generators are first, followed by lowest priority,
		// generator type and then by generator name.
		sorted(Comparator.comparing(GeneratorTierObject::isDefaultGenerator).reversed()
			.thenComparing(GeneratorTierObject::getPriority)
			.thenComparing(GeneratorTierObject::getGeneratorType)
			.thenComparing(GeneratorTierObject::getFriendlyName))
		.
		// Return as list collection.
		collect(Collectors.toList());
    }

    /**
     * This method returns all generator tiers for given world accessible by user.
     *
     * @param world World which generators must be returned.
     * @param user  Targeted user.
     * @return List of generator tier objects for given world.
     */
    public List<GeneratorTierObject> getIslandGeneratorTiers(World world, User user) {
	return this.getIslandGeneratorTiers(world, this.getGeneratorData(user, world));
    }

    /**
     * This method returns all generator tiers for given world accessible by island.
     *
     * @param world      World which generators must be returned.
     * @param islandData IslandData which generators must be returned
     * @return List of generator tier objects for given world.
     */
    public List<GeneratorTierObject> getIslandGeneratorTiers(World world, @Nullable GeneratorDataObject islandData) {
	// Optimization could be done by generating bundles for each situation, but
	// currently I do not
	// think it should be an actual problem here.
	Stream<GeneratorTierObject> generatorTiers = this.getAllGeneratorTiers(world).stream()
		.filter(GeneratorTierObject::isDeployed);

	if (islandData != null) {
	    // Owner bundle has larger priority then island bundle.
	    if (islandData.getOwnerBundle() != null
		    && this.generatorBundleCache.containsKey(islandData.getOwnerBundle())) {
		GeneratorBundleObject bundle = this.generatorBundleCache.get(islandData.getOwnerBundle());

		return generatorTiers
			.filter(generatorTier -> bundle.getGeneratorTiers().contains(generatorTier.getUniqueId()))
			.collect(Collectors.toList());
	    } else if (islandData.getIslandBundle() != null
		    && this.generatorBundleCache.containsKey(islandData.getIslandBundle())) {
		GeneratorBundleObject bundle = this.generatorBundleCache.get(islandData.getIslandBundle());

		return generatorTiers
			.filter(generatorTier -> bundle.getGeneratorTiers().contains(generatorTier.getUniqueId()))
			.collect(Collectors.toList());
	    } else {
		return generatorTiers.collect(Collectors.toList());
	    }
	} else {
	    return generatorTiers.collect(Collectors.toList());
	}
    }

    /**
     * Tis method finds all default generators in given world.
     *
     * @param world World where generators must be searched
     * @return List with default generators.
     */
    public List<GeneratorTierObject> findDefaultGeneratorList(World world) {
	String gameMode = this.addon.getPlugin().getIWM().getAddon(world)
		.map(gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

	if (gameMode.isEmpty()) {
	    // If not a gamemode world then return.
	    return Collections.emptyList();
	}

	// Find default generator from cache.
	return this.generatorTierCache.values().stream().
	// Filter generators that starts with name.
		filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase())).
		// Filter deployed and default generators.
		filter(GeneratorTierObject::isDefaultGenerator).filter(GeneratorTierObject::isDeployed).
		// Return as list collection.
		collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Section: Generator Bundle Methods
    // ---------------------------------------------------------------------

    /**
     * This method returns all generator bundles for given world.
     *
     * @param world World which generators must be returned.
     * @return List of generator bundle objects for given world.
     */
    public List<GeneratorBundleObject> getAllGeneratorBundles(World world) {
	String gameMode = this.addon.getPlugin().getIWM().getAddon(world)
		.map(gameModeAddon -> gameModeAddon.getDescription().getName()).orElse("");

	if (gameMode.isEmpty()) {
	    // If not a gamemode world then return.
	    return Collections.emptyList();
	}

	// Find default generator from cache.
	return this.generatorBundleCache.values().stream().
	// Filter generators that starts with name.
		filter(generator -> generator.getUniqueId().startsWith(gameMode.toLowerCase())).
		// Sort in order: default generators are first, followed by lowest priority,
		// Return as list collection.
		collect(Collectors.toList());
    }

    /**
     * Gets bundle by id.
     *
     * @param bundleId the bundle id
     * @return the bundle by id
     */
    public GeneratorBundleObject getBundleById(String bundleId) {
	return this.generatorBundleCache.get(bundleId);
    }

    /**
     * This method removes given bundle tier from database.
     *
     * @param bundleObject bundle tier that must be removed.
     */
    public void wipeBundle(GeneratorBundleObject bundleObject) {
	if (this.generatorBundleCache.containsKey(bundleObject.getUniqueId())) {
	    this.generatorBundleCache.remove(bundleObject.getUniqueId());
	    this.generatorBundleDatabase.deleteID(bundleObject.getUniqueId());
	}
    }

    // ---------------------------------------------------------------------
    // Section: Generator Island Data
    // ---------------------------------------------------------------------

    /**
     * This method checks every island in stored worlds for user and loads them in
     * cache.
     *
     * @param uniqueId User unique id.
     */
    public void loadUserIslands(UUID uniqueId) {
	this.operationWorlds.stream().map(world -> this.addon.getIslands().getIsland(world, uniqueId))
		.filter(Objects::nonNull).forEach(island -> {
		    if (island.getOwner() == uniqueId) {
			// Owner island must be validated.
			this.validateIslandData(island);
		    } else {
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
    private void addIslandData(@NotNull Island island) {
	final String uniqueID = island.getUniqueId();

	if (this.generatorDataCache.containsKey(uniqueID)) {
	    return;
	}

	// The island is not in the cache
	// Check if the island exists in the database

	if (this.generatorDataDatabase.objectExists(uniqueID)) {
	    // Load player from database
	    GeneratorDataObject data = this.generatorDataDatabase.loadObject(uniqueID);
	    // Store in cache

	    if (data != null) {
		this.generatorDataCache.put(uniqueID, data);
	    } else {
		this.addon.logError("Could not load NULL generator data object.");
	    }
	} else {
	    // Create the island data
	    GeneratorDataObject pd = new GeneratorDataObject();
	    pd.setUniqueId(uniqueID);

	    // Update island data
	    pd.setIslandWorkingRange(this.addon.getSettings().getDefaultWorkingRange());
	    pd.setIslandActiveGeneratorCount(this.addon.getSettings().getDefaultActiveGeneratorCount());
	    pd.setIslandBundle(null);

	    // Update owner data.
	    if (!island.isSpawn()) {
		this.updateOwnerBundle(island, pd);
		this.updateOwnerGeneratorCount(island, pd);
		this.updateOwnerWorkingRange(island, pd);
	    }

	    // Save data.
	    this.saveGeneratorData(pd);

	    // Add to cache
	    this.generatorDataCache.put(uniqueID, pd);
	}
    }

    /**
     * This method adds, validates and returns island generator data for given
     * island.
     *
     * @param island Island which data must be returned.
     * @return GeneratorDataObject or null if failed to create.
     */
    public @Nullable GeneratorDataObject validateIslandData(@Nullable Island island) {
	if (island == null || island.getOwner() == null) {
	    return null;
	}

	this.addIslandData(island);
	GeneratorDataObject dataObject = this.generatorDataCache.get(island.getUniqueId());

	if (dataObject == null) {
	    return null;
	}

	// Validate data in generator object.
	this.updateOwnerBundle(island, dataObject);
	this.updateOwnerGeneratorCount(island, dataObject);
	this.updateOwnerWorkingRange(island, dataObject);

	// Call check command which finds unlocked generators.
	this.checkGeneratorUnlockStatus(island, null, null);

	// Remove Generators From Active Generator List:
	dataObject.getActiveGeneratorList().removeIf(generator -> {
	    // if generator is not unlocked (by admin?) then remove from active list.
	    return !dataObject.getUnlockedTiers().contains(generator) ||
	    // if generator does not exist anymore, most likely after reimporting.
		    this.getGeneratorByID(generator) == null ||
	    // if generator is default then it should not be here
		    this.getGeneratorByID(generator).isDefaultGenerator() ||
	    // if generator is undeployed, remove from active list too.
		    !this.getGeneratorByID(generator).isDeployed();
	});

	if (dataObject.getActiveGeneratorCount() > 0
		&& dataObject.getActiveGeneratorCount() < dataObject.getActiveGeneratorList().size()) {
	    // There are more active generators then allowed.
	    // Start to remove from first element.

	    Iterator<String> activeGenerators = new ArrayList<>(dataObject.getActiveGeneratorList()).iterator();

	    while (dataObject.getActiveGeneratorList().size() > dataObject.getActiveGeneratorCount()
		    && activeGenerators.hasNext()) {
		dataObject.getActiveGeneratorList().remove(activeGenerators.next());
	    }
	}

	return dataObject;
    }

    /**
     * This method updates owner working range for island.
     *
     * @param island     Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateOwnerWorkingRange(@NotNull Island island, @NotNull GeneratorDataObject dataObject) {
	// Permission check can be done only to a player object.
	if (island.getOwner() != null) {
	    User owner = User.getInstance(island.getOwner());

	    if (!owner.isPlayer()) {
		return;
	    }

	    // Update max island generation range.
	    int permissionRange = Utils.getPermissionValue(owner,
		    Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.max-range"), 0);
	    dataObject.setOwnerWorkingRange(permissionRange);
	}
    }

    /**
     * This method updates owner active generator count.
     *
     * @param island     Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateOwnerGeneratorCount(@NotNull Island island, @NotNull GeneratorDataObject dataObject) {
	// Permission check can be done only to a player object.
	if (island.getOwner() != null) {
	    User owner = User.getInstance(island.getOwner());

	    if (!owner.isPlayer()) {
		return;
	    }

	    // Update max active generator count.
	    int permissionSize = Utils.getPermissionValue(owner,
		    Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.active-generators"), 0);
	    dataObject.setOwnerActiveGeneratorCount(permissionSize);
	}
    }

    /**
     * This method updates owner bundle for island.
     *
     * @param island     Island object that requires update.
     * @param dataObject Data Object that need to be populated.
     */
    private void updateOwnerBundle(@NotNull Island island, @NotNull GeneratorDataObject dataObject) {
	// Permission check can be done only to a player object.
	if (island.getOwner() != null) {
	    User owner = User.getInstance(island.getOwner());

	    if (!owner.isPlayer()) {
		return;
	    }

	    // Update max island generation range.
	    String permissionBundle = Utils.getPermissionValue(owner,
		    Utils.getPermissionString(island.getWorld(), "[gamemode].stone-generator.bundle"), null);
	    dataObject.setOwnerBundle(permissionBundle);
	}
    }

    /**
     * This method checks for all generators, if they are unlocked.
     *
     * @param island Island which is targeted for unlocking check.
     * @param user   User who triggered check.
     * @param level  New island level value.
     */
    public void checkGeneratorUnlockStatus(Island island, @Nullable User user, @Nullable Long level) {
	if (island == null || island.getOwner() == null) {
	    // Island or island owner is not set.
	    return;
	}

	this.addIslandData(island);
	GeneratorDataObject dataObject = this.generatorDataCache.get(island.getUniqueId());

	if (dataObject == null) {
	    // Could not find any data for current island.
	    return;
	}

	// Update owner bundle, as it may influence island generators.
	this.updateOwnerBundle(island, dataObject);

	// If level is null, check value from addon.
	final long islandLevel = level == null ? this.getIslandLevel(island) : level;
	final User owner = island.isSpawn() ? null : User.getInstance(island.getOwner());
	this.getIslandGeneratorTiers(island.getWorld(), dataObject).stream().
	// Filter out default generators. They are always unlocked and active.
		filter(generator -> !generator.isDefaultGenerator()).
		// Filter out unlocked generators. Not necessary to check them again
		filter(generator -> !dataObject.getUnlockedTiers().contains(generator.getUniqueId())).
		// Filter out generators with larger minimal island level then current island
		// level.
		filter(generator -> generator.getRequiredMinIslandLevel() <= islandLevel).
		// Filter out generators with missing permissions
		filter(generator -> generator.getRequiredPermissions().isEmpty() || owner != null && owner.isOnline()
			&& Utils.matchAllPermissions(owner, generator.getRequiredPermissions()))
		.
		// Now process each generator.
		forEach(generator -> this.unlockGenerator(dataObject, user, island, generator));
    }

    /**
     * This method allows to get generator data for given island.
     *
     * @param island Island which data must be returned.
     * @return instance of GeneratorDataObject.
     */
    public @Nullable GeneratorDataObject getGeneratorData(@Nullable Island island) {
	if (island == null) {
	    return null;
	}

	this.addIslandData(island);
	return this.generatorDataCache.get(island.getUniqueId());
    }

    /**
     * This method allows to get generator data for given user.
     *
     * @param user  User which data must be returned.
     * @param world World where user island must be returned.
     * @return instance of GeneratorDataObject.
     */
    public @Nullable GeneratorDataObject getGeneratorData(@Nullable User user, @NotNull World world) {
	if (user == null) {
	    return null;
	}

	Island island = this.addon.getIslands().getIsland(world, user);

	if (island == null) {
	    return null;
	}

	this.addIslandData(island);
	return this.generatorDataCache.get(island.getUniqueId());
    }

    /**
     * This method unlocks given generator for given island.
     *
     * @param dataObject DataObject where all data will be saved.
     * @param user       the user
     * @param island     Island that unlocks generator.
     * @param generator  Generator that must be unlocked.
     */
    public void unlockGenerator(@NotNull GeneratorDataObject dataObject, @Nullable User user, @NotNull Island island,
	    @NotNull GeneratorTierObject generator) {
	if (!generator.isDeployed() || generator.isDefaultGenerator()) {
	    // Do not add undeployed generators and default generators to the unlock list.
	    if (user != null) {
		Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-cannot-be-unlocked",
			Constants.GENERATOR, generator.getFriendlyName()));
	    }
	    return;
	}
	// Create and call bukkit event to check if unlocking should be cancelled.
	GeneratorUnlockEvent event = new GeneratorUnlockEvent(generator, user, island);
	Bukkit.getPluginManager().callEvent(event);

	if (!event.isCancelled()) {
	    // Add to unlocked generator set.
	    dataObject.getUnlockedTiers().add(generator.getUniqueId());
	    // save data.
	    this.saveGeneratorData(dataObject);

	    if (!this.addon.getSettings().isNotifyUnlockedGenerators()) {
		// Not necessary to notify users.
		return;
	    }

	    // Send message to user
	    if (this.addon.isVaultProvided() && generator.getGeneratorTierCost() > 0) {
		// Send message that generator is available for purchase.

		island.getMemberSet()
			.forEach(uuid -> Utils.sendUnlockMessage(uuid, island, generator, this.addon, false));
	    } else {
		// Send message that generator is available for activation.

		island.getMemberSet()
			.forEach(uuid -> Utils.sendUnlockMessage(uuid, island, generator, this.addon, true));
	    }
	}
    }

    /**
     * This is just a wrapper method that allows to deactivate generator.
     *
     * @param user          User who deactivates generator.
     * @param generatorData Data which will be populated.
     * @param generatorTier Generator that will be removed.
     * @return {@code true} if deactivation was successful, {@code false} otherwise.
     */
    public boolean deactivateGenerator(@NotNull User user, @NotNull GeneratorDataObject generatorData,
	    @NotNull GeneratorTierObject generatorTier) {
	// If generator is not active, do nothing.
	if (!generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId())) {
	    return false;
	}

	// Call event about generator activation.
	GeneratorActivationEvent event = new GeneratorActivationEvent(generatorTier, user, generatorData.getUniqueId(),
		false);
	Bukkit.getPluginManager().callEvent(event);

	if (!event.isCancelled()) {
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-deactivated",
		    Constants.GENERATOR, generatorTier.getFriendlyName()));
	    generatorData.getActiveGeneratorList().remove(generatorTier.getUniqueId());

	    // Save object.
	    this.saveGeneratorData(generatorData);
	    return true;
	} else {
	    // Generator is not deactivated.
	    return false;
	}
    }

    /**
     * This method checks if given user can activate given generator tier. This
     * method includes money withdraw, so it is assumed, that it is used as check
     * before activating the generator tier.
     *
     * @param user          User who will pay for activating.
     * @param island        the island
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be activated.
     * @return {@code true} if can activate, {@code false} if cannot activate.
     */
    public boolean canActivateGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier) {
	if (generatorData.getActiveGeneratorCount() > 0
		&& generatorData.getActiveGeneratorList().size() >= generatorData.getActiveGeneratorCount()) {
	    if (!this.addon.getSettings().isOverwriteOnActive()) {
		// Too many generators.
		Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "active-generators-reached"));
		return false;
	    }
	}

	if (!generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId())) {
	    // Generator is not unlocked. Return false.
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-not-unlocked",
		    Constants.GENERATOR, generatorTier.getFriendlyName()));

	    return false;
	} else if (!generatorData.getPurchasedTiers().contains(generatorTier.getUniqueId())
		&& this.addon.isVaultProvided() && generatorTier.getGeneratorTierCost() > 0) {
	    // Generator is not purchased. Return false.
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-not-purchased",
		    Constants.GENERATOR, generatorTier.getFriendlyName()));

	    return false;
	} else {
	    if (this.addon.isVaultProvided() && generatorTier.getActivationCost() > 0) {
		if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided()) {
		    // Return true only if user has enough money and its removal was successful.

		    if (this.addon.getBankAddon().getBankManager().getBalance(island).getValue() >= generatorTier
			    .getActivationCost()) {
			return true;
		    } else {
			Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "no-credits-bank",
				TextVariables.NUMBER, String.valueOf(generatorTier.getActivationCost())));
			return false;
		    }
		} else {
		    // Return true only if user has enough money and its removal was successful.
		    if (this.addon.getVaultHook().has(user, generatorTier.getActivationCost())) {
			return true;
		    } else {
			Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "no-credits",
				TextVariables.NUMBER, String.valueOf(generatorTier.getActivationCost())));
			return false;
		    }
		}
	    } else {
		// Vault is not enabled or cost is not set. Allow change.
		return true;
	    }
	}
    }

    /**
     * This is just a wrapper method that allows to activate generator with money
     * withdraw.
     *
     * @param user          the user
     * @param island        the island
     * @param generatorData the generator data
     * @param generatorTier the generator tier
     */
    public void activateGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier) {
	this.activateGenerator(user, island, generatorData, generatorTier, false);
    }

    /**
     * This is just a wrapper method that allows to activate generator.
     *
     * @param user          User who activates generator.
     * @param island        the island
     * @param generatorData Data which will be populated.
     * @param generatorTier Generator that will be added.
     * @param bypassCost    the bypass cost
     */
    public void activateGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier,
	    boolean bypassCost) {
	CompletableFuture<Boolean> activateGenerator = new CompletableFuture<>();
	activateGenerator.thenAccept(runActivationTask -> {
	    if (runActivationTask) {
		// Call event about generator activation.
		GeneratorActivationEvent event = new GeneratorActivationEvent(generatorTier, user, island.getUniqueId(),
			true);
		Bukkit.getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
		    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-activated",
			    Constants.GENERATOR, generatorTier.getFriendlyName()));
		    generatorData.getActiveGeneratorList().add(generatorTier.getUniqueId());

		    // check and send message that generator is disabled
		    if (!island.isAllowed(StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR)) {
			Utils.sendMessage(user, user
				.getTranslation(StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR.getHintReference()));
		    }

		    // Save object.
		    this.saveGeneratorData(generatorData);
		}
	    }
	});

	if (this.addon.getSettings().isOverwriteOnActive() && generatorData.getActiveGeneratorCount() > 0
		&& generatorData.getActiveGeneratorList().size() >= generatorData.getActiveGeneratorCount()) {
	    // Try to deactivate first generator, because overwrite is active.
	    GeneratorTierObject oldGenerator = this
		    .getGeneratorByID(generatorData.getActiveGeneratorList().iterator().next());

	    if (!this.deactivateGenerator(user, generatorData, oldGenerator)) {
		// If deactivation was not successful, send message about reached limit.
		Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "active-generators-reached"));
		activateGenerator.complete(false);
	    }
	}

	if (!activateGenerator.isDone() && this.addon.isVaultProvided() && !bypassCost) {
	    this.withdrawMoney(activateGenerator, user, island, generatorTier.getActivationCost());
	}

	if (!activateGenerator.isDone()) {
	    activateGenerator.complete(true);
	}
    }

    /**
     * This method checks if given user can purchase given generator tier. This
     * method includes money withdraw, so it is assumed, that it is used as check
     * before purchasing the generator tier.
     *
     * @param user          User who will pay for purchase.
     * @param island        GeneratorData linked island.
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be purchased.
     * @return {@code true} if can purchase, {@code false} if cannot purchase.
     */
    public boolean canPurchaseGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier) {

	final User owner = island.isSpawn() || island.getOwner() == null ? null : User.getInstance(island.getOwner());
    NumberFormat numberFormat = NumberFormat.getNumberInstance(owner.getLocale());

	if (generatorData.getPurchasedTiers().contains(generatorTier.getUniqueId())) {
	    // Generator is not unlocked. Return false.
	    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-already-purchased",
		    Constants.GENERATOR, generatorTier.getFriendlyName()));
	    return false;
	} else if (!island.isAllowed(StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION)) {
	    Utils.sendMessage(user, user.getTranslation("general.errors.insufficient-rank", TextVariables.RANK,
                user.getTranslation(RanksManager.getInstance().getRank(island.getRank(user)))));
	    return false;
	} else if (generatorTier.getRequiredMinIslandLevel() > this.getIslandLevel(island)) {
	    // Generator is not unlocked. Return false.
	    Utils.sendMessage(user,
		    user.getTranslation(Constants.MESSAGES + "island-level-not-reached", Constants.GENERATOR,
			    generatorTier.getFriendlyName(), TextVariables.NUMBER,
                        numberFormat.format(generatorTier.getRequiredMinIslandLevel())));
	    return false;
	} else if (!generatorTier.getRequiredPermissions().isEmpty() && (owner == null || !owner.isPlayer()
		|| !generatorTier.getRequiredPermissions().stream().allMatch(owner::hasPermission))) {
	    Optional<String> missingPermission = generatorTier.getRequiredPermissions().stream()
		    .filter(permission -> owner == null || !owner.isPlayer() || !owner.hasPermission(permission))
		    .findAny();

	    // Generator is not unlocked. Return false.
	    missingPermission.ifPresent(
		    s -> Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "missing-permission",
			    Constants.GENERATOR, generatorTier.getFriendlyName(), TextVariables.PERMISSION, s)));
	    return false;
	} else {
	    if (this.addon.isVaultProvided() && generatorTier.getGeneratorTierCost() > 0) {
		if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided()) {
		    if (this.addon.getBankAddon().getBankManager().getBalance(island).getValue() >= generatorTier
			    .getGeneratorTierCost()) {
			// Return true only if user has enough money.
			return true;
		    } else {
		        
			Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "no-credits-buy-bank",
				TextVariables.NUMBER, numberFormat.format(generatorTier.getGeneratorTierCost())));
			return false;
		    }
		} else {
		    if (this.addon.getVaultHook().has(user, generatorTier.getGeneratorTierCost())) {
			// Return true only if user has enough money.
			return true;
		    } else {
			Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "no-credits-buy",
                    TextVariables.NUMBER, numberFormat.format(generatorTier.getGeneratorTierCost())));
			return false;
		    }
		}
	    } else {
		// Vault is not enabled or cost is not set. Allow change.
		return true;
	    }
	}
    }

    /**
     * This method adds generator tier to purchased generators without bypassing
     * cost.
     *
     * @param user          the user
     * @param island        the island
     * @param generatorData the generator data
     * @param generatorTier the generator tier
     */
    public void purchaseGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier) {
	this.purchaseGenerator(user, island, generatorData, generatorTier, false);
    }

    /**
     * This method adds generator tier to purchased generators.
     *
     * @param user          User who will pays.
     * @param island        the island
     * @param generatorData Data that stores island generators.
     * @param generatorTier Generator tier that need to be purchased.
     * @param bypassCost    indicate if cost can be bypassed.
     */
    public void purchaseGenerator(@NotNull User user, @NotNull Island island,
	    @NotNull GeneratorDataObject generatorData, @NotNull GeneratorTierObject generatorTier,
	    boolean bypassCost) {
	CompletableFuture<Boolean> purchaseGenerator = new CompletableFuture<>();
	purchaseGenerator.thenAccept(runActivationTask -> {
	    if (runActivationTask) {
		// Call event about successful purchase
		Bukkit.getPluginManager()
			.callEvent(new GeneratorBuyEvent(generatorTier, user, generatorData.getUniqueId()));

		Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "generator-purchased",
			Constants.GENERATOR, generatorTier.getFriendlyName()));
		generatorData.getPurchasedTiers().add(generatorTier.getUniqueId());

		// Save object.
		this.saveGeneratorData(generatorData);
	    }
	});

	if (this.addon.isVaultProvided() && !bypassCost) {
	    this.withdrawMoney(purchaseGenerator, user, island, generatorTier.getGeneratorTierCost());
	}

	if (!purchaseGenerator.isDone()) {
	    purchaseGenerator.complete(true);
	}
    }

    /**
     * Withdraw money for activating/purchasing generator tier.
     *
     * @param withdrawStage the change biome stage
     * @param user          the user
     * @param island        the island
     * @param money         the money
     */
    private void withdrawMoney(CompletableFuture<Boolean> withdrawStage, User user, Island island, double money) {
	if(money <= 0.0)
		return;

	if (this.addon.getSettings().isUseBankAccount() && this.addon.isBankProvided()) {
	    BankManager bankManager = this.addon.getBankAddon().getBankManager();
	    bankManager.withdraw(user, island, new Money(money), TxType.WITHDRAW).thenAccept(response -> {
		if (response != BankResponse.SUCCESS) {
		    Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "could-not-remove-money"));
		    withdrawStage.complete(false);
		}
	    });
	} else {
	    EconomyResponse withdraw = this.addon.getVaultHook().withdraw(user, money);

	    if (!withdraw.transactionSuccess()) {
		// Something went wrong on withdraw.

		Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "could-not-remove-money"));
		this.addon.logError(withdraw.errorMessage);
		withdrawStage.complete(false);
	    }
	}
    }

    /**
     * This method removes given data object from cache and database.
     *
     * @param uniqueId Object that must be removed.
     */
    public void wipeGeneratorData(String uniqueId) {
	this.generatorDataCache.remove(uniqueId);
	this.generatorDataDatabase.deleteID(uniqueId);
    }

    /**
     * This method removes given data object from cache and database.
     *
     * @param dataObject Object that must be removed.
     */
    public void wipeGeneratorData(GeneratorDataObject dataObject) {
	this.wipeGeneratorData(dataObject.getUniqueId());
    }

    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------

    /**
     * This method returns if Stone generator should operate in given world.
     *
     * @param world World object that must be checked.
     * @return <code>true</code> if addon should work in given world.
     */
    public boolean canOperateInWorld(World world) {
	return this.operationWorlds.contains(world);
    }

    /**
     * This method returns true if offline generation is enabled or at least one
     * member of island is online.
     *
     * @param location Location of the generated block.
     * @return true if offline generation is enabled or at least one member is
     *         online.
     */
    public boolean isMembersOnline(Location location) {
	if (this.addon.getSettings().isOfflineGeneration()) {
	    return true;
	}

	Optional<Island> optionalIsland = this.addon.getIslands().getIslandAt(location);

	if (optionalIsland.isPresent()) {
	    for (UUID member : optionalIsland.get().getMemberSet()) {
		if (User.getInstance(member).isOnline()) {
		    return true;
		}
	    }
	}

	return false;
    }

    /**
     * This method returns long that represents given island level.
     *
     * @param island the island.
     * @return Island level
     */
    public long getIslandLevel(Island island) {
	if (!this.addon.isLevelProvided()) {
	    // No level addon. Return max value.
	    return Long.MAX_VALUE;
	}

	return this.addon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner());
    }

    /**
     * This method returns long that represents given user level.
     *
     * @param user the user.
     * @return Island level
     */
    public long getIslandLevel(User user) {
	if (!this.addon.isLevelProvided()) {
	    // No level addon. Return max value.
	    return Long.MAX_VALUE;
	}

	return this.addon.getLevelAddon().getIslandLevel(user.getWorld(), user.getUniqueId());
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

    /**
     * Variable stores map that links String to loaded generator bundle object.
     */
    private final Map<String, GeneratorBundleObject> generatorBundleCache;

    /**
     * Variable stores database of generator bundle objects.
     */
    private final Database<GeneratorBundleObject> generatorBundleDatabase;
}
