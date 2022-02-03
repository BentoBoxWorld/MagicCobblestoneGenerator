package world.bentobox.magiccobblestonegenerator.panels.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This Panel Allows to select Multiple Biomes.
 */
public class MultiBiomeSelector extends PagedSelector<Biome>
{
    /**
     * Instantiates a new Select biome panel.
     *
     * @param user the user
     * @param inputBiomes the input biome
     * @param consumer the consumer
     */
    private MultiBiomeSelector(User user, Set<Biome> inputBiomes, Consumer<Set<Biome>> consumer)
    {
        super(user);
        this.consumer = consumer;

        this.selectedBiomes = new LinkedHashSet<>(inputBiomes);

        // Disable All group Activations
        this.activeGroup = BiomeGroup.NONE;

        // Sort by name
        this.elements = Arrays.stream(Biome.values()).
            sorted(Comparator.comparing(Biome::name)).
            collect(Collectors.toList());
        // Init without filters applied.
        this.filterElements = this.elements;
    }


    /**
     * This method builds panel that allows to select biomes.
     */
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "select-biome"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        panelBuilder.item(0, this.buildButton(BiomeGroup.TEMPERATE));
        panelBuilder.item(1, this.buildButton(BiomeGroup.WARM));
        panelBuilder.item(2, this.buildButton(BiomeGroup.COLD));
        panelBuilder.item(3, this.buildButton(BiomeGroup.SNOWY));
        panelBuilder.item(4, this.buildButton(BiomeGroup.OCEAN));
        panelBuilder.item(5, this.buildButton(BiomeGroup.NETHER));
        panelBuilder.item(6, this.buildButton(BiomeGroup.THE_END));
        panelBuilder.item(7, this.buildButton(BiomeGroup.NEUTRAL));
        panelBuilder.item(8, this.buildButton(BiomeGroup.CAVE));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(39, this.createButton(Action.ACCEPT_BIOME));
        panelBuilder.item(44, this.createButton(Action.RETURN));

