package world.bentobox.magiccobblestonegenerator.panels.player;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * This class opens GUI that shows generator view for user.
 */
public class GeneratorViewPanel extends CommonPanel
{
	// ---------------------------------------------------------------------
	// Section: Internal Constructor
	// ---------------------------------------------------------------------


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param addon VisitAddon object
	 * @param world World where user is operating
	 * @param user User who opens panel
	 * @param generatorTier generator tier that must be viewed.
	 */
	private GeneratorViewPanel(StoneGeneratorAddon addon,
		World world,
		User user,
		GeneratorTierObject generatorTier)
	{
		super(addon, user, world);
		this.island = this.addon.getIslands().getIsland(world, user);

		// Get valid user island data
		this.generatorData = this.manager.validateIslandData(this.island);
		this.generatorTier = generatorTier;

		// By default no-filters are active.
		this.activeTab = Tab.INFO;

		this.applyFormatting();
	}


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param panel Parent Panel
	 * @param generatorTier Generator that must be displayed.
	 */
	private GeneratorViewPanel(CommonPanel panel,
		GeneratorTierObject generatorTier)
	{
		super(panel);
		this.island = this.addon.getIslands().getIsland(this.world, this.user);

		// Get valid user island data
		this.generatorData = this.manager.validateIslandData(this.island);
		this.generatorTier = generatorTier;

		// By default no-filters are active.
		this.activeTab = Tab.INFO;

		this.applyFormatting();
	}


	/**
	 * This method creates number formatting for user locale.
	 */
	private void applyFormatting()
	{
		this.tensFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.tensFormat.applyPattern("###.#");

		this.hundredsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.hundredsFormat.applyPattern("###.##");

		this.thousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.thousandsFormat.applyPattern("###.###");

		this.tenThousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.tenThousandsFormat.applyPattern("###.####");

		this.hundredThousandsFormat = (DecimalFormat) NumberFormat.getNumberInstance(this.user.getLocale());
		this.hundredThousandsFormat.applyPattern("###.#####");
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param addon VisitAddon object
	 * @param world World where user is operating
	 * @param user User who opens panel
	 * @param generatorTier generator tier that must be viewed.
	 */
	public static void openPanel(StoneGeneratorAddon addon,
		World world,
		User user,
		GeneratorTierObject generatorTier)
	{
		new GeneratorViewPanel(addon, world, user, generatorTier).build();
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param panel Parent Panel
	 * @param generatorTier Generator that must be displayed.
	 */
	public static void openPanel(CommonPanel panel,
		GeneratorTierObject generatorTier)
	{
		new GeneratorViewPanel(panel, generatorTier).build();
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
		if (this.generatorTier == null)
		{
			Utils.sendMessage(this.user,
				this.user.getTranslation(Constants.ERRORS + "no-generator-data"));
			return;
		}

		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "view-generator",
				Constants.GENERATOR, this.generatorTier.getFriendlyName()));

		GuiUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

		this.populateHeader(panelBuilder);

		switch (this.activeTab)
		{
			case INFO:
				this.populateInfo(panelBuilder);
				break;
			case BLOCKS:
				this.populateBlocks(panelBuilder);
				break;
			case TREASURES:
				this.populateTreasures(panelBuilder);
				break;
		}

		panelBuilder.item(44, this.createButton(Action.RETURN));

		// Build panel.
		panelBuilder.build();
	}


