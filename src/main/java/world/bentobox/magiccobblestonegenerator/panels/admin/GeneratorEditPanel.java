package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
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
        this.selectedTreasures = new HashSet<>();
        this.materialChanceList = new ArrayList<>();
        this.treasureChanceList = new ArrayList<>();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        if (this.generatorTier == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-generator-data"));
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

                // Add listener that allows to change icons
                panelBuilder.listener(new IconChanger());

                break;
            case BLOCKS:

                if (!this.materialChanceList.isEmpty())
                {
                    this.populateBlocks(panelBuilder);
                }

                panelBuilder.item(39, this.createButton(Action.ADD_MATERIAL));
                panelBuilder.item(41, this.createButton(Action.REMOVE_MATERIAL));

                // Add listener that allows to change blocks
//				panelBuilder.listener(new BlockChanger());

                break;
            case TREASURES:
                if (!this.treasureChanceList.isEmpty())
                {
                    this.populateTreasures(panelBuilder);
                }

                panelBuilder.item(39, this.createButton(Action.ADD_MATERIAL));
                panelBuilder.item(41, this.createButton(Action.REMOVE_MATERIAL));

                // Add listener that allows to change blocks
                panelBuilder.listener(new TreasureChanger());

                break;
        }

        panelBuilder.item(44, this.createButton(Action.RETURN));

        // Build panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method populates header with buttons for switching between tabs.
     *
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
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateInfo(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createButton(Button.NAME));
        panelBuilder.item(19, this.createButton(Button.ICON));
        panelBuilder.item(28, this.createButton(Button.DESCRIPTION));

        // Add locked icon
        panelBuilder.item(20, this.createButton(Button.LOCKED_ICON));

        // Usefull information to know about generators.
        panelBuilder.item(12, this.createButton(Button.DEFAULT));
        panelBuilder.item(21, this.createButton(Button.PRIORITY));
        panelBuilder.item(30, this.createButton(Button.TYPE));

        // Default genertator do not have requirements.
        if (!this.generatorTier.isDefaultGenerator())
        {
            if (this.addon.isLevelProvided())
            {
                panelBuilder.item(13, this.createButton(Button.REQUIRED_MIN_LEVEL));
            }

            // Display only permissions if they are required.
            panelBuilder.item(22, this.createButton(Button.REQUIRED_PERMISSIONS));

            if (this.addon.isVaultProvided())
            {
                // Display cost only if there exist vault.
                panelBuilder.item(31, this.createButton(Button.PURCHASE_COST));
            }
        }

        // If vault is disabled.
        if (this.addon.isVaultProvided())
        {
            panelBuilder.item(15, this.createButton(Button.ACTIVATION_COST));
        }

        panelBuilder.item(24, this.createButton(Button.BIOMES));

        // deployed button.
        panelBuilder.item(33, this.createButton(Button.DEPLOYED));

        // display treasures.
        panelBuilder.item(25, this.createButton(Button.TREASURE_CHANCE));
        panelBuilder.item(34, this.createButton(Button.TREASURE_AMOUNT));
    }


    /**
     * This method populates panel body with blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateBlocks(PanelBuilder panelBuilder)
    {
        final int MAX_ELEMENTS = 21;
        final int correctPage;

        this.maxPageIndex = (int) Math.ceil(1.0 * materialChanceList.size() / 21) - 1;

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

        // Update page index.
        this.pageIndex = correctPage;

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
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateTreasures(PanelBuilder panelBuilder)
    {
        final int MAX_ELEMENTS = 21;
        final int correctPage;

        this.maxPageIndex = (int) Math.ceil(1.0 * treasureChanceList.size() / 21) - 1;

        if (this.pageIndex < 0)
        {
            correctPage = this.treasureChanceList.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (this.treasureChanceList.size() / MAX_ELEMENTS))
        {
            correctPage = 0;
        }
        else
        {
            correctPage = this.pageIndex;
        }

        // Update page index.
        this.pageIndex = correctPage;

        if (this.treasureChanceList.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary

            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        int materialIndex = MAX_ELEMENTS * correctPage;

        // I want first row to be only for navigation and return button.
        int index = 10;

        // Store previous object value for displaying correct chance value.
        Double maxValue = this.generatorTier.getTreasureItemChanceMap().lastKey();

        while (materialIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
            materialIndex < this.treasureChanceList.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                // Get entry from list.
                Pair<ItemStack, Double> materialEntry = this.treasureChanceList.get(materialIndex++);
                // Add to panel
                panelBuilder.item(index, this.createTreasureButton(materialEntry, maxValue));
            }

            index++;
        }
    }


    /**
     * This method creates panel item for given button type.
     *
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
            case NAME:
            {
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.GENERATOR, this.generatorTier.getFriendlyName()));

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
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                // Not implemented in current GUI.
                break;
            }
            case ICON:
            case LOCKED_ICON:
            {
                itemStack = button == Button.LOCKED_ICON ?
                    this.generatorTier.getLockedIcon() :
                    this.generatorTier.getGeneratorIcon();

                clickHandler = (panel, user, clickType, i) ->
                {
                    // TODO: implement GUI for block selection
                    this.selectedButton = button;
                    // Requires more then one button updating.
                    this.build();

                    return true;
                };

                if (this.selectedButton != button)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }

                glow = this.selectedButton == button;

                break;
            }
            case DESCRIPTION:
            {
                itemStack = new ItemStack(Material.WRITTEN_BOOK);

                description.add(this.user.getTranslation(reference + ".value"));
                description.addAll(this.generatorTier.getDescription());

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

                    if (!this.generatorTier.getDescription().isEmpty() &&
                        clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));
                    }

                    if (!this.generatorTier.getDescription().isEmpty())
                    {
                        description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                    }

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                // Not implemented in current GUI.
                break;
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

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.generatorTier.setDefaultGenerator(!this.generatorTier.isDefaultGenerator());
                    this.manager.saveGeneratorTier(this.generatorTier);
                    this.build();

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                break;
            }
            case PRIORITY:
            {
                itemStack = new ItemStack(Material.HOPPER);
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getPriority())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

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
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case REQUIRED_MIN_LEVEL:
            {
                itemStack = new ItemStack(Material.DIAMOND);
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getRequiredMinIslandLevel())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Long.MAX_VALUE);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case REQUIRED_PERMISSIONS:
            {
                itemStack = new ItemStack(Material.BOOK);

                description.add(this.user.getTranslation(reference + ".list"));
                this.generatorTier.getRequiredPermissions().stream().sorted().forEach(permission ->
                    description.add(this.user.getTranslation(reference + ".value",
                        Constants.PERMISSION, permission)));

                if (this.generatorTier.getRequiredPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".none"));
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

                    if (!this.generatorTier.getRequiredPermissions().isEmpty() &&
                        clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-permissions"),
                            user.getTranslation(Constants.CONVERSATIONS + "permissions-changed"));
                    }

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.generatorTier.getRequiredPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }

                break;
            }
            case PURCHASE_COST:
            {
                itemStack = new ItemStack(Material.GOLD_BLOCK);
                
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getGeneratorTierCost())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case ACTIVATION_COST:
            {
                itemStack = new ItemStack(Material.GOLD_INGOT);

                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getActivationCost())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case BIOMES:
            {
                itemStack = new ItemStack(Material.FILLED_MAP);

                description.add(this.user.getTranslation(reference + ".list"));

                if (this.generatorTier.getRequiredBiomes().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".any"));
                }
                else
                {
                    this.generatorTier.getRequiredBiomes().stream().sorted().forEach(biome ->
                        description.add(this.user.getTranslation(reference + ".value",
                            Constants.BIOME, Utils.prettifyObject(this.user, biome))));
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
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case DEPLOYED:
            {
                itemStack = new ItemStack(Material.LEVER);

                if (this.generatorTier.isDeployed())
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                }

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.generatorTier.setDeployed(!this.generatorTier.isDeployed());
                    this.manager.saveGeneratorTier(this.generatorTier);
                    this.build();

                    return true;
                };

                glow = this.generatorTier.isDeployed();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

                break;
            }
            case TREASURE_AMOUNT:
            {
                itemStack = new ItemStack(Material.EMERALD);
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getMaxTreasureAmount())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        1,
                        Integer.MAX_VALUE);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                break;
            }
            case TREASURE_CHANCE:
            {
                itemStack = new ItemStack(Material.PAPER);
                description.add(this.user.getTranslation(reference + ".value",
                    Constants.NUMBER, String.valueOf(this.generatorTier.getTreasureChance())));

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
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

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
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Tab button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        if (button == Tab.TREASURES)
        {
            description.add(this.user.getTranslationOrNothing(reference + ".drag-and-drop"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            if (this.activeTab != button)
            {
                this.selectedMaterial.clear();
                this.materialChanceList.clear();
                this.selectedTreasures.clear();
                this.treasureChanceList.clear();

                if (button == Tab.BLOCKS)
                {
                    // Operate with clone
                    this.materialChanceList =
                        Utils.treeMap2PairList(new TreeMap<>(this.generatorTier.getBlockChanceMap()));
                }
                else if (button == Tab.TREASURES)
                {
                    // Operate with clone
                    this.treasureChanceList =
                        Utils.treeMap2PairList(new TreeMap<>(this.generatorTier.getTreasureItemChanceMap()));
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
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        boolean glow = false;
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
            case ADD_MATERIAL:
            {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));

                icon = Material.WRITABLE_BOOK;
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
                                        if (this.activeTab == Tab.BLOCKS)
                                        {
                                            this.materialChanceList.add(
                                                new Pair<>(value.iterator().next(),
                                                    number.doubleValue()));

                                            this.generatorTier.setBlockChanceMap(
                                                Utils.pairList2TreeMap(this.materialChanceList));
                                            this.manager.saveGeneratorTier(this.generatorTier);
                                        }
                                        else if (this.activeTab == Tab.TREASURES)
                                        {
                                            this.treasureChanceList.add(
                                                new Pair<>(new ItemStack(value.iterator().next()),
                                                    number.doubleValue()));

                                            this.generatorTier.setTreasureItemChanceMap(
                                                Utils.pairList2TreeMap(this.treasureChanceList));
                                            this.manager.saveGeneratorTier(this.generatorTier);
                                        }
                                    }

                                    this.build();
                                };

                                ConversationUtils.createNumericInput(numberConsumer,
                                    this.user,
                                    this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
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
                icon = this.selectedMaterial.isEmpty() && this.selectedTreasures.isEmpty() ?
                    Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedMaterial.isEmpty() || !this.selectedTreasures.isEmpty();

                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (glow)
                {
                    clickHandler = (panel, user1, clickType, slot) -> {

                        this.materialChanceList.removeAll(this.selectedMaterial);
                        this.treasureChanceList.removeAll(this.selectedTreasures);

                        if (this.activeTab == Tab.BLOCKS)
                        {
                            this.generatorTier.setBlockChanceMap(Utils.pairList2TreeMap(this.materialChanceList));
                            this.manager.saveGeneratorTier(this.generatorTier);
                            this.selectedMaterial.clear();
                        }
                        else if (this.activeTab == Tab.TREASURES)
                        {
                            this.generatorTier
                                .setTreasureItemChanceMap(Utils.pairList2TreeMap(this.treasureChanceList));
                            this.manager.saveGeneratorTier(this.generatorTier);
                            this.selectedTreasures.clear();
                        }

                        this.build();

                        return true;
                    };

                    description.add(this.user.getTranslation(reference + ".selected-materials"));

                    if (!this.selectedMaterial.isEmpty())
                    {
                        this.selectedMaterial.forEach(pair ->
                            description.add(this.user.getTranslation(reference + ".list-value",
                                Constants.VALUE, Utils.prettifyObject(this.user, pair.getKey()),
                                Constants.NUMBER, String.valueOf(pair.getValue()))));
                    }

                    if (!this.selectedTreasures.isEmpty())
                    {
                        this.selectedTreasures.forEach(pair ->
                            description.add(this.user.getTranslation(reference + ".list-value",
                                Constants.VALUE, Utils.prettifyObject(this.user, pair.getKey()),
                                Constants.NUMBER, String.valueOf(pair.getValue()))));
                    }

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "select-before"));
                }

                break;
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(count).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates button for material.
     *
     * @param blockChanceEntry blockChanceEntry which button must be created.
     * @param maxValue Displays maximal value for map.
     * @return PanelItem for generator tier.
     */
    private PanelItem createMaterialButton(Pair<Material, Double> blockChanceEntry, Double maxValue)
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

        if (glow)
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (!glow)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
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
                    this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
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
     *
     * @param treasureChanceEntry treasureChanceEntry which button must be created.
     * @param maxValue Displays maximal value for map.
     * @return PanelItem for generator tier.
     */
    private PanelItem createTreasureButton(Pair<ItemStack, Double> treasureChanceEntry, Double maxValue)
    {
        // Get item.
        ItemStack treasure = treasureChanceEntry.getKey().clone();

        // Normalize value
        Double value = treasureChanceEntry.getValue() / maxValue * 100.0 * this.generatorTier.getTreasureChance();

        List<String> description = new ArrayList<>();

        // if item meta is set and lore is not empty, add it to the description
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
        description.add(this.user.getTranslation(Constants.BUTTON + "treasure-icon.actual",
            TextVariables.NUMBER, String.valueOf(treasureChanceEntry.getValue())));

        boolean glow = this.selectedTreasures.contains(treasureChanceEntry);

        if (glow)
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (!glow)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) -> {
            if (clickType.isRightClick())
            {
                if (!this.selectedTreasures.remove(treasureChanceEntry))
                {
                    this.selectedTreasures.add(treasureChanceEntry);
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
                    this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                    0.0,
                    Long.MAX_VALUE);
            }

            return true;
        };

        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "treasure-icon.name",
                Constants.BLOCK, Utils.prettifyObject(this.user, treasure))).
            description(description).
            icon(GuiUtils.getMaterialItem(treasure.getType())).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param generatorTier Generator that must be displayed.
     */
    public static void open(CommonPanel panel,
        GeneratorTierObject generatorTier)
    {
        new GeneratorEditPanel(panel, generatorTier).build();
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
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // Handle icon changing
            if (GeneratorEditPanel.this.selectedButton != null &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.

                if (GeneratorEditPanel.this.selectedButton == Button.ICON)
                {
                    GeneratorEditPanel.this.generatorTier.setGeneratorIcon(event.getCurrentItem().clone());

                    // Deselect icon
                    GeneratorEditPanel.this.selectedButton = null;
                    // Rebuild icon
                    event.getInventory().setItem(19,
                        GeneratorEditPanel.this.createButton(Button.ICON).getItem());
                }
                else
                {
                    GeneratorEditPanel.this.generatorTier.setLockedIcon(event.getCurrentItem().clone());

                    // Deselect icon
                    GeneratorEditPanel.this.selectedButton = null;
                    // Rebuild icon
                    event.getInventory().setItem(20,
                        GeneratorEditPanel.this.createButton(Button.LOCKED_ICON).getItem());
                }

                // save change
                GeneratorEditPanel.this.manager.saveGeneratorTier(GeneratorEditPanel.this.generatorTier);
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


    /**
     * This class allows to change treasures for Generator Tier
     */
    private class TreasureChanger implements PanelListener
    {
        /**
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            if (GeneratorEditPanel.this.activeTab != Tab.TREASURES)
            {
                // Not a treasure gui
                event.setCancelled(true);
                return;
            }

            if (event.getRawSlot() > 44)
            {
                // Can select any item outside treasure gui.
                event.setCancelled(false);

                if (event.isShiftClick())
                {
                    this.addItemToTreasures(event.getCurrentItem());
                }
            }
            else if (event.getRawSlot() < 44)
            {
                if (event.getCursor() == null || event.getCursor().getType().isAir())
                {
                    // There is no selected items.
                    event.setCancelled(true);
                    return;
                }

                if (event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir())
                {
                    // There is an element in the spot.
                    event.setCancelled(true);
                    return;
                }

                this.addItemToTreasures(event.getCursor());
            }
        }


        /**
         * This method adds item to the treasure list.
         *
         * @param itemStack Object that must be added to treasure list.
         */
        private void addItemToTreasures(ItemStack itemStack)
        {
            if (itemStack != null && !itemStack.getType().isAir())
            {
                // Clone to avoid issues.
                GeneratorEditPanel.this.treasureChanceList.add(new Pair<>(itemStack.clone(), 1.0));
                GeneratorEditPanel.this.generatorTier.setTreasureItemChanceMap(
                    Utils.pairList2TreeMap(GeneratorEditPanel.this.treasureChanceList));
                GeneratorEditPanel.this.manager.saveGeneratorTier(GeneratorEditPanel.this.generatorTier);
                // Recall gui building.
                GeneratorEditPanel.this.build();
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
        NAME,
        /**
         * Holds Name type that allows to interact with generator icon.
         */
        ICON,
        /**
         * Holds Name type that allows to interact with generator locked icon.
         */
        LOCKED_ICON,
        /**
         * Holds Name type that allows to interact with generator description.
         */
        DESCRIPTION,
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
         * Holds Name type that allows to interact with generator deployment status.
         */
        DEPLOYED,
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
     * This variable stores generator tier that is viewed.
     */
    private final GeneratorTierObject generatorTier;

    /**
     * This set is used to detect and delete selected blocks.
     */
    private final Set<Pair<Material, Double>> selectedMaterial;

    /**
     * This set is used to detect and delete selected blocks.
     */
    private final Set<Pair<ItemStack, Double>> selectedTreasures;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;

    /**
     * This variable holds current max page index for multi-page generator choosing.
     */
    private int maxPageIndex;

    /**
     * This variable stores which tab currently is active.
     */
    private Tab activeTab;

    /**
     * This variable stores if icon is selected for changing.
     */
    private Button selectedButton;

    /**
     * This list contains elements of tree map that we can edit with a panel.
     */
    private List<Pair<Material, Double>> materialChanceList;

    /**
     * This list contains elements of tree map that we can edit with a panel.
     */
    private List<Pair<ItemStack, Double>> treasureChanceList;
}
