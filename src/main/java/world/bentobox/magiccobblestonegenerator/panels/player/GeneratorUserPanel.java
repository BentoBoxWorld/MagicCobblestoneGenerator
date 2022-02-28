package world.bentobox.magiccobblestonegenerator.panels.player;


import com.google.common.base.Enums;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows generators for user.
 */
public class GeneratorUserPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon StoneGeneratorAddon object.
     */
    private GeneratorUserPanel(StoneGeneratorAddon addon,
        World world,
        User user)
    {
        super(addon, user, world);
        this.island = this.addon.getIslands().getIsland(world, user);

        // Get valid user island data
        this.generatorData = this.manager.validateIslandData(this.island);

        // Store generators in local list to avoid building it every time.
        this.generatorList = this.manager.getIslandGeneratorTiers(world, this.generatorData);

        // By default no-filters are active.
        this.activeFilterButton = Filter.NONE;

        // Update element list.
        this.updateElements();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        if (this.island == null || this.generatorData == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation("general.errors.no-island"));
            return;
        }

        // Do not open gui if there is no magic sticks.
        if (this.generatorList.isEmpty())
        {
            this.addon.logError("There are no available generators!");
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-generators-in-world",
                Constants.WORLD, this.world.getName()));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("main_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("GENERATOR", this::createGeneratorButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);
        panelBuilder.registerTypeBuilder("RETURN", this::createReturnButton);

        // Register Filter button
        panelBuilder.registerTypeBuilder("FILTER", this::createFilterButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Create Action Buttons
// ---------------------------------------------------------------------


    /**
     * Create next button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createNextButton(@NotNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        int size = this.elementList.size();

        if (size <= slot.amountMap().getOrDefault("GENERATOR", 1) ||
            1.0 * size / slot.amountMap().getOrDefault("GENERATOR", 1) <= this.generatorIndex + 1)
        {
            // There are no next elements
            return null;
        }

        int nextPageIndex = this.generatorIndex + 2;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.NUMBER, String.valueOf(nextPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType() || ClickType.UNKNOWN.equals(action.clickType()))
                {
                    if ("NEXT".equalsIgnoreCase(action.actionType()))
                    {
                        this.generatorIndex++;
                        this.build();
                    }
                }
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create previous button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createPreviousButton(@NotNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.generatorIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.generatorIndex;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description(),
                Constants.NUMBER, String.valueOf(previousPageIndex)));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType() || ClickType.UNKNOWN.equals(action.clickType()))
                {
                    if ("PREVIOUS".equalsIgnoreCase(action.actionType()))
                    {
                        this.generatorIndex--;
                        this.build();
                    }
                }
            }

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    /**
     * Create return button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createReturnButton(@NotNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }

        // Add ClickHandler
        if (this.returnButton.getClickHandler().isPresent())
        {
            builder.clickHandler(this.returnButton.getClickHandler().get());
        }

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Create Filter Buttons
// ---------------------------------------------------------------------


    /**
     * Create filter button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createFilterButton(@NotNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            // Set icon
            builder.icon(template.icon().clone());
        }

        if (template.title() != null)
        {
            // Set title
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            // Set description
            builder.description(this.user.getTranslation(this.world, template.description()));
        }

        Filter filter = Enums.getIfPresent(Filter.class, String.valueOf(template.dataMap().get("filter"))).or(Filter.NONE);

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
        {
            switch (action.actionType().toUpperCase())
            {
                case "ENABLE" -> {
                    return this.activeFilterButton == filter;
                }
                case "DISABLE" -> {
                    return this.activeFilterButton != filter;
                }
                default -> {
                    return false;
                }
            }
        });

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType() || ClickType.UNKNOWN.equals(action.clickType()))
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "ENABLE" -> this.activeFilterButton = filter;
                        case "DISABLE" -> this.activeFilterButton = Filter.NONE;
                    }

                    // Reset page.
                    this.generatorIndex = 0;
                    // Update elements.
                    this.updateElements();
                    this.build();
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = activeActions.stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        builder.glow(this.activeFilterButton == filter);

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Create Generator Buttons
// ---------------------------------------------------------------------


    /**
     * Create generator button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createGeneratorButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.elementList.isEmpty())
        {
            // Does not contain any generators.
            return null;
        }

        GeneratorTierObject generatorTier;

        // Check if that is a specific generator
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a generator with given id;
            generatorTier = this.generatorList.stream().
                filter(generatorId -> generatorId.getUniqueId().equals(id)).
                findFirst().
                orElse(null);

            if (generatorTier == null)
            {
                // There is no generator in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.generatorIndex * slot.amountMap().getOrDefault("GENERATOR", 1) + slot.slot();

            if (index >= this.elementList.size())
            {
                // Out of index.
                return null;
            }

            generatorTier = this.elementList.get(index);
        }

        return this.createGeneratorButton(template, generatorTier);
    }


    /**
     * Create generator button panel item.
     *
     * @param template the template
     * @param generatorTier the generator tier object
     * @return the panel item
     */
    @NotNull
    private PanelItem createGeneratorButton(ItemTemplateRecord template, GeneratorTierObject generatorTier)
    {
        // Default generator should be active.
        boolean isActive = generatorTier.isDefaultGenerator() ||
            this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId());
        // Default generator should be always isUnlocked.
        boolean isUnlocked = generatorTier.isDefaultGenerator() ||
            this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId());
        // Default generators cannot be isPurchased.
        boolean isPurchased = generatorTier.isDefaultGenerator() ||
            !this.addon.isVaultProvided() ||
            generatorTier.getGeneratorTierCost() == 0 ||
            this.generatorData.getPurchasedTiers().contains(generatorTier.getUniqueId());

        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        if (template.icon() != null)
        {
            builder.icon(template.icon());
        }
        else if (isUnlocked)
        {
            builder.icon(generatorTier.getGeneratorIcon());
        }
        else
        {
            builder.icon(generatorTier.getLockedIcon());
        }

        builder.icon(template.icon() != null ? template.icon().clone() : generatorTier.getGeneratorIcon());

        // Template specific title is always more important than generatorTier name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.NAME, generatorTier.getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(generatorTier.getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateGeneratorDescription(generatorTier,
                isActive,
                isUnlocked,
                isPurchased,
                this.manager.getIslandLevel(this.island)));
        }

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
        {
            switch (action.actionType().toUpperCase())
            {
                case "BUY" -> {
                    return this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        isPurchased ||
                        generatorTier.isDefaultGenerator();
                }
                case "ACTIVATE" -> {
                    return this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        generatorTier.isDefaultGenerator() ||
                        isActive;
                }
                case "DEACTIVATE" -> {
                    return this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        generatorTier.isDefaultGenerator() ||
                        !isActive;
                }
                default -> {
                    return false;
                }
            }
        });

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType() || ClickType.UNKNOWN.equals(action.clickType()))
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "VIEW" -> {
                            GeneratorViewPanel.openPanel(this, generatorTier);
                        }
                        case "BUY" -> {
                            if (this.island != null && this.manager.canPurchaseGenerator(user, this.island, this.generatorData, generatorTier))
                            {
                                this.manager.purchaseGenerator(this.user, this.island, this.generatorData, generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "ACTIVATE" -> {
                            if (this.island != null && this.manager.canActivateGenerator(user, this.island, this.generatorData, generatorTier))
                            {
                                this.manager.activateGenerator(user, this.island, this.generatorData, generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "DEACTIVATE" -> {
                            this.manager.deactivateGenerator(user, this.generatorData, generatorTier);
                            // Rebuild whole gui.
                            this.build();
                        }
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = activeActions.stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        // Add glow
        builder.glow(isActive);

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Other Methods
// ---------------------------------------------------------------------


    /**
     * This method updates element list with active filter.
     */
    private void updateElements()
    {
        this.elementList = switch (this.activeFilterButton) {
            case COBBLESTONE -> this.generatorList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)).
                collect(Collectors.toList());
            case STONE -> this.generatorList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)).
                collect(Collectors.toList());
            case BASALT -> this.generatorList.stream().
                filter(generatorTier ->
                    generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)).
                collect(Collectors.toList());
            case VISIBILITY -> this.generatorList.stream().
                filter(generatorTier -> generatorTier.isDefaultGenerator() ||
                    this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId())).
                collect(Collectors.toList());
            case ACTIVE -> this.generatorList.stream().
                filter(generatorTier -> generatorTier.isDefaultGenerator() ||
                    this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId())).
                collect(Collectors.toList());
            default -> this.generatorList;
        };
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon VisitAddon object
     * @param user User who opens panel
     */
    public static void openPanel(StoneGeneratorAddon addon,
        World world,
        User user)
    {
        new GeneratorUserPanel(addon, world, user).build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds variable that allows to switch between button creation.
     */
    private enum Filter
    {
        /**
         * Button that on click shows only cobblestone generators.
         */
        COBBLESTONE,
        /**
         * Button that on click shows only stone generators.
         */
        STONE,
        /**
         * Button that on click shows only basalt generators.
         */
        BASALT,
        /**
         * Button that toggles between visibility modes.
         */
        VISIBILITY,
        /**
         * Button that on click shows only active generators.
         */
        ACTIVE,
        /**
         * Filter for none.
         */
        NONE
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds targeted island.
     */
    @Nullable
    private final Island island;

    /**
     * This variable holds user's island generator data.
     */
    private final GeneratorDataObject generatorData;

    /**
     * This variable stores all generator tiers in the given world.
     */
    private final List<GeneratorTierObject> generatorList;

    /**
     * This variable stores all generator tiers in the given world.
     */
    private List<GeneratorTierObject> elementList;

    /**
     * Stores currently active filter button.
     */
    private Filter activeFilterButton;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int generatorIndex;
}