	/**
	 * This method populates header with buttons for switching between tabs.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateHeader(PanelBuilder panelBuilder)
	{
		panelBuilder.item(3, this.createButton(Tab.INFO));
		panelBuilder.item(5, this.createButton(Tab.BLOCKS));

		if (!this.generatorTier.getTreasureItemChanceMap().isEmpty())
		{
			// This tab is useful only if there exist treasures.
			panelBuilder.item(6, this.createButton(Tab.TREASURES));
		}
	}


	/**
	 * This method populates panel body with info blocks.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateInfo(PanelBuilder panelBuilder)
	{
		// Users should see only icon
		panelBuilder.item(19, this.createButton(Button.GENERATOR));

		// Usefull information to know about generators.
		panelBuilder.item(11, this.createButton(Button.DEFAULT));
		panelBuilder.item(20, this.createButton(Button.PRIORITY));
		panelBuilder.item(29, this.createButton(Button.TYPE));

		// Default genertator do not have requirements.
		if (!this.generatorTier.isDefaultGenerator())
		{
			if (this.generatorTier.getRequiredMinIslandLevel() > 0)
			{
				// If level is 0 or less, then it is not checked.
				panelBuilder.item(13, this.createButton(Button.REQUIRED_MIN_LEVEL));
			}

			if (!this.generatorTier.getRequiredPermissions().isEmpty())
			{
				// Display only permissions if they are required.
				panelBuilder.item(22, this.createButton(Button.REQUIRED_PERMISSIONS));
			}

			if (this.addon.isVaultProvided() &&
				//this.addon.isUpgradesProvided() &&
				this.generatorTier.getGeneratorTierCost() > 0)
			{
				// Display cost only if there exist vault, upgrades and it is larger than 0.
				panelBuilder.item(31, this.createButton(Button.PURCHASE_COST));
			}
		}

		// If vault is disabled or cost is 0, then this info is useless.
		if (this.addon.isVaultProvided() && this.generatorTier.getActivationCost() > 0)
		{
			panelBuilder.item(15, this.createButton(Button.ACTIVATION_COST));
		}

		if (!this.generatorTier.getRequiredBiomes().isEmpty())
		{
			// Do not display biomes if they do not exist.
			panelBuilder.item(24, this.createButton(Button.BIOMES));
		}

		if (!this.generatorTier.getTreasureItemChanceMap().isEmpty())
		{
			// Do not display treasures if they are not Button.
			panelBuilder.item(25, this.createButton(Button.TREASURE_CHANCE));
			panelBuilder.item(34, this.createButton(Button.TREASURE_AMOUNT));
		}
	}


	/**
	 * This method populates panel body with blocks.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateBlocks(PanelBuilder panelBuilder)
	{
		final int MAX_ELEMENTS = 21;
		final int correctPage;

		List<Map.Entry<Double, Material>> materialChanceList =
			this.generatorTier.getBlockChanceMap().entrySet().stream().
				sorted(Map.Entry.comparingByKey()).
				collect(Collectors.toList());

		// Calculate max page count.
		this.maxPageIndex = (int) Math.ceil(1.0 * materialChanceList.size() / 21) - 1;

		if (this.pageIndex < 0)
		{
			correctPage = materialChanceList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (materialChanceList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (materialChanceList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		int materialIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		// Store previous object value for displaying correct chance value.
		Double previousValue = materialIndex > 0 ?
			materialChanceList.get(materialIndex - 1).getKey() : 0.0;
		Double maxValue = this.generatorTier.getBlockChanceMap().lastKey();

		while (materialIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			materialIndex < materialChanceList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				// Get entry from list.
				Map.Entry<Double, Material> materialEntry = materialChanceList.get(materialIndex++);
				// Add to panel
				panelBuilder.item(index, this.createMaterialButton(materialEntry, previousValue, maxValue));
				// Assign previous value to current entry.
				previousValue = materialEntry.getKey();
			}

			index++;
		}
	}


	/**
	 * This method populates panel body with treasures.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateTreasures(PanelBuilder panelBuilder)
	{
		final int MAX_ELEMENTS = 21;
		final int correctPage;

		List<Map.Entry<Double, ItemStack>> treasureChanceList =
			this.generatorTier.getTreasureItemChanceMap().entrySet().stream().
				sorted(Map.Entry.comparingByKey()).
				collect(Collectors.toList());

		// Calculate max page count.
		this.maxPageIndex = (int) Math.ceil(1.0 * treasureChanceList.size() / 21) - 1;

		if (this.pageIndex < 0)
		{
			correctPage = treasureChanceList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (treasureChanceList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (treasureChanceList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		int materialIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		// Store previous object value for displaying correct chance value.
		Double previousValue = materialIndex > 0 ?
			treasureChanceList.get(materialIndex - 1).getKey() : 0.0;
		Double maxValue = this.generatorTier.getTreasureItemChanceMap().lastKey();

		while (materialIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			materialIndex < treasureChanceList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				// Get entry from list.
				Map.Entry<Double, ItemStack> materialEntry = treasureChanceList.get(materialIndex++);
				// Add to panel
				panelBuilder.item(index, this.createTreasureButton(materialEntry, previousValue, maxValue));
				// Assign previous value to current entry.
				previousValue = materialEntry.getKey();
			}

			index++;
		}
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

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

		boolean glow = false;
		ItemStack itemStack = new ItemStack(Material.AIR);

		switch (button)
		{
			case GENERATOR:
			{
				// Return created button.
				return this.createGeneratorButton(this.generatorTier);
			}
			case DEFAULT:
			{
				if (this.generatorTier.isDefaultGenerator())
				{
					itemStack = new ItemStack(Material.GREEN_BANNER);
					description.add(this.user.getTranslation(reference + ".enabled"));
				}
				else
				{
					itemStack = new ItemStack(Material.RED_BANNER);
					description.add(this.user.getTranslation(reference + ".disabled"));
				}

				break;
			}
			case PRIORITY:
			{
				itemStack = new ItemStack(Material.HOPPER);
				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getPriority())));
				break;
			}
			case TYPE:
			{
				itemStack = new ItemStack(
					GuiUtils.getGeneratorTypeMaterial(this.generatorTier.getGeneratorType()));

				description.add(this.user.getTranslation(reference + ".value",
					Constants.TYPE, this.user.getTranslation(
						Constants.GENERATOR_TYPE_BUTTON +
							this.generatorTier.getGeneratorType().name().toLowerCase() + ".name")));
				break;
			}
			case REQUIRED_MIN_LEVEL:
			{
				itemStack = new ItemStack(Material.DIAMOND);
				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getRequiredMinIslandLevel())));
				break;
			}
			case REQUIRED_PERMISSIONS:
			{
				itemStack = new ItemStack(Material.BOOK);

				description.add(this.user.getTranslation(reference + ".list"));

				this.generatorTier.getRequiredPermissions().stream().sorted().forEach(permission ->
					description.add(this.user.getTranslation(reference + ".value",
						Constants.PERMISSION, permission)));

				break;
			}
			case PURCHASE_COST:
			{
				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getGeneratorTierCost())));

				if (this.generatorData.getPurchasedTiers().contains(this.generatorTier.getUniqueId()))
				{
					itemStack = new ItemStack(Material.MAP);
				}
				else
				{
					itemStack = new ItemStack(Material.GOLD_BLOCK);
					description.add("");
					description.add(this.user.getTranslation(Constants.TIPS + "click-to-purchase"));
				}

				clickHandler = (panel, user, clickType, i) ->
				{
					if (this.manager.canPurchaseGenerator(user,
						this.island,
						this.generatorData,
						this.generatorTier))
					{
						this.manager.purchaseGenerator(user, this.generatorData, this.generatorTier);
						this.hasPurchased = true;

						// rebuild gui as several items relay on purchase setting.
						this.build();
					}

					return true;
				};

				break;
			}
			case ACTIVATION_COST:
			{
				itemStack = new ItemStack(Material.GOLD_INGOT);
				glow = this.generatorData.getActiveGeneratorList().contains(this.generatorTier.getUniqueId());

				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getActivationCost())));

				// boolean for click-handler.
				final boolean deactivate;

				if (glow)
				{
					if (this.island.isAllowed(user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION))
					{
						description.add("");
						description.add(this.user.getTranslation(Constants.TIPS + "click-to-deactivate"));
					}

					deactivate = true;
				}
				else
				{
					if (this.island.isAllowed(user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION))
					{
						description.add("");
						description.add(this.user.getTranslation(Constants.TIPS + "click-to-activate"));
					}

					deactivate = false;
				}

				clickHandler = (panel, user, clickType, i) ->
				{
					if (this.island.isAllowed(user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION))
					{
						if (deactivate)
						{
							this.manager.deactivateGenerator(user, this.generatorData, generatorTier);
							// rebuild gui as several items relay on purchase setting.
							this.build();
						}
						else if (this.manager.canActivateGenerator(user, this.generatorData, generatorTier))
						{
							this.manager.activateGenerator(user, this.island, this.generatorData, generatorTier);
							// rebuild gui as several items relay on purchase setting.
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

					return true;
				};

				break;
			}
			case BIOMES:
			{
				itemStack = new ItemStack(Material.FILLED_MAP);

				description.add(this.user.getTranslation(reference + ".list"));

				this.generatorTier.getRequiredBiomes().stream().sorted().forEach(biome ->
					description.add(this.user.getTranslation(reference + ".value",
						Constants.BIOME, Utils.prettifyObject(this.user, biome))));

				break;
			}
			case TREASURE_AMOUNT:
			{
				itemStack = new ItemStack(Material.EMERALD, this.generatorTier.getMaxTreasureAmount());
				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getMaxTreasureAmount())));
				break;
			}
			case TREASURE_CHANCE:
			{
				itemStack = new ItemStack(Material.PAPER);
				description.add(this.user.getTranslation(reference + ".value",
					Constants.NUMBER, String.valueOf(this.generatorTier.getTreasureChance())));
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
	    final String reference = Constants.BUTTON + button.name().toLowerCase();
		String name = this.user.getTranslation(reference + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(reference + ".description"));
		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));


		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			this.activeTab = button;
			this.pageIndex = 0;
			this.build();
			return true;
		};

		Material material = Material.PAPER;

		switch (button)
		{
			case INFO:
				material = Material.WRITTEN_BOOK;
				break;
			case BLOCKS:
				material = Material.CHEST;
				break;
			case TREASURES:
				material = Material.SHULKER_BOX;
				break;
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
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-return"));

				clickHandler = (panel, user, clickType, i) -> {
					if (this.parentPanel != null)
					{
						if (this.hasPurchased && this.parentPanel instanceof GeneratorUserPanel)
						{
							// Regenerate GUI as new generators are purchased.
							GeneratorUserPanel.openPanel(this.addon, this.world, this.user);
						}
						else
						{
							// Just open a parent gui.
							this.parentPanel.build();
						}
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
	 * This method creates button for generator tier.
	 * @param generatorTier GeneratorTier which button must be created.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createGeneratorButton(GeneratorTierObject generatorTier)
	{
		boolean glow = this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId());

		List<String> description;

		if (this.island != null)
		{
			description = this.generateGeneratorDescription(generatorTier,
				glow,
				this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId()),
				this.manager.getIslandLevel(this.island));
		}
		else
		{
			description = this.generateGeneratorDescription(generatorTier,
				false,
				false,
				0L);
		}

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
		{
			if (this.island.isAllowed(user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION))
			{
				if (glow)
				{
					this.manager.deactivateGenerator(user, this.generatorData, generatorTier);
					// rebuild gui as several items relay on purchase setting.
					this.build();
				}
				else if (this.manager.canActivateGenerator(user, this.generatorData, generatorTier))
				{
					this.manager.activateGenerator(user, this.island, this.generatorData, generatorTier);
					// rebuild gui as several items relay on purchase setting.
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
	 * This method creates button for material.
	 * @param blockChanceEntry blockChanceEntry which button must be created.
	 * @param previousValue Previous chance value for correct display.
	 * @param maxValue Displays maximal value for map.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createMaterialButton(Map.Entry<Double, Material> blockChanceEntry, Double previousValue, Double maxValue)
	{
		// Normalize value
		Double value = (blockChanceEntry.getKey() - previousValue) / maxValue * 100.0;

		return new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "block-icon.name",
				Constants.BLOCK, Utils.prettifyObject(this.user, blockChanceEntry.getValue()))).
			description(this.user.getTranslation(Constants.BUTTON + "block-icon.description",
				TextVariables.NUMBER, String.valueOf(value),
				Constants.TENS, this.tensFormat.format(value),
				Constants.HUNDREDS, this.hundredsFormat.format(value),
				Constants.THOUSANDS, this.thousandsFormat.format(value),
				Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
				Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value))).
			icon(blockChanceEntry.getValue()).
			clickHandler((panel, user1, clickType, i) -> true).
			build();
	}


	/**
	 * This method creates button for treasure.
	 * @param treasureChanceEntry treasureChanceEntry which button must be created.
	 * @param previousValue Previous chance value for correct display.
	 * @param maxValue Displays maximal value for map.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createTreasureButton(Map.Entry<Double, ItemStack> treasureChanceEntry, Double previousValue, Double maxValue)
	{
		// Normalize value
		Double value = (treasureChanceEntry.getKey() - previousValue) / maxValue * 100.0 * this.generatorTier.getTreasureChance();

		// TODO: It would be necessary to add some item meta data information.
		ItemStack treasure = treasureChanceEntry.getValue().clone();
		List<String> description = new ArrayList<>();

		if (treasure.hasItemMeta() && treasure.getItemMeta() != null)
		{
			ItemMeta itemMeta = treasure.getItemMeta();

			if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty())
			{
				description.addAll(itemMeta.getLore());
				// Add empty line after lore.
				description.add("");
			}
		}

		description.add(this.user.getTranslation(Constants.BUTTON + "treasure-icon.description",
			TextVariables.NUMBER, String.valueOf(value),
			Constants.TENS, this.tensFormat.format(value),
			Constants.HUNDREDS, this.hundredsFormat.format(value),
			Constants.THOUSANDS, this.thousandsFormat.format(value),
			Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
			Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));

		return new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "treasure-icon.name",
				Constants.BLOCK, Utils.prettifyObject(this.user, treasure))).
			description(description).
			icon(GuiUtils.getMaterialItem(treasure.getType())).
			clickHandler((panel, user1, clickType, i) -> true).
			build();
	}


	// ---------------------------------------------------------------------
	// Section: Generator description
	// ---------------------------------------------------------------------


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

		if (isUnlocked)
		{
			// Add tips.
			description.add("");

			if (isActive)
			{
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-deactivate"));
			}
			else
			{
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-activate"));
			}
		}
		else
		{
			if (this.addon.isVaultProvided() &&
				generator.getGeneratorTierCost() > 0 &&
				!this.generatorData.getPurchasedTiers().contains(generator.getUniqueId()))
			{
				description.add(this.user.getTranslation(Constants.TIPS + "click-gold-to-purchase"));
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


	/**
	 * This enum holds possible tabs for current gui.
	 */
	private enum Tab
	{
		/**
		 * General Information Tab
		 */
		INFO,
		/**
		 * Blocks Tab.
		 */
		BLOCKS,
		/**
		 * Treasure Tab.
		 */
		TREASURES
	}


