package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableSet;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.utils.BundleSelector;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class IslandEditPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     * @param island Island that must be displayed.
     */
    private IslandEditPanel(CommonPanel panel,
        Island island)
    {
        super(panel);

        this.island = island;
        this.generatorData = this.addon.getAddonManager().getGeneratorData(island);

        this.title = this.island.getName();

        if (this.title == null || this.title.equals(""))
        {
            // Deal with situations when island name is not set.

            if (island.getOwner() != null)
            {
                this.title = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PLAYER, this.addon.getPlayers().getName(island.getOwner()));
            }
            else
            {
                this.title = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PLAYER, this.user.getTranslation(Constants.DESCRIPTIONS + "unknown"));
            }
        }

        // Store generators in local list to avoid building it every time.
        this.generatorList = this.manager.getIslandGeneratorTiers(world, this.generatorData);

        this.activeTab = Tab.ISLAND_INFO;
        this.activeFilterButton = Filter.NONE;
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        // Do not enable this GUI if there is an issue with getting data.
        if (this.generatorData == null || this.island == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-island-data"));
            return;
        }

        // PanelBuilder is a BentoBox API that provides ability to easily create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "view-island",
                Constants.ISLAND, this.title));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);
        this.populateHeader(panelBuilder);

        switch (this.activeTab) {
            case ISLAND_INFO -> this.populateInfo(panelBuilder);
            case ISLAND_GENERATORS -> this.fillGeneratorTiers(panelBuilder);
        }

        panelBuilder.item(44, this.createButton(Action.RETURN));

        // Build panel.
        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method populates header with buttons for switching between tabs.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateHeader(PanelBuilder panelBuilder)
    {
        panelBuilder.item(1, this.createButton(Tab.ISLAND_INFO));
        panelBuilder.item(2, this.createButton(Tab.ISLAND_GENERATORS));

        if (this.activeTab == Tab.ISLAND_GENERATORS)
        {
            panelBuilder.item(2, this.createButton(Filter.SHOW_ACTIVE));

            boolean hasCobblestoneGenerators = this.generatorList.stream().anyMatch(generator ->
                generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE));
            boolean hasStoneGenerators = this.generatorList.stream().anyMatch(generator ->
                generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE));
            boolean hasBasaltGenerators = this.generatorList.stream().anyMatch(generator ->
                generator.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT));

            // Do not show cobblestone button if there are no cobblestone generators.
            if (hasCobblestoneGenerators && (hasStoneGenerators || hasBasaltGenerators))
            {
                panelBuilder.item(4, this.createButton(Filter.SHOW_COBBLESTONE));
            }

            // Do not show stone if there are no stone generators.
            if (hasStoneGenerators && (hasCobblestoneGenerators || hasBasaltGenerators))
            {
                panelBuilder.item(5, this.createButton(Filter.SHOW_STONE));
            }

            // Do not show basalt if there are no basalt generators.
            if (hasBasaltGenerators && (hasStoneGenerators || hasCobblestoneGenerators))
            {
                panelBuilder.item(6, this.createButton(Filter.SHOW_BASALT));
            }

            panelBuilder.item(8, this.createButton(Filter.TOGGLE_VISIBILITY));
        }
        else
        {
            panelBuilder.item(7, this.createButton(Button.RESET_TO_DEFAULT));
        }
    }


    /**
     * This method populates panel body with info blocks.
     *
     * @param panelBuilder PanelBuilder that must be created.
     */
    private void populateInfo(PanelBuilder panelBuilder)
    {
        panelBuilder.item(10, this.createButton(Button.ISLAND_NAME));

        // Island things can be changed and updated.
        panelBuilder.item(13, this.createButton(Button.ISLAND_MAX_GENERATORS));
        panelBuilder.item(14, this.createButton(Button.ISLAND_WORKING_RANGE));
        panelBuilder.item(15, this.createButton(Button.ISLAND_BUNDLE));

        // Owner things are defined by permission. Show in view mode.
        panelBuilder.item(31, this.createButton(Button.OWNER_MAX_GENERATORS));
        panelBuilder.item(32, this.createButton(Button.OWNER_WORKING_RANGE));
        panelBuilder.item(33, this.createButton(Button.OWNER_BUNDLE));
    }


    /**
     * This method fills panel builder empty spaces with generator tiers and adds previous next buttons if necessary.
     *
     * @param panelBuilder PanelBuilder that is necessary to populate.
     */
    private void fillGeneratorTiers(PanelBuilder panelBuilder)
    {
        int MAX_ELEMENTS = 21;

        final int correctPage;

        List<GeneratorTierObject> filteredList = switch (this.activeFilterButton) {
            case SHOW_COBBLESTONE -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.COBBLESTONE)).
                    collect(Collectors.toList());
            case SHOW_STONE -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.STONE)).
                    collect(Collectors.toList());
            case SHOW_BASALT -> this.generatorList.stream().
                    filter(generatorTier ->
                            generatorTier.getGeneratorType().includes(GeneratorTierObject.GeneratorType.BASALT)).
                    collect(Collectors.toList());
            case TOGGLE_VISIBILITY -> this.generatorList.stream().
                    filter(generatorTier ->
                            this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId())).
                    collect(Collectors.toList());
            case SHOW_ACTIVE -> this.generatorList.stream().
                    filter(generatorTier ->
                            this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId())).
                    collect(Collectors.toList());
            default -> this.generatorList;
        };

        this.maxPageIndex = (int) Math.ceil(1.0 * filteredList.size() / MAX_ELEMENTS) - 1;

        if (this.pageIndex < 0)
        {
            correctPage = filteredList.size() / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (filteredList.size() / MAX_ELEMENTS))
        {
            correctPage = 0;
        }
        else
        {
            correctPage = this.pageIndex;
        }

        if (filteredList.size() > MAX_ELEMENTS)
        {
            // Navigation buttons if necessary

            panelBuilder.item(18, this.createButton(Action.PREVIOUS));
            panelBuilder.item(26, this.createButton(Action.NEXT));
        }

        int generatorIndex = MAX_ELEMENTS * correctPage;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (generatorIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
            generatorIndex < filteredList.size() &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index,
                    this.createGeneratorButton(filteredList.get(generatorIndex++)));
            }

            index++;
        }
    }


    /**
     * This method creates button for generator tier.
     *
     * @param generatorTier GeneratorTier which button must be created.
     * @return PanelItem for generator tier.
     */
    private PanelItem createGeneratorButton(GeneratorTierObject generatorTier)
    {
        // Default generator should be active.
        boolean isActive = generatorTier.isDefaultGenerator() ||
            this.generatorData.getActiveGeneratorList().contains(generatorTier.getUniqueId());
        // Default generator should be always unlocked.
        boolean isUnlocked = generatorTier.isDefaultGenerator() ||
            this.generatorData.getUnlockedTiers().contains(generatorTier.getUniqueId());
        // Default generators cannot be purchased.
        boolean isPurchased = generatorTier.isDefaultGenerator() ||
            !this.addon.isVaultProvided() ||
            generatorTier.getGeneratorTierCost() == 0 ||
            this.generatorData.getPurchasedTiers().contains(generatorTier.getUniqueId());

        List<String> description = this.generateGeneratorDescription(generatorTier,
            isActive,
            isUnlocked,
            isPurchased,
            this.manager.getIslandLevel(this.island));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {

            if (clickType.isRightClick())
            {
                // Open view panel.
                if (isUnlocked)
                {
                    // Default generators are not lockable.
                    if (!generatorTier.isDefaultGenerator())
                    {
                        // Direct access to data.
                        this.generatorData.getUnlockedTiers().remove(generatorTier.getUniqueId());
                    }
                }
                else
                {
                    // Call a proper method.
                    this.addon.getAddonManager().unlockGenerator(this.generatorData,
                        this.user,
                        this.island,
                        generatorTier);
                }

                this.build();
            }
            else if (clickType.isLeftClick())
            {
                if (isUnlocked)
                {
                    if (!isPurchased)
                    {
                        this.addon.getAddonManager().purchaseGenerator(this.user,
                            this.generatorData,
                            generatorTier);
                    }
                    else if (isActive)
                    {
                        this.addon.getAddonManager().deactivateGenerator(this.user,
                            this.generatorData,
                            generatorTier);
                    }
                    else
                    {
                        this.addon.getAddonManager().activateGenerator(this.user,
                            this.island,
                            this.generatorData,
                            generatorTier);
                    }
                }

                this.build();
            }

            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(generatorTier.getFriendlyName()).
            description(description).
            icon(isUnlocked ? generatorTier.getGeneratorIcon() : generatorTier.getLockedIcon()).
            clickHandler(clickHandler).
            glow(isActive).
            build();
    }


    /**
     * This class generates given generator tier description based on input parameters.
     *
     * @param generator GeneratorTier which description must be generated.
     * @param isActive Boolean that indicates if generator is active.
     * @param isUnlocked Boolean that indicates if generator is unlocked.
     * @param isPurchased Boolean that indicates if generator is purchased.
     * @param islandLevel Long that shows island level.
     * @return List of strings that describes generator tier.
     */
    @Override
    protected List<String> generateGeneratorDescription(GeneratorTierObject generator,
        boolean isActive,
        boolean isUnlocked,
        boolean isPurchased,
        long islandLevel)
    {
        List<String> description =
            super.generateGeneratorDescription(generator, isActive, isUnlocked, isPurchased, islandLevel);

        description.add("");

        if (isUnlocked)
        {
            if (!isPurchased)
            {
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-purchase"));
            }
            else if (isActive)
            {
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-deactivate"));
            }
            else
            {
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-activate"));
            }
        }

        if (!generator.isDefaultGenerator())
        {
            if (isUnlocked)
            {
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-lock"));
            }
            else
            {
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-unlock"));
            }
        }

        return description;
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Button button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        ItemStack itemStack = new ItemStack(Material.AIR);

        switch (button) {
            case ISLAND_NAME -> {
                // Create owner name translated string.
                String ownerName = this.addon.getPlayers().getName(this.island.getOwner());

                if (ownerName.equals("")) {
                    ownerName = this.user.getTranslation(Constants.DESCRIPTIONS + "unknown");
                }

                ownerName = this.user.getTranslation(reference + ".owner", Constants.PLAYER, ownerName);

                // Create island members translated string.

                StringBuilder builder = new StringBuilder();

                ImmutableSet<UUID> members = this.island.getMemberSet();
                if (members.size() > 1) {
                    builder.append(this.user.getTranslation(reference + ".list"));

                    for (UUID uuid : members) {
                        if (uuid != this.island.getOwner()) {
                            builder.append("\n").append(this.user.getTranslation(reference + ".value",
                                    Constants.PLAYER, this.addon.getPlayers().getName(uuid)));
                        }
                    }
                }

                // Get descriptionLine that contains [members]
                description.add(this.user.getTranslation(reference + ".description",
                        Constants.OWNER, ownerName,
                        Constants.MEMBERS, builder.toString(),
                        Constants.ID, this.island.getUniqueId()));

                itemStack = new ItemStack(Material.NAME_TAG);

                // Transform name into button title.
                name = this.user.getTranslation(reference + ".name",
                        Constants.NAME, this.title);
            }
            case ISLAND_WORKING_RANGE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorData.getIslandWorkingRange())));

                if (this.generatorData.getOwnerWorkingRange() != 0) {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                    description.add(this.user.getTranslation(reference + ".overwritten"));
                } else {
                    itemStack = new ItemStack(Material.REPEATER);
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null) {
                            this.generatorData.setIslandWorkingRange(number.intValue());
                            this.manager.saveGeneratorData(this.generatorData);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                            -1,
                            2000);

                    return true;
                };
            }
            case OWNER_WORKING_RANGE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                        Constants.GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

                if (this.generatorData.getOwnerWorkingRange() != 0) {
                    itemStack = new ItemStack(Material.REPEATER);

                    description.add(this.user.getTranslationOrNothing(reference + ".value",
                            Constants.NUMBER, String.valueOf(this.generatorData.getOwnerWorkingRange())));
                } else {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                }
            }
            case ISLAND_MAX_GENERATORS -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add(this.user.getTranslation(reference + ".value",
                        Constants.NUMBER, String.valueOf(this.generatorData.getIslandActiveGeneratorCount())));

                if (this.generatorData.getOwnerActiveGeneratorCount() != 0) {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                    description.add(this.user.getTranslation(reference + ".overwritten"));
                } else {
                    itemStack = new ItemStack(Material.REPEATER);
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null) {
                            this.generatorData.setIslandActiveGeneratorCount(number.intValue());
                            this.manager.saveGeneratorData(this.generatorData);
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                            -1,
                            2000);

                    return true;
                };
            }
            case OWNER_MAX_GENERATORS -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                        Constants.GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

                if (this.generatorData.getOwnerActiveGeneratorCount() != 0) {
                    itemStack = new ItemStack(Material.COBBLESTONE,
                            Math.max(1, this.generatorData.getOwnerActiveGeneratorCount()));

                    description.add(this.user.getTranslationOrNothing(reference + ".value",
                            Constants.NUMBER, String.valueOf(this.generatorData.getOwnerActiveGeneratorCount())));
                } else {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                }
            }
            case ISLAND_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                GeneratorBundleObject ownerBundle = this.generatorData.getOwnerBundle() != null ?
                        this.addon.getAddonManager().getBundleById(this.generatorData.getOwnerBundle()) : null;

                final GeneratorBundleObject islandBundle = this.generatorData.getIslandBundle() != null ?
                        this.addon.getAddonManager().getBundleById(this.generatorData.getIslandBundle()) : null;

                if (ownerBundle != null) {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                    description.add(this.user.getTranslation(reference + ".overwritten"));
                } else {
                    if (islandBundle != null) {
                        itemStack = islandBundle.getGeneratorIcon();

                        description.add(this.user.getTranslation(reference + ".value",
                                Constants.BUNDLE, islandBundle.getFriendlyName()));
                    } else {
                        itemStack = new ItemStack(Material.NAME_TAG);
                    }
                }

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                clickHandler = (panel, user, clickType, slot) ->
                {
                    BundleSelector.open(this, islandBundle, bundle -> {
                        if (bundle == null || bundle == GeneratorBundleObject.dummyBundle) {
                            this.generatorData.setIslandBundle(null);
                        } else {
                            this.generatorData.setIslandBundle(bundle.getUniqueId());
                        }

                        this.manager.saveGeneratorData(this.generatorData);

                        // Recreate list based on new bundle.
                        this.generatorList =
                                this.manager.getIslandGeneratorTiers(world, this.generatorData);

                        this.build();
                    });

                    return true;
                };
            }
            case OWNER_BUNDLE -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description",
                        Constants.GAMEMODE, Utils.getGameMode(this.world).toLowerCase()));

                GeneratorBundleObject bundle = this.generatorData.getOwnerBundle() != null ?
                        this.addon.getAddonManager().getBundleById(this.generatorData.getOwnerBundle()) : null;

                if (bundle != null) {
                    itemStack = bundle.getGeneratorIcon();

                    description.add(this.user.getTranslation(reference + ".value",
                            Constants.BUNDLE, bundle.getFriendlyName()));
                } else {
                    itemStack = new ItemStack(Material.STRUCTURE_VOID);
                }
            }
            case RESET_TO_DEFAULT -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                // add empty line
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-perform"));

                clickHandler = (panel, user, clickType, i) -> {
                    Settings settings = this.addon.getSettings();

                    this.generatorData.setIslandBundle(null);
                    this.generatorData.setIslandWorkingRange(settings.getDefaultWorkingRange());
                    this.generatorData.setIslandActiveGeneratorCount(settings.getDefaultActiveGeneratorCount());

                    this.manager.saveGeneratorData(this.generatorData);

                    this.build();
                    return true;
                };

                itemStack = new ItemStack(Material.OBSERVER);
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(itemStack).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Tab button)
    {
        String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description
            .add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.activeTab = button;
            this.pageIndex = 0;

            this.build();
            return true;
        };

        Material material = switch (button) {
            case ISLAND_INFO -> Material.WRITTEN_BOOK;
            case ISLAND_GENERATORS -> Material.COBBLESTONE;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeTab == button).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Filter button)
    {
        String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description
            .add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

        description.add("");
        if (this.activeFilterButton != button)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-enable"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "click-to-filter-disable"));
        }

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            this.activeFilterButton = this.activeFilterButton == button ? Filter.NONE : button;
            // Rebuild everything.
            this.build();

            // Always return true.
            return true;
        };

        Material material = Material.PAPER;

        switch (button) {
            case SHOW_COBBLESTONE -> material = Material.COBBLESTONE;
            case SHOW_STONE -> material = Material.STONE;
            case SHOW_BASALT -> material = Material.BASALT;
            case TOGGLE_VISIBILITY -> material = Material.REDSTONE;
            case SHOW_ACTIVE -> material = Material.GREEN_STAINED_GLASS_PANE;
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
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

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> true;

        Material icon = Material.PAPER;
        int count = 1;

        switch (button) {
            case RETURN -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-return"));

                clickHandler = (panel, user, clickType, i) -> {
                    if (this.parentPanel != null) {
                        this.parentPanel.reopen();
                    } else {
                        user.closeInventory();
                    }
                    return true;
                };

                icon = Material.OAK_DOOR;
            }
            case PREVIOUS -> {
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
            }
            case NEXT -> {
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
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(count).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     * @param island Island that must be displayed.
     */
    public static void open(CommonPanel panel,
        Island island)
    {
        new IslandEditPanel(panel, island).build();
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
        SHOW_COBBLESTONE,
        /**
         * Button that on click shows only stone generators.
         */
        SHOW_STONE,
        /**
         * Button that on click shows only basalt generators.
         */
        SHOW_BASALT,
        /**
         * Button that toggles between visibility modes.
         */
        TOGGLE_VISIBILITY,
        /**
         * Button that on click shows only active generators.
         */
        SHOW_ACTIVE,
        /**
         * Default value for filter.
         */
        NONE
    }


    /**
     * Enum that holds different actions that can be performed in current gui.
     */
    private enum Action
    {
        /**
         * Return button that exists GUI.
         */
        RETURN,
        /**
         * Allows selecting previous generators in multi-page situation.
         */
        PREVIOUS,
        /**
         * Allows to select next generators in multi-page situation.
         */
        NEXT
    }


    /**
     * This enum holds possible tabs for current gui.
     */
    private enum Tab
    {
        /**
         * General Information Tab
         */
        ISLAND_INFO,
        /**
         * Generators Tab.
         */
        ISLAND_GENERATORS
    }


    /**
     * This enum holds possible actions that this gui allows.
     */
    private enum Button
    {
        /**
         * Holds name of the island that is edited.
         */
        ISLAND_NAME,
        /**
         * Displays working range for island.
         */
        ISLAND_WORKING_RANGE,
        /**
         * Displays working range for owner.
         */
        OWNER_WORKING_RANGE,
        /**
         * Displays max number of active generators for island.
         */
        ISLAND_MAX_GENERATORS,
        /**
         * Displays max number of active generators for owner.
         */
        OWNER_MAX_GENERATORS,
        /**
         * Displays island active bundle.
         */
        ISLAND_BUNDLE,
        /**
         * Displays owner active bundle.
         */
        OWNER_BUNDLE,
        /**
         * Action that allows to reset to default values.
         */
        RESET_TO_DEFAULT
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable store island that is viewed.
     */
    private final Island island;

    /**
     * This variable store generator data for this island.
     */
    private final GeneratorDataObject generatorData;

    /**
     * Stores island title.
     */
    private String title;

    /**
     * This variable stores all generator tiers in the given world.
     */
    private List<GeneratorTierObject> generatorList;

    /**
     * Stores currently active filter button.
     */
    private Filter activeFilterButton;

    /**
     * This variable holds current pageIndex for multi-page generator choosing.
     */
    private int pageIndex;

    /**
     * This variable holds max page index.
     */
    private int maxPageIndex;

    /**
     * This variable stores which tab currently is active.
     */
    private Tab activeTab;
}
