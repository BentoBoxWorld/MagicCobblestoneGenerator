package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPagedPanel;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class BundleManagePanel extends CommonPagedPanel<GeneratorBundleObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parentPanel Parent Panel object.
     */
    private BundleManagePanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        // Store bundles in local list to avoid building it every time.
        this.elementList = this.manager.getAllGeneratorBundles(this.world);

        // Init set with selected bundles.
        this.selectedBundles = new HashSet<>(this.elementList.size());
    }


    /**
     * Update filters.
     */
    @Override
    protected void updateFilters()
    {
        // There are no filters that should be updated.
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
            name(this.user.getTranslation(Constants.TITLE + "manage-bundles"));


        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.CREATE_BUNDLE));
        panelBuilder.item(2, this.createButton(Action.DELETE_BUNDLE));

        this.populateElements(panelBuilder, this.elementList);

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
        boolean glow = false;

        Material icon = Material.PAPER;
        int count = 1;

        switch (button) {
            case CREATE_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-create"));

                icon = Material.WRITABLE_BOOK;
                clickHandler = (panel, user1, clickType, slot) -> {
                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value..
                    Consumer<String> bundleIdConsumer = value -> {
                        if (value != null) {
                            GeneratorBundleObject newBundle = new GeneratorBundleObject();
                            newBundle.setFriendlyName(value);
                            newBundle.setUniqueId(gameModePrefix + Utils.sanitizeInput(value));
                            // Add PAPER as new icon.
                            newBundle.setGeneratorIcon(new ItemStack(Material.PAPER));
                            newBundle.setDescription(new ArrayList<>());
                            newBundle.setGeneratorTiers(new HashSet<>());

                            this.manager.saveGeneratorBundle(newBundle);
                            this.manager.loadGeneratorBundle(newBundle, false, this.user);

                            // Add new generator to generatorList.
                            this.elementList.add(newBundle);
                            // Open bundle edit panel.
                            BundleEditPanel.open(this, newBundle);
                        } else {
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
                            this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                            this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                                    Constants.WORLD, world.getName()),
                            Constants.ERRORS + "object-already-exists");

                    return true;
                };
            }
            case DELETE_BUNDLE -> {
                icon = this.selectedBundles.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedBundles.isEmpty();

                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedBundles.isEmpty()) {
                    description.add(this.user.getTranslation(reference + ".list"));
                    this.selectedBundles.forEach(bundle ->
                            description.add(this.user.getTranslation(reference + ".value",
                                    Constants.BUNDLE, bundle.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) -> {

                        // Create consumer that accepts value from conversation.
                        Consumer<Boolean> consumer = value -> {
                            if (value) {
                                this.selectedBundles.forEach(bundle -> {
                                    this.manager.wipeBundle(bundle);
                                    this.elementList.remove(bundle);
                                });

                                this.selectedBundles.clear();
                            }

                            this.build();
                        };

                        String generatorString;

                        if (!this.selectedBundles.isEmpty()) {
                            Iterator<GeneratorBundleObject> iterator = this.selectedBundles.iterator();

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
                                        TextVariables.NUMBER, String.valueOf(this.selectedBundles.size()),
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
     * This method creates button for bundle tier.
     *
     * @param bundle Bundle which button must be created.
     * @return PanelItem for bundle.
     */
    @Override
    protected PanelItem createElementButton(GeneratorBundleObject bundle)
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
        List<String> description = super.generateBundleDescription(bundle);

        if (this.selectedBundles.contains(bundle))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.selectedBundles.contains(bundle))
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
        new BundleManagePanel(parentPanel).build();
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
    private final List<GeneratorBundleObject> elementList;

    /**
     * This variable stores all selected bundles.
     */
    private final Set<GeneratorBundleObject> selectedBundles;
}
