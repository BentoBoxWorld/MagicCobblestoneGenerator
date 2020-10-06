//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.managers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class will manage importing generators from template into database.
 */
public class StoneGeneratorImportManager
{
	/**
	 * Default constructor.
	 * @param addon Instance of addon object.
	 */
	public StoneGeneratorImportManager(StoneGeneratorAddon addon)
	{
		this.addon = addon;

		this.generatorFile = new File(this.addon.getDataFolder(), "generatorTemplate.yml");

		if (!this.generatorFile.exists())
		{
			this.addon.saveResource("generatorTemplate.yml", false);
		}
	}

	// ---------------------------------------------------------------------
	// Section: Template Methods
	// ---------------------------------------------------------------------


	/**
	 * This method imports generator tiers from template
	 *
	 * @param user - user
	 * @param world - world to import into
	 * @return true if successful
	 */
	public boolean importFile(@Nullable User user, World world)
	{
		if (!this.generatorFile.exists())
		{
			if (user != null)
			{
				user.sendMessage(Constants.ERRORS + "no-file");
			}

			return false;
		}

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(this.generatorFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			if (user != null)
			{
				user.sendMessage(Constants.ERRORS + "no-load",
					TextVariables.DESCRIPTION,
					e.getMessage());
			}
			else
			{
				this.addon.logError("Exception when loading file. " + e.getMessage());
			}

			return false;
		}

		Optional<GameModeAddon> optional = this.addon.getPlugin().getIWM().getAddon(world);

		if (!optional.isPresent())
		{
			if (user != null)
			{
				user.sendMessage(Constants.ERRORS + "not-a-gamemode-world",
					Constants.WORLD, world.getName());
			}
			else
			{
				this.addon.logWarning("Given world is not a gamemode world.");
			}

			return false;
		}

		this.addon.getAddonManager().wipeGameModeGenerators(optional);
		this.createGenerators(config, user, optional.get());

		return true;
	}


	/**
	 * This method creates generator tier object from config file.
	 * @param config YamlConfiguration that contains all generators.
	 * @param user User who calls reading.
	 * @param gameMode GameMode in which generator tiers must be imported
	 */
	private void createGenerators(YamlConfiguration config, @Nullable User user, GameModeAddon gameMode)
	{
		final String prefix = gameMode.getDescription().getName().toLowerCase() + "_";

		int generatorSize = 0;

		ConfigurationSection reader = config.getConfigurationSection("tiers");

		// TODO: 1.15.2 compatibility.
		boolean canAddBasaltGenerator = Material.getMaterial("BASALT") != null;
		Map<String, Biome> biomeMap = Utils.getBiomeNameMap();

		for (String generatorId : reader.getKeys(false))
		{
			GeneratorTierObject generatorTier = new GeneratorTierObject();
			generatorTier.setUniqueId(prefix + generatorId.toLowerCase());

			ConfigurationSection details = reader.getConfigurationSection(generatorId);

			if (details != null)
			{
				// Read prefix
				generatorTier.setFriendlyName(details.getString("prefix",
					generatorId.replaceAll("_", " ")));
				// Read description
				generatorTier.setDescription(details.getStringList("description"));
				// Read icon

				// TODO: 1.15.2 compatibility
				ItemStack icon = ItemParser.parse(details.getString("icon"));
				generatorTier.setGeneratorIcon(icon == null ? new ItemStack(Material.PAPER) : icon);

				// Read type
				generatorTier.setGeneratorType(GeneratorTierObject.GeneratorType.valueOf(
					details.getString("type", "COBBLESTONE").toUpperCase()));

				if (!canAddBasaltGenerator &&
					generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT))
				{
					// Basalt generators cannot be added yet in 1.15.2
					continue;
				}

				// Get default generator option
				generatorTier.setDefaultGenerator(details.getBoolean("default", false));
				// Set priority
				generatorTier.setPriority(details.getInt("priority", 1));
				// Set activation cost
				generatorTier.setActivationCost(details.getDouble("activation-cost", 0.0));

				// Search and read requirements only if it is not default generator.
				if (!generatorTier.isDefaultGenerator())
				{
					this.populateRequirements(generatorTier,
						details.getConfigurationSection("requirements"),
						biomeMap);
				}

				// Read blocks
				this.populateMaterials(generatorTier, details.getConfigurationSection("blocks"));
				// Read treasures
				this.populateTreasures(generatorTier, details.getConfigurationSection("treasure"));
			}

			// Save object in database.
			this.addon.getAddonManager().saveGeneratorTier(generatorTier);
			this.addon.getAddonManager().loadGeneratorTier(generatorTier, false, null, true);
			generatorSize++;
		}

