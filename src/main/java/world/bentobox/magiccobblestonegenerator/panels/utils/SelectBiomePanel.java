package world.bentobox.magiccobblestonegenerator.panels.utils;


import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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


    /**
     * This method builds panel that allows to select single challenge from input challenges.
     */
    private void build()
    {
        PanelBuilder panelBuilder =
            new PanelBuilder().user(this.user).name(this.user.getTranslation(Constants.TITLE + "select-biome"));

        GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        // Maximal elements in page.
        final int MAX_ELEMENTS = 21;

        List<Biome> biomeList = Utils.getBiomeNameMap().values().stream().
            filter(biome -> {
                switch (activeGroup)
                {
                    case NONE:
                        return true;
                    case LUSH:
                        return SelectBiomePanel.isLushBiome(biome);
                    case DRY:
                        return SelectBiomePanel.isDryBiome(biome);
                    case COLD:
                        return SelectBiomePanel.isColdBiome(biome);
                    case SNOWY:
                        return SelectBiomePanel.isSnowyBiome(biome);
                    case OCEAN:
                        return SelectBiomePanel.isOceanBiome(biome);
                    case NETHER:
                        return SelectBiomePanel.isNetherBiome(biome);
                    case THE_END:
                        return SelectBiomePanel.isTheEndBiome(biome);
                    case NEUTRAL:
                        return SelectBiomePanel.isNeutralBiome(biome);
                    case UNUSED:
                        return SelectBiomePanel.isUnusedBiome(biome);
                    default:
                        return true;
                }
            }).
            sorted().
            collect(Collectors.toList());

        if (this.pageIndex < 0)
        {
            this.pageIndex = (biomeList.size() - 1) / MAX_ELEMENTS;
        }
        else if (this.pageIndex > ((biomeList.size() - 1) / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        panelBuilder.item(0, this.buildButton(BiomeGroup.LUSH));
        panelBuilder.item(1, this.buildButton(BiomeGroup.DRY));
        panelBuilder.item(2, this.buildButton(BiomeGroup.COLD));
        panelBuilder.item(3, this.buildButton(BiomeGroup.SNOWY));
        panelBuilder.item(4, this.buildButton(BiomeGroup.OCEAN));
        panelBuilder.item(5, this.buildButton(BiomeGroup.NETHER));
        panelBuilder.item(6, this.buildButton(BiomeGroup.THE_END));
        panelBuilder.item(7, this.buildButton(BiomeGroup.NEUTRAL));
        panelBuilder.item(8, this.buildButton(BiomeGroup.UNUSED));

        int biomesIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (biomesIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            biomesIndex < (biomeList.size() - 1) &&
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

        panelBuilder.item(40, this.createButton(Action.ACCEPT));
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
                    this.consumer.accept(null);
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
            case ACCEPT:
            {
                if (!this.selectedBiomes.isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-values"));

                    for (Biome biome : this.selectedBiomes)
                    {
                        description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value-list",
                            Constants.VALUE, Utils.prettifyObject(this.user, biome)));
                    }
                }
                
                description.add("");
                description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-accept"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return selected biomes.
                    this.consumer.accept(this.selectedBiomes);
                    return true;
                };

                material = Material.FILLED_MAP;

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
     * This method builds PanelItem for given biome.
     *
     * @param biome Biome which PanelItem must be created.
     * @return new PanelItem for given biome.
     */
    private PanelItem createBiomeIcon(Biome biome)
    {
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BIOMES + biome.name() + ".description"));

        // Add empty line
        if (!description.get(0).isEmpty())
        {
            description.add("");
        }

        if (this.selectedBiomes.contains(biome))
        {
            description.add(this.user.getTranslationOrNothing(Constants.DESCRIPTION + "click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslationOrNothing(Constants.DESCRIPTION + "click-to-select"));
        }

        return new PanelItemBuilder().
            name(Utils.prettifyObject(this.user, biome)).
            description(description).
            icon(Material.MAP).
            clickHandler((panel, user1, clickType, slot) -> {
                if (!this.selectedBiomes.remove(biome))
                {
                    this.selectedBiomes.add(biome);
                }

                // update icons
                panel.getInventory().setItem(slot, this.createBiomeIcon(biome).getItem());
                panel.getInventory().setItem(40, this.createButton(Action.ACCEPT).getItem());
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
        String name = this.user.getTranslation(Constants.BIOME_GROUP_BUTTON + biomeGroup.name().toLowerCase() + ".name");

        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslation(Constants.BIOME_GROUP_BUTTON + biomeGroup.name().toLowerCase() + ".description"));

        if (this.activeGroup == biomeGroup)
        {
            description.add("");
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-deselect"));
        }
        else
        {
            description.add("");
            description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-select"));
        }

        switch (biomeGroup)
        {
            case LUSH:
                icon = new ItemStack(Material.SUNFLOWER);
                break;
            case DRY:
                icon = new ItemStack(Material.SAND);
                break;
            case COLD:
                icon = new ItemStack(Material.GRAVEL);
                break;
            case SNOWY:
                icon = new ItemStack(Material.SNOW_BLOCK);
                break;
            case OCEAN:
                icon = new ItemStack(Material.TROPICAL_FISH);
                break;
            case NETHER:
                icon = new ItemStack(Material.NETHERRACK);
                break;
            case THE_END:
                icon = new ItemStack(Material.END_STONE);
                break;
            case NEUTRAL:
                icon = new ItemStack(Material.STRUCTURE_VOID);
                break;
            case UNUSED:
                icon = new ItemStack(Material.BARRIER);
                break;
            default:
                name = "";
                icon = new ItemStack(Material.AIR);
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
        switch (biome)
        {
            case SNOWY_TUNDRA:
            case ICE_SPIKES:
            case SNOWY_TAIGA:
            case SNOWY_TAIGA_MOUNTAINS:
            case FROZEN_RIVER:
            case SNOWY_BEACH:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as cold biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cold biome, {@code false} otherwise.
     */
    private static boolean isColdBiome(Biome biome)
    {
        switch (biome)
        {
            case MOUNTAINS:
            case GRAVELLY_MOUNTAINS:
            case WOODED_MOUNTAINS:
            case MODIFIED_GRAVELLY_MOUNTAINS:
            case TAIGA:
            case TAIGA_MOUNTAINS:
            case GIANT_TREE_TAIGA:
            case GIANT_SPRUCE_TAIGA:
            case STONE_SHORE:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as lush biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is lush biome, {@code false} otherwise.
     */
    private static boolean isLushBiome(Biome biome)
    {
        switch (biome)
        {
            case PLAINS:
            case SUNFLOWER_PLAINS:
            case FOREST:
            case FLOWER_FOREST:
            case BIRCH_FOREST:
            case TALL_BIRCH_FOREST:
            case DARK_FOREST:
            case DARK_FOREST_HILLS:
            case SWAMP:
            case SWAMP_HILLS:
            case JUNGLE:
            case MODIFIED_JUNGLE:
            case JUNGLE_EDGE:
            case MODIFIED_JUNGLE_EDGE:
            case BAMBOO_JUNGLE:
            case RIVER:
            case BEACH:
            case MUSHROOM_FIELDS:
            case MUSHROOM_FIELD_SHORE:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as dry biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is dry biome, {@code false} otherwise.
     */
    private static boolean isDryBiome(Biome biome)
    {
        switch (biome)
        {
            case DESERT:
            case DESERT_LAKES:
            case SAVANNA:
            case SHATTERED_SAVANNA:
            case BADLANDS:
            case ERODED_BADLANDS:
            case WOODED_BADLANDS_PLATEAU:
            case MODIFIED_WOODED_BADLANDS_PLATEAU:
            case BADLANDS_PLATEAU:
            case SAVANNA_PLATEAU:
            case MODIFIED_BADLANDS_PLATEAU:
            case SHATTERED_SAVANNA_PLATEAU:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as ocean biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is ocean biome, {@code false} otherwise.
     */
    private static boolean isOceanBiome(Biome biome)
    {
        switch (biome)
        {
            case WARM_OCEAN:
            case LUKEWARM_OCEAN:
            case DEEP_LUKEWARM_OCEAN:
            case OCEAN:
            case DEEP_OCEAN:
            case COLD_OCEAN:
            case DEEP_COLD_OCEAN:
            case FROZEN_OCEAN:
            case DEEP_FROZEN_OCEAN:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as neutral biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
     */
    private static boolean isNeutralBiome(Biome biome)
    {
        switch (biome)
        {
            case THE_VOID:
            case WOODED_HILLS:
            case TAIGA_HILLS:
            case SNOWY_TAIGA_HILLS:
            case JUNGLE_HILLS:
            case DESERT_HILLS:
            case BIRCH_FOREST_HILLS:
            case TALL_BIRCH_HILLS:
            case GIANT_TREE_TAIGA_HILLS:
            case GIANT_SPRUCE_TAIGA_HILLS:
            case SNOWY_MOUNTAINS:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as unused biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is unused biome, {@code false} otherwise.
     */
    private static boolean isUnusedBiome(Biome biome)
    {
        switch (biome)
        {
            case MOUNTAIN_EDGE:
            case DEEP_WARM_OCEAN:
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as nether biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is nether biome, {@code false} otherwise.
     */
    private static boolean isNetherBiome(Biome biome)
    {
        // TODO: 1.15.2 support.
        switch (biome.name())
        {
            case "NETHER":
            case "NETHER_WASTES":
            case "SOUL_SAND_VALLEY":
            case "CRIMSON_FOREST":
            case "WARPED_FOREST":
            case "BASALT_DELTAS":
                return true;
            default:
                return false;
        }
    }


    /**
     * This method returns if current biome is locally detected as the end biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is the end biome, {@code false} otherwise.
     */
    private static boolean isTheEndBiome(Biome biome)
    {
        switch (biome)
        {
            case THE_END:
            case SMALL_END_ISLANDS:
            case END_MIDLANDS:
            case END_HIGHLANDS:
            case END_BARRENS:
                return true;
            default:
                return false;
        }
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * 9 Groups for each biome group from WIKI.
     */
    private enum BiomeGroup
    {
        LUSH,
        DRY,
        COLD,
        SNOWY,
        OCEAN,
        NETHER,
        THE_END,
        NEUTRAL,
        UNUSED,
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
        ACCEPT
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
}
