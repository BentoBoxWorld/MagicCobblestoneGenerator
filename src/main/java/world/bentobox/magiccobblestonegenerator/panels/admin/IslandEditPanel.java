package world.bentobox.magiccobblestonegenerator.panels.admin;


import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.panels.player.GeneratorUserPanel;
import world.bentobox.magiccobblestonegenerator.panels.player.GeneratorViewPanel;
import world.bentobox.magiccobblestonegenerator.panels.utils.SelectBlocksPanel;
import world.bentobox.magiccobblestonegenerator.panels.utils.SelectBundlePanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class IslandEditPanel extends CommonPanel
{
	// ---------------------------------------------------------------------
	// Section: Internal Constructor
	// ---------------------------------------------------------------------


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param panel Parent Panel
	 * @param island Island that must be displayed.
	 */
	private IslandEditPanel(CommonPanel panel,
		Island island)
	{
		super(panel);

		this.island = island;
		this.generatorData = this.addon.getAddonManager().getGeneratorData(island);

		// Store generators in local list to avoid building it every time.
		this.generatorList = this.manager.getIslandGeneratorTiers(world, this.generatorData);

		this.activeTab = Tab.ISLAND_INFO;
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param panel Parent Panel
	 * @param island Island that must be displayed.
	 */
	public static void open(CommonPanel panel,
		Island island)
	{
		new IslandEditPanel(panel, island).build();
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
			name(this.user.getTranslation(Constants.TITLE + "island-edit"));

		GuiUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);
		this.populateHeader(panelBuilder);

		switch (this.activeTab)
		{
			case ISLAND_INFO:
				this.populateInfo(panelBuilder);
				break;
			case ISLAND_GENERATORS:
				this.fillGeneratorTiers(panelBuilder);
				break;
		}

		// Build panel.
		panelBuilder.build();
	}


	/**
	 * This method populates header with buttons for switching between tabs.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateHeader(PanelBuilder panelBuilder)
	{
		panelBuilder.item(1, this.createButton(Tab.ISLAND_INFO));
		panelBuilder.item(2, this.createButton(Tab.ISLAND_GENERATORS));

		if (this.activeTab == Tab.ISLAND_GENERATORS)
		{
			panelBuilder.item(2, this.createButton(Action.SHOW_ACTIVE));

			// Do not show cobblestone button if there are no cobblestone generators.
			if (this.generatorList.stream().anyMatch(generator ->
				generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)))
			{
				panelBuilder.item(4, this.createButton(Action.SHOW_COBBLESTONE));
			}

			// Do not show stone if there are no stone generators.
			if (this.generatorList.stream().anyMatch(generator ->
				generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)))
			{
				panelBuilder.item(5, this.createButton(Action.SHOW_STONE));
			}

			// Do not show basalt if there are no basalt generators.
			if (this.generatorList.stream().anyMatch(generator ->
				generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)))
			{
				panelBuilder.item(6, this.createButton(Action.SHOW_BASALT));
			}

			panelBuilder.item(8, this.createButton(Action.TOGGLE_VISIBILITY));
		}
	}


	/**
	 * This method populates panel body with info blocks.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateInfo(PanelBuilder panelBuilder)
	{
		panelBuilder.item(10, this.createButton(Button.ISLAND_NAME));

		// Island things can be changed and updated.
		panelBuilder.item(13, this.createButton(Button.ISLAND_MAX_GENERATORS));
		panelBuilder.item(14, this.createButton(Button.ISLAND_WORKING_RANGE));
		panelBuilder.item(15, this.createButton(Button.ISLAND_BUNDLE));

		// Owner things are defined by permission. Show in view mode.
		panelBuilder.item(31, this.createButton(Button.OWNER_MAX_GENERATORS));
		panelBuilder.item(32, this.createButton(Button.OWNER_WORKING_RANGE));
		panelBuilder.item(33, this.createButton(Button.OWNER_BUNDLE));
	}


	/**
	 * This method fills panel builder empty spaces with generator tiers and adds previous
	 * next buttons if necessary.
	 * @param panelBuilder PanelBuilder that is necessary to populate.
	 */
	private void fillGeneratorTiers(PanelBuilder panelBuilder)
	{
		int MAX_ELEMENTS = 21;

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

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
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

		List<String> description = this.generateGeneratorDescription(generatorTier,
			glow,
			this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId()),
			this.manager.getIslandLevel(this.island));

		final boolean isUnlocked = this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId());
		final boolean isActive = this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId());

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {

			if (clickType.isRightClick())
			{
				// Open view panel.
				if (isUnlocked)
				{
					this.generatorData.getUnlockedTiers().remove(generatorTier.getUniqueId());
				}
				else
				{
					this.generatorData.getUnlockedTiers().add(generatorTier.getUniqueId());
				}

				panel.getInventory().setItem(i, this.createGeneratorButton(generatorTier).getItem());
			}
			else if (clickType.isLeftClick())
			{
				if (isActive)
				{
					this.generatorData.getActiveGeneratorList().remove(generatorTier.getUniqueId());
				}
				else
				{
					this.generatorData.getActiveGeneratorList().add(generatorTier.getUniqueId());
				}

				panel.getInventory().setItem(i, this.createGeneratorButton(generatorTier).getItem());
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


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Button button)
	{
		String name = this.user.getTranslation(
			Constants.BUTTON + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(
			Constants.BUTTON + button.name().toLowerCase() + ".description"));

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			return true;
		};

		boolean glow = false;
		ItemStack itemStack = new ItemStack(Material.AIR);

		switch (button)
		{
			case ISLAND_NAME:
			{
				itemStack = new ItemStack(Material.NAME_TAG);

				UUID ownerId = this.island.getOwner();
				String ownerName = this.addon.getPlayers().getName(ownerId);
				description.add(ownerName);

				description.add(this.island.getUniqueId());

				ImmutableSet<UUID> members = this.island.getMemberSet();

				if (members.size() > 1)
				{
					for (UUID uuid : members)
					{
						if (uuid != ownerId)
						{
							description.add(ChatColor.AQUA + this.addon.getPlayers().getName(uuid));
						}
					}
				}

				name = this.island.getName() != null ? this.island.getName() : ownerName;
				break;
			}
			case ISLAND_WORKING_RANGE:
			{
				if (this.generatorData.getOwnerWorkingRange() != 0)
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "overwritten-by-permission"));
				}
				else
				{
					itemStack = new ItemStack(Material.REPEATER);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
						Constants.VALUE, String.valueOf(this.generatorData.getIslandWorkingRange())));
				}

				clickHandler = (panel, user, clickType, slot) ->
				{
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorData.setIslandWorkingRange(number.intValue());
							this.manager.saveGeneratorData(this.generatorData);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						-1,
						2000);

					return true;
				};

				break;
			}
			case OWNER_WORKING_RANGE:
			{
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "working-range-permission",
					Constants.GAMEMODE, Utils.getGameMode(this.world)));

				if (this.generatorData.getOwnerWorkingRange() != 0)
				{
					itemStack = new ItemStack(Material.REPEATER);

					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
						Constants.VALUE, String.valueOf(this.generatorData.getOwnerWorkingRange())));
				}
				else
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);
				}

				break;
			}
			case ISLAND_MAX_GENERATORS:
			{
				if (this.generatorData.getOwnerActiveGeneratorCount() != 0)
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "overwritten-by-permission"));
				}
				else
				{
					itemStack = new ItemStack(Material.REPEATER);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
						Constants.VALUE, String.valueOf(this.generatorData.getIslandActiveGeneratorCount())));
				}

				clickHandler = (panel, user, clickType, slot) ->
				{
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorData.setIslandActiveGeneratorCount(number.intValue());
							this.manager.saveGeneratorData(this.generatorData);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						-1,
						2000);

					return true;
				};

				break;
			}
			case OWNER_MAX_GENERATORS:
			{
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "active-generator-permission",
					Constants.GAMEMODE, Utils.getGameMode(this.world)));

				if (this.generatorData.getOwnerActiveGeneratorCount() != 0)
				{
					itemStack = new ItemStack(Material.COBBLESTONE,
						Math.max(1, this.generatorData.getOwnerActiveGeneratorCount()));

					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
						Constants.VALUE, String.valueOf(this.generatorData.getOwnerActiveGeneratorCount())));
				}
				else
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);
				}

				break;
			}
			case ISLAND_BUNDLE:
			{
				GeneratorBundleObject ownerBundle = this.generatorData.getOwnerBundle() != null ?
					this.addon.getAddonManager().getBundleById(this.generatorData.getOwnerBundle()) : null;

				final GeneratorBundleObject islandBundle = this.generatorData.getIslandBundle() != null ?
					this.addon.getAddonManager().getBundleById(this.generatorData.getIslandBundle()) : null;

				if (ownerBundle != null)
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "overwritten-by-permission"));
				}
				else
				{
					if (islandBundle != null)
					{
						itemStack = islandBundle.getGeneratorIcon();
						description = this.generateBundleDescription(islandBundle);
					}
					else
					{
						itemStack = new ItemStack(Material.NAME_TAG);
					}
				}

				clickHandler = (panel, user, clickType, slot) ->
				{
					SelectBundlePanel.open(this, islandBundle, bundle -> {
						this.generatorData.setIslandBundle(bundle.getUniqueId());
						this.build();
					});

					return true;
				};

				break;
			}
			case OWNER_BUNDLE:
			{
				GeneratorBundleObject bundle = this.generatorData.getOwnerBundle() != null ?
					this.addon.getAddonManager().getBundleById(this.generatorData.getOwnerBundle()) : null;

				if (bundle != null)
				{
					itemStack = bundle.getGeneratorIcon();

					description = this.generateBundleDescription(bundle);
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "bundle-permission",
						Constants.GAMEMODE, Utils.getGameMode(this.world),
						Constants.BUNDLE, bundle.getUniqueId()));
				}
				else
				{
					itemStack = new ItemStack(Material.STRUCTURE_VOID);

					description.add(this.user.getTranslation(Constants.DESCRIPTION + "bundle-permission",
						Constants.GAMEMODE, Utils.getGameMode(this.world)));
				}

				break;
			}
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(itemStack).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Tab button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
		description.add("");
		description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-see"));

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			this.activeTab = button;
			this.pageIndex = 0;

			this.build();
			return true;
		};

		Material material;

		switch (button)
		{
			case ISLAND_INFO:
				material = Material.WRITTEN_BOOK;
				break;
			case ISLAND_GENERATORS:
				material = Material.COBBLESTONE;
				break;
			default:
				material = Material.PAPER;
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(this.activeTab == button).
			build();
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

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

		boolean glow = false;
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
			case RETURN:
			{
				description.add("");
				if (this.parentPanel != null)
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-return"));
				}
				else
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-quit"));
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

				material = Material.OAK_DOOR;

				break;
			}
			case PREVIOUS:
			{
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-previous"));

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
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-next"));

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


	/**
	 * This enum holds possible tabs for current gui.
	 */
	private enum Tab
	{
		/**
		 * General Information Tab
		 */
		ISLAND_INFO,
		/**
		 * Generators Tab.
		 */
		ISLAND_GENERATORS
	}


	/**
	 * This enum holds possible actions that this gui allows.
	 */
	private enum Button
	{
		/**
		 * Holds name of the island that is edited.
		 */
		ISLAND_NAME,
		/**
		 * Displays working range for island.
		 */
		ISLAND_WORKING_RANGE,
		/**
		 * Displays working range for owner.
		 */
		OWNER_WORKING_RANGE,
		/**
		 * Displays max number of active generators for island.
		 */
		ISLAND_MAX_GENERATORS,
		/**
		 * Displays max number of active generators for owner.
		 */
		OWNER_MAX_GENERATORS,
		/**
		 * Displays island active bundle.
		 */
		ISLAND_BUNDLE,
		/**
		 * Displays owner active bundle.
		 */
		OWNER_BUNDLE,
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable stores island that is viewed.
	 */
	private final Island island;

	/**
	 * This variable stores generator data for this island.
	 */
	private final GeneratorDataObject generatorData;

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
	 * This variable stores which tab currently is active.
	 */
	private Tab activeTab;
}
