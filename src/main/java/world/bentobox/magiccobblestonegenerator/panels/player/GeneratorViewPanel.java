package world.bentobox.magiccobblestonegenerator.panels.player;


import com.google.common.base.Enums;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
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
 * This class opens GUI that shows generator view for user.
 */
public class GeneratorViewPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon VisitAddon object
     * @param world World where user is operating
     * @param user User who opens panel
     * @param generatorTier generator tier that must be viewed.
     */
    private GeneratorViewPanel(StoneGeneratorAddon addon,
        World world,
        User user,
        GeneratorTierObject generatorTier)
    {
        super(addon, user, world);
        this.island = this.addon.getIslands().getIsland(world, user);

        // Get valid user island data
        this.generatorData = this.manager.validateIslandData(this.island);
        this.generatorTier = generatorTier;

        // By default no-filters are active.
        this.activeTab = Tab.INFO;
    }


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     * @param generatorTier Generator that must be displayed.
     */
    private GeneratorViewPanel(CommonPanel panel,
        GeneratorTierObject generatorTier)
    {
        super(panel);
        this.island = this.addon.getIslands().getIsland(this.world, this.user);

        // Get valid user island data
        this.generatorData = this.manager.validateIslandData(this.island);
        this.generatorTier = generatorTier;

        // By default no-filters are active.
        this.activeTab = Tab.INFO;
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        if (this.generatorTier == null)
        {
            Utils.sendMessage(this.user,
                this.user.getTranslation(Constants.ERRORS + "no-generator-data"));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());
        panelBuilder.parameters(Constants.GENERATOR, this.generatorTier.getFriendlyName());

        // Set main template.
        switch (this.activeTab)
        {
            case INFO -> {
                panelBuilder.template("info_tab", "view_panel", new File(this.addon.getDataFolder(), "panels"));

                panelBuilder.registerTypeBuilder("GENERATOR", this::createGeneratorButton);
                panelBuilder.registerTypeBuilder("INFO", this::createInfoButton);
                panelBuilder.registerTypeBuilder("HEIGHT_RANGE", this::createHeightRangeButton);
            }
            case BLOCKS -> {
                panelBuilder.template("block_tab", "view_panel", new File(this.addon.getDataFolder(), "panels"));

                panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
                panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);
                panelBuilder.registerTypeBuilder("BLOCK", this::createMaterialButton);

                this.materialChanceList =
                    this.generatorTier.getBlockChanceMap().entrySet().stream().
                        sorted(Map.Entry.comparingByKey()).
                        collect(Collectors.toList());
            }
            case TREASURES -> {
                panelBuilder.template("treasure_tab", "view_panel", new File(this.addon.getDataFolder(), "panels"));

                panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
                panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);
                panelBuilder.registerTypeBuilder("TREASURE", this::createTreasureButton);

                this.treasureChanceList =
                    this.generatorTier.getTreasureItemChanceMap().entrySet().stream().
                        sorted(Map.Entry.comparingByKey()).
                        collect(Collectors.toList());
            }
        }

        // Register tabs
        panelBuilder.registerTypeBuilder("TAB", this::createTabButton);

        // Register return
        panelBuilder.registerTypeBuilder("RETURN", this::createReturnButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Tab Button Type
// ---------------------------------------------------------------------


    /**
     * Create tab button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createTabButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
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

        Tab tab = Enums.getIfPresent(Tab.class, String.valueOf(template.dataMap().get("tab"))).or(Tab.INFO);

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
            "VIEW".equalsIgnoreCase(action.actionType()) && this.activeTab == tab);

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            for (ItemTemplateRecord.ActionRecords action : activeActions)
            {
                if (clickType == action.clickType() || ClickType.UNKNOWN.equals(action.clickType()))
                {
                    if ("VIEW".equalsIgnoreCase(action.actionType()))
                    {
                        this.activeTab = tab;

                        // Reset page.
                        this.pageIndex = 0;
                        this.build();
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

        builder.glow(this.activeTab == tab);

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Create common buttons
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
        long size;

        if (this.activeTab == Tab.BLOCKS)
        {
            size = this.materialChanceList.size();

            if (size <= slot.amountMap().getOrDefault("BLOCK", 1) ||
                1.0 * size / slot.amountMap().getOrDefault("BLOCK", 1) <= this.pageIndex + 1)
            {
                // There are no next elements
                return null;
            }
        }
        else if (this.activeTab == Tab.TREASURES)
        {
            size = this.treasureChanceList.size();

            if (size <= slot.amountMap().getOrDefault("TREASURE", 1) ||
                1.0 * size / slot.amountMap().getOrDefault("TREASURE", 1) <= this.pageIndex + 1)
            {
                // There are no next elements
                return null;
            }
        }
        else
        {
            // Only works in BLOCKS and TREASURES tabs.
            return null;
        }

        int nextPageIndex = this.pageIndex + 2;

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
                        this.pageIndex++;
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
        if (this.pageIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.pageIndex;

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
                        this.pageIndex--;
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
// Section: Create Material Button
// ---------------------------------------------------------------------


    /**
     * Create material button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createMaterialButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.materialChanceList.isEmpty())
        {
            // Does not contain any generators.
            return null;
        }

        int index = this.pageIndex * slot.amountMap().getOrDefault("BLOCK", 1) + slot.slot();

        if (index >= this.materialChanceList.size())
        {
            // Out of index.
            return null;
        }

        Double previousValue = index > 0 ? this.materialChanceList.get(index - 1).getKey() : 0.0;
        Double maxValue = this.generatorTier.getBlockChanceMap().lastKey();

        return this.createMaterialButton(template, this.materialChanceList.get(index), previousValue, maxValue);
    }


    /**
     * This method creates button for material.
     *
     * @param template the template of the button
     * @param blockChanceEntry blockChanceEntry which button must be created.
     * @param previousValue Previous chance value for correct display.
     * @param maxValue Displays maximal value for map.
     * @return PanelItem for generator tier.
     */
    private PanelItem createMaterialButton(ItemTemplateRecord template,
        Map.Entry<Double, Material> blockChanceEntry,
        Double previousValue,
        Double maxValue)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(blockChanceEntry.getValue());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.BLOCK, Utils.prettifyObject(this.user, blockChanceEntry.getValue())));
        }

        if (template.description() != null)
        {
            Double value = (blockChanceEntry.getKey() - previousValue) / maxValue * 100.0;

            builder.description(this.user.getTranslation(this.world, template.description(),
                TextVariables.NUMBER, String.valueOf(value),
                Constants.TENS, this.tensFormat.format(value),
                Constants.HUNDREDS, this.hundredsFormat.format(value),
                Constants.THOUSANDS, this.thousandsFormat.format(value),
                Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
                Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));
        }

        // Add height range information if available
        int minHeight = this.generatorTier.getMinHeight();
        int maxHeight = this.generatorTier.getMaxHeight();
        
        // Check if material has specific height range
        int[] heightRange = this.generatorTier.getMaterialHeightRange(blockChanceEntry.getValue());
        
        if (heightRange != null)
        {
            minHeight = heightRange[0];
            maxHeight = heightRange[1];
        }
        
        builder.description("");
        builder.description(this.user.getTranslation(Constants.BUTTON + "block-icon.height-range",
            Constants.MIN_HEIGHT, String.valueOf(minHeight),
            Constants.MAX_HEIGHT, String.valueOf(maxHeight)));

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Create Treasure Button
// ---------------------------------------------------------------------


    /**
     * Create treasure button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createTreasureButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.treasureChanceList.isEmpty())
        {
            // Does not contain any generators.
            return null;
        }

        int index = this.pageIndex * slot.amountMap().getOrDefault("TREASURE", 1) + slot.slot();

        if (index >= this.treasureChanceList.size())
        {
            // Out of index.
            return null;
        }

        Double previousValue = index > 0 ? this.treasureChanceList.get(index - 1).getKey() : 0.0;
        Double maxValue = this.generatorTier.getTreasureItemChanceMap().lastKey();

        return this.createTreasureButton(template, this.treasureChanceList.get(index), previousValue, maxValue);
    }


    /**
     * This method creates button for treasure.
     *
     * @param template the template
     * @param treasureChanceEntry treasureChanceEntry which button must be created.
     * @param previousValue Previous chance value for correct display.
     * @param maxValue Displays maximal value for map.
     * @return PanelItem for generator tier.
     */
    private PanelItem createTreasureButton(ItemTemplateRecord template,
        Map.Entry<Double, ItemStack> treasureChanceEntry,
        Double previousValue,
        Double maxValue)
    {
        ItemStack treasure = treasureChanceEntry.getValue().clone();
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(PanelUtils.getMaterialItem(treasure.getType()));
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.BLOCK, Utils.prettifyObject(this.user, treasure)));
        }

        if (treasure.hasItemMeta() && treasure.getItemMeta() != null)
        {
            ItemMeta itemMeta = treasure.getItemMeta();

            if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty())
            {
                builder.description(itemMeta.getLore());
                // Add empty line after lore.
                builder.description("");
            }
        }

        if (template.description() != null)
        {
            Double value = (treasureChanceEntry.getKey() - previousValue) / maxValue * 100.0  * this.generatorTier.getTreasureChance();

            builder.description(this.user.getTranslation(this.world, template.description(),
                TextVariables.NUMBER, String.valueOf(value),
                Constants.TENS, this.tensFormat.format(value),
                Constants.HUNDREDS, this.hundredsFormat.format(value),
                Constants.THOUSANDS, this.thousandsFormat.format(value),
                Constants.TEN_THOUSANDS, this.tenThousandsFormat.format(value),
                Constants.HUNDRED_THOUSANDS, this.hundredThousandsFormat.format(value)));
        }

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Create Generator Button
// ---------------------------------------------------------------------


    /**
     * This method creates button for generator tier.
     *
     * @param template the template
     * @param slot the slot
     * @return PanelItem for generator tier.
     */
    private PanelItem createGeneratorButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        // Default generator should be active.
        boolean isActive = this.generatorTier.isDefaultGenerator() ||
            this.generatorData.getActiveGeneratorList().contains(this.generatorTier.getUniqueId());
        // Default generator should be always isUnlocked.
        boolean isUnlocked = this.generatorTier.isDefaultGenerator() ||
            this.generatorData.getUnlockedTiers().contains(this.generatorTier.getUniqueId());
        // Default generators cannot be isPurchased.
        boolean isPurchased = this.generatorTier.isDefaultGenerator() ||
            !this.addon.isVaultProvided() ||
            this.generatorTier.getGeneratorTierCost() == 0 ||
            this.generatorData.getPurchasedTiers().contains(this.generatorTier.getUniqueId());

        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        if (template.icon() != null)
        {
            builder.icon(template.icon());
        }
        else if (isUnlocked)
        {
            builder.icon(this.generatorTier.getGeneratorIcon());
        }
        else
        {
            builder.icon(this.generatorTier.getLockedIcon());
        }

        builder.icon(template.icon() != null ? template.icon().clone() : this.generatorTier.getGeneratorIcon());

        // Template specific title is always more important than generatorTier name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.NAME, this.generatorTier.getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(this.generatorTier.getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateGeneratorDescription(this.generatorTier,
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
                        this.generatorTier.isDefaultGenerator();
                }
                case "ACTIVATE" -> {
                    return this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        this.generatorTier.isDefaultGenerator() ||
                        isActive;
                }
                case "DEACTIVATE" -> {
                    return this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        this.generatorTier.isDefaultGenerator() ||
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
                            GeneratorViewPanel.openPanel(this, this.generatorTier);
                        }
                        case "BUY" -> {
                            if (this.island != null && this.manager.canPurchaseGenerator(user, this.island, this.generatorData, this.generatorTier))
                            {
                                this.manager.purchaseGenerator(this.user, this.island, this.generatorData, this.generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "ACTIVATE" -> {
                            if (this.island != null && this.manager.canActivateGenerator(user, this.island, this.generatorData, this.generatorTier))
                            {
                                this.manager.activateGenerator(user, this.island, this.generatorData, this.generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "DEACTIVATE" -> {
                            this.manager.deactivateGenerator(user, this.generatorData, this.generatorTier);
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
// Section: Create Info Button
// ---------------------------------------------------------------------


    /**
     * Create info button panel item.
     *
     * @param template the template record
     * @param slot the item slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createInfoButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        // Default generator should be active.
        boolean isActive = this.generatorTier.isDefaultGenerator() ||
            this.generatorData.getActiveGeneratorList().contains(this.generatorTier.getUniqueId());
        // Default generator should be always isUnlocked.
        boolean isUnlocked = this.generatorTier.isDefaultGenerator() ||
            this.generatorData.getUnlockedTiers().contains(this.generatorTier.getUniqueId());
        // Default generators cannot be isPurchased.
        boolean isPurchased = this.generatorTier.isDefaultGenerator() ||
            !this.addon.isVaultProvided() ||
            this.generatorTier.getGeneratorTierCost() == 0 ||
            this.generatorData.getPurchasedTiers().contains(this.generatorTier.getUniqueId());

        Button button = Enums.getIfPresent(Button.class, String.valueOf(template.dataMap().get("display"))).or(Button.NONE);

        switch (button)
        {
            case NONE -> {
                return null;
            }
            case REQUIRED_MIN_LEVEL -> {
                if (this.generatorTier.isDefaultGenerator() ||
                    !this.addon.isLevelProvided() ||
                    this.generatorTier.getRequiredMinIslandLevel() <= 0)
                {
                    // Default generator cannot set min level.
                    // If vault is not provided, then purchase cannot be performed.
                    // If min level is set to 0 or smaller, then icon is not necessary.
                    return null;
                }
            }
            case REQUIRED_PERMISSIONS -> {
                if (this.generatorTier.isDefaultGenerator() ||
                    this.generatorTier.getRequiredPermissions().isEmpty())
                {
                    // Default generator cannot require permission
                    // If permissions are not set, then ignore button.
                    return null;
                }
            }
            case PURCHASE_COST -> {
                if (this.generatorTier.isDefaultGenerator() ||
                    !this.addon.isVaultProvided() ||
                    this.generatorTier.getGeneratorTierCost() <= 0)
                {
                    // Default generator cannot be purchased. It is purchased by default.
                    // If vault is not provided, then purchase cannot be performed.
                    // If generator tier cost is set to 0, then purchase is not necessary.
                    return null;
                }
            }
            case ACTIVATION_COST -> {
                if (!this.addon.isVaultProvided() ||
                    this.generatorTier.getActivationCost() <= 0)
                {
                    // If vault is not provided, then activation cannot be performed.
                    // If generator tier cost is set to 0, then activation is not necessary.
                    return null;
                }
            }
            case BIOMES -> {
                if (this.generatorTier.getRequiredBiomes().isEmpty())
                {
                    // If no biomes are set, then do not display the button.
                    return null;
                }
            }
            case TREASURE_AMOUNT, TREASURE_CHANCE -> {
                if (this.generatorTier.getTreasureItemChanceMap().isEmpty())
                {
                    // If treasure map is empty, then do not display icon.
                    return null;
                }
            }
            case MIN_HEIGHT -> {
                if (this.generatorTier.getMinHeight() <= 0)
                {
                    // If min height is set to 0 or smaller, then icon is not necessary.
                    return null;
                }
            }
            case MAX_HEIGHT -> {
                if (this.generatorTier.getMaxHeight() <= 0)
                {
                    // If max height is set to 0 or smaller, then icon is not necessary.
                    return null;
                }
            }
        }

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            // Set icon
            builder.icon(template.icon().clone());
        }
        else
        {
            switch (button)
            {
                case DEFAULT -> {
                    if (this.generatorTier.isDefaultGenerator())
                    {
                        builder.icon(Material.GREEN_BANNER);
                    }
                    else
                    {
                        builder.icon(Material.RED_BANNER);
                    }
                }
                case PRIORITY -> builder.icon(Material.HOPPER);
                case TYPE -> builder.icon(Utils.getGeneratorTypeMaterial(this.generatorTier.getGeneratorType()));
                case REQUIRED_MIN_LEVEL -> builder.icon(Material.DIAMOND);
                case REQUIRED_PERMISSIONS -> builder.icon(Material.BOOK);
                case PURCHASE_COST -> {
                    if (this.generatorData.getPurchasedTiers().contains(this.generatorTier.getUniqueId()))
                    {
                        builder.icon(Material.MAP);
                    }
                    else
                    {
                        builder.icon(Material.GOLD_BLOCK);
                    }
                }
                case ACTIVATION_COST -> builder.icon(Material.GOLD_INGOT);
                case BIOMES -> builder.icon(Material.FILLED_MAP);
                case TREASURE_AMOUNT -> {
                    builder.icon(Material.EMERALD);
                    builder.amount(this.generatorTier.getMaxTreasureAmount());
                }
                case TREASURE_CHANCE -> builder.icon(Material.PAPER);
                case MIN_HEIGHT -> builder.icon(Material.STONE);
                case MAX_HEIGHT -> builder.icon(Material.STONE);
            }
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
        else
        {
            final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

            builder.description(this.user.getTranslation(this.world, reference + "description"));

            switch (button)
            {
                case DEFAULT -> {
                    if (this.generatorTier.isDefaultGenerator())
                    {
                        builder.description(this.user.getTranslation(reference + ".enabled"));
                    }
                    else
                    {
                        builder.description(this.user.getTranslation(reference + ".disabled"));
                    }
                }
                case PRIORITY -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getPriority())));
                }
                case TYPE -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.TYPE, this.user.getTranslation(
                            Constants.GENERATOR_TYPE_BUTTON +
                                this.generatorTier.getGeneratorType().name().toLowerCase() + ".name")));
                }
                case REQUIRED_MIN_LEVEL -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getRequiredMinIslandLevel())));
                }
                case REQUIRED_PERMISSIONS -> {
                    builder.description(this.user.getTranslation(reference + ".list"));

                    this.generatorTier.getRequiredPermissions().stream().sorted().forEach(permission ->
                        builder.description(this.user.getTranslation(reference + ".value",
                            Constants.PERMISSION, permission)));
                }
                case PURCHASE_COST -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getGeneratorTierCost())));
                }
                case ACTIVATION_COST -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getActivationCost())));
                }
                case BIOMES -> {
                    builder.description(this.user.getTranslation(reference + ".list"));

                    this.generatorTier.getRequiredBiomes().stream().sorted().forEach(biome ->
                        builder.description(this.user.getTranslation(reference + ".value",
                            Constants.BIOME, Utils.prettifyObject(biome, this.user))));
                }
                case TREASURE_AMOUNT -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getMaxTreasureAmount())));
                }
                case TREASURE_CHANCE -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getTreasureChance())));
                }
                case MIN_HEIGHT -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getMinHeight())));
                }
                case MAX_HEIGHT -> {
                    builder.description(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorTier.getMaxHeight())));
                }
            }
        }

        // Get only possible actions, by removing all inactive ones.
        List<ItemTemplateRecord.ActionRecords> activeActions = new ArrayList<>(template.actions());

        activeActions.removeIf(action ->
        {
            switch (action.actionType().toUpperCase())
            {
                case "BUY" -> {
                    return Button.PURCHASE_COST != button ||
                        this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        isPurchased ||
                        this.generatorTier.isDefaultGenerator();
                }
                case "ACTIVATE" -> {
                    return Button.ACTIVATION_COST != button ||
                        this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        this.generatorTier.isDefaultGenerator() ||
                        isActive;
                }
                case "DEACTIVATE" -> {
                    return Button.ACTIVATION_COST != button ||
                        this.island == null ||
                        !this.island.isAllowed(this.user, StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_PERMISSION) ||
                        !isUnlocked ||
                        !isPurchased ||
                        this.generatorTier.isDefaultGenerator() ||
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
                            GeneratorViewPanel.openPanel(this, this.generatorTier);
                        }
                        case "BUY" -> {
                            if (this.island != null && this.manager.canPurchaseGenerator(user, this.island, this.generatorData, this.generatorTier))
                            {
                                this.manager.purchaseGenerator(this.user, this.island, this.generatorData, this.generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "ACTIVATE" -> {
                            if (this.island != null && this.manager.canActivateGenerator(user, this.island, this.generatorData, this.generatorTier))
                            {
                                this.manager.activateGenerator(user, this.island, this.generatorData, this.generatorTier);
                            }

                            // Build whole gui.
                            this.build();
                        }
                        case "DEACTIVATE" -> {
                            this.manager.deactivateGenerator(user, this.generatorData, this.generatorTier);
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

        return builder.build();
    }


    /**
     * Create height range button panel item.
     *
     * @param template the template
     * @param slot the slot
     * @return the panel item
     */
    @Nullable
    private PanelItem createHeightRangeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }
        else
        {
            builder.icon(Material.LADDER);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            String heightRangeName = this.user.getTranslation(Constants.BUTTON + "height_range.name");
            String heightRangeDescription = this.user.getTranslation(Constants.BUTTON + "height_range.description");
            
            builder.description(heightRangeDescription);
            builder.description("");
            builder.description(this.user.getTranslation(Constants.BUTTON + "height_range.value",
                Constants.MIN_HEIGHT, String.valueOf(this.generatorTier.getMinHeight()),
                Constants.MAX_HEIGHT, String.valueOf(this.generatorTier.getMaxHeight())));
            
        }

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Other Methods
// ---------------------------------------------------------------------


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param addon VisitAddon object
     * @param world World where user is operating
     * @param user User who opens panel
     * @param generatorTier generator tier that must be viewed.
     */
    public static void openPanel(StoneGeneratorAddon addon,
        World world,
        User user,
        GeneratorTierObject generatorTier)
    {
        new GeneratorViewPanel(addon, world, user, generatorTier).build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param generatorTier Generator that must be displayed.
     */
    public static void openPanel(CommonPanel panel,
        GeneratorTierObject generatorTier)
    {
        new GeneratorViewPanel(panel, generatorTier).build();
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This enum holds possible tabs for current gui.
     */
    private enum Tab
    {
        /**
         * General Information Tab
         */
        INFO,
        /**
         * Blocks Tab.
         */
        BLOCKS,
        /**
         * Treasure Tab.
         */
        TREASURES
    }


    /**
     * This enum holds possible actions that this gui allows.
     */
    private enum Button
    {
        /**
         * Indicates that no button is selected.
         */
        NONE,
        /**
         * Holds Name type that allows to interact with generator default status.
         */
        DEFAULT,
        /**
         * Holds Name type that allows to interact with generator priority.
         */
        PRIORITY,
        /**
         * Holds Name type that allows to interact with generator type.
         */
        TYPE,
        /**
         * Holds Name type that allows to interact with generator min island requirement.
         */
        REQUIRED_MIN_LEVEL,
        /**
         * Holds Name type that allows to interact with generator required permissions.
         */
        REQUIRED_PERMISSIONS,
        /**
         * Holds Name type that allows to interact with generator purchase cost.
         */
        PURCHASE_COST,
        /**
         * Holds Name type that allows to interact with generator activation cost.
         */
        ACTIVATION_COST,
        /**
         * Holds Name type that allows to interact with generator working biomes.
         */
        BIOMES,
        /**
         * Holds Name type that allows to interact with generator treasure amount.
         */
        TREASURE_AMOUNT,
        /**
         * Holds Name type that allows to interact with generator treasure chance.
         */
        TREASURE_CHANCE,
        /**
         * Holds Name type that displays the minimum height at which the generator can operate.
         */
        MIN_HEIGHT,
        /**
         * Holds Name type that displays the maximum height at which the generator can operate.
         */
        MAX_HEIGHT,
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This variable holds targeted island.
     */
    private final Island island;

    /**
     * This variable holds user's island generator data.
     */
    private final GeneratorDataObject generatorData;

    /**
     * This variable stores generator tier that is viewed.
     */
    private final GeneratorTierObject generatorTier;

    /**
     * This variable stores chance for every block to be spawned.
     */
    private List<Map.Entry<Double, Material>> materialChanceList;

    /**
     * This variable stores chance for every treasure to be spawned.
     */
    private List<Map.Entry<Double, ItemStack>> treasureChanceList;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;

    /**
     * This variable stores which tab currently is active.
     */
    private Tab activeTab;
}
