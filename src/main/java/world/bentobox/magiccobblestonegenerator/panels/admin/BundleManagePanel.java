package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class BundleManagePanel extends CommonPanel
{
	// ---------------------------------------------------------------------
	// Section: Internal Constructor
	// ---------------------------------------------------------------------


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param parentPanel Parent Panel object.
	 */
	private BundleManagePanel(CommonPanel parentPanel)
	{
		super(parentPanel);
		// Store bundles in local list to avoid building it every time.
		this.bundleList = this.manager.getAllGeneratorBundles(this.world);

		// Stores how many elements will be in display.
		this.updateRows();

		// Init set with selected bundles.
		this.selectedBundles = new HashSet<>(this.bundleList.size());
	}


	/**
	 * This method updates row count for Panel.
	 */
	private void updateRows()
	{
		this.rowCount = this.bundleList.size() > 14 ? 3 : this.bundleList.size() > 7 ? 2 : 1;
	}


	/**
	 * This method is used to open GeneratorManagePanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param parentPanel Parent Panel object.
	 */
	public static void open(CommonPanel parentPanel)
	{
		new BundleManagePanel(parentPanel).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds this GUI.
	 */
	@Override
	public void build()
	{
		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "manage-bundles"));

		GuiUtils.fillBorder(panelBuilder, this.rowCount + 2, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(1, this.createButton(Action.CREATE_BUNDLE));
		panelBuilder.item(2, this.createButton(Action.DELETE_BUNDLE));

		this.fillBundles(panelBuilder);

		panelBuilder.item((this.rowCount + 2) * 9 - 1, this.createButton(Action.RETURN));

		// Build panel.
		panelBuilder.build();
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Action button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

		// Add helper description
		switch (button)
		{
			case PREVIOUS:
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-previous"));
				break;
			case NEXT:
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-next"));
				break;
			case CREATE_BUNDLE:
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-create"));
				break;
			case DELETE_BUNDLE:
				// add empty line
				description.add("");
				if (this.selectedBundles.isEmpty())
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "select-to-delete"));
				}
				else
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-delete"));
				}
				break;
			case RETURN:
				description.add("");
				if (this.parentPanel != null)
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-return"));
				}
				else
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-quit"));
				}
				break;
		}

		PanelItem.ClickHandler clickHandler;
		boolean glow = false;

		Material material = Material.PAPER;

		switch (button)
		{
			case RETURN:
			{
				clickHandler = (panel, user, clickType, i) -> {
					if (this.parentPanel != null)
					{
						this.parentPanel.build();
					}
					else
					{
						user.closeInventory();
					}
					
					return true;
				};

				material = Material.OAK_DOOR;

				break;
			}
			case PREVIOUS:
			{
				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				material = Material.ARROW;
				break;
			}
			case NEXT:
			{
				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				material = Material.ARROW;
				break;
			}
			case CREATE_BUNDLE:
			{
				material = Material.WRITABLE_BOOK;
				clickHandler = (panel, user1, clickType, slot) -> {
					String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

					// This consumer process new bundle creating with a name and id from given
					// consumer value..
					Consumer<String> bundleIdConsumer = value -> {
						if (value != null)
						{
							GeneratorBundleObject newBundle = new GeneratorBundleObject();
							newBundle.setFriendlyName(value);
							newBundle.setUniqueId(gameModePrefix + Utils.sanitizeInput(value));
							// Add PAPER as new icon.
							newBundle.setGeneratorIcon(new ItemStack(Material.PAPER));
							newBundle.setDescription(new ArrayList<>());
							newBundle.setGeneratorTiers(new HashSet<>());

							this.manager.saveGeneratorBundle(newBundle);
							this.manager.loadGeneratorBundle(newBundle, false, this.user, true);

							// Add new generator to generatorList.
							this.bundleList.add(newBundle);
							// Update row count
							this.updateRows();
							// Open bundle edit panel.
							BundleEditPanel.open(this, newBundle);
						}
						else
						{
							// Operation is canceled. Open this panel again.
							this.build();
						}
					};

					// This function checks if generator with a given ID already exist.
					Function<String, Boolean> validationFunction = bundleId ->
						this.manager.getBundleById(gameModePrefix + Utils.sanitizeInput(bundleId)) == null;

					// Call a conversation API to get input string.
					ConversationUtils.createIDStringInput(bundleIdConsumer,
						validationFunction,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "write-bundle-name"),
						this.user.getTranslation(Constants.MESSAGE + "new-bundle-created",
							Constants.WORLD, world.getName()),
						Constants.ERRORS + "bundle-already-exists");

					return true;
				};

				break;
			}
			case DELETE_BUNDLE:
			{
				material = this.selectedBundles.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
				glow = !this.selectedBundles.isEmpty();

				if (!this.selectedBundles.isEmpty())
				{
					clickHandler = (panel, user1, clickType, slot) -> {

						// Create consumer that accepts value from conversation.
						Consumer<Boolean> consumer = value -> {
							if (value)
							{
								this.selectedBundles.forEach(bundle -> {
									this.manager.wipeBundle(bundle);
									this.bundleList.remove(bundle);
								});

								this.updateRows();
							}

							this.build();
						};

						String generatorString;

						if (!this.selectedBundles.isEmpty())
						{
							Iterator<GeneratorBundleObject> iterator = this.selectedBundles.iterator();

							StringBuilder builder = new StringBuilder();
							builder.append(iterator.next().getFriendlyName());

							while (iterator.hasNext())
							{
								builder.append(", ").append(iterator.next().getFriendlyName());
							}

							generatorString = builder.toString();
						}
						else
						{
							generatorString = "";
						}

						// Create conversation that gets user acceptance to delete selected generator data.
						ConversationUtils.createConfirmation(
							consumer,
							this.user,
							this.user.getTranslation(Constants.QUESTIONS + "confirm-bundle-deletion",
								TextVariables.NUMBER, String.valueOf(this.selectedBundles.size()),
								Constants.BUNDLES, generatorString),
							this.user.getTranslation(Constants.MESSAGE + "bundle-data-removed",
								Constants.GAMEMODE, Utils.getGameMode(this.world)));


						return true;
					};
				}
				else
				{
					// Do nothing as no generators are selected.
					clickHandler = (panel, user1, clickType, slot) -> true;
				}

				break;
			}
			default:
				clickHandler = (panel, user1, clickType, slot) -> true;
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
	 * This method fills panel builder empty spaces with bundles tiers and adds previous
	 * next buttons if necessary.
	 * @param panelBuilder PanelBuilder that is necessary to populate.
	 */
	private void fillBundles(PanelBuilder panelBuilder)
	{
		int MAX_ELEMENTS = this.rowCount * 7;

		final int correctPage;

		if (this.pageIndex < 0)
		{
			correctPage = this.bundleList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.bundleList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (this.bundleList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(9, this.createButton(Action.PREVIOUS));
			panelBuilder.item(17, this.createButton(Action.NEXT));
		}

		int bundleIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (bundleIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			bundleIndex < this.bundleList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createBundleButton(this.bundleList.get(bundleIndex++)));
			}

			index++;
		}
	}


	/**
	 * This method creates button for bundle tier.
	 * @param bundle Bundle which button must be created.
	 * @return PanelItem for bundle.
	 */
	private PanelItem createBundleButton(GeneratorBundleObject bundle)
	{
		boolean glow = this.selectedBundles.contains(bundle);

		List<String> description = this.generateBundleDescription(bundle);

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			// Click handler should work only if user has a permission to change anything.
			// Otherwise just to view.

			if (clickType.isRightClick())
			{
				// Open edit panel.
				if (this.selectedBundles.contains(bundle))
				{
					this.selectedBundles.remove(bundle);
				}
				else
				{
					this.selectedBundles.add(bundle);
				}

				// Build necessary as multiple icons are changed.
				this.build();
			}
			else
			{
				BundleEditPanel.open(this, bundle);
			}

			// Always return true.
			return true;
		};

		return new PanelItemBuilder().
			name(bundle.getFriendlyName()).
			description(description).
			icon(bundle.getGeneratorIcon()).
			clickHandler(clickHandler).
			glow(glow).
			build();
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

		description.add(this.user.getTranslation(Constants.DESCRIPTION + "bundle-permission",
			Constants.ID, bundle.getUniqueId(),
			Constants.GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

		// Add missing permissions
		if (!bundle.getGeneratorTiers().isEmpty())
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "generators"));

			bundle.getGeneratorTiers().stream().
				map(this.manager::getGeneratorByID).
				filter(Objects::nonNull).
				forEach(generator ->
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value-list",
						Constants.VALUE, generator.getFriendlyName())));
		}

		description.add("");
		description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-edit"));

		if (this.selectedBundles.contains(bundle))
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-select"));
		}

		if (this.selectedBundles.contains(bundle))
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "selected"));
		}

		return description;
	}


	// ---------------------------------------------------------------------
	// Section: Enums
	// ---------------------------------------------------------------------


	/**
	 * This enum holds variable that allows to switch between button creation.
	 */
	private enum Action
	{
		/**
		 * Return button that exists GUI.
		 */
		RETURN,
		/**
		 * Allows to select previous bundles in multi-page situation.
		 */
		PREVIOUS,
		/**
		 * Allows to select next bundles in multi-page situation.
		 */
		NEXT,
		/**
		 * Allows to add new bundles to the bundleList.
		 */
		CREATE_BUNDLE,
		/**
		 * Allows to delete selected bundles from bundleList.
		 */
		DELETE_BUNDLE
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable stores all bundles in the given world.
	 */
	private final List<GeneratorBundleObject> bundleList;

	/**
	 * This variable stores all selected bundles.
	 */
	private final Set<GeneratorBundleObject> selectedBundles;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * Stores how many elements will be in display.
	 */
	private int rowCount;
}
