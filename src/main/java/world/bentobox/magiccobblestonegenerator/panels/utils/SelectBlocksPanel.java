package world.bentobox.magiccobblestonegenerator.panels.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class contains all necessary things that allows to select single or multiple blocks from all game blocks.
 */
public class SelectBlocksPanel
{
    /**
     * Instantiates a new Select blocks panel.
     *
     * @param user the user
     * @param singleSelect the single select
     * @param blockSelect the block select
     * @param excludedMaterial the excluded material
     * @param consumer the consumer
     */
    private SelectBlocksPanel(User user,
        boolean singleSelect,
        boolean blockSelect,
        Set<Material> excludedMaterial,
        Consumer<Set<Material>> consumer)
    {
        this.consumer = consumer;
        this.user = user;
        this.singleSelect = singleSelect;

        // Current GUI cannot display air blocks. It crashes with null-pointer
        excludedMaterial.add(Material.AIR);
        excludedMaterial.add(Material.CAVE_AIR);
        excludedMaterial.add(Material.VOID_AIR);

        // Piston head and moving piston is not necessary. useless.
        excludedMaterial.add(Material.PISTON_HEAD);
        excludedMaterial.add(Material.MOVING_PISTON);

        // Barrier cannot be accessible to user.
        excludedMaterial.add(Material.BARRIER);

        this.elements = new ArrayList<>();
        this.selectedMaterials = new HashSet<>();

        for (Material material : Material.values())
        {
            if ((!blockSelect || material.isBlock()) && !material.isLegacy() && !excludedMaterial.contains(material))
            {
                this.elements.add(material);
            }
        }

        this.filterElements = this.elements;

        this.pageIndex = 0;
        // Calculate max page count.
        this.maxPageIndex = (int) Math.ceil(1.0 * this.elements.size() / 21) - 1;
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "select-block"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        final int MAX_ELEMENTS = 21;

        if (this.filterElements == null)
        {
            if (this.searchString == null || this.searchString.isEmpty())
            {
                this.filterElements = this.elements;
            }
            else
            {
                this.filterElements = this.searchElements(this.elements);
            }

            this.maxPageIndex = (int) Math.ceil(1.0 * this.filterElements.size() / 21) - 1;
        }

        if (this.pageIndex < 0)
        {
            this.pageIndex = this.filterElements.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (this.filterElements.size() / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int materialIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (materialIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            materialIndex < this.filterElements.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createMaterialButton(this.filterElements.get(materialIndex++)));
            }

            index++;
        }

        if (this.filterElements.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary


            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        if (!this.singleSelect)
        {
            // Add accept blocks only if there are multi-select.
            panelBuilder.item(40, this.createButton(Action.ACCEPT_BLOCKS));
        }

        // Add search block icon.
        panelBuilder.item(4, this.createButton(Action.SEARCH_BLOCK));

        panelBuilder.item(44, this.createButton(Action.RETURN));

        panelBuilder.build();
    }


    /**
     * This method creates PanelItem that represents given material. Some materials is not displayable in Inventory GUI,
     * so they are replaced with "placeholder" items.
     *
     * @param material Material which icon must be created.
     * @return PanelItem that represents given material.
     */
    private PanelItem createMaterialButton(Material material)
    {
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.MATERIALS + material.name() + ".description"));

