///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.magiccobblestonegenerator.panels.utils;


import org.bukkit.Material;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPagedPanel;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * The Bundle Selector GUI.
 */
public class BundleSelector extends CommonPagedPanel<GeneratorBundleObject>
{
    /**
     * Instantiates a new Select bundle panel.
     *
     * @param panel the parent panel
     * @param bundle the input bundle
     * @param consumer the consumer
     */
    private BundleSelector(CommonPanel panel, GeneratorBundleObject bundle, Consumer<GeneratorBundleObject> consumer)
    {
        super(panel);
        this.consumer = consumer;
        this.selectedBundle = bundle == null ? GeneratorBundleObject.dummyBundle : bundle;

        this.bundleList = this.addon.getAddonManager().getAllGeneratorBundles(this.world);

        // Add dummy bundle that contains all generators from GameMode.
        this.bundleList.add(0, GeneratorBundleObject.dummyBundle);
        // Add all generators to the dummy bundle
        GeneratorBundleObject.dummyBundle.setGeneratorTiers(
            this.addon.getAddonManager().getAllGeneratorTiers(this.world).stream().
                map(GeneratorTierObject::getUniqueId).
                collect(Collectors.toSet()));
    }


    /**
     * Update filters.
     */
    @Override
    protected void updateFilters()
    {
        // Not implemented for this gui.
    }


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    protected void build()
    {
        PanelBuilder panelBuilder =
            new PanelBuilder().user(this.user).name(this.user.getTranslation(Constants.TITLE + "select-bundle"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        this.populateElements(panelBuilder, this.bundleList);
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method builds PanelItem for given bundle.
     *
     * @param bundle Bundle which PanelItem must be created.
     * @return new PanelItem for given bundle.
     */
    @Override
    protected PanelItem createElementButton(GeneratorBundleObject bundle)
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
    @Override
    protected List<String> generateBundleDescription(GeneratorBundleObject bundle)
    {
        List<String> description = super.generateBundleDescription(bundle);

        if (this.selectedBundle == bundle)
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

        return description;
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
        new BundleSelector(panel, bundle, consumer).build();
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
}
