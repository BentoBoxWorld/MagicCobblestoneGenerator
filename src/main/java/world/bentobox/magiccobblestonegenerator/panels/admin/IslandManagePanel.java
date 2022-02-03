//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.panels.CommonPagedPanel;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This class manages User islands that are stored in database with some custom data. It also allows to add a custom
 * data to island.
 */
public class IslandManagePanel extends CommonPagedPanel<Island>
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected IslandManagePanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        this.activeTab = Tab.IS_ONLINE;

        // Store bundles in local list to avoid building it every time.
        this.elementList = this.addon.getIslands().getIslands(this.world).stream().
            filter(Island::isOwned).
            sorted((o1, o2) ->
            {
                User u1 = User.getInstance(Objects.requireNonNull(o1.getOwner()));
                User u2 = User.getInstance(Objects.requireNonNull(o2.getOwner()));

                if (!u1.isPlayer())
                {
                    return -1;
                }
                else if (!u2.isPlayer())
                {
                    return 1;
                }
                else
                {
                    return u1.getName().compareTo(u2.getName());
                }
            }).
            distinct().
            collect(Collectors.toList());

        this.updateFilters();
    }


    /**
     * This method allows to build panel.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-islands"));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(3, this.createButton(Tab.IS_ONLINE));
        panelBuilder.item(5, this.createButton(Tab.ALL_ISLANDS));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);
        panelBuilder.build();
    }


    /**
     * Updates filter element list based on filter values and search string.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = new ArrayList<>(this.elementList);
        }
        else
        {
            this.filterElements = this.elementList.stream().
                filter(island ->
                {
                    // If island name is set and name contains search field, then do not filter out.
                    if (island.getName() != null &&
                        !island.getName().isEmpty() &&
                        island.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                    {
                        return true;
                    }

                    // If island member names do not contains search field, then filter it out.
                    for (UUID uuid : island.getMemberSet())
                    {
                        User member = User.getInstance(uuid);

                        if (member.isPlayer() &&
                            member.getName().toLowerCase().contains(this.searchString.toLowerCase()))
                        {
                            return true;
                        }
                    }

                    // Island do not contains filter field.
                    return false;
                }).
                distinct().
                collect(Collectors.toList());
        }

        if (this.activeTab == Tab.IS_ONLINE)
        {
            Set<UUID> onlinePlayerSet = Bukkit.getOnlinePlayers().stream().
                map(Player::getUniqueId).collect(Collectors.toSet());

            // Remove all islands from filter list where none of members are online.
            this.filterElements.removeIf(island ->
                island.getMemberSet().stream().noneMatch(onlinePlayerSet::contains));
        }
    }


    /**
     * This method creates button for given island.
     *
     * @param island Island which button must be created.
     * @return PanelItem button for given island.
     */
    @Override
    protected PanelItem createElementButton(Island island)
    {
        // Generate island name.
        String name = island.getName();

        // If name is not set, then use owner island translation.
        if (name == null || name.equals(""))
        {
            // Deal with situations when island name is not set.

            if (island.getOwner() != null)
            {
                name = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PLAYER, this.addon.getPlayers().getName(island.getOwner()));
            }
            else
            {
                name = this.user.getTranslation(Constants.DESCRIPTIONS + "island-owner",
                    Constants.PLAYER, this.user.getTranslation(Constants.DESCRIPTIONS + "unknown"));
            }
        }

        // Transform name into button title.
        name = this.user.getTranslation(Constants.BUTTON + "island_name.name",
            Constants.NAME, name);

        // Create owner name translated string.
        String ownerName = this.addon.getPlayers().getName(island.getOwner());

        if (ownerName.equals(""))
        {
            ownerName = this.user.getTranslation(Constants.DESCRIPTIONS + "unknown");
        }

        ownerName = this.user.getTranslation(Constants.BUTTON + "island_name.owner",
            Constants.PLAYER, ownerName);

        // Create island members translated string.

        StringBuilder builder = new StringBuilder();

        ImmutableSet<UUID> members = island.getMemberSet();
        if (members.size() > 1)
        {
            builder.append(this.user.getTranslation(Constants.BUTTON + "island_name.list"));

            for (UUID uuid : members)
            {
                if (uuid != island.getOwner())
                {
                    builder.append("\n").append(this.user.getTranslation(Constants.BUTTON + "island_name.value",
                        Constants.PLAYER, this.addon.getPlayers().getName(uuid)));
                }
            }
        }

        // Create description list
        List<String> description = new ArrayList<>();

        description.add(this.user.getTranslation(Constants.BUTTON + "island_name.description",
            Constants.OWNER, ownerName,
            Constants.MEMBERS, builder.toString(),
            Constants.ID, island.getUniqueId()));

        // Add tip
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            IslandEditPanel.open(this, island);
            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(this.addon.getPlayers().getName(island.getOwner())).
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
            this.updateFilters();
            this.build();
            return true;
        };

        Material material = switch (button) {
            case IS_ONLINE -> Material.WRITTEN_BOOK;
            case ALL_ISLANDS -> Material.CHEST;
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
     * This method build island panel from parent panel.
     *
     * @param panel ParentPanel.
     */
    public static void open(CommonPanel panel)
    {
        new IslandManagePanel(panel).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Tab
    {
        /**
         * Shows islands with online users.
         */
        IS_ONLINE,
        /**
         * Shows islands with data.
         */
        ALL_ISLANDS
    }

    /**
     * This list contains currently displayed island list.
     */
    private final List<Island> elementList;

    /**
     * This list contains currently displayed island list.
     */
    private List<Island> filterElements;

    /**
     * Allows to switch between active tabs.
     */
    private Tab activeTab;
}
