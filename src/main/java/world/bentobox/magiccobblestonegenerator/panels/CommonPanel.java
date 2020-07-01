package world.bentobox.magiccobblestonegenerator.panels;



import com.sun.istack.internal.Nullable;

import org.bukkit.World;
import java.util.List;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;


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
		List<String> description = generator.getDescription();

		if (isActive)
		{
			// Add message about activation
			description.add(this.user.getTranslation("stonegenerator.descriptions.generator-active"));
		}
		else if (isUnlocked)
		{
			// Add message about activation cost

			if (generator.getActivationCost() > 0 && this.addon.isVaultProvided())
			{
				description.add(this.user.getTranslation("stonegenerator.descriptions.activation-cost",
					"[cost]", String.valueOf(generator.getActivationCost())));
			}
		}
		else
		{
			description.add(this.user.getTranslation("stonegenerator.descriptions.locked"));

			// Add missing permissions
			if (!generator.getRequiredPermissions().isEmpty())
			{
				description.add(this.user.getTranslation("stonegenerator.descriptions.required-permission"));

				generator.getRequiredPermissions().forEach(permission -> {
					if (!this.user.hasPermission(permission))
					{
						description.add(this.user.getTranslation("stonegenerator.descriptions.has-not-permission",
							"[permission]", permission));
					}
				});
			}

			// Add missing level
			if (generator.getRequiredMinIslandLevel() > islandLevel)
			{
				description.add(this.user.getTranslation("stonegenerator.descriptions.required-level",
					"[level]", String.valueOf(generator.getRequiredMinIslandLevel())));
			}

			if (generator.getGeneratorTierCost() > 0 &&
				this.addon.isVaultProvided() &&
				this.addon.isUpgradesProvided())
			{
				description.add(this.user.getTranslation("stonegenerator.descriptions.use-upgrades",
					"[generator]", generator.getFriendlyName(),
					"[cost]", String.valueOf(generator.getGeneratorTierCost())));
			}
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
