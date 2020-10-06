//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.managers;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
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