        panelBuilder.build();
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.elements;
        }
        else
        {
            this.filterElements = this.elements.stream().
                filter(element ->
                {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.name().toLowerCase().contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }

        if (!BiomeGroup.NONE.equals(this.activeGroup))
        {
            // Filter biomes according selected mode.

            this.filterElements = this.filterElements.stream().
                filter(biome -> {
                    switch (this.activeGroup)
                    {
                        case TEMPERATE -> {
                            return Utils.isTemperateBiome(biome);
                        }
                        case WARM -> {
                            return Utils.isWarmBiome(biome);
                        }
                        case COLD -> {
                            return Utils.isColdBiome(biome);
                        }
                        case SNOWY -> {
                            return Utils.isSnowyBiome(biome);
                        }
                        case OCEAN -> {
                            return Utils.isAquaticBiome(biome);
                        }
                        case NETHER -> {
                            return Utils.isNetherBiome(biome);
                        }
                        case THE_END -> {
                            return Utils.isTheEndBiome(biome);
                        }
                        case NEUTRAL -> {
                            return Utils.isNeutralBiome(biome);
                        }
                        case CAVE -> {
                            return Utils.isCaveBiome(biome);
                        }
                        default -> {
                            return false;
                        }
                    }
                }).
                collect(Collectors.toList());
        }
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

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Always return true.
            return true;
        };

        Material icon = Material.PAPER;
        int count = 1;

        switch (button) {
            case RETURN -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return NULL.
                    this.consumer.accept(null);
                    return true;
                };

                icon = Material.OAK_DOOR;
            }
            case ACCEPT_BIOME -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedBiomes.isEmpty()) {
                    description.add(this.user.getTranslation(reference + ".selected-biomes"));

                    for (Biome biome : this.selectedBiomes) {
                        description.add(this.user.getTranslation(reference + ".list-value",
                                Constants.VALUE, Utils.prettifyObject(this.user, biome)));
                    }
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-accept"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return selected biomes.
                    this.consumer.accept(this.selectedBiomes);
                    return true;
                };

                icon = Material.FILLED_MAP;
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            amount(count).
            icon(icon).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method builds PanelItem for given biome.
     *
     * @param biome Biome which PanelItem must be created.
     * @return new PanelItem for given biome.
     */
    @Override
    protected PanelItem createElementButton(Biome biome)
    {
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.MATERIALS + biome.name() + ".description"));

        // Add empty line
        if (!description.get(0).isEmpty())
        {
            if (this.selectedBiomes.contains(biome))
            {
                description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
            }

            description.add("");
        }
        else if (this.selectedBiomes.contains(biome))
        {
            // Append to the start
            description.add(0, this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        // Add tips section
        if (this.selectedBiomes.contains(biome))
        {
            description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-select"));
        }

        String name = this.user.getTranslation(Constants.BUTTON + "biome-icon.name",
            Constants.BIOME, Utils.prettifyObject(this.user, biome));

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(Material.MAP).
            clickHandler((panel, user1, clickType, slot) -> {
                if (!this.selectedBiomes.remove(biome))
                {
                    this.selectedBiomes.add(biome);
                }

                // update icons
                panel.getInventory().setItem(slot, this.createElementButton(biome).getItem());
                panel.getInventory().setItem(39, this.createButton(Action.ACCEPT_BIOME).getItem());
                return true;
            }).
            glow(this.selectedBiomes.contains(biome)).
            build();
    }


    /**
     * This method builds icon for given BiomeGroup object.
     *
     * @param biomeGroup object which icon must be created.
     * @return PanelItem for given BiomeGroup.
     */
    private PanelItem buildButton(BiomeGroup biomeGroup)
    {
        ItemStack icon;
        String name =
            this.user.getTranslation(Constants.BIOME_GROUP_BUTTON + biomeGroup.name().toLowerCase() + ".name");

        List<String> description = new ArrayList<>();
        description.add(this.user
            .getTranslation(Constants.BIOME_GROUP_BUTTON + biomeGroup.name().toLowerCase() + ".description"));

        if (this.activeGroup == biomeGroup)
        {
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-disable"));
        }
        else
        {
            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-enable"));
        }

        switch (biomeGroup) {
            case TEMPERATE -> icon = new ItemStack(Material.SUNFLOWER);
            case WARM -> icon = new ItemStack(Material.SAND);
            case COLD -> icon = new ItemStack(Material.GRAVEL);
            case SNOWY -> icon = new ItemStack(Material.SNOW_BLOCK);
            case OCEAN -> icon = new ItemStack(Material.TROPICAL_FISH);
            case NETHER -> icon = new ItemStack(Material.NETHERRACK);
            case THE_END -> icon = new ItemStack(Material.END_STONE);
            case CAVE -> icon = new ItemStack(Material.POINTED_DRIPSTONE);
            case NEUTRAL -> icon = new ItemStack(Material.STRUCTURE_VOID);
            case UNUSED -> icon = new ItemStack(Material.BARRIER);
            default -> {
                name = "";
                icon = new ItemStack(Material.AIR);
            }
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(this.activeGroup == biomeGroup).
            clickHandler((panel, user, clickType, slot) -> {
                if (this.activeGroup == biomeGroup)
                {
                    this.activeGroup = BiomeGroup.NONE;
                }
                else
                {
                    this.activeGroup = biomeGroup;
                }

                // Reset page index.
                this.updateFilters();
                this.build();
                return true;
            }).
            build();
    }


    /**
     * Opens panel for this class without necessity to create new class instance.
     *
     * @param user the user
     * @param inputBiomes the input biomes
     * @param consumer the consumer
     */
    public static void open(User user, Set<Biome> inputBiomes, Consumer<Set<Biome>> consumer)
    {
        new MultiBiomeSelector(user, inputBiomes, consumer).build();
    }


// ---------------------------------------------------------------------
// Section: Enum
// ---------------------------------------------------------------------


    /**
     * 9 Groups for each biome group from WIKI.
     */
    private enum BiomeGroup
    {
        SNOWY,
        COLD,
        TEMPERATE,
        WARM,
        OCEAN,
        NEUTRAL,
        NETHER,
        THE_END,
        UNUSED,
        CAVE,
        NONE
    }


    /**
     * Stores all available actions for Panel.
     */
    private enum Action
    {
        RETURN,
        ACCEPT_BIOME
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable stores consumer.
     */
    private final Consumer<Set<Biome>> consumer;

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<Biome> elements;

    /**
     * Selected biomes.
     */
    private final Set<Biome> selectedBiomes;

    /**
     * Stores filtered items.
     */
    private List<Biome> filterElements;

    /**
     * This variable stores currently active biome group.
     */
    private BiomeGroup activeGroup;
}
