package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class BundleEditPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     * @param bundle Bundle that must be displayed.
     */
    private BundleEditPanel(CommonPanel panel,
        GeneratorBundleObject bundle)
    {
        super(panel);

        this.bundle = bundle;

        // By default no-filters are active.
        this.activeTab = Tab.BUNDLE_INFO;
        this.mode = Mode.VIEW;
        this.selectedGenerators = new HashSet<>();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        if (this.bundle == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-bundle-data"));
            return;
        }

        // PanelBuilder is a BentoBox API that provides ability to easily create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "view-bundle",
                Constants.BUNDLE, this.bundle.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder, this.activeTab == Tab.BUNDLE_INFO ? 3 : 5, Material.MAGENTA_STAINED_GLASS_PANE);
        this.populateHeader(panelBuilder);

        switch (this.activeTab) {
            case BUNDLE_INFO -> {
                this.populateInfo(panelBuilder);

                // Add listener that allows to change icons
                panelBuilder.listener(new IconChanger());

                // Reset mode as active tab is switched.
                this.mode = Mode.VIEW;
                this.selectedGenerators.clear();
            }
            case BUNDLE_GENERATORS -> {
                this.populateGenerators(panelBuilder);
                panelBuilder.item(39, this.createButton(Action.ADD_GENERATOR));
                if (this.mode == Mode.VIEW) {
                    // Add Remove button only in view mode.
                    panelBuilder.item(41, this.createButton(Action.REMOVE_GENERATOR));
                }
            }
        }

        panelBuilder.item(this.activeTab == Tab.BUNDLE_INFO ? 26 : 44, this.createButton(Action.RETURN));

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
        panelBuilder.item(3, this.createButton(Tab.BUNDLE_INFO));
        panelBuilder.item(5, this.createButton(Tab.BUNDLE_GENERATORS));
    }


    /**
     * This method populates panel body with info blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateInfo(PanelBuilder panelBuilder)
    {
        // Users should see only icon
        panelBuilder.item(13, this.createButton(Button.BUNDLE_NAME));
        panelBuilder.item(11, this.createButton(Button.BUNDLE_ICON));
        panelBuilder.item(14, this.createButton(Button.BUNDLE_DESCRIPTION));
    }


    /**
     * This method populates panel body with blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateGenerators(PanelBuilder panelBuilder)
    {
        List<GeneratorTierObject> generatorTierObjects = this.bundle.getGeneratorTiers().stream().
            map(this.addon.getAddonManager()::getGeneratorByID).
            filter(Objects::nonNull).
            sorted(Comparator.comparing(GeneratorTierObject::isDefaultGenerator).reversed().
                thenComparing(GeneratorTierObject::getPriority).
                thenComparing(GeneratorTierObject::getGeneratorType).
                thenComparing(GeneratorTierObject::getFriendlyName)).
            collect(Collectors.toList());

        if (this.mode == Mode.ADD)
        {
            // Need a list that does not contain current generators.
            final Set<GeneratorTierObject> currentSet = new HashSet<>(generatorTierObjects);

            // Assign new list to generator tier objects.
            generatorTierObjects = this.addon.getAddonManager().getAllGeneratorTiers(this.world).stream().
                filter(generator -> !currentSet.contains(generator)).
                collect(Collectors.toList());
        }

        final int MAX_ELEMENTS = 21;
        final int correctPage;
        this.maxPageIndex = (int) Math.ceil(1.0 * generatorTierObjects.size() / MAX_ELEMENTS) - 1;

        if (this.pageIndex < 0)
        {
            correctPage = generatorTierObjects.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (generatorTierObjects.size() / MAX_ELEMENTS))
        {
            correctPage = 0;
        }
        else
        {
            correctPage = this.pageIndex;
        }

        if (generatorTierObjects.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary

            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        int generatorIndex = MAX_ELEMENTS * correctPage;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (generatorIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
            generatorIndex < generatorTierObjects.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                // Add to panel
                panelBuilder.item(index,
                    this.createGeneratorButton(generatorTierObjects.get(generatorIndex++)));
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

        switch (button) {
            case BUNDLE_NAME -> {
                itemStack = new ItemStack(Material.NAME_TAG);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null) {
                            this.bundle.setFriendlyName(value);
                            this.manager.saveGeneratorBundle(this.bundle);
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

                description.add(this.user.getTranslation(reference + ".value",
                        Constants.BUNDLE, this.bundle.getFriendlyName()));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                // Not implemented in current GUI.
                break;
            }
            case BUNDLE_ICON -> {
                itemStack = this.bundle.getGeneratorIcon();

                clickHandler = (panel, user, clickType, i) ->
                {
                    // TODO: implement GUI for block selection
                    this.isIconSelected = !this.isIconSelected;
                    panel.getInventory().setItem(i, this.createButton(button).getItem());

                    return true;
                };

                if (!this.isIconSelected) {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                } else {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }

                glow = this.isIconSelected;

                break;
            }
            case BUNDLE_DESCRIPTION -> {
                itemStack = new ItemStack(Material.WRITTEN_BOOK);
                description = new ArrayList<>(this.bundle.getDescription());

                clickHandler = (panel, user, clickType, i) ->
                {
                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null) {
                            this.bundle.setDescription(value);
                            this.manager.saveGeneratorBundle(this.bundle);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));

                    return true;
                };

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                // Not implemented in current GUI.
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
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.activeTab = button;
            this.pageIndex = 0;

            this.build();
            return true;
        };

        Material material = switch (button) {
            case BUNDLE_INFO -> Material.WRITTEN_BOOK;
            case BUNDLE_GENERATORS -> Material.CHEST;
            default -> Material.PAPER;
        };

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

                    if (this.mode == Mode.ADD) {
                        // Rebuild view mode.
                        this.mode = Mode.VIEW;
                        this.build();
                    } else if (this.parentPanel != null) {
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
                count = Utils.getPreviousPage(this.pageIndex, this.maxPageIndex);
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
                count = Utils.getNextPage(this.pageIndex, this.maxPageIndex);
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
            case ADD_GENERATOR -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (this.mode == Mode.VIEW) {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));
                } else {
                    if (this.selectedGenerators.isEmpty()) {
                        description.add("");
                        description.add(this.user.getTranslation(Constants.TIPS + "select-before"));
                    } else {
                        description.add(this.user.getTranslation(reference + ".list"));
                        this.selectedGenerators.forEach(generator ->
                                description.add(this.user.getTranslation(reference + ".value",
                                        Constants.GENERATOR, generator.getFriendlyName())));

                        description.add("");
                        description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
                    }
                }

                icon = Material.WRITABLE_BOOK;
                glow = !this.selectedGenerators.isEmpty() && this.mode.equals(Mode.ADD);
                clickHandler = (panel, user1, clickType, slot) -> {

                    if (this.mode == Mode.VIEW) {
                        this.mode = Mode.ADD;
                        this.pageIndex = 0;
                    } else {
                        // Switch mode to view.
                        this.mode = Mode.VIEW;

                        // Switch page mode.
                        this.pageIndex = 0;

                        // Add all selected generators to the current bundle
                        this.selectedGenerators.forEach(generator ->
                                this.bundle.getGeneratorTiers().add(generator.getUniqueId()));

                        // clear selected generator list.
                        this.selectedGenerators.clear();
                    }

                    this.build();
                    return true;
                };

                break;
            }
            case REMOVE_GENERATOR -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                icon = this.selectedGenerators.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedGenerators.isEmpty() && this.mode.equals(Mode.VIEW);

                if (!this.selectedGenerators.isEmpty()) {
                    description.add(this.user.getTranslation(reference + ".list"));
                    this.selectedGenerators.forEach(generator ->
                            description.add(this.user.getTranslation(reference + ".value",
                                    Constants.GENERATOR, generator.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) -> {

                        // Should be active only in view mode.
                        if (this.mode == Mode.VIEW) {
                            // Remove all selected generators to the current bundle
                            this.selectedGenerators.forEach(generator ->
                                    this.bundle.getGeneratorTiers().remove(generator.getUniqueId()));

                            // clear selected generator list.
                            this.selectedGenerators.clear();
                            this.build();
                        }

                        return true;
                    };
                } else {
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

        if (this.selectedGenerators.contains(generator))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
        }

        return description;
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param bundle Bundle that must be displayed.
     */
    public static void open(CommonPanel panel,
        GeneratorBundleObject bundle)
    {
        new BundleEditPanel(panel, bundle).build();
    }


    // ---------------------------------------------------------------------
    // Section: Private Classes
    // ---------------------------------------------------------------------


    /**
     * This class allows to change the icon for the Generator Tier
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
            if (BundleEditPanel.this.isIconSelected &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                // set material and amount only. Other data should be removed.
                BundleEditPanel.this.bundle.setGeneratorIcon(event.getCurrentItem().clone());
                // save change
                BundleEditPanel.this.manager.saveGeneratorBundle(BundleEditPanel.this.bundle);
                // Deselect icon
                BundleEditPanel.this.isIconSelected = false;

                event.getInventory().setItem(11,
                    BundleEditPanel.this.createButton(Button.BUNDLE_ICON).getItem());
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
         * Allows to add a new generator to the generator list.
         */
        ADD_GENERATOR,
        /**
         * Allows to remove selected generator from generator list
         */
        REMOVE_GENERATOR
    }


    /**
     * This enum holds possible tabs for current gui.
     */
    private enum Tab
    {
        /**
         * General Information Tab
         */
        BUNDLE_INFO,
        /**
         * Generators Tab.
         */
        BUNDLE_GENERATORS
    }


    /**
     * This enum holds possible actions that this gui allows.
     */
    private enum Button
    {
        /**
         * Holds Name type that allows to interact with bundle name.
         */
        BUNDLE_NAME,
        /**
         * Holds Name type that allows to interact with bundle icon.
         */
        BUNDLE_ICON,
        /**
         * Holds Name type that allows to interact with bundle description.
         */
        BUNDLE_DESCRIPTION
    }


    /**
     * This allows to separate between adding and viewing/removing assigned generators to current bundle.
     */
    private enum Mode
    {
        /**
         * Holds generator view mode that allows to see currently assigned generators to viewed bundle.
         */
        VIEW,
        /**
         * Holds generator view mode that allows to see see generators which are not assigned to viewed bundle.
         */
        ADD
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores bundle that is viewed.
     */
    private final GeneratorBundleObject bundle;

    /**
     * Thi map stores selected generators.
     */
    private final Set<GeneratorTierObject> selectedGenerators;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;

    /**
     * This variable holds max page index for multi-page generator choosing.
     */
    private int maxPageIndex;

    /**
     * This variable stores which tab currently is active.
     */
    private Tab activeTab;

    /**
     * This variable stores if icon is selected for changing.
     */
    private boolean isIconSelected;

    /**
     * This variable stores currently used mode.
     */
    private Mode mode;
}