	/**
	 * This enum holds possible actions that this gui allows.
	 */
	private enum Button
	{
		/**
		 * Holds icon for generator.
		 */
		GENERATOR,
		/**
		 * Holds Name type that allows to interact with generator default status.
		 */
		DEFAULT,
		/**
		 * Holds Name type that allows to interact with generator priority.
		 */
		PRIORITY,
		/**
		 * Holds Name type that allows to interact with generator type.
		 */
		TYPE,
		/**
		 * Holds Name type that allows to interact with generator min island requirement.
		 */
		REQUIRED_MIN_LEVEL,
		/**
		 * Holds Name type that allows to interact with generator required permissions.
		 */
		REQUIRED_PERMISSIONS,
		/**
		 * Holds Name type that allows to interact with generator purchase cost.
		 */
		PURCHASE_COST,
		/**
		 * Holds Name type that allows to interact with generator activation cost.
		 */
		ACTIVATION_COST,
		/**
		 * Holds Name type that allows to interact with generator working biomes.
		 */
		BIOMES,
		/**
		 * Holds Name type that allows to interact with generator treasure amount.
		 */
		TREASURE_AMOUNT,
		/**
		 * Holds Name type that allows to interact with generator treasure chance.
		 */
		TREASURE_CHANCE,
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable holds targeted island.
	 */
	private final Island island;

	/**
	 * This variable holds user's island generator data.
	 */
	private final GeneratorDataObject generatorData;

	/**
	 * This variable stores generator tier that is viewed.
	 */
	private final GeneratorTierObject generatorTier;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * This variable holds maximal page index.
	 */
	private int maxPageIndex;

	/**
	 * This variable stores which tab currently is active.
	 */
	private Tab activeTab;

	/**
	 * This boolean indicates if a generator has been purchased or not.
	 * If generator is purchased, it is necessary to recreate user gui.
	 */
	private boolean hasPurchased;

// ---------------------------------------------------------------------
// Section: Formatting
// ---------------------------------------------------------------------


	/**
	 * Stores decimal format object for one digit after separator.
	 */
	private DecimalFormat tensFormat;

	/**
	 * Stores decimal format object for two digit after separator.
	 */
	private DecimalFormat hundredsFormat;

	/**
	 * Stores decimal format object for three digit after separator.
	 */
	private DecimalFormat thousandsFormat;

	/**
	 * Stores decimal format object for four digit after separator.
	 */
	private DecimalFormat tenThousandsFormat;

	/**
	 * Stores decimal format object for five digit after separator.
	 */
	private DecimalFormat hundredThousandsFormat;
}
