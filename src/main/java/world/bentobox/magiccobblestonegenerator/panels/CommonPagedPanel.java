///
// Created by BONNe
// Copyright - 2022
///


package world.bentobox.magiccobblestonegenerator.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This panel implements common things for Paged pages.
 *
 * @param <T> Type of elements that are paged.
 */
public abstract class CommonPagedPanel<T> extends CommonPanel
{
    /**
     * Instantiates a new Common paged panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     */
    protected CommonPagedPanel(StoneGeneratorAddon addon,
        User user,
        World world)
    {
        super(addon, user, world);
    }


    /**
     * Instantiates a new Common paged panel.
     *
     * @param parentPanel the parent panel
     */
    protected CommonPagedPanel(@NotNull CommonPanel parentPanel)
    {
        super(parentPanel);
    }


    /**
     * This method is called when filter value is updated.
     */
    protected abstract void updateFilters();


    /**
     * Create element button panel item.
     *
     * @param object the object
     * @return the panel item
     */
    protected abstract PanelItem createElementButton(T object);


    /**
     * Populate elements.
     *
     * @param panelBuilder the panel builder
     * @param objectList the object list
     */
    protected void populateElements(PanelBuilder panelBuilder, List<T> objectList)
    {
        final int MAX_ELEMENTS = 21;
        final int size = objectList.size();

        if (this.pageIndex < 0)
        {
            this.pageIndex = size / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (size / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int objectIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (objectIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            objectIndex < size &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createElementButton(objectList.get(objectIndex++)));
            }

            index++;
        }

        if (size > MAX_ELEMENTS && !(1.0 * size / MAX_ELEMENTS <= this.pageIndex + 1))
        {
            panelBuilder.item(26, this.getButton(CommonButtons.NEXT));
        }

        if (this.pageIndex > 0)
        {
            panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
        }

        // Add search button only if there is more than MAX_ELEMENTS objects or searchString
        // is not blank.
        if (!this.searchString.isBlank() || objectList.size() > MAX_ELEMENTS)
        {
            panelBuilder.item(40, this.getButton(CommonButtons.SEARCH));
        }
    }


    /**
     * This method returns PanelItem that represents given Button.
     *
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    protected PanelItem getButton(CommonButtons button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);

        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        if (button == CommonButtons.NEXT)
        {
            description.add(this.user.getTranslation(reference + "description",
                Constants.NUMBER, String.valueOf(this.pageIndex + 2)));

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));

            icon = new ItemStack(Material.OAK_SIGN, this.pageIndex + 2);
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.pageIndex++;
                this.build();
                return true;
            };
        }
        else if (button == CommonButtons.PREVIOUS)
        {
            description.add(this.user.getTranslation(reference + "description",
                Constants.NUMBER, String.valueOf(this.pageIndex)));

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));

            icon = new ItemStack(Material.OAK_SIGN, Math.max(1, this.pageIndex));
            clickHandler = (panel, user, clickType, slot) ->
            {
                this.pageIndex--;
                this.build();
                return true;
            };
        }
        else if (button == CommonButtons.SEARCH)
        {
            description.add(this.user.getTranslation(reference + "description"));

            if (this.searchString != null && !this.searchString.isEmpty())
            {
                description.add(this.user.getTranslation(reference + "search",
                    Constants.VALUE, this.searchString));
            }

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

            if (!this.searchString.isEmpty())
            {
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-clear"));
            }

            icon = new ItemStack(Material.ANVIL);

            clickHandler = (panel, user, clickType, slot) ->
            {
                if (clickType.isRightClick())
                {
                    // Clear string.
                    this.searchString = "";
                    this.updateFilters();
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
                            this.updateFilters();
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
        }
        else
        {
            icon = new ItemStack(Material.PAPER);
            clickHandler = null;
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            build();
    }


    /**
     * Next and Previous Buttons.
     */
    private enum CommonButtons
    {
        /**
         * Next common buttons.
         */
        NEXT,
        /**
         * Previous common buttons.
         */
        PREVIOUS,
        /**
         * Search common buttons.
         */
        SEARCH
    }

    /**
     * Text that contains filter string.
     */
    protected String searchString = "";

    /**
     * Current page index.
     */
    private int pageIndex;
}
