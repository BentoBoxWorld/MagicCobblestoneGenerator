package world.bentobox.magiccobblestonegenerator.panels.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * The type Select generator type panel.
 */
public class SelectGeneratorTypePanel
{
    /**
     * Instantiates a new Select generator type panel.
     *
     * @param user the user
     * @param generatorType the generator type
     * @param consumer the consumer
     */
    private SelectGeneratorTypePanel(User user,
        GeneratorTierObject.GeneratorType generatorType,
        Consumer<GeneratorTierObject.GeneratorType> consumer)
    {
        this.consumer = consumer;
        this.user = user;
        this.generatorType = generatorType;
    }


    /**
     * This method builds panel.
     */
    private void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "select-type"));

        PanelUtils.fillBorder(panelBuilder, 3, Material.BLUE_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.buildButton(GeneratorTierObject.GeneratorType.COBBLESTONE));
        panelBuilder.item(11, this.buildButton(GeneratorTierObject.GeneratorType.STONE));
        panelBuilder.item(12, this.buildButton(GeneratorTierObject.GeneratorType.BASALT));

        panelBuilder.item(13, this.buildButton(GeneratorTierObject.GeneratorType.COBBLESTONE_OR_STONE));
        panelBuilder.item(14, this.buildButton(GeneratorTierObject.GeneratorType.BASALT_OR_COBBLESTONE));
        panelBuilder.item(15, this.buildButton(GeneratorTierObject.GeneratorType.BASALT_OR_STONE));

        panelBuilder.item(16, this.buildButton(GeneratorTierObject.GeneratorType.ANY));

        panelBuilder.item(26, this.createButton());

        panelBuilder.build();
    }


    /**
     * This method creates return button.
     *
     * @return Return Button item.
     */
    private PanelItem createButton()
    {
        String name = this.user.getTranslation(Constants.BUTTON + "return.name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BUTTON + "return.description"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Return NULL.
            this.consumer.accept(null);
            return true;
        };

        Material material = Material.OAK_DOOR;

        // Add tips:
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method builds icon for given GeneratorType object.
     *
     * @param generatorType object which icon must be created.
     * @return PanelItem for given BiomeGroup.
     */
    private PanelItem buildButton(GeneratorTierObject.GeneratorType generatorType)
    {
        List<String> description = new ArrayList<>();
        description.add(this.user
            .getTranslation(Constants.GENERATOR_TYPE_BUTTON + generatorType.name().toLowerCase() + ".description"));

        // Add tips:
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

        return new PanelItemBuilder().
            icon(Utils.getGeneratorTypeMaterial(generatorType)).
            name(this.user
                .getTranslation(Constants.GENERATOR_TYPE_BUTTON + generatorType.name().toLowerCase() + ".name")).
            description(description).
            glow(this.generatorType == generatorType).
            clickHandler((panel, user, clickType, slot) -> {
                this.consumer.accept(generatorType);
                return true;
            }).
            build();
    }


    /**
     * Opens panel for this class without necessity to create new class instance.
     *
     * @param user the user
     * @param generatorType the input generator type
     * @param consumer the consumer
     */
    public static void open(User user,
        GeneratorTierObject.GeneratorType generatorType,
        Consumer<GeneratorTierObject.GeneratorType> consumer)
    {
        new SelectGeneratorTypePanel(user, generatorType, consumer).build();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores consumer.
     */
    private final Consumer<GeneratorTierObject.GeneratorType> consumer;

    /**
     * User who runs GUI.
     */
    private final User user;

    /**
     * Input biome.
     */
    private final GeneratorTierObject.GeneratorType generatorType;
}
