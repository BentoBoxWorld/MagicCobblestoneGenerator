package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class GeneratorManagePanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parentPanel Parent Panel object.
     */
    private GeneratorManagePanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        // Store generators in local list to avoid building it every time.
        this.generatorList = this.manager.getAllGeneratorTiers(this.world);

        // Stores how many elements will be in display.
        this.updateRows();

        // By default no-filters are active.
        this.activeFilterButton = Button.NONE;

        // Init set with selected generators.
        this.selectedGenerators = new HashSet<>(this.generatorList.size());
    }


    /**
     * This method updates row count for Panel.
     */
    private void updateRows()
    {
        this.rowCount = this.generatorList.size() > 14 ? 3 : this.generatorList.size() > 7 ? 2 : 1;
        this.maxPageIndex = (int) Math.ceil(1.0 * this.generatorList.size() / 7 * this.rowCount) - 1;
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-generators"));

        GuiUtils.fillBorder(panelBuilder, this.rowCount + 2, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.CREATE_GENERATOR));
        panelBuilder.item(2, this.createButton(Action.DELETE_GENERATOR));

        boolean hasCobblestoneGenerators = this.generatorList.stream().anyMatch(generator ->
            generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE));
        boolean hasStoneGenerators = this.generatorList.stream().anyMatch(generator ->
            generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE));
        boolean hasBasaltGenerators = this.generatorList.stream().anyMatch(generator ->
            generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT));

        // Do not show cobblestone button if there are no cobblestone generators.
        if (hasCobblestoneGenerators && (hasStoneGenerators || hasBasaltGenerators))
        {
            panelBuilder.item(5, this.createButton(Button.SHOW_COBBLESTONE));
        }

        // Do not show stone if there are no stone generators.
        if (hasStoneGenerators && (hasCobblestoneGenerators || hasBasaltGenerators))
        {
            panelBuilder.item(6, this.createButton(Button.SHOW_STONE));
        }

        // Do not show basalt if there are no basalt generators.
        if (hasBasaltGenerators && (hasStoneGenerators || hasCobblestoneGenerators))
        {
            panelBuilder.item(7, this.createButton(Button.SHOW_BASALT));
        }

        this.fillGeneratorTiers(panelBuilder);

        panelBuilder.item((this.rowCount + 2) * 9 - 1, this.createButton(Action.RETURN));

        // Build panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


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

        PanelItem.ClickHandler clickHandler;

        Material icon = Material.PAPER;
        boolean glow = false;
        int count = 1;

        switch (button) {
            case RETURN -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                if (this.parentPanel != null) {
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-return"));
                } else {
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-quit"));
                }

                clickHandler = (panel, user, clickType, i) -> {

                    if (this.parentPanel != null) {
                        this.parentPanel.build();
                    } else {
                        user.closeInventory();
                    }
                    return true;
                };

                icon = Material.OAK_DOOR;

                break;
            }
            case PREVIOUS -> {
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
            case NEXT -> {
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
            case CREATE_GENERATOR -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-create"));

                icon = Material.WRITABLE_BOOK;
                clickHandler = (panel, user1, clickType, slot) -> {
                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new generator creating with a name and id from given
                    // consumer value..
                    Consumer<String> generatorIdConsumer = value -> {
                        if (value != null) {
                            GeneratorTierObject newGeneratorTier = new GeneratorTierObject();
                            newGeneratorTier.setFriendlyName(value);
                            newGeneratorTier.setUniqueId(gameModePrefix + Utils.sanitizeInput(value));

                            // Default type is cobblestone.
                            newGeneratorTier.setGeneratorType(GeneratorTierObject.GeneratorType.COBBLESTONE);
                            // By default set as undeployed.
                            newGeneratorTier.setDeployed(false);
                            // Add PAPER as new icon.
                            newGeneratorTier.setGeneratorIcon(new ItemStack(Material.PAPER));

                            this.manager.saveGeneratorTier(newGeneratorTier);
                            this.manager.loadGeneratorTier(newGeneratorTier, false, this.user);

                            // Add new generator to generatorList.
                            this.generatorList.add(newGeneratorTier);
                            // Update row count
                            this.updateRows();

                            // Open generator edit panel.
                            GeneratorEditPanel.open(this, newGeneratorTier);
                        } else {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = generatorId ->
                            this.manager.getGeneratorByID(gameModePrefix + Utils.sanitizeInput(generatorId)) == null;

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(generatorIdConsumer,
                            validationFunction,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                            this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                                    Constants.WORLD, world.getName()),
                            Constants.ERRORS + "object-already-exists");

                    return true;
                };

                break;
            }
            case DELETE_GENERATOR -> {
                icon = this.selectedGenerators.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedGenerators.isEmpty();

                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedGenerators.isEmpty()) {
                    description.add(this.user.getTranslation(reference + ".list"));
                    this.selectedGenerators.forEach(generator ->
                            description.add(this.user.getTranslation(reference + ".value",
                                    Constants.GENERATOR, generator.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) -> {

                        // Create consumer that accepts value from conversation.
                        Consumer<Boolean> consumer = value -> {
                            if (value) {
                                this.selectedGenerators.forEach(generator -> {
                                    this.manager.wipeGeneratorTier(generator);
                                    this.generatorList.remove(generator);
                                });

                                this.selectedGenerators.clear();
                                this.updateRows();
                            }

                            this.build();
                        };

                        String generatorString;

                        if (!this.selectedGenerators.isEmpty()) {
                            Iterator<GeneratorTierObject> iterator = this.selectedGenerators.iterator();

                            StringBuilder builder = new StringBuilder();
                            builder.append(iterator.next().getFriendlyName());

                            while (iterator.hasNext()) {
                                builder.append(", ").append(iterator.next().getFriendlyName());
                            }

                            generatorString = builder.toString();
                        } else {
                            generatorString = "";
                        }

                        // Create conversation that gets user acceptance to delete selected generator data.
                        ConversationUtils.createConfirmation(
                                consumer,
                                this.user,
                                this.user.getTranslation(Constants.CONVERSATIONS + "confirm-deletion",
                                        TextVariables.NUMBER, String.valueOf(this.selectedGenerators.size()),
                                        Constants.VALUE, generatorString),
                                this.user.getTranslation(Constants.CONVERSATIONS + "data-removed",
                                        Constants.GAMEMODE, Utils.getGameMode(this.world)));

                        return true;
                    };
                } else {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "select-before"));

                    // Do nothing as no generators are selected.
                    clickHandler = (panel, user1, clickType, slot) -> true;
                }

                break;
            }
            default -> clickHandler = (panel, user1, clickType, slot) -> true;
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

        // Add helper description
        description.add("");
        if (this.activeFilterButton == button)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-deactivate"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-activate"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.activeFilterButton = this.activeFilterButton == button ? Button.NONE : button;
            // Rebuild everything.
            this.build();

            // Always return true.
            return true;
        };

        boolean glow = this.activeFilterButton == button;

        Material material = Material.PAPER;

        switch (button) {
            case SHOW_COBBLESTONE -> {
                material = Material.COBBLESTONE;
                break;
            }
            case SHOW_STONE -> {
                material = Material.STONE;
                break;
            }
            case SHOW_BASALT -> {
                material = Material.BASALT;
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
     * This method fills panel builder empty spaces with generator tiers and adds previous next buttons if necessary.
     *
     * @param panelBuilder PanelBuilder that is necessary to populate.
     */
    private void fillGeneratorTiers(PanelBuilder panelBuilder)
    {
        int MAX_ELEMENTS = this.rowCount * 7;

        final int correctPage;

        List<GeneratorTierObject> filteredList = switch (this.activeFilterButton) {
            case SHOW_COBBLESTONE -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)).
                    collect(Collectors.toList());
            case SHOW_STONE -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)).
                    collect(Collectors.toList());
            case SHOW_BASALT -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)).
                    collect(Collectors.toList());
            default -> this.generatorList;
        };

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
     *
     * @param generatorTier GeneratorTier which button must be created.
     * @return PanelItem for generator tier.
     */
    private PanelItem createGeneratorButton(GeneratorTierObject generatorTier)
    {
        boolean glow = this.selectedGenerators.contains(generatorTier);

        List<String> description = this.generateGeneratorDescription(generatorTier);

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Click handler should work only if user has a permission to change anything.
            // Otherwise just to view.

            if (clickType.isRightClick())
            {
                // Open edit panel.
                if (this.selectedGenerators.contains(generatorTier))
                {
                    this.selectedGenerators.remove(generatorTier);
                }
                else
                {
                    this.selectedGenerators.add(generatorTier);
                }

                // Build necessary as multiple icons are changed.
                this.build();
            }
            else
            {
                GeneratorEditPanel.open(this, generatorTier);
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
     * Admin should see simplified view. It is not necessary to view all unnecessary things.
     *
     * @param generator GeneratorTier which description must be generated.
     * @return List of strings that describes generator tier.
     */
    @Override
    protected List<String> generateGeneratorDescription(GeneratorTierObject generator)
    {
        List<String> description = super.generateGeneratorDescription(generator);

        if (this.selectedGenerators.contains(generator))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.selectedGenerators.contains(generator))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }

        return description;
    }


    /**
     * This method is used to open GeneratorManagePanel outside this class. It will be much easier to open panel with
     * single method call then initializing new object.
     *
     * @param parentPanel Parent Panel object.
     */
    public static void open(CommonPanel parentPanel)
    {
        new GeneratorManagePanel(parentPanel).build();
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
         * Filter for none.
         */
        NONE
    }


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
         * Allows to add new generator to the generatorList.
         */
        CREATE_GENERATOR,
        /**
         * Allows to delete selected generators from generatorList.
         */
        DELETE_GENERATOR
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores all generator tiers in the given world.
     */
    private final List<GeneratorTierObject> generatorList;

    /**
     * This variable stores all selected generator tiers.
     */
    private final Set<GeneratorTierObject> selectedGenerators;

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
    private int rowCount;

    /**
     * This variable holds current max page index for multi-page generator choosing.
     */
    private int maxPageIndex;
}
