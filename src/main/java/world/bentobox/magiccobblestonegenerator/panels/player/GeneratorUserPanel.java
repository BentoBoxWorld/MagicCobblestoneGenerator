package world.bentobox.magiccobblestonegenerator.panels.player;


import org.jetbrains.annotations.Nullable;
import org.bukkit.Material;
import org.bukkit.World;
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
		this.generatorList = this.manager.getAllGeneratorTiers(world);
		// Stores how many elements will be in display.
		this.rowCount = this.generatorList.size() > 14 ? 3 : this.generatorList.size() > 7 ? 2 : 1;

		// By default no-filters are active.
		this.activeFilterButton = Action.RETURN;
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
			this.user.sendMessage(Constants.ERRORS + "no-generators-in-world",
				Constants.WORLD, this.world.getName());
			return;
		}

		if (this.island == null || this.generatorData == null)
		{
			this.user.sendMessage("general.errors.no-island");
			return;
		}

		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "generator-list"));

		GuiUtils.fillBorder(panelBuilder, this.rowCount + 2, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(2, this.createButton(Action.SHOW_ACTIVE));

		panelBuilder.item(4, this.createButton(Action.SHOW_COBBLESTONE));
		panelBuilder.item(5, this.createButton(Action.SHOW_STONE));
		panelBuilder.item(6, this.createButton(Action.SHOW_BASALT));

		panelBuilder.item(8, this.createButton(Action.TOGGLE_VISIBILITY));

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
	private PanelItem createButton(Action button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		String description = this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description");

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			this.activeFilterButton = this.activeFilterButton == button ? Action.RETURN : button;
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
			case RETURN:
			{
				clickHandler = (panel, user, clickType, i) -> {
					user.closeInventory();
					return true;
				};

				material = Material.OAK_DOOR;

				break;
			}
			case SHOW_ACTIVE:
			{
				material = Material.GREEN_STAINED_GLASS_PANE;
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
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(this.activeFilterButton == button && button != Action.RETURN).
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
						generatorTier.getGeneratorType().equals(GeneratorTierObject.GeneratorType.COBBLESTONE)).
					collect(Collectors.toList());
				break;
			case SHOW_STONE:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						generatorTier.getGeneratorType().equals(GeneratorTierObject.GeneratorType.STONE)).
					collect(Collectors.toList());
				break;
			case SHOW_BASALT:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						generatorTier.getGeneratorType().equals(GeneratorTierObject.GeneratorType.BASALT)).
					collect(Collectors.toList());
				break;
			case TOGGLE_VISIBILITY:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						this.generatorData.getUnlockedTiers().contains(generatorTier)).
					collect(Collectors.toList());
				break;
			case SHOW_ACTIVE:
				filteredList = this.generatorList.stream().
					filter(generatorTier ->
						this.generatorData.getActiveGeneratorList().contains(generatorTier)).
					collect(Collectors.toList());
				break;
			default:
				filteredList = this.generatorList;
		}

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
		boolean glow = this.generatorData.getActiveGeneratorList().contains(generatorTier);

		List<String> description = this.generateGeneratorDescription(generatorTier,
			glow,
			this.generatorData.getUnlockedTiers().contains(generatorTier),
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
				user.sendMessage("general.errors.insufficient-rank",
					TextVariables.RANK,
					user.getTranslation(this.addon.getPlugin().getRanksManager().getRank(this.island.getRank(user))));
			}

			// Always return true.
			return true;
		};

		return new PanelItemBuilder().
			name(generatorTier.getFriendlyName()).
			description(description).
			icon(generatorTier.getGeneratorIcon()).
			clickHandler(clickHandler).
			glow(glow).
			build();
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
	private @Nullable final GeneratorDataObject generatorData;

	/**
	 * This variable stores all generator tiers in the given world.
	 */
	private final List<GeneratorTierObject> generatorList;

	/**
	 * Stores currently active filter button.
	 */
	private Action activeFilterButton;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * Stores how many elements will be in display.
	 */
	private final int rowCount;
}
