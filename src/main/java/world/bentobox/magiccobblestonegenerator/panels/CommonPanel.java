package world.bentobox.magiccobblestonegenerator.panels;



import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	}


	/**
	 * This method allows to build panel.
	 */
	public abstract void build();


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This class generates given generator tier description based on input parameters.
	 * @param generator GeneratorTier which description must be generated.
	 * @param isActive Boolean that indicates if generator is active.
	 * @param isUnlocked Boolean that indicates if generator is unlocked.
	 * @param islandLevel Long that shows island level.
	 * @return List of strings that describes generator tier.
	 */
	protected List<String> generateGeneratorDescription(GeneratorTierObject generator,
		boolean isActive,
		boolean isUnlocked,
		long islandLevel)
	{
		List<String> description = new ArrayList<>(5);

		generator.getDescription().forEach(line ->
			description.add(ChatColor.translateAlternateColorCodes('&', line)));

		if (isActive)
		{
			// Add message about activation
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "generator-active"));
		}
		else if (isUnlocked)
		{
			// Add message about activation cost

			if (generator.getActivationCost() > 0 && this.addon.isVaultProvided())
			{
				description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "activation-cost",
					TextVariables.NUMBER, String.valueOf(generator.getActivationCost())));
			}
		}
		else
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "locked"));

			// Add missing permissions
			if (!generator.getRequiredPermissions().isEmpty())
			{
				List<String> missingPermissions = new ArrayList<>();

				generator.getRequiredPermissions().forEach(permission -> {
					if (!this.user.hasPermission(permission))
					{
						missingPermissions.add(this.user.getTranslation(Constants.DESCRIPTIONS + "missing-permission",
							TextVariables.PERMISSION, permission));
					}
				});

				if (!missingPermissions.isEmpty())
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "required-permissions"));
					description.addAll(missingPermissions);
				}
			}

			// Add missing level
			if (generator.getRequiredMinIslandLevel() > islandLevel)
			{
				description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "required-level",
					TextVariables.NUMBER, String.valueOf(generator.getRequiredMinIslandLevel())));
			}

			if (generator.getGeneratorTierCost() > 0 &&
				this.addon.isVaultProvided())
			{
				description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "purchase-cost",
					Constants.GENERATOR, generator.getFriendlyName(),
					TextVariables.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
			}
		}

		return description;
	}


	/**
	 * Admin should see simplified view. It is not necessary to view all unnecessary things.
	 *
	 * @param generator GeneratorTier which description must be generated.
	 * @return List of strings that describes generator tier.
	 */
	protected List<String> generateGeneratorDescription(GeneratorTierObject generator)
	{
		List<String> description = new ArrayList<>(5);

		generator.getDescription().forEach(line ->
			description.add(ChatColor.translateAlternateColorCodes('&', line)));

		if (generator.getActivationCost() > 0 && this.addon.isVaultProvided())
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "activation-cost",
				TextVariables.NUMBER, String.valueOf(generator.getActivationCost())));
		}

		// Add missing permissions
		if (!generator.getRequiredPermissions().isEmpty())
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "required-permissions"));

			generator.getRequiredPermissions().forEach(permission ->
				description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "missing-permission",
					TextVariables.PERMISSION, permission)));
		}

		// Add missing level
		description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "required-level",
			TextVariables.NUMBER, String.valueOf(generator.getRequiredMinIslandLevel())));

		if (generator.getGeneratorTierCost() > 0 && this.addon.isVaultProvided())
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "purchase-cost",
				Constants.GENERATOR, generator.getFriendlyName(),
				TextVariables.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
		}

		return description;
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
}
