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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


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
	// Section: Methods
	// ---------------------------------------------------------------------


	/**
	 * This method imports generator tiers from template
	 *
	 * @param user - user
	 * @param world - world to import into
	 * @return true if successful
	 */
	public boolean importFile(User user, World world)
	{
		if (!this.generatorFile.exists())
		{
			user.sendMessage("stonegenerator.errors.no-file");
			return false;
		}

		YamlConfiguration config = new YamlConfiguration();

		try
		{
			config.load(this.generatorFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			user.sendMessage("stonegenerator.errors.no-load",
				"[message]",
				e.getMessage());
			return false;
		}

		Optional<GameModeAddon> optional = this.addon.getPlugin().getIWM().getAddon(world);

		if (!optional.isPresent())
		{
			user.sendMessage("stonegenerator.errors.not-a-gamemode-world");
			return false;
		}

		this.addon.getManager().wipeGameModeGenerators(optional);
		this.createGenerators(config, user, optional.get());

		// Load everything from database
		this.addon.getManager().load();

		return true;
	}


	/**
	 * This method creates generator tier object from config file.
	 * @param config YamlConfiguration that contains all generators.
	 * @param user User who calls reading.
	 * @param gameMode GameMode in which generator tiers must be imported
	 */
	private void createGenerators(YamlConfiguration config, User user, GameModeAddon gameMode)
	{
		final String name = gameMode.getDescription().getName().toLowerCase();

		int size = 0;

		ConfigurationSection reader = config.getConfigurationSection("tiers");

		for (String generatorId : reader.getKeys(false))
		{
			GeneratorTierObject generatorTier = new GeneratorTierObject();
			generatorTier.setUniqueId(name + "-" + generatorId.toLowerCase());

			ConfigurationSection details = reader.getConfigurationSection(generatorId);

			if (details != null)
			{
				// Read name
				generatorTier.setFriendlyName(details.getString("name",
					generatorId.replaceAll("_", " ")));
				// Read description
				generatorTier.setDescription(details.getStringList("description"));
				// Read icon
				generatorTier.setGeneratorIcon(ItemParser.parse(details.getString("icon")));
				// Read type
				generatorTier.setGeneratorType(GeneratorTierObject.GeneratorType.valueOf(
					details.getString("type", "COBBLESTONE").toUpperCase()));
				// Get default generator option
				generatorTier.setDefaultGenerator(details.getBoolean("default", false));
				// Set priority
				generatorTier.setPriority(details.getInt("priority", 1));

				// Search and read requirements only if it is not default generator.
				if (!generatorTier.isDefaultGenerator())
				{
					this.populateRequirements(generatorTier, details.getConfigurationSection("requirements"));
				}

				// Read blocks
				this.populateMaterials(generatorTier, details.getConfigurationSection("blocks"));
				// Read treasures
				this.populateTreasures(generatorTier, details.getConfigurationSection("treasure"));
			}

			// Save object in database.
			this.addon.getManager().saveGeneratorTier(generatorTier);
		}

		user.sendMessage("stonegenerator.messages.import-count",
			TextVariables.NUMBER,
			String.valueOf(size));
	}


	/**
	 * This method populates generatorTier object with requirements from given config section.
	 * @param generatorTier GeneratorTier that must be populated.
	 * @param requirements Config that contains data.
	 */
	private void populateRequirements(GeneratorTierObject generatorTier,
		ConfigurationSection requirements)
	{
		if (requirements != null)
		{
			generatorTier.setRequiredMinIslandLevel(requirements.getLong("island-level", 0));
			generatorTier.setRequiredPermissions(new HashSet<>(requirements.getStringList("required-permissions")));

			Set<Biome> biomeSet = requirements.getStringList("required-biomes").stream().
				map(name -> Biome.valueOf(name.toUpperCase())).
				collect(Collectors.toSet());

			generatorTier.setRequiredBiomes(biomeSet);
			generatorTier.setGeneratorTierCost(requirements.getDouble("upgrade-cost", 0.0));
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
					Material material = Material.valueOf(materialKey);
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
						Material material = Material.valueOf(materialKey);
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
	private File generatorFile;
}