		reader = config.getConfigurationSection("bundles");
		int bundleSize = 0;

		for (String bundleId : reader.getKeys(false))
		{
			GeneratorBundleObject generatorBundle = new GeneratorBundleObject();
			generatorBundle.setUniqueId(prefix + bundleId.toLowerCase());

			ConfigurationSection details = reader.getConfigurationSection(bundleId);

			if (details != null)
			{
				// Read prefix
				generatorBundle.setFriendlyName(details.getString("prefix",
					bundleId.replaceAll("_", " ")));
				// Read description
				generatorBundle.setDescription(details.getStringList("description"));
				// Read icon
				ItemStack icon = ItemParser.parse(details.getString("icon"));
				generatorBundle.setGeneratorIcon(icon == null ? new ItemStack(Material.PAPER) : icon);
				// Read generators
				generatorBundle.setGeneratorTiers(
					details.getStringList("generators").stream().
						map(id -> prefix + id).
						collect(Collectors.toSet()));
			}

			// Save object in database.
			this.addon.getAddonManager().saveGeneratorBundle(generatorBundle);
			this.addon.getAddonManager().loadGeneratorBundle(generatorBundle, false, null, true);
			bundleSize++;
		}

		if (user != null)
		{
			user.sendMessage(Constants.MESSAGE + "import-count",
				TextVariables.NUMBER,
				String.valueOf(generatorSize));
			user.sendMessage(Constants.MESSAGE + "import-bundle-count",
				TextVariables.NUMBER,
				String.valueOf(bundleSize));
		}

