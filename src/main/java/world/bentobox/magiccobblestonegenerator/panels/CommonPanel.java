package world.bentobox.magiccobblestonegenerator.panels;



import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import org.bukkit.World;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class contains common methods for all panels.
 */
public abstract class CommonPanel
{
	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 * @param addon StoneGeneratorAddon instance.
	 * @param user User who opens panel.
	 */
	protected CommonPanel(StoneGeneratorAddon addon, User user, World world)
	{
		this.addon = addon;
		this.world = world;
		this.manager = addon.getAddonManager();
		this.user = user;

		this.parentPanel = null;

		// Init formatting instances.
		this.tensFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.thousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.tenThousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.hundredThousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());

		// Apply formatting for each instance.
		this.applyFormatting();
	}


	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 * @param parentPanel Parent panel of current panel.
	 */
	protected CommonPanel(CommonPanel parentPanel)
	{
		this.addon = parentPanel.addon;
		this.manager = parentPanel.manager;
		this.user = parentPanel.user;
		this.world = parentPanel.world;

		this.parentPanel = parentPanel;

		// Use parent panel formatting ...
		this.tensFormat = parentPanel.tensFormat;
		this.hundredsFormat = parentPanel.hundredsFormat;
		this.thousandsFormat = parentPanel.thousandsFormat;
		this.tenThousandsFormat = parentPanel.tenThousandsFormat;
		this.hundredThousandsFormat = parentPanel.hundredThousandsFormat;
	}


	/**
	 * This method allows to build panel.
	 */
	public abstract void build();


// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


	/**
	 * This method creates number formatting for user locale.
	 */
	private void applyFormatting()
	{
		this.tensFormat.applyPattern("###.#");
		this.hundredsFormat.applyPattern("###.##");
		this.thousandsFormat.applyPattern("###.###");
		this.tenThousandsFormat.applyPattern("###.####");
		this.hundredThousandsFormat.applyPattern("###.#####");
	}


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This class generates given generator tier description based on input parameters.
	 * @param generator GeneratorTier which description must be generated.
	 * @param isActive Boolean that indicates if generator is active.
	 * @param isUnlocked Boolean that indicates if generator is unlocked.
	 * @param isPurchased Indicates that generator is purchased.
	 * @param islandLevel Long that shows island level.
	 * @return List of strings that describes generator tier.
	 */
	protected List<String> generateGeneratorDescription(GeneratorTierObject generator,
		boolean isActive,
		boolean isUnlocked,
		boolean isPurchased,
		long islandLevel)
	{
		final String reference = Constants.DESCRIPTIONS + "generator.";

		// Get description in single string
		String description = ChatColor.translateAlternateColorCodes('&',
			String.join("\n", generator.getDescription()));

		// Non-memory optimal code used for easier debugging and nicer code layout for my eye :)
		// Get blocks in single string
		String blocks = this.generateBlockListDescription(generator);
		// Get treasures in single string
		String treasures = this.generateTreasuresListDescription(generator);
		// Get requirements in single string
		String requirements = this.generateRequirementsDescription(generator, isUnlocked, islandLevel, false);
		// Get type in single string
		String type = this.generateTypeDescription(generator);
		// Get status in single string
		String status = this.generateStatusDescription(generator, isActive, isUnlocked, isPurchased);

		String returnString = this.user.getTranslation(reference + "lore",
			Constants.DESCRIPTION, description,
			"[blocks]", blocks,
			"[treasures]", treasures,
			"[requirements]", requirements,
			"[type]", type,
			"[status]", status);

		// Remove empty lines and returns as a list.

		return Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
			split("\n")).
			collect(Collectors.toList());
	}


	/**
	 * Admin should see simplified view. It is not necessary to view all unnecessary things.
	 *
	 * @param generator GeneratorTier which description must be generated.
	 * @return List of strings that describes generator tier.
	 */
	protected List<String> generateGeneratorDescription(GeneratorTierObject generator)
	{
		final String reference = Constants.DESCRIPTIONS + "generator.";

		// Get description in single string
		String description = ChatColor.translateAlternateColorCodes('&',
			String.join("\n", generator.getDescription()));

		// Non-memory optimal code used for easier debugging and nicer code layout for my eye :)
		// Get blocks in single string
		String blocks = this.generateBlockListDescription(generator);
		// Get treasures in single string
		String treasures = this.generateTreasuresListDescription(generator);
		// Get requirements in single string
		String requirements = this.generateRequirementsDescription(generator, false, 0L, true);
		// Get type in single string
		String type = this.generateTypeDescription(generator);
		// Get status in single string
		String status = this.generateStatusDescription(generator, false, true, false);

		String returnString = this.user.getTranslation(reference + "lore",
			Constants.DESCRIPTION, description,
			"[blocks]", blocks,
			"[treasures]", treasures,
			"[requirements]", requirements,
			"[type]", type,
			"[status]", status);

		// Remove empty lines and returns as a list.

		return Arrays.stream(returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "").
			split("\n")).
			collect(Collectors.toList());
	}


	/**
	 * This method generates list of required blocks in a single string with using user translations.
	 * @param generator Generator which blocks must be generated.
	 * @return String that contains all blocks with their chances translated in user locale.
	 */
	private String generateBlockListDescription(GeneratorTierObject generator)
	{
		TreeMap<Double, Material> blockChanceMap = generator.getBlockChanceMap();

		if (blockChanceMap.isEmpty())
		{
			return "";
		}

		final String reference = Constants.DESCRIPTIONS + "generator.blocks.";

		StringBuilder blocks = new StringBuilder();
		blocks.append(this.user.getTranslationOrNothing(reference + "title"));

		if (blocks.length() > 0)
		{
			// Append new line after non-empty title.
			blocks.append("\n");
		}

		Double maxValue = blockChanceMap.lastKey();
		Double previousValue = 0.0;

		List<Map.Entry<Double, Material>> materialChanceList =
			blockChanceMap.entrySet().stream().
				sorted(Map.Entry.comparingByKey()).
				collect(Collectors.toList());

		for (Map.Entry<Double, Material> entry : materialChanceList)
		{
			Double value = (entry.getKey() - previousValue) / maxValue * 100.0;

			blocks.append(this.user.getTranslation(reference + "value",
				Constants.BLOCK, Utils.prettifyObject(this.user, entry.getValue()),
				TextVariables.NUMBER, String.valueOf(value),
				Constants.TENS, this.tensFormat.format(value),
				Constants.HUNDREDS, this.hundredsFormat.format(value),
				Constants.THOUSANDS, this.thousandsFormat.format(value),
				Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
				Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));

			previousValue = entry.getKey();

			if (!previousValue.equals(maxValue))
			{
				blocks.append("\n");
			}
		}

		return blocks.toString();
	}


	/**
	 * This method generates list of required treasures in a single string with using user translations.
	 * @param generator Generator which treasures must be generated.
	 * @return String that contains all treasures with their chances translated in user locale.
	 */
	private String generateTreasuresListDescription(GeneratorTierObject generator)
	{
		TreeMap<Double, ItemStack> treasuresChanceMap = generator.getTreasureItemChanceMap();

		if (treasuresChanceMap.isEmpty())
		{
			return "";
		}

		final String reference = Constants.DESCRIPTIONS + "generator.treasures.";

		StringBuilder treasures = new StringBuilder();
		treasures.append(this.user.getTranslationOrNothing(reference + "title"));
		treasures.append("\n");

		Double maxValue = treasuresChanceMap.lastKey();
		Double previousValue = 0.0;

		List<Map.Entry<Double, ItemStack>> treasureChanceList =
			treasuresChanceMap.entrySet().stream().
				sorted(Map.Entry.comparingByKey()).
				collect(Collectors.toList());

		for (Map.Entry<Double, ItemStack> entry : treasureChanceList)
		{
			Double value = (entry.getKey() - previousValue) / maxValue * 100.0 * generator.getTreasureChance();

			treasures.append(this.user.getTranslation(reference + "value",
				Constants.BLOCK, Utils.prettifyObject(this.user, entry.getValue()),
				TextVariables.NUMBER, String.valueOf(value),
				Constants.TENS, this.tensFormat.format(value),
				Constants.HUNDREDS, this.hundredsFormat.format(value),
				Constants.THOUSANDS, this.thousandsFormat.format(value),
				Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
				Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));

			previousValue = entry.getKey();

			if (!previousValue.equals(maxValue))
			{
				treasures.append("\n");
			}
		}

		return treasures.toString();
	}


	/**
	 * This method generates list of requirements (biomes, level, permissions) in a single string
	 * with using user translations.
	 * @param generator Generator which requirements must be generated.
	 * @param isUnlocked Boolean that shows if level and permissions must be showed.
	 * @param islandLevel Island Level for checking if level requirement should be showed.
	 * @param complete Shows if all requirements must be showed. (admin)
	 * @return String that contains requirements for this generator.
	 */
	private String generateRequirementsDescription(GeneratorTierObject generator,
		boolean isUnlocked,
		Long islandLevel,
		boolean complete)
	{
		final String reference = Constants.DESCRIPTIONS + "generator.requirements.";

		String level;

		if (this.addon.isLevelProvided() &&
			generator.getRequiredMinIslandLevel() > 0 &&
			!isUnlocked && generator.getRequiredMinIslandLevel() > islandLevel)
		{
			level = this.user.getTranslationOrNothing(reference + "level",
				Constants.NUMBER, String.valueOf(generator.getRequiredMinIslandLevel()));
		}
		else
		{
			level = "";
		}

		StringBuilder permissions = new StringBuilder();

		if (!generator.getRequiredPermissions().isEmpty() && !isUnlocked)
		{
			// Yes list duplication for complete menu.
			List<String> missingPermissions = generator.getRequiredPermissions().stream().
				filter(permission -> complete || !this.user.hasPermission(permission)).
				sorted().
				collect(Collectors.toList());

			if (!missingPermissions.isEmpty())
			{
				permissions.append(this.user.getTranslationOrNothing(reference + "permission-title"));
				missingPermissions.forEach(permission ->
				{
					permissions.append("\n");
					permissions.append(this.user.getTranslationOrNothing(reference + "permission",
						Constants.PERMISSION, permission));
				});
			}
		}

		StringBuilder biomes = new StringBuilder();

		if (!generator.getRequiredBiomes().isEmpty())
		{
			biomes.append(this.user.getTranslationOrNothing(reference + "biome-title"));

			generator.getRequiredBiomes().stream().
				sorted().
				forEach(biome ->
				{
					biomes.append("\n");
					biomes.append(this.user.getTranslationOrNothing(reference + "biome",
						Constants.BIOME, Utils.prettifyObject(this.user, biome)));
				});
		}
		else
		{
			biomes.append(this.user.getTranslationOrNothing(reference + "any"));
		}

		return this.user.getTranslationOrNothing(reference + "description",
			"[biomes]", biomes.toString(),
			"[level]", level,
			"[missing-permissions]", permissions.toString());
	}


	/**
	 * This method generates generator type string with using user translations.
	 * @param generator Generator which type must be generated.
	 * @return String that contains type for this generator.
	 */
	private String generateTypeDescription(GeneratorTierObject generator)
	{
		final String reference = Constants.DESCRIPTIONS + "generator.type.";

		StringBuilder type = new StringBuilder();

		if (generator.getGeneratorType().equals(GeneratorTierObject.GeneratorType.ANY))
		{
			type.append(this.user.getTranslationOrNothing(reference + "any"));
		}
		else
		{
			type.append(this.user.getTranslationOrNothing(reference + "title"));

			if (generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT))
			{
				type.append("\n");
				type.append(this.user.getTranslationOrNothing(reference + "basalt"));
			}

			if (generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE))
			{
				type.append("\n");
				type.append(this.user.getTranslationOrNothing(reference + "cobblestone"));
			}

			if (generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE))
			{
				type.append("\n");
				type.append(this.user.getTranslationOrNothing(reference + "stone"));
			}
		}

		return type.toString();
	}


	/**
	 * This method generates generator status string with using user translations.
	 * @param generator Generator which status must be generated.
	 * @param isActive Indicate if generator is activated by player.
	 * @param isUnlocked Indicate if generator is unlocked by player.
	 * @param isPurchased Indicate if generator is purchased by player.
	 * @return String that contains status for this generator.
	 */
	private String generateStatusDescription(GeneratorTierObject generator,
		boolean isActive,
		boolean isUnlocked,
		boolean isPurchased)
	{
		final String reference = Constants.DESCRIPTIONS + "generator.status.";

		StringBuilder status = new StringBuilder();

		if (!isPurchased && this.addon.isVaultProvided() && generator.getGeneratorTierCost() > 0)
		{
			status.append(this.user.getTranslationOrNothing(reference + "purchase-cost",
				Constants.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
		}

		if (!isActive && this.addon.isVaultProvided() && generator.getActivationCost() > 0)
		{
			status.append("\n");
			status.append(this.user.getTranslationOrNothing(reference + "activation-cost",
				Constants.NUMBER, String.valueOf(generator.getActivationCost())));
		}

		if (isActive)
		{
			status.append("\n");
			status.append(this.user.getTranslationOrNothing(reference + "active",
				Constants.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
		}

		if (!isUnlocked)
		{
			status.append("\n");
			status.append(this.user.getTranslationOrNothing(reference + "locked",
				Constants.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
		}

		if (!generator.isDeployed())
		{
			status.append("\n");
			status.append(this.user.getTranslationOrNothing(reference + "undeployed"));
		}

		return status.toString();
	}


	/**
	 * Admin should see simplified view. It is not necessary to view all unnecessary things.
	 *
	 * @param bundle Bundle which description must be generated.
	 * @return List of strings that describes bundle.
	 */
	protected List<String> generateBundleDescription(GeneratorBundleObject bundle)
	{
		List<String> description = new ArrayList<>(5);
		bundle.getDescription().forEach(line ->
			description.add(ChatColor.translateAlternateColorCodes('&', line)));

		description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "bundle-permission",
			Constants.ID, bundle.getUniqueId(),
			Constants.GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

		// Add missing permissions
		if (!bundle.getGeneratorTiers().isEmpty())
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "generators"));

			bundle.getGeneratorTiers().stream().
				map(this.manager::getGeneratorByID).
				filter(Objects::nonNull).
				forEach(generator ->
					description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "generator-list",
						Constants.GENERATOR, generator.getFriendlyName())));
		}

		return description;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable allows to access addon object.
	 */
	protected final StoneGeneratorAddon addon;

	/**
	 * This variable allows to access addon manager object.
	 */
	protected final StoneGeneratorManager manager;

	/**
	 * This variable holds user who opens panel. Without it panel cannot be opened.
	 */
	protected final User user;

	/**
	 * This variable holds world where panel is operating.
	 */
	protected final World world;

	/**
	 * This variable allows to create nested panel structure.
	 */
	protected @Nullable final CommonPanel parentPanel;


// ---------------------------------------------------------------------
// Section: Formatting
// ---------------------------------------------------------------------


	/**
	 * Stores decimal format object for one digit after separator.
	 */
	protected final DecimalFormat tensFormat;

	/**
	 * Stores decimal format object for two digit after separator.
	 */
	protected final DecimalFormat hundredsFormat;

	/**
	 * Stores decimal format object for three digit after separator.
	 */
	protected final DecimalFormat thousandsFormat;

	/**
	 * Stores decimal format object for four digit after separator.
	 */
	protected final DecimalFormat tenThousandsFormat;

	/**
	 * Stores decimal format object for five digit after separator.
	 */
	protected final DecimalFormat hundredThousandsFormat;
}
