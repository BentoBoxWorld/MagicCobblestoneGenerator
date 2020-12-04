package world.bentobox.magiccobblestonegenerator.panels.player;


import org.jetbrains.annotations.Nullable;
import org.bukkit.Material;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows generators for user.
 */
public class GeneratorUserPanel extends CommonPanel
{
	// ---------------------------------------------------------------------
	// Section: Internal Constructor
	// ---------------------------------------------------------------------


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param addon StoneGeneratorAddon object.
	 */
	private GeneratorUserPanel(StoneGeneratorAddon addon,
		World world,
		User user)
	{
		super(addon, user, world);
		this.island = this.addon.getIslands().getIsland(world, user);

		// Get valid user island data
		this.generatorData = this.manager.validateIslandData(this.island);

		// Store generators in local list to avoid building it every time.
		this.generatorList = this.manager.getIslandGeneratorTiers(world, this.generatorData);
		// Stores how many elements will be in display.
		this.rowCount = this.generatorList.size() > 14 ? 3 : this.generatorList.size() > 7 ? 2 : 1;

		// By default no-filters are active.
		this.activeFilterButton = Button.NONE;
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param addon VisitAddon object
	 * @param user User who opens panel
	 */
	public static void openPanel(StoneGeneratorAddon addon,
		World world,
		User user)
	{
		new GeneratorUserPanel(addon, world, user).build();
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
		if (this.generatorList.isEmpty())
		{
			Utils.sendMessage(this.user, this.user.getTranslation(
				Constants.ERRORS + "no-generators-in-world",
				Constants.WORLD, this.world.getName()));
			return;
		}

		if (this.island == null || this.generatorData == null)
		{
			Utils.sendMessage(this.user, this.user.getTranslation("general.errors.no-island"));
			return;
		}

		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "player-panel"));

		GuiUtils.fillBorder(panelBuilder, this.rowCount + 2, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(2, this.createButton(Button.SHOW_ACTIVE));

		// Do not show cobblestone button if there are no cobblestone generators.
		if (this.generatorList.stream().anyMatch(generator ->
			generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)))
		{
			panelBuilder.item(4, this.createButton(Button.SHOW_COBBLESTONE));
		}

