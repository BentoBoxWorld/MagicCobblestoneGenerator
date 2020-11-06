package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.panels.utils.SelectBiomePanel;
import world.bentobox.magiccobblestonegenerator.panels.utils.SelectBlocksPanel;
import world.bentobox.magiccobblestonegenerator.panels.utils.SelectGeneratorTypePanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Pair;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows generator view for user.
 */
public class GeneratorEditPanel extends CommonPanel
{
	// ---------------------------------------------------------------------
	// Section: Internal Constructor
	// ---------------------------------------------------------------------


	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param panel Parent Panel
	 * @param generatorTier Generator that must be displayed.
	 */
	private GeneratorEditPanel(CommonPanel panel,
		GeneratorTierObject generatorTier)
	{
		super(panel);

		this.generatorTier = generatorTier;

		// By default no-filters are active.
		this.activeTab = Tab.INFO;
		this.selectedMaterial = new HashSet<>();
		this.materialChanceList = new ArrayList<>();

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
	 * @param panel Parent Panel
	 * @param generatorTier Generator that must be displayed.
	 */
	public static void open(CommonPanel panel,
		GeneratorTierObject generatorTier)
	{
		new GeneratorEditPanel(panel, generatorTier).build();
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
			this.user.sendMessage(Constants.ERRORS + "no-generator-data");
			return;
		}

		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "generator-view",
				Constants.GENERATOR, this.generatorTier.getFriendlyName()));

		GuiUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

		this.populateHeader(panelBuilder);

		switch (this.activeTab)
		{
			case INFO:
				this.populateInfo(panelBuilder);

				// Add listener that allows to change icons
				panelBuilder.listener(new IconChanger());

				break;
			case BLOCKS:
				this.populateBlocks(panelBuilder);

				panelBuilder.item(39, this.createButton(Action.ADD_MATERIAL));
				panelBuilder.item(41, this.createButton(Action.REMOVE_MATERIAL));

				// Add listener that allows to change blocks
//				panelBuilder.listener(new BlockChanger());

				break;
			case TREASURES:
				this.populateTreasures(panelBuilder);

				panelBuilder.item(39, this.createButton(Action.ADD_MATERIAL));
				panelBuilder.item(41, this.createButton(Action.REMOVE_MATERIAL));

				// Add listener that allows to change blocks
//				panelBuilder.listener(new TreasureChanger());

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
		panelBuilder.item(6, this.createButton(Tab.TREASURES));
	}


	/**
	 * This method populates panel body with info blocks.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateInfo(PanelBuilder panelBuilder)
	{
		// Users should see only icon
		panelBuilder.item(10, this.createButton(Button.CHANGE_NAME));
		panelBuilder.item(19, this.createButton(Button.CHANGE_ICON));
		panelBuilder.item(28, this.createButton(Button.CHANGE_DESCRIPTION));

		// Usefull information to know about generators.
		panelBuilder.item(11, this.createButton(Button.CHANGE_DEFAULT));
		panelBuilder.item(20, this.createButton(Button.CHANGE_PRIORITY));
		panelBuilder.item(29, this.createButton(Button.CHANGE_TYPE));

		// Default genertator do not have requirements.
		if (!this.generatorTier.isDefaultGenerator())
		{
			if (this.addon.isLevelProvided())
			{
				panelBuilder.item(13, this.createButton(Button.CHANGE_REQUIRED_MIN_LEVEL));
			}

			// Display only permissions if they are required.
			panelBuilder.item(22, this.createButton(Button.CHANGE_REQUIRED_PERMISSIONS));

			if (this.addon.isVaultProvided())
			{
				// Display cost only if there exist vault.
				panelBuilder.item(31, this.createButton(Button.CHANGE_PURCHASE_COST));
			}
		}

		// If vault is disabled.
		if (this.addon.isVaultProvided())
		{
			panelBuilder.item(15, this.createButton(Button.CHANGE_ACTIVATION_COST));
		}

		panelBuilder.item(24, this.createButton(Button.CHANGE_BIOMES));

		// deployed button.
		panelBuilder.item(33, this.createButton(Button.CHANGE_DEPLOYED));

		// display treasures.
		panelBuilder.item(25, this.createButton(Button.CHANGE_TREASURE_CHANCE));
		panelBuilder.item(34, this.createButton(Button.CHANGE_TREASURE_AMOUNT));
	}


	/**
	 * This method populates panel body with blocks.
	 * @param panelBuilder PanelBuilder that must be created.
	 */
	private void populateBlocks(PanelBuilder panelBuilder)
	{
		final int MAX_ELEMENTS = 21;
		final int correctPage;

		if (this.pageIndex < 0)
		{
			correctPage = this.materialChanceList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.materialChanceList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (this.materialChanceList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		int materialIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		Double maxValue = this.generatorTier.getBlockChanceMap().lastKey();

		while (materialIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			materialIndex < this.materialChanceList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				// Get entry from list.
				Pair<Material, Double> materialEntry = this.materialChanceList.get(materialIndex++);
				// Add to panel
				panelBuilder.item(index, this.createMaterialButton(materialEntry, maxValue));
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

		if (this.pageIndex < 0)
		{
			correctPage = this.materialChanceList.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.materialChanceList.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (this.materialChanceList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		int materialIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		// Store previous object value for displaying correct chance value.
		Double maxValue = this.generatorTier.getTreasureChanceMap().lastKey();

		while (materialIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			materialIndex < this.materialChanceList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				// Get entry from list.
				Pair<Material, Double> materialEntry = this.materialChanceList.get(materialIndex++);
				// Add to panel
				panelBuilder.item(index, this.createTreasureButton(materialEntry, maxValue));
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
			case CHANGE_NAME:
			{
				itemStack = new ItemStack(Material.NAME_TAG);

				clickHandler = (panel, user, clickType, i) ->
				{
					// Create consumer that process description change
					Consumer<String> consumer = value ->
					{
						if (value != null)
						{
							this.generatorTier.setFriendlyName(value);
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						this.build();
					};

					// start conversation
					ConversationUtils.createStringInput(consumer,
						user,
						user.getTranslation(Constants.QUESTIONS + "write-name"),
						user.getTranslation(Constants.MESSAGE + "name-changed"));

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				// Not implemented in current GUI.
				break;
			}
			case CHANGE_ICON:
			{
				itemStack = this.generatorTier.getGeneratorIcon();

				clickHandler = (panel, user, clickType, i) ->
				{
					// TODO: implement GUI for block selection
					this.isIconSelected = !this.isIconSelected;
					panel.getInventory().setItem(i, this.createButton(button).getItem());

					return true;
				};

				if (!this.isIconSelected)
				{
					description.add("");
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));
				}
				else
				{
					description.add("");
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-on-item"));
				}

				glow = this.isIconSelected;

				break;
			}
			case CHANGE_DESCRIPTION:
			{
				itemStack = new ItemStack(Material.WRITTEN_BOOK);
				description = new ArrayList<>(this.generatorTier.getDescription());

				clickHandler = (panel, user, clickType, i) ->
				{
					// Create consumer that process description change
					Consumer<List<String>> consumer = value ->
					{
						if (value != null)
						{
							this.generatorTier.setDescription(value);
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						this.build();
					};

					// start conversation
					ConversationUtils.createStringListInput(consumer,
						user,
						user.getTranslation(Constants.QUESTIONS + "write-description"),
						user.getTranslation(Constants.MESSAGE + "description-changed"));

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));
				
				// Not implemented in current GUI.
				break;
			}
			case CHANGE_DEFAULT:
			{
				itemStack = this.generatorTier.isDefaultGenerator() ?
					new ItemStack(Material.GREEN_BANNER) :
					new ItemStack(Material.RED_BANNER);

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.isDefaultGenerator())));

				clickHandler = (panel, user, clickType, i) ->
				{
					this.generatorTier.setDefaultGenerator(!this.generatorTier.isDefaultGenerator());
					this.manager.saveGeneratorTier(this.generatorTier);
					this.build();

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-toggle"));

				break;
			}
			case CHANGE_PRIORITY:
			{
				itemStack = new ItemStack(Material.HOPPER);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getPriority())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setPriority(number.intValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						2000);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_TYPE:
			{
				itemStack = new ItemStack(GuiUtils.getGeneratorTypeMaterial(this.generatorTier.getGeneratorType()));

				clickHandler = (panel, user, clickType, i) ->
				{
					SelectGeneratorTypePanel.open(user,
						this.generatorTier.getGeneratorType(),
						type -> {
							if (type != null)
							{
								this.generatorTier.setGeneratorType(type);
							}

							this.build();
						});

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_REQUIRED_MIN_LEVEL:
			{
				itemStack = new ItemStack(Material.DIAMOND);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getRequiredMinIslandLevel())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setRequiredMinIslandLevel(number.longValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						Long.MAX_VALUE);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_REQUIRED_PERMISSIONS:
			{
				itemStack = new ItemStack(Material.BOOK);

				if (this.generatorTier.getRequiredPermissions().size() == 1)
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
						Constants.VALUE, this.generatorTier.getRequiredPermissions().iterator().next()));
				}
				else
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-values"));

					for (String permission : this.generatorTier.getRequiredPermissions())
					{
						description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value-list",
							Constants.VALUE, permission));
					}
				}

				clickHandler = (panel, user, clickType, i) ->
				{
					// Create consumer that process description change
					Consumer<List<String>> consumer = value ->
					{
						if (value != null)
						{
							this.generatorTier.setRequiredPermissions(new HashSet<>(value));
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						this.build();
					};

					// start conversation
					ConversationUtils.createStringListInput(consumer,
						user,
						user.getTranslation(Constants.QUESTIONS + "write-permissions"),
						user.getTranslation(Constants.MESSAGE + "permissions-changed"));

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_PURCHASE_COST:
			{
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getGeneratorTierCost())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setGeneratorTierCost(number.doubleValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						Double.MAX_VALUE);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_ACTIVATION_COST:
			{
				itemStack = new ItemStack(Material.GOLD_INGOT);

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getActivationCost())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setActivationCost(number.doubleValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						Double.MAX_VALUE);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_BIOMES:
			{
				itemStack = new ItemStack(Material.FILLED_MAP);

				if (!this.generatorTier.getRequiredBiomes().isEmpty())
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-values"));

					for (Biome biome : this.generatorTier.getRequiredBiomes())
					{
						description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value-list",
							Constants.VALUE, Utils.prettifyObject(this.user, biome)));
					}
				}

				clickHandler = (panel, user, clickType, i) ->
				{
					SelectBiomePanel.open(user,
						this.generatorTier.getRequiredBiomes(),
						biomes -> {
							if (biomes != null)
							{
								this.generatorTier.setRequiredBiomes(biomes);
								this.manager.saveGeneratorTier(this.generatorTier);
							}

							this.build();
						});

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_DEPLOYED:
			{
				itemStack = new ItemStack(Material.LEVER);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.isDeployed())));

				clickHandler = (panel, user, clickType, i) ->
				{
					this.generatorTier.setDeployed(!this.generatorTier.isDeployed());
					this.manager.saveGeneratorTier(this.generatorTier);
					this.build();

					return true;
				};

				glow = this.generatorTier.isDeployed();

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-toggle"));

				break;
			}
			case CHANGE_TREASURE_AMOUNT:
			{
				itemStack = new ItemStack(Material.EMERALD, this.generatorTier.getMaxTreasureAmount());
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getMaxTreasureAmount())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setMaxTreasureAmount(number.intValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						1,
						Integer.MAX_VALUE);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

				break;
			}
			case CHANGE_TREASURE_CHANCE:
			{
				itemStack = new ItemStack(Material.PAPER);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.generatorTier.getTreasureChance())));

				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.generatorTier.setTreasureChance(number.doubleValue());
							this.manager.saveGeneratorTier(this.generatorTier);
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						Double.MAX_VALUE);

					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-change"));

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
			if (this.activeTab != button)
			{
				this.selectedMaterial.clear();
				this.materialChanceList.clear();

				if (button == Tab.BLOCKS)
				{
					// Operate with clone
					this.materialChanceList =
						Utils.treeMap2PairList(new TreeMap<>(this.generatorTier.getBlockChanceMap()));
				}
				else if (button == Tab.TREASURES)
				{
					// Operate with clone
					this.materialChanceList =
						Utils.treeMap2PairList(new TreeMap<>(this.generatorTier.getTreasureChanceMap()));
				}
			}

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
				// add empty line
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
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

		boolean glow = false;
		Material material = Material.PAPER;

		switch (button)
		{
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
			case ADD_MATERIAL:
			{
				material = Material.WRITABLE_BOOK;
				clickHandler = (panel, user1, clickType, slot) -> {

					SelectBlocksPanel.open(user1,
						true,
						this.activeTab == Tab.BLOCKS,
						value -> {
							if (value != null)
							{
								Consumer<Number> numberConsumer = number -> {
									if (number != null)
									{
										this.materialChanceList.add(
											new Pair<>(value.iterator().next(),
												number.doubleValue()));

										if (this.activeTab == Tab.BLOCKS)
										{
											this.generatorTier.setBlockChanceMap(
												Utils.pairList2TreeMap(this.materialChanceList));
											this.manager.saveGeneratorTier(this.generatorTier);
										}
										else if (this.activeTab == Tab.TREASURES)
										{
											this.generatorTier.setTreasureChanceMap(
												Utils.pairList2TreeMap(this.materialChanceList));
											this.manager.saveGeneratorTier(this.generatorTier);
										}
									}

									this.build();
								};

								ConversationUtils.createNumericInput(numberConsumer,
									this.user,
									this.user.getTranslation(Constants.QUESTIONS+ "input-number"),
									0.0,
									Long.MAX_VALUE);
							}
							else
							{
								this.build();
							}
						});

					return true;
				};

				break;
			}
			case REMOVE_MATERIAL:
			{
				material = this.selectedMaterial.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
				glow = !this.selectedMaterial.isEmpty();

				if (!this.selectedMaterial.isEmpty())
				{
					clickHandler = (panel, user1, clickType, slot) -> {

						this.materialChanceList.removeAll(this.selectedMaterial);

						if (this.activeTab == Tab.BLOCKS)
						{
							this.generatorTier.setBlockChanceMap(Utils.pairList2TreeMap(this.materialChanceList));
							this.manager.saveGeneratorTier(this.generatorTier);
							this.selectedMaterial.clear();
						}
						else if (this.activeTab == Tab.TREASURES)
						{
							this.generatorTier.setTreasureChanceMap(Utils.pairList2TreeMap(this.materialChanceList));
							this.manager.saveGeneratorTier(this.generatorTier);
							this.selectedMaterial.clear();
						}

						this.build();

						return true;
					};
				}

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


	/**
	 * This method creates button for material.
	 * @param blockChanceEntry blockChanceEntry which button must be created.
	 * @param maxValue Displays maximal value for map.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createMaterialButton(Pair<Material, Double> blockChanceEntry,  Double maxValue)
	{
		// Normalize value
		Double value = blockChanceEntry.getValue() / maxValue * 100.0;

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(Constants.BUTTON + "block-icon.description",
			TextVariables.NUMBER, String.valueOf(value),
			Constants.TENS, this.tensFormat.format(value),
			Constants.HUNDREDS, this.hundredsFormat.format(value),
			Constants.THOUSANDS, this.thousandsFormat.format(value),
			Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
			Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));
		description.add(this.user.getTranslation(Constants.BUTTON + "block-icon.actual",
			TextVariables.NUMBER, String.valueOf(blockChanceEntry.getValue())));

		boolean glow = this.selectedMaterial.contains(blockChanceEntry);

		description.add("");
		description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-edit"));

		if (!glow)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-select"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-deselect"));
		}

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
			if (clickType.isRightClick())
			{
				if (!this.selectedMaterial.remove(blockChanceEntry))
				{
					this.selectedMaterial.add(blockChanceEntry);
				}

				this.build();
			}
			else
			{
				Consumer<Number> numberConsumer = newValue -> {
					if (newValue != null)
					{
						blockChanceEntry.setValue(newValue.doubleValue());
						this.generatorTier.setBlockChanceMap(Utils.pairList2TreeMap(this.materialChanceList));
						this.manager.saveGeneratorTier(this.generatorTier);
					}

					this.build();
				};

				ConversationUtils.createNumericInput(numberConsumer,
					this.user,
					this.user.getTranslation(Constants.QUESTIONS+ "input-number"),
					0.0,
					Long.MAX_VALUE);
			}

			return true;
		};

		return new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "block-icon.name",
				Constants.BLOCK, Utils.prettifyObject(this.user, blockChanceEntry.getKey()))).
			description(description).
			icon(blockChanceEntry.getKey()).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
	 * This method creates button for treasure.
	 * @param treasureChanceEntry treasureChanceEntry which button must be created.
	 * @param maxValue Displays maximal value for map.
	 * @return PanelItem for generator tier.
	 */
	private PanelItem createTreasureButton(Pair<Material, Double> treasureChanceEntry, Double maxValue)
	{
		// Normalize value
		Double value = treasureChanceEntry.getValue() / maxValue * 100.0 * this.generatorTier.getTreasureChance();

		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslation(Constants.BUTTON + "treasure-icon.description",
			TextVariables.NUMBER, String.valueOf(value),
			Constants.TENS, this.tensFormat.format(value),
			Constants.HUNDREDS, this.hundredsFormat.format(value),
			Constants.THOUSANDS, this.thousandsFormat.format(value),
			Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
			Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));
		description.add(this.user.getTranslation(Constants.BUTTON + "treasure-icon.actual",
			TextVariables.NUMBER, String.valueOf(treasureChanceEntry.getValue())));

		boolean glow = this.selectedMaterial.contains(treasureChanceEntry);

		description.add("");
		description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-edit"));

		if (glow)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-select"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "right-click-to-deselect"));
		}

		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
			if (clickType.isRightClick())
			{
				if (!this.selectedMaterial.remove(treasureChanceEntry))
				{
					this.selectedMaterial.add(treasureChanceEntry);
				}

				this.build();
			}
			else
			{
				Consumer<Number> numberConsumer = newValue -> {
					if (newValue != null)
					{
						treasureChanceEntry.setValue(newValue.doubleValue());
						this.generatorTier.setTreasureChanceMap(Utils.pairList2TreeMap(this.materialChanceList));
						this.manager.saveGeneratorTier(this.generatorTier);
					}

					this.build();
				};

				ConversationUtils.createNumericInput(numberConsumer,
					this.user,
					this.user.getTranslation(Constants.QUESTIONS+ "input-number"),
					0.0,
					Long.MAX_VALUE);
			}

			return true;
		};

		return new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "treasure-icon.name",
				Constants.BLOCK, Utils.prettifyObject(this.user, treasureChanceEntry.getKey()))).
			description(description).
			icon(treasureChanceEntry.getKey()).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	// ---------------------------------------------------------------------
	// Section: Private Classes
	// ---------------------------------------------------------------------


	/**
	 * This class allows to change icon for Generator Tier
	 */
	private class IconChanger implements PanelListener
	{
		/**
		 * Process inventory click.
		 * If generator icon is selected and user clicks on item in his inventory, then change
		 * icon to the item from inventory.
		 *
		 * @param user the user
		 * @param event the event
		 */
		@Override
		public void onInventoryClick(User user, InventoryClickEvent event)
		{
			// Handle icon changing
			if (GeneratorEditPanel.this.isIconSelected &&
				event.getCurrentItem() != null &&
				!event.getCurrentItem().getType().equals(Material.AIR) &&
				event.getRawSlot() > 44)
			{
				// set material and amount only. Other data should be removed.
				GeneratorEditPanel.this.generatorTier.setGeneratorIcon(event.getCurrentItem().clone());
				// save change
				GeneratorEditPanel.this.manager.saveGeneratorTier(GeneratorEditPanel.this.generatorTier);
				// Deselect icon
				GeneratorEditPanel.this.isIconSelected = false;

				event.getInventory().setItem(19,
					GeneratorEditPanel.this.createButton(Button.CHANGE_ICON).getItem());
			}
		}


		/**
		 * On inventory close.
		 *
		 * @param event the event
		 */
		@Override
		public void onInventoryClose(InventoryCloseEvent event)
		{
			// Do nothing
		}


		/**
		 * Setup current listener.
		 */
		@Override
		public void setup()
		{
			// Do nothing
		}
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
		NEXT,
		/**
		 * Allows to add a new material to the block list.
		 */
		ADD_MATERIAL,
		/**
		 * Allows to remove selected materials from block list
		 */
		REMOVE_MATERIAL
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
		 * Holds Name type that allows to interact with generator name.
		 */
		CHANGE_NAME,
		/**
		 * Holds Name type that allows to interact with generator icon.
		 */
		CHANGE_ICON,
		/**
		 * Holds Name type that allows to interact with generator description.
		 */
		CHANGE_DESCRIPTION,
		/**
		 * Holds Name type that allows to interact with generator default status.
		 */
		CHANGE_DEFAULT,
		/**
		 * Holds Name type that allows to interact with generator priority.
		 */
		CHANGE_PRIORITY,
		/**
		 * Holds Name type that allows to interact with generator type.
		 */
		CHANGE_TYPE,
		/**
		 * Holds Name type that allows to interact with generator min island requirement.
		 */
		CHANGE_REQUIRED_MIN_LEVEL,
		/**
		 * Holds Name type that allows to interact with generator required permissions.
		 */
		CHANGE_REQUIRED_PERMISSIONS,
		/**
		 * Holds Name type that allows to interact with generator purchase cost.
		 */
		CHANGE_PURCHASE_COST,
		/**
		 * Holds Name type that allows to interact with generator activation cost.
		 */
		CHANGE_ACTIVATION_COST,
		/**
		 * Holds Name type that allows to interact with generator working biomes.
		 */
		CHANGE_BIOMES,
		/**
		 * Holds Name type that allows to interact with generator deployment status.
		 */
		CHANGE_DEPLOYED,
		/**
		 * Holds Name type that allows to interact with generator treasure amount.
		 */
		CHANGE_TREASURE_AMOUNT,
		/**
		 * Holds Name type that allows to interact with generator treasure chance.
		 */
		CHANGE_TREASURE_CHANCE,
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This variable stores generator tier that is viewed.
	 */
	private final GeneratorTierObject generatorTier;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * This variable stores which tab currently is active.
	 */
	private Tab activeTab;

	/**
	 * This variable stores if icon is selected for changing.
	 */
	private boolean isIconSelected;

	/**
	 * This set is used to detect and delete selected blocks.
	 */
	private Set<Pair<Material, Double>> selectedMaterial;

	/**
	 * This list contains elements of tree map that we can edit with a panel.
	 */
	private List<Pair<Material, Double>> materialChanceList;

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
