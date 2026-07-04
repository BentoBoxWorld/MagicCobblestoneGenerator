package world.bentobox.magiccobblestonegenerator.panels.utils;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This Panel allows to select multiple generator tiers, used for choosing prerequisite generators. It returns the
 * selected generators' unique ids.
 */
public class MultiGeneratorSelector extends PagedSelector<GeneratorTierObject>
{
    /**
     * Instantiates a new Multi generator selector.
     *
     * @param user the user
     * @param addon the addon
     * @param world the world whose generators are shown
     * @param excluded a generator that must not be selectable (e.g. the one being edited), or null
     * @param selectedIds the currently selected generator ids
     * @param consumer the consumer that receives the selected ids, or receives null as the value if cancelled
     */
    private MultiGeneratorSelector(User user,
        StoneGeneratorAddon addon,
        World world,
        GeneratorTierObject excluded,
        Set<String> selectedIds,
        Consumer<Set<String>> consumer)
    {
        super(user);
        this.consumer = consumer;
        this.selectedIds = new LinkedHashSet<>(selectedIds);

        // Show deployed, non-default generators (valid prerequisites), plus any generator that is already selected
        // even if it would otherwise be filtered out (e.g. undeployed/default), so existing selections remain
        // visible and can be deselected. The excluded generator (the one being edited) is never shown.
        // Non-deployed generators can never be unlocked, so they must not be selectable as *new* prerequisites.
        this.elements = addon.getAddonManager().getAllGeneratorTiers(world).stream().
            filter(generator -> excluded == null || !generator.getUniqueId().equals(excluded.getUniqueId())).
            filter(generator -> (generator.isDeployed() && !generator.isDefaultGenerator())
                || this.selectedIds.contains(generator.getUniqueId())).
            sorted(Comparator.comparing(GeneratorTierObject::getFriendlyName)).
            toList();

        this.filterElements = this.elements;
    }


    /**
     * This method builds panel that allows to select generators.
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
        panelBuilder.name(this.user.getTranslation(Constants.TITLE + "select-generator"));

        PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(39, this.createButton(Action.ACCEPT_GENERATOR));
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
                filter(element -> element.getFriendlyName().toLowerCase().contains(this.searchString.toLowerCase())).
                distinct().
                toList();
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

        PanelItem.ClickHandler clickHandler;
        Material icon;

        if (button == Action.ACCEPT_GENERATOR)
        {
            description.add(this.user.getTranslationOrNothing(reference + ".description"));

            if (!this.selectedIds.isEmpty())
            {
                description.add(this.user.getTranslation(reference + ".selected-generators"));

                this.elements.stream().
                    filter(generator -> this.selectedIds.contains(generator.getUniqueId())).
                    forEach(generator -> description.add(this.user.getTranslation(reference + ".list-value",
                        Constants.VALUE, generator.getFriendlyName())));
            }

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-accept"));

            clickHandler = (panel, user, clickType, i) -> {
                this.consumer.accept(this.selectedIds);
                return true;
            };

            icon = Material.FILLED_MAP;
        }
        else
        {
            description.add(this.user.getTranslationOrNothing(reference + ".description"));

            description.add("");
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

            clickHandler = (panel, user, clickType, i) -> {
                this.consumer.accept(null);
                return true;
            };

            icon = Material.OAK_DOOR;
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            amount(1).
            icon(icon).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method builds PanelItem for given generator.
     *
     * @param generator Generator which PanelItem must be created.
     * @return new PanelItem for given generator.
     */
    @Override
    protected PanelItem createElementButton(GeneratorTierObject generator)
    {
        List<String> description = new ArrayList<>();

        if (this.selectedIds.contains(generator.getUniqueId()))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
            description.add("");
            description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-select"));
        }

        return new PanelItemBuilder().
            name(generator.getFriendlyName()).
            description(description).
            icon(generator.getGeneratorIcon().clone()).
            clickHandler((panel, user1, clickType, slot) -> {
                if (!this.selectedIds.remove(generator.getUniqueId()))
                {
                    this.selectedIds.add(generator.getUniqueId());
                }

                // update icons
                panel.getInventory().setItem(slot, this.createElementButton(generator).getItem());
                panel.getInventory().setItem(39, this.createButton(Action.ACCEPT_GENERATOR).getItem());
                return true;
            }).
            glow(this.selectedIds.contains(generator.getUniqueId())).
            build();
    }


    /**
     * Opens panel for this class without necessity to create new class instance.
     *
     * @param user the user
     * @param addon the addon
     * @param world the world whose generators are shown
     * @param excluded a generator that must not be selectable, or null
     * @param selectedIds the currently selected generator ids
     * @param consumer the consumer that receives the selected ids, or receives null as the value if cancelled
     */
    public static void open(User user,
        StoneGeneratorAddon addon,
        World world,
        GeneratorTierObject excluded,
        Set<String> selectedIds,
        Consumer<Set<String>> consumer)
    {
        new MultiGeneratorSelector(user, addon, world, excluded, selectedIds, consumer).build();
    }


// ---------------------------------------------------------------------
// Section: Enum
// ---------------------------------------------------------------------


    /**
     * Stores all available actions for Panel.
     */
    private enum Action
    {
        RETURN,
        ACCEPT_GENERATOR
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable stores consumer.
     */
    private final Consumer<Set<String>> consumer;

    /**
     * List with elements that will be displayed in current GUI.
     */
    private final List<GeneratorTierObject> elements;

    /**
     * Selected generator ids.
     */
    private final Set<String> selectedIds;

    /**
     * Stores filtered items.
     */
    private List<GeneratorTierObject> filterElements;
}