		this.addon.log("Imported " + generatorSize + " generator tiers and " +
			bundleSize + " bundles into database.");
	}


	/**
	 * This method populates generatorTier object with requirements from given config section.
	 * @param generatorTier GeneratorTier that must be populated.
	 * @param requirements Config that contains data.
	 */
	private void populateRequirements(GeneratorTierObject generatorTier,
		ConfigurationSection requirements,
		Map<String, Biome> biomeMap)
	{
		if (requirements != null)
		{
			generatorTier.setRequiredMinIslandLevel(requirements.getLong("island-level", 0));
			generatorTier.setRequiredPermissions(new HashSet<>(requirements.getStringList("required-permissions")));

			// TODO: 1.15.2 compatibility. Non-existing biomes will be removed.
			Set<Biome> biomeSet = requirements.getStringList("required-biomes").stream().
				map(name -> biomeMap.get(name.toUpperCase())).
				filter(Objects::nonNull).
				collect(Collectors.toSet());

			generatorTier.setRequiredBiomes(biomeSet);
			generatorTier.setGeneratorTierCost(requirements.getDouble("purchase-cost", 0.0));
		}
	}


	/**
	 * This method populates generatorTier object with materials from given config section.
	 * @param generatorTier GeneratorTier that must be populated.
	 * @param materials Config that contains data.
	 */
	private void populateMaterials(GeneratorTierObject generatorTier,
		ConfigurationSection materials)
	{
		if (materials != null)
		{
			TreeMap<Double, Material> blockChances = new TreeMap<>();

			for (String materialKey : materials.getKeys(false))
			{
				try
				{
					Material material = Material.valueOf(materialKey.toUpperCase());
					double lastEntry = blockChances.isEmpty() ? 0D : blockChances.lastKey();
					blockChances.put(lastEntry + materials.getDouble(materialKey, 0), material);
				}
				catch (Exception e)
				{
					this.addon.logWarning("Unknown material (" + materialKey +
						") in generatorTemplate.yml blocks section for tier " +
						generatorTier.getUniqueId() + ". Skipping...");
				}
			}

			generatorTier.setBlockChanceMap(blockChances);
		}
	}


	/**
	 * This method populates generatorTier object with treasures from given config section.
	 * @param generatorTier GeneratorTier that must be populated.
	 * @param treasures Config that contains data.
	 */
	private void populateTreasures(GeneratorTierObject generatorTier,
		ConfigurationSection treasures)
	{
		if (treasures != null)
		{
			generatorTier.setTreasureChance(treasures.getDouble("chance", 0));
			generatorTier.setMaxTreasureAmount(treasures.getInt("amount", 0));

			// Populate treasures
			ConfigurationSection materials = treasures.getConfigurationSection("material");

			if (materials != null)
			{
				TreeMap<Double, Material> blockChances = new TreeMap<>();

				for (String materialKey : materials.getKeys(false))
				{
					try
					{
						Material material = Material.valueOf(materialKey.toUpperCase());
						double lastEntry = blockChances.isEmpty() ? 0D : blockChances.lastKey();
						blockChances.put(lastEntry + materials.getDouble(materialKey, 0), material);
					}
					catch (Exception e)
					{
						this.addon.logWarning("Unknown material (" + materialKey +
							") in generatorTemplate.yml blocks section for tier " +
							generatorTier.getUniqueId() + ". Skipping...");
					}
				}

				generatorTier.setTreasureChanceMap(blockChances);
			}
		}
	}


	// ---------------------------------------------------------------------
	// Section: Database Methods
	// ---------------------------------------------------------------------


	/**
	 * This method generates file with a given name that contains everything from database for given world.
	 * It creates exact copy of data from database.
	 * @param user User who triggers this method.
	 * @param world World which generators must be exported.
	 * @param fileName FileName that will be used.
	 * @return {@code true} if export was successful, {@code false} otherwise.
	 */
	public boolean generateDatabaseFile(User user, World world, String fileName)
	{
		File defaultFile = new File(this.addon.getDataFolder(), fileName + ".json");

		if (defaultFile.exists())
		{
			if (user.isPlayer())
			{
				user.sendMessage(Constants.ERRORS + "file-exist");
			}
			else
			{
				this.addon.logWarning(Constants.ERRORS + "file-exist");
			}

			return false;
		}

		try
		{
			if (defaultFile.createNewFile())
			{
				String replacementString = Utils.getGameMode(world).toLowerCase() + "_";
				StoneGeneratorManager manager = this.addon.getAddonManager();

				List<GeneratorTierObject> generatorTierList = manager.getAllGeneratorTiers(world).
					stream().
					map(generatorTier -> {
						// Use clone to avoid any changes in existing challenges.
						GeneratorTierObject clone = generatorTier.clone();
						// Remove gamemode from generatorTier id.
						clone.setUniqueId(generatorTier.getUniqueId().replaceFirst(replacementString, ""));
						return clone;
					}).
					collect(Collectors.toList());

				List<GeneratorBundleObject> levelList = manager.getAllGeneratorBundles(world).
					stream().
					map(generatorBundle -> {
						// Use clone to avoid any changes in existing levels.
						GeneratorBundleObject clone = generatorBundle.clone();
						// Remove gamemode from bundle ID.
						clone.setUniqueId(generatorBundle.getUniqueId().replaceFirst(replacementString, ""));
						// Remove gamemode form generators.
						clone.setGeneratorTiers(generatorBundle.getGeneratorTiers().stream().
							map(id -> id.replaceFirst(replacementString, "")).
							collect(Collectors.toSet()));

						return clone;
					}).
					collect(Collectors.toList());

				DefaultDataHolder defaultChallenges = new DefaultDataHolder();
				defaultChallenges.setGeneratorTiers(generatorTierList);
				defaultChallenges.setGeneratorBundles(levelList);
				defaultChallenges.setVersion(this.addon.getDescription().getVersion());

				try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(defaultFile), StandardCharsets.UTF_8))) {
					writer.write(Objects.requireNonNull(
						new DefaultJSONHandler(this.addon).toJsonString(defaultChallenges)));
				}
			}
		}
		catch (IOException e)
		{
			if (user.isPlayer())
			{
				user.sendMessage(Constants.ERRORS + "file-error");
			}

			this.addon.logError("Could not save json file: " + e.getMessage());
			return false;
		}
		finally
		{
			if (user.isPlayer())
			{
				user.sendMessage(Constants.MESSAGE + "database-export-completed",
					Constants.WORLD, world.getName(),
					Constants.FILE, fileName);
			}
			else
			{
				this.addon.logWarning(Constants.MESSAGE + "database-export-completed");
			}
		}

		return true;
	}


	/**
	 * This method imports everything from database file into database.
	 * @param user User who triggers this method.
	 * @param world World which generators must be exported.
	 * @param fileName FileName that will be used.
	 * @return {@code true} if export was successful, {@code false} otherwise.
	 */
	public boolean importDatabaseFile(User user, World world, String fileName)
	{
		StoneGeneratorManager manager = this.addon.getAddonManager();

		// If exist any generator that is bound to current world, then do not load generators.
		if (!manager.getAllGeneratorTiers(world).isEmpty())
		{
			manager.wipeGameModeGenerators(this.addon.getPlugin().getIWM().getAddon(world));
		}

		try
		{
			// This prefix will be used to all generators. That is a unique way how to separate generators for
			// each game mode.
			String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
			DefaultDataHolder downloadedGenerators = new DefaultJSONHandler(this.addon).loadObject(fileName);

			if (downloadedGenerators == null)
			{
				return false;
			}

			// All new generators should get correct ID. So we need to map it to loaded generators.
			downloadedGenerators.getGeneratorTiers().forEach(generatorTier -> {
				// Set correct generatorTier ID
				generatorTier.setUniqueId(uniqueIDPrefix + generatorTier.getUniqueId());
				// Load generator in memory
				manager.loadGeneratorTier(generatorTier, false, user, user == null);
			});

			downloadedGenerators.getGeneratorBundles().forEach(generatorBundle -> {
				// Set correct bundle ID
				generatorBundle.setUniqueId(uniqueIDPrefix + generatorBundle.getUniqueId());
				// Reset names for all generators.
				generatorBundle.setGeneratorTiers(generatorBundle.getGeneratorTiers().stream().
					map(generatorTier -> uniqueIDPrefix + generatorTier).
					collect(Collectors.toSet()));
				// Load level in memory
				manager.loadGeneratorBundle(generatorBundle, false, user, user == null);
			});
		}
		catch (Exception e)
		{
			addon.getPlugin().logStacktrace(e);
			return false;
		}

		this.addon.getAddonManager().save();

		return true;
	}


	// ---------------------------------------------------------------------
	// Section: Class instances
	// ---------------------------------------------------------------------


	/**
	 * This Class allows to load  and their levels as objects much easier.
	 */
	private static final class DefaultJSONHandler
	{
		/**
		 * This constructor inits JSON builder that will be used to parse challenges.
		 * @param addon Challenges Adddon
		 */
		DefaultJSONHandler(StoneGeneratorAddon addon)
		{
			GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
			// Register adapters
			builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(addon.getPlugin()));

			// Keep null in the database
			builder.serializeNulls();
			// Allow characters like < or > without escaping them
			builder.disableHtmlEscaping();

			this.addon = addon;
			this.gson = builder.setPrettyPrinting().create();
		}


		/**
		 * This method returns json object that is parsed to string. Json object is made from given instance.
		 * @param instance Instance that must be parsed to json string.
		 * @return String that contains JSON information from instance object.
		 */
		String toJsonString(DefaultDataHolder instance)
		{
			// Null check
			if (instance == null)
			{
				this.addon.logError("JSON database request to store a null. ");
				return null;
			}

			return this.gson.toJson(instance);
		}


		/**
		 * This method creates and adds to list all objects from default.json file.
		 * @return List of all objects from default.json that is with T instance.
		 */
		DefaultDataHolder loadObject(String fileName)
		{
			if (!fileName.endsWith(".json"))
			{
				fileName = fileName + ".json";
			}

			File defaultFile = new File(this.addon.getDataFolder(), fileName);

			try (InputStreamReader reader = new InputStreamReader(new FileInputStream(defaultFile), StandardCharsets.UTF_8))
			{
				DefaultDataHolder object = this.gson.fromJson(reader, DefaultDataHolder.class);
				object.setUniqueId(fileName);

				reader.close(); // NOSONAR Required to keep OS file handlers low and not rely on GC

				return object;
			}
			catch (FileNotFoundException e)
			{
				this.addon.logError("Could not load file '" + defaultFile.getName() + "': File not found.");
			}
			catch (Exception e)
			{
				this.addon.logError("Could not load objects " + defaultFile.getName() + " " + e.getMessage());
			}

			return null;
		}


		/**
		 * This method creates and adds to list all objects from default.json file.
		 * @return List of all objects from default.json that is with T instance.
		 */
		DefaultDataHolder loadWebObject(String downloadedObject)
		{
			return this.gson.fromJson(downloadedObject, DefaultDataHolder.class);
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------


		/**
		 * Holds JSON builder object.
		 */
		private Gson gson;

		/**
		 * Holds StoneGeneratorAddon object.
		 */
		private StoneGeneratorAddon addon;
	}


	/**
	 * This is simple object that will allow to store all current generators and bundles
	 * in single file.
	 */
	private static final class DefaultDataHolder implements DataObject
	{
		/**
		 * Default constructor. Creates object with empty lists.
		 */
		DefaultDataHolder()
		{
			this.generatorTiers = Collections.emptyList();
			this.generatorBundles = Collections.emptyList();
			this.version = "";
		}


		/**
		 * This method returns stored challenge list.
		 * @return list that contains default challenges.
		 */
		List<GeneratorTierObject> getGeneratorTiers()
		{
			return generatorTiers;
		}


		/**
		 * This method sets given list as generator tiers.
		 * @param generatorTiers new generator tiers.
		 */
		void setGeneratorTiers(List<GeneratorTierObject> generatorTiers)
		{
			this.generatorTiers = generatorTiers;
		}


		/**
		 * This method returns list of generator bundles.
		 * @return List that contains generator bundles.
		 */
		List<GeneratorBundleObject> getGeneratorBundles()
		{
			return generatorBundles;
		}


		/**
		 * This method sets given list as generator bundle list.
		 * @param generatorBundles new generator bundle list.
		 */
		void setGeneratorBundles(List<GeneratorBundleObject> generatorBundles)
		{
			this.generatorBundles = generatorBundles;
		}


		/**
		 * This method returns the version value.
		 * @return the value of version.
		 */
		public String getVersion()
		{
			return version;
		}


		/**
		 * This method sets the version value.
		 * @param version the version new value.
		 *
		 */
		public void setVersion(String version)
		{
			this.version = version;
		}


		/**
		 * @return unqinue Id;
		 */
		@Override
		public String getUniqueId()
		{
			return this.uniqueId;
		}


		/**
		 * @param uniqueId - unique ID the uniqueId to set
		 */
		@Override
		public void setUniqueId(String uniqueId)
		{
			this.uniqueId = uniqueId;
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------


		/**
		 * Holds a list with generator tier objects.
		 */
		@Expose
		private List<GeneratorTierObject> generatorTiers;

		/**
		 * Holds a list with generator bundles.
		 */
		@Expose
		private List<GeneratorBundleObject> generatorBundles;

		/**
		 * Holds a variable that stores in which addon version file was made.
		 */
		@Expose
		private String version;

		@Expose
		private String uniqueId;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * Addon class instance.
	 */
	private final StoneGeneratorAddon addon;

	/**
	 * Variable stores generatorTemplate.yml location
	 */
	private final File generatorFile;
}
