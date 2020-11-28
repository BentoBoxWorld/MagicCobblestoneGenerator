package world.bentobox.magiccobblestonegenerator.panels.utils;


import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This Panel Allows to select Bundle Panel
 */
public class SelectBundlePanel extends CommonPanel
{

    /**
     * Instantiates a new Select bundle panel.
     *
     * @param panel the parent panel
     * @param bundle the input bundle
     * @param consumer the consumer
     */
    private SelectBundlePanel(CommonPanel panel, GeneratorBundleObject bundle, Consumer<GeneratorBundleObject> consumer)
    {
        super(panel);
        this.consumer = consumer;
        this.selectedBundle = bundle;

        this.bundleList = this.addon.getAddonManager().getAllGeneratorBundles(this.world);

        // Set page index to 0
        this.pageIndex = 0;
    }


    /**
     * Opens panel for this class without necessity to create new class instance.
     *
     * @param panel the parent panel
     * @param bundle the input bundle
     * @param consumer the consumer
     */
    public static void open(CommonPanel panel, GeneratorBundleObject bundle, Consumer<GeneratorBundleObject> consumer)
    {
        new SelectBundlePanel(panel, bundle, consumer).build();
    }


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    public void build()
    {
        PanelBuilder panelBuilder =
            new PanelBuilder().user(this.user).name(this.user.getTranslation(Constants.TITLE + "select-bundle"));

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        // Maximal elements in page.
        final int MAX_ELEMENTS = 21;

        if (this.pageIndex < 0)
        {
            this.pageIndex = (this.bundleList.size() - 1) / MAX_ELEMENTS;
        }
        else if (this.pageIndex > ((this.bundleList.size() - 1) / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int elementIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (elementIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            elementIndex < (this.bundleList.size() - 1) &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                if (this.bundleList.size() != elementIndex)
                {
                    panelBuilder.item(index,
                        this.createBundleIcon(this.bundleList.get(elementIndex++)));
                }
            }

            index++;
        }

        // Add buttons
        if ((this.bundleList.size() - 1) > MAX_ELEMENTS)
        {
            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        panelBuilder.item(44, this.createButton(Action.RETURN));

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
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Always return true.
            return true;
        };


        Material material = Material.PAPER;

        switch (button)
        {
            case RETURN:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-cancel"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return NULL.
                    this.consumer.accept(this.selectedBundle);
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
            build();
    }


    /**
     * This method builds PanelItem for given bundle.
     *
     * @param bundle Bundle which PanelItem must be created.
     * @return new PanelItem for given bundle.
     */
    private PanelItem createBundleIcon(GeneratorBundleObject bundle)
    {
        boolean glow = this.selectedBundle == bundle;

        List<String> description = this.generateBundleDescription(bundle);

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.consumer.accept(bundle);
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

        description.add("");
        description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-choose"));

        return description;
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * Stores all available actions for Panel.
     */
    private enum Action
    {
        PREVIOUS,
        NEXT,
        RETURN
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable stores consumer.
     */
    private final Consumer<GeneratorBundleObject> consumer;

    /**
     * Bundle which was selected previously.
     */
    private final GeneratorBundleObject selectedBundle;

    /**
     * List of bundles that will be listed in this panel.
     */
    private final List<GeneratorBundleObject> bundleList;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;
}
