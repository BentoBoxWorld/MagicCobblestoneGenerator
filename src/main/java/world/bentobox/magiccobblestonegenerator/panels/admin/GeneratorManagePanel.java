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

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPagedPanel;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class GeneratorManagePanel extends CommonPagedPanel<GeneratorTierObject>
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
        this.elementList = this.manager.getAllGeneratorTiers(this.world);

        // By default no-filters are active.
        this.activeFilterButton = Button.NONE;

        // Init set with selected generators.
        this.selectedGenerators = new HashSet<>(this.elementList.size());
        this.updateFilters();
    }


    @Override
    protected void updateFilters()
    {
        this.filterElements = switch (this.activeFilterButton) {
            case SHOW_COBBLESTONE -> this.elementList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)).
                collect(Collectors.toList());
            case SHOW_STONE -> this.elementList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)).
                collect(Collectors.toList());
            case SHOW_BASALT -> this.elementList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)).
                collect(Collectors.toList());
            default -> new ArrayList<>(this.elementList);
        };

        if (this.searchString != null && !this.searchString.isEmpty())
        {
            // Remove all that does not match search string.

            this.filterElements.removeIf(tier -> !tier.getUniqueId().contains(this.searchString.toLowerCase()) &&
                !tier.getFriendlyName().toLowerCase().contains(this.searchString.toLowerCase()));
        }
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

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.CREATE_GENERATOR));
        panelBuilder.item(2, this.createButton(Action.DELETE_GENERATOR));

        boolean hasCobblestoneGenerators = this.elementList.stream().anyMatch(generator ->
            generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE));
        boolean hasStoneGenerators = this.elementList.stream().anyMatch(generator ->
            generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE));
        boolean hasBasaltGenerators = this.elementList.stream().anyMatch(generator ->
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

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);

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
                            this.elementList.add(newGeneratorTier);
                            // Update row count
                            this.updateFilters();

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
                                    this.elementList.remove(generator);
                                });

                                this.selectedGenerators.clear();
                                this.updateFilters();
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
            this.updateFilters();
            this.build();

            // Always return true.
            return true;
        };

        boolean glow = this.activeFilterButton == button;

        Material material = switch (button) {
            case SHOW_COBBLESTONE -> Material.COBBLESTONE;
            case SHOW_STONE -> Material.STONE;
            case SHOW_BASALT -> Material.BASALT;
            case NONE -> Material.PAPER;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates button for generator tier.
     *
     * @param generatorTier GeneratorTier which button must be created.
     * @return PanelItem for generator tier.
     */
    @Override
    protected PanelItem createElementButton(GeneratorTierObject generatorTier)
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
    private final List<GeneratorTierObject> elementList;

    /**
     * This variable stores all selected generator tiers.
     */
    private final Set<GeneratorTierObject> selectedGenerators;

    /**
     * This list contains currently displayed gui list.
     */
    private List<GeneratorTierObject> filterElements;

    /**
     * Stores currently active filter button.
     */
    private Button activeFilterButton;
}