        // Add empty line
        if (!description.get(0).isEmpty())
        {
            if (this.selectedMaterials.contains(material))
            {
                description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
            }

            description.add("");
        }
        else if (this.selectedMaterials.contains(material))
        {
            // Append to the start
            description.add(0, this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        // Add tips section
        if (this.singleSelect)
        {
            description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-choose"));
        }
        else
        {
            if (this.selectedMaterials.contains(material))
            {
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-deselect"));
            }
            else
            {
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-select"));
            }
        }

        String name = this.user.getTranslation(Constants.BUTTON + "material-icon.name",
            Constants.BLOCK, Utils.prettifyObject(this.user, material));

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(PanelUtils.getMaterialItem(material)).
            clickHandler((panel, user1, clickType, slot) -> {
                if (this.singleSelect)
                {
                    this.consumer.accept(Collections.singleton(material));
                }
                else
                {
                    if (!this.selectedMaterials.remove(material))
                    {
                        this.selectedMaterials.add(material);
                    }

                    // update icons
                    panel.getInventory().setItem(slot, this.createMaterialButton(material).getItem());
                    panel.getInventory().setItem(40, this.createButton(Action.ACCEPT_BLOCKS).getItem());
                }

                return true;
            }).
            glow(this.selectedMaterials.contains(material)).
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

        PanelItem.ClickHandler clickHandler;
        Material icon;
        int count = 1;

        switch (button)
        {
            case RETURN:
            {
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
            case PREVIOUS:
            {
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
            case NEXT:
            {
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
            case ACCEPT_BLOCKS:
            {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedMaterials.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "selected-blocks"));

                    for (Material material : this.selectedMaterials)
                    {
                        description.add(this.user.getTranslation(reference + "list-value",
                            Constants.VALUE, Utils.prettifyObject(this.user, material)));
                    }
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-accept"));

                clickHandler = (panel, user, clickType, i) -> {
                    // Return selected biomes.
                    this.consumer.accept(this.selectedMaterials);
                    return true;
                };

                icon = Material.FILLED_MAP;

                break;
            }
            case SEARCH_BLOCK:
            {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (this.searchString != null && !this.searchString.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".search",
                        Constants.VALUE, this.searchString));
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-clear"));

                clickHandler = (panel, user, clickType, slot) -> {
                    if (clickType.isRightClick())
                    {
                        // Clear string.
                        this.searchString = "";
                        this.filterElements = null;
                        // Rebuild gui.
                        this.build();
                    }
                    else
                    {
                        // Create consumer that process description change
                        Consumer<String> consumer = value ->
                        {
                            if (value != null)
                            {
                                this.searchString = value;
                                this.filterElements = null;
                            }

                            this.build();
                        };

                        // start conversation
                        ConversationUtils.createStringInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-search"),
                            user.getTranslation(Constants.CONVERSATIONS + "search-updated"));
                    }

                    return true;
                };

                icon = Material.ANVIL;

                break;
            }
            default:
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            name(name).
            amount(count).
            description(description).
            icon(icon).
            clickHandler(clickHandler).
            build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method filters out materials that do not contains search field.
     *
     * @param materialCollection Collection of the materials from which it should search.
     * @return List of Materials that contains searched field.
     */
    private List<Material> searchElements(Collection<Material> materialCollection)
    {
        return materialCollection.stream().
            filter(material -> {
                // If material name is set and name contains search field, then do not filter out.
                return material.name().toLowerCase().contains(this.searchString.toLowerCase());
            }).
            distinct().
            collect(Collectors.toList());
    }


    /**
     * Opens panel that with all blocks from choose.
     *
     * @param user the user
     * @param singleSelect the single select
     * @param consumer the consumer
     */
    public static void open(User user, boolean singleSelect, Consumer<Set<Material>> consumer)
    {
        new SelectBlocksPanel(user, singleSelect, false, new HashSet<>(), consumer).build();
    }


    /**
     * Opens panel that with all blocks from choose.
     *
     * @param user the user
     * @param singleSelect the single select
     * @param blockSelect the block select
     * @param consumer the consumer
     */
    public static void open(User user, boolean singleSelect, boolean blockSelect, Consumer<Set<Material>> consumer)
    {
        new SelectBlocksPanel(user, singleSelect, blockSelect, new HashSet<>(), consumer).build();
    }


    /**
     * Opens panel that with all blocks from choose.
     *
     * @param user the user
     * @param singleSelect the single select
     * @param blockSelect the block select
     * @param excludedMaterial the excluded material
     * @param consumer the consumer
     */
    public static void open(User user,
        boolean singleSelect,
        boolean blockSelect,
        Set<Material> excludedMaterial,
        Consumer<Set<Material>> consumer)
    {
        new SelectBlocksPanel(user, singleSelect, blockSelect, excludedMaterial, consumer).build();
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
        RETURN,
        ACCEPT_BLOCKS,
        SEARCH_BLOCK
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<Material> elements;

    /**
     * Set that contains selected materials.
     */
    private final Set<Material> selectedMaterials;

    /**
     * This variable stores consumer.
     */
    private final Consumer<Set<Material>> consumer;

    /**
     * User who runs GUI.
     */
    private final User user;

    /**
     * This indicate that return set must contain only single item.
     */
    private final boolean singleSelect;

    /**
     * String that allows to search for a material.
     */
    private String searchString = null;

    /**
     * List with elements that will be displayed in current GUI.
     */
    private List<Material> filterElements;

    /**
     * Page index.
     */
    private int pageIndex;

    /**
     * This variable stores maximal page index for elements.
     */
    private int maxPageIndex;
}
