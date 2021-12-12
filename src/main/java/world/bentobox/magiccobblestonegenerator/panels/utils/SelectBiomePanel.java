package world.bentobox.magiccobblestonegenerator.panels.utils;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This Panel Allows to select Multiple Biomes.
 */
public class SelectBiomePanel
{

    /**
     * Instantiates a new Select biome panel.
     *
     * @param user the user
     * @param inputBiomes the input biome
     * @param consumer the consumer
     */
    private SelectBiomePanel(User user, Set<Biome> inputBiomes, Consumer<Set<Biome>> consumer)
    {
        this.consumer = consumer;
        this.user = user;

        this.selectedBiomes = new LinkedHashSet<>(inputBiomes);

        // Disable All group Activations
        this.activeGroup = BiomeGroup.NONE;

        // Set page index to 0
        this.pageIndex = 0;
    }


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    private void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "select-biome"));

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        // Maximal elements in page.
        final int MAX_ELEMENTS = 21;

        List<Biome> biomeList = Utils.getBiomeNameMap().values().stream().
            filter(biome -> {
                return switch (activeGroup) {
                    case NONE -> true;
                    case TEMPERATE -> SelectBiomePanel.isTemperateBiome(biome);
                    case WARM -> SelectBiomePanel.isWarmBiome(biome);
                    case COLD -> SelectBiomePanel.isColdBiome(biome);
                    case SNOWY -> SelectBiomePanel.isSnowyBiome(biome);
                    case AQUATIC -> SelectBiomePanel.isAquaticBiome(biome);
                    case CAVE -> SelectBiomePanel.isCaveBiome(biome);
                    case NETHER -> SelectBiomePanel.isNetherBiome(biome);
                    case THE_END -> SelectBiomePanel.isTheEndBiome(biome);
                    case NEUTRAL -> SelectBiomePanel.isNeutralBiome(biome);
                    default -> true;
                };
            }).
            sorted().
            collect(Collectors.toList());

        // Calculate max page count.
        this.maxPageIndex = (int) Math.ceil(1.0 * biomeList.size() / 21) - 1;

        if (this.pageIndex < 0)
        {
            this.pageIndex = (biomeList.size() - 1) / MAX_ELEMENTS;
        }
        else if (this.pageIndex > ((biomeList.size() - 1) / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        panelBuilder.item(0, this.buildButton(BiomeGroup.TEMPERATE));
        panelBuilder.item(1, this.buildButton(BiomeGroup.WARM));
        panelBuilder.item(2, this.buildButton(BiomeGroup.COLD));
        panelBuilder.item(3, this.buildButton(BiomeGroup.SNOWY));
        panelBuilder.item(4, this.buildButton(BiomeGroup.AQUATIC));
        panelBuilder.item(5, this.buildButton(BiomeGroup.NETHER));
        panelBuilder.item(6, this.buildButton(BiomeGroup.THE_END));
        panelBuilder.item(7, this.buildButton(BiomeGroup.NEUTRAL));
        panelBuilder.item(8, this.buildButton(BiomeGroup.CAVE));

        int biomesIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (biomesIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            biomesIndex < biomeList.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                if (biomeList.size() != biomesIndex)
                {
                    panelBuilder.item(index,
                        this.createBiomeIcon(biomeList.get(biomesIndex++)));
                }
            }

            index++;
        }

        // Add buttons
        if ((biomeList.size() - 1) > MAX_ELEMENTS)
        {
            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        panelBuilder.item(40, this.createButton(Action.ACCEPT_BIOME));
        panelBuilder.item(44, this.createButton(Action.RETURN));

        panelBuilder.build();
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

                break;
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
    private PanelItem createBiomeIcon(Biome biome)
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
                panel.getInventory().setItem(slot, this.createBiomeIcon(biome).getItem());
                panel.getInventory().setItem(40, this.createButton(Action.ACCEPT_BIOME).getItem());
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
            case AQUATIC -> icon = new ItemStack(Material.TROPICAL_FISH);
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
                this.pageIndex = 0;
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
        new SelectBiomePanel(user, inputBiomes, consumer).build();
    }


    // ---------------------------------------------------------------------
    // Section: Static methods
    // ---------------------------------------------------------------------


    /**
     * This method returns if current biome is locally detected as snowy biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is snowy biome, {@code false} otherwise.
     */
    private static boolean isSnowyBiome(Biome biome)
    {
        return switch (biome) {
            //case SNOWY_SLOPES:
            case SNOWY_PLAINS, SNOWY_TAIGA, ICE_SPIKES, FROZEN_RIVER, SNOWY_BEACH -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as cold biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cold biome, {@code false} otherwise.
     */
    private static boolean isColdBiome(Biome biome)
    {
        return switch (biome) {
            case WINDSWEPT_HILLS, WINDSWEPT_GRAVELLY_HILLS, WINDSWEPT_FOREST, TAIGA, OLD_GROWTH_PINE_TAIGA, OLD_GROWTH_SPRUCE_TAIGA, STONY_SHORE -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as temperate biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is temperate biome, {@code false} otherwise.
     */
    private static boolean isTemperateBiome(Biome biome)
    {
        return switch (biome) {
            case PLAINS, SUNFLOWER_PLAINS, FOREST, FLOWER_FOREST, BIRCH_FOREST, OLD_GROWTH_BIRCH_FOREST, DARK_FOREST, SWAMP, JUNGLE, SPARSE_JUNGLE, BAMBOO_JUNGLE, RIVER, BEACH, MUSHROOM_FIELDS -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as warm biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is warm biome, {@code false} otherwise.
     */
    private static boolean isWarmBiome(Biome biome)
    {
        return switch (biome) {
            case DESERT, SAVANNA, WINDSWEPT_SAVANNA, BADLANDS, ERODED_BADLANDS, WOODED_BADLANDS, SAVANNA_PLATEAU ->
                    // case BADLANDS_PLATEAU:
                    true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as aquatic biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is aquatic biome, {@code false} otherwise.
     */
    private static boolean isAquaticBiome(Biome biome)
    {
        return switch (biome) {
            case WARM_OCEAN, LUKEWARM_OCEAN, DEEP_LUKEWARM_OCEAN, OCEAN, DEEP_OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN, FROZEN_OCEAN, DEEP_FROZEN_OCEAN -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as neutral biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
     */
    private static boolean isNeutralBiome(Biome biome)
    {
        return biome == Biome.THE_VOID;
    }


    /**
     * This method returns if current biome is locally detected as cave biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cave biome, {@code false} otherwise.
     */
    private static boolean isCaveBiome(Biome biome)
    {
        return switch (biome) {
            case LUSH_CAVES, DRIPSTONE_CAVES -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as nether biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is nether biome, {@code false} otherwise.
     */
    private static boolean isNetherBiome(Biome biome)
    {
        return switch (biome) {
            case NETHER_WASTES, SOUL_SAND_VALLEY, CRIMSON_FOREST, WARPED_FOREST, BASALT_DELTAS -> true;
            default -> false;
        };
    }


    /**
     * This method returns if current biome is locally detected as the end biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is the end biome, {@code false} otherwise.
     */
    private static boolean isTheEndBiome(Biome biome)
    {
        return switch (biome) {
            case THE_END, SMALL_END_ISLANDS, END_MIDLANDS, END_HIGHLANDS, END_BARRENS -> true;
            default -> false;
        };
    }


    // ---------------------------------------------------------------------
    // Section: Enums
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
        AQUATIC,
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
        PREVIOUS,
        NEXT,
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
     * User who runs GUI.
     */
    private final User user;

    /**
     * Selected biomes.
     */
    private final Set<Biome> selectedBiomes;

    /**
     * This variable stores currently active biome group.
     */
    private BiomeGroup activeGroup;

    /**
     * Variable stores active pageIndex.
     */
    private int pageIndex;

    /**
     * Variable stores max page index.
     */
    private int maxPageIndex;
}