		// Do not show stone if there are no stone generators.
		if (this.generatorList.stream().anyMatch(generator ->
			generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)))
		{
			panelBuilder.item(5, this.createButton(Button.SHOW_STONE));
		}

		// Do not show basalt if there are no basalt generators.
		if (this.generatorList.stream().anyMatch(generator ->
			generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)))
		{
			panelBuilder.item(6, this.createButton(Button.SHOW_BASALT));
		}

		panelBuilder.item(8, this.createButton(Button.TOGGLE_VISIBILITY));

		this.fillGeneratorTiers(panelBuilder);

		panelBuilder.item((this.rowCount + 2) * 9 - 1, this.createButton(Action.RETURN));

		// Build panel.
		panelBuilder.build();
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase();
		String name = this.user.getTranslation(reference + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(reference + ".description"));

		description.add("");
		if (this.activeFilterButton != button)
		{
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-enable"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-disable"));
		}

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			this.activeFilterButton = this.activeFilterButton == button ? Button.NONE : button;
			// Rebuild everything.
			this.build();

			// Always return true.
			return true;
		};

		Material material = Material.PAPER;

		switch (button)
		{
			case SHOW_COBBLESTONE:
			{
				material = Material.COBBLESTONE;
				break;
			}
			case SHOW_STONE:
			{
				material = Material.STONE;
				break;
			}
			case SHOW_BASALT:
			{
				// TODO: 1.15.2 support
				material = Material.getMaterial("BASALT") == null ?
					Material.BARRIER : Material.getMaterial("BASALT");
				break;
			}
			case TOGGLE_VISIBILITY:
			{
				material = Material.REDSTONE;
				break;
			}
			case SHOW_ACTIVE:
			{
				material = Material.GREEN_STAINED_GLASS_PANE;
				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(this.activeFilterButton == button).
			build();
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Action button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase();
		String name = this.user.getTranslation(reference+ ".name");
		List<String> description = new ArrayList<>();

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

		Material icon = Material.PAPER;
		int count = 1;

		switch (button)
		{
			case RETURN:
			{
				description.add(this.user.getTranslationOrNothing(reference + ".description"));
				description.add("");

				if (this.parentPanel != null)
				{
					description.add(this.user.getTranslation(Constants.TIPS + "click-to-return"));
				}
				else
				{
					description.add(this.user.getTranslation(Constants.TIPS + "click-to-quit"));
				}

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

				icon = Material.OAK_DOOR;

				break;
			}
			case PREVIOUS:
			{
				count = GuiUtils.getPreviousPage(this.pageIndex, this.maxPageIndex);
				description.add(this.user.getTranslationOrNothing(reference + ".description",
					Constants.NUMBER, String.valueOf(count)));

				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));

				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				icon = Material.TIPPED_ARROW;
				break;
			}
			case NEXT:
			{
				count = GuiUtils.getNextPage(this.pageIndex, this.maxPageIndex);
				description.add(this.user.getTranslationOrNothing(reference + ".description",
					Constants.NUMBER, String.valueOf(count)));

				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));

				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				icon = Material.TIPPED_ARROW;
				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(icon).
			amount(count).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method fills panel builder empty spaces with generator tiers and adds previous
	 * next buttons if necessary.
	 * @param panelBuilder PanelBuilder that is necessary to populate.
	 */
	private void fillGeneratorTiers(PanelBuilder panelBuilder)
	{
		int MAX_ELEMENTS = this.rowCount * 7;

		final int correctPage;

		List<GeneratorTierObject> filteredList;

		switch (this.activeFilterButton)
		{
			case SHOW_COBBLESTONE:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)).
					collect(Collectors.toList());
				break;
			case SHOW_STONE:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)).
					collect(Collectors.toList());
				break;
			case SHOW_BASALT:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)).
					collect(Collectors.toList());
				break;
			case TOGGLE_VISIBILITY:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId())).
					collect(Collectors.toList());
				break;
			case SHOW_ACTIVE:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId())).
					collect(Collectors.toList());
				break;
			default:
				filteredList = this.generatorList;
		}

		this.maxPageIndex = (int) Math.ceil(1.0 * filteredList.size() / MAX_ELEMENTS) - 1;

		if (this.pageIndex < 0)
		{
			correctPage = filteredList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (filteredList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (filteredList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(9, this.createButton(Action.PREVIOUS));
			panelBuilder.item(17, this.createButton(Action.NEXT));
		}

		int generatorIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (generatorIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			generatorIndex < filteredList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createGeneratorButton(filteredList.get(generatorIndex++)));
			}

			index++;
		}
	}


	/**
	 * This method creates button for generator tier.
	 * @param generatorTier GeneratorTier which button must be created.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createGeneratorButton(GeneratorTierObject generatorTier)
	{
		boolean glow = this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId());
		boolean unlocked = this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId());

		List<String> description = this.generateGeneratorDescription(generatorTier,
			glow,
			unlocked,
			this.manager.getIslandLevel(this.island));

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			// Click handler should work only if user has a permission to change anything.
			// Otherwise just to view.

			if (clickType.isRightClick())
			{
				// Open view panel.
				GeneratorViewPanel.openPanel(this, generatorTier);
			}
			else if (this.island.isAllowed(user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION))
			{
				if (glow)
				{
					this.manager.deactivateGenerator(user, this.generatorData, generatorTier);
					// Rebuild whole gui.
					this.build();
				}
				else if (this.manager.canActivateGenerator(user, this.generatorData, generatorTier))
				{
					this.manager.activateGenerator(user, this.island, this.generatorData, generatorTier);
					// Build whole gui.
					this.build();
				}
			}
			else
			{
				Utils.sendMessage(this.user,
					this.user.getTranslation("general.errors.insufficient-rank",
						TextVariables.RANK, user.getTranslation(
							this.addon.getPlugin().getRanksManager().getRank(this.island.getRank(user)))));
			}

			// Always return true.
			return true;
		};

		return new PanelItemBuilder().
			name(generatorTier.getFriendlyName()).
			description(description).
			icon(unlocked ? generatorTier.getGeneratorIcon() : generatorTier.getLockedIcon()).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
	 * This class generates given generator tier description based on input parameters.
	 *
	 * @param generator GeneratorTier which description must be generated.
	 * @param isActive Boolean that indicates if generator is active.
	 * @param isUnlocked Boolean that indicates if generator is unlocked.
	 * @param islandLevel Long that shows island level.
	 * @return List of strings that describes generator tier.
	 */
	@Override
	protected List<String> generateGeneratorDescription(GeneratorTierObject generator,
		boolean isActive,
		boolean isUnlocked,
		long islandLevel)
	{
		List<String> description =
			super.generateGeneratorDescription(generator, isActive, isUnlocked, islandLevel);

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-view"));

		if (isUnlocked)
		{
			if (isActive)
			{
				description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-deactivate"));
			}
			else
			{
				description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-activate"));
			}
		}

		return description;
	}


	// ---------------------------------------------------------------------
	// Section: Enums
	// ---------------------------------------------------------------------


	/**
	 * This enum holds variable that allows to switch between button creation.
	 */
	private enum Button
	{
		/**
		 * Button that on click shows only cobblestone generators.
		 */
		SHOW_COBBLESTONE,
		/**
		 * Button that on click shows only stone generators.
		 */
		SHOW_STONE,
		/**
		 * Button that on click shows only basalt generators.
		 */
		SHOW_BASALT,
		/**
		 * Button that toggles between visibility modes.
		 */
		TOGGLE_VISIBILITY,
		/**
		 * Button that on click shows only active generators.
		 */
		SHOW_ACTIVE,
		/**
		 * Filter for none.
		 */
		NONE
	}


	/**
	 * This enum holds variable that allows to switch between actions.
	 */
	private enum Action
	{
		/**
		 * Return button that exists GUI.
		 */
		RETURN,
		/**
		 * Allows to select previous generators in multi-page situation.
		 */
		PREVIOUS,
		/**
		 * Allows to select next generators in multi-page situation.
		 */
		NEXT
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable holds targeted island.
	 */
	private @Nullable final Island island;

	/**
	 * This variable holds user's island generator data.
	 */
	private final GeneratorDataObject generatorData;

	/**
	 * This variable stores all generator tiers in the given world.
	 */
	private final List<GeneratorTierObject> generatorList;

	/**
	 * Stores currently active filter button.
	 */
	private Button activeFilterButton;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * Stores how many elements will be in display.
	 */
	private final int rowCount;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int maxPageIndex;
}
