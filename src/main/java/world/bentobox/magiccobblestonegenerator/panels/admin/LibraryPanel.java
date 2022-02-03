///
// Created by BONNe
// Copyright - 2022
///

package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.panels.CommonPagedPanel;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;
import world.bentobox.magiccobblestonegenerator.web.object.LibraryEntry;


/**
 * This class contains all necessary elements to create GUI that lists all challenges. It allows to edit them or remove,
 * depending on given input mode.
 */
public class LibraryPanel extends CommonPagedPanel<LibraryEntry>
{
    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------


    /**
     * Instantiates a new Library panel.
     *
     * @param parentGUI ParentGUI object.
     * @param mode the mode
     */
    private LibraryPanel(CommonPanel parentGUI, Library mode)
    {
        super(parentGUI);

        this.mode = mode;

        this.libraryEntries = switch (mode)
            {
                case WEB -> this.addon.getWebManager().getLibraryEntries();
                case DATABASE -> this.generateDatabaseEntries();
                case TEMPLATE -> this.generateTemplateEntries();
            };

        this.filterElements = this.libraryEntries;
    }


    /**
     * This method generates list of database file entries.
     *
     * @return List of entries for database files.
     */
    private List<LibraryEntry> generateDatabaseEntries()
    {
        File localeDir = this.addon.getDataFolder();
        File[] files = localeDir.listFiles(pathname ->
            pathname.getName().endsWith(".json") && pathname.isFile());

        if (files == null || files.length == 0)
        {
            // No
            return Collections.emptyList();
        }

        return Arrays.stream(files).
            map(file -> LibraryEntry.fromTemplate(
                file.getName().substring(0, file.getName().length() - 5),
                Material.PAPER)).
            collect(Collectors.toList());
    }


// ---------------------------------------------------------------------
// Section: Data Collectors
// ---------------------------------------------------------------------


    /**
     * This method generates list of template file entries.
     *
     * @return List of entries for template files.
     */
    private List<LibraryEntry> generateTemplateEntries()
    {
        File localeDir = this.addon.getDataFolder();
        File[] files = localeDir.listFiles(pathname ->
            pathname.getName().endsWith(".yml") &&
                pathname.isFile() &&
                !pathname.getName().equals("config.yml"));

        if (files == null || files.length == 0)
        {
            // No
            return Collections.emptyList();
        }

        return Arrays.stream(files).
            map(file -> LibraryEntry.fromTemplate(
                file.getName().substring(0, file.getName().length() - 4),
                Material.PAPER)).
            collect(Collectors.toList());
    }


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.libraryEntries;
        }
        else
        {
            this.filterElements = this.libraryEntries.stream().
                filter(element ->
                {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.name().toLowerCase().contains(this.searchString.toLowerCase()) ||
                        element.author().toLowerCase().contains(this.searchString.toLowerCase()) ||
                        element.gameMode().toLowerCase().contains(this.searchString.toLowerCase()) ||
                        element.language().toLowerCase().contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    protected void build()
    {
        if (this.libraryEntries.isEmpty())
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-library-entries"));
            return;
        }

        // No point to display. Single element.
        if (this.libraryEntries.size() == 1 && !this.mode.equals(Library.WEB))
        {
            this.generateConfirmationInput(this.libraryEntries.get(0));
            return;
        }

        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "library"));

        PanelUtils.fillBorder(panelBuilder);

        this.populateElements(panelBuilder, this.filterElements);

        if (this.mode == Library.WEB)
        {
            panelBuilder.item(4, this.createDownloadNow());
        }

        panelBuilder.item(44, this.returnButton);

        panelBuilder.listener(new DownloadCanceller());

        panelBuilder.build();
    }


    /**
     * This creates download now button, that can skip waiting for automatic request.
     *
     * @return PanelItem button that allows to manually download libraries.
     */
    private PanelItem createDownloadNow()
    {
        final String reference = Constants.BUTTON + "download.";

        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add(this.user.getTranslation(reference +
            (this.clearCache ? "enabled" : "disabled")));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-download"));
        description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-toggle"));

        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name")).
            description(description).
            icon(Material.COBWEB).
            glow(this.clearCache);

        itemBuilder.clickHandler((panel, user1, clickType, slot) ->
        {
            if (clickType.isRightClick())
            {
                this.clearCache = !this.clearCache;
                panel.getInventory().setItem(slot, this.createDownloadNow().getItem());
            }
            else
            {
                this.addon.getWebManager().requestCatalogGitHubData(this.clearCache);

                // Fix multiclick issue.
                if (this.updateTask != null)
                {
                    this.updateTask.cancel();
                }

                // add some delay to rebuilding gui.
                this.updateTask = this.addon.getPlugin().getServer().getScheduler().runTaskLater(
                    this.addon.getPlugin(),
                    this::build,
                    100L);
            }

            return true;
        });

        return itemBuilder.build();
    }


    /**
     * This method creates button for given library entry.
     *
     * @param libraryEntry LibraryEntry which button must be created.
     * @return Entry button.
     */
    @Override
    protected PanelItem createElementButton(LibraryEntry libraryEntry)
    {
        PanelItemBuilder itemBuilder = new PanelItemBuilder().
            name(ChatColor.translateAlternateColorCodes('&', libraryEntry.name())).
            description(this.generateEntryDescription(libraryEntry)).
            description("").
            description(this.user.getTranslation(Constants.TIPS + "click-to-install")).
            icon(libraryEntry.icon()).
            glow(false);

        itemBuilder.clickHandler((panel, user1, clickType, i) ->
        {
            this.generateConfirmationInput(libraryEntry);
            return true;
        });

        return itemBuilder.build();
    }


    /**
     * This method generates consumer and calls ConversationAPI for confirmation that processes file downloading,
     * importing and gui opening or closing.
     *
     * @param libraryEntry Entry that must be processed.
     */
    private void generateConfirmationInput(LibraryEntry libraryEntry)
    {
        Consumer<Boolean> consumer = value ->
        {
            if (value)
            {
                switch (this.mode)
                {
                    case TEMPLATE -> {
                        this.addon.getImportManager().importFile(this.user,
                            this.world,
                            libraryEntry.name());

                        if (this.parentPanel != null)
                        {
                            this.parentPanel.reopen();
                        }
                    }
                    case DATABASE -> {
                        this.addon.getImportManager().importDatabaseFile(this.user,
                            this.world,
                            libraryEntry.name());

                        if (this.parentPanel != null)
                        {
                            this.parentPanel.reopen();
                        }
                    }
                    case WEB -> {
                        if (!this.blockedForDownland)
                        {
                            this.blockedForDownland = true;

                            Utils.sendMessage(this.user, this.user.getTranslation(
                                Constants.MESSAGES + "start-downloading"));

                            // Run download task after 5 ticks.
                            this.updateTask = this.addon.getPlugin().getServer().getScheduler().
                                runTaskLaterAsynchronously(
                                    this.addon.getPlugin(),
                                    () -> this.addon.getWebManager().requestEntryGitHubData(this.user,
                                        this.world,
                                        libraryEntry),
                                    5L);
                        }

                        if (this.parentPanel != null)
                        {
                            this.parentPanel.reopen();
                        }
                    }
                }
            }

            if (this.mode.equals(Library.WEB) || this.libraryEntries.size() > 1)
            {
                this.build();
            }
        };

        ConversationUtils.createConfirmation(
            consumer,
            this.user,
            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-data-replacement",
                Constants.GAMEMODE, Utils.getGameMode(this.world)),
            this.user.getTranslation(Constants.CONVERSATIONS + "new-data-imported",
                Constants.GAMEMODE, Utils.getGameMode(this.world)));
    }


    /**
     * This method generated description for LibraryEntry object.
     *
     * @param entry LibraryEntry object which description must be generated.
     * @return List of strings that will be placed in ItemStack lore message.
     */
    private List<String> generateEntryDescription(LibraryEntry entry)
    {
        final String reference = Constants.DESCRIPTIONS + "library.";

        List<String> description = new ArrayList<>();

        description.add(this.user.getTranslation(reference + "author",
            "[author]", entry.author()));
        description.add(entry.description());

        description.add(this.user.getTranslation(reference + "gamemode",
            "[gamemode]", entry.gameMode()));
        description.add(this.user.getTranslation(reference + "lang",
            "[lang]", entry.language()));
        description.add(this.user.getTranslation(reference + "version",
            "[version]", entry.version()));

        return description;
    }


    /**
     * This class allows changing icon for Generator Tier
     */
    private class DownloadCanceller implements PanelListener
    {
        /**
         * On inventory click.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // do nothing
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            if (LibraryPanel.this.updateTask != null)
            {
                LibraryPanel.this.updateTask.cancel();
            }
        }


        /**
         * Setup current listener.
         */
        @Override
        public void setup()
        {
            // Do nothing
        }
    }


    /**
     * This static method allows to easier open Library GUI.
     *
     * @param parentGui ParentGUI object.
     * @param mode Library view mode.
     */
    public static void open(CommonPanel parentGui, Library mode)
    {
        new LibraryPanel(parentGui, mode).build();
    }


    /**
     * Enum that holds different view modes for current panel.
     */
    public enum Library
    {
        /**
         * Mode for templates available in main folder.
         */
        TEMPLATE,
        /**
         * Mode for database files available in main folder.
         */
        DATABASE,
        /**
         * Mode for web library.
         */
        WEB
    }


// ---------------------------------------------------------------------
// Section: Instance Variables
// ---------------------------------------------------------------------

    /**
     * Stores active library that must be searched.
     */
    private final Library mode;

    /**
     * List of library elements.
     */
    private final List<LibraryEntry> libraryEntries;

    /**
     * Indicates if download now button should trigger cache clearing.
     */
    private boolean clearCache;

    /**
     * Stores update task that is triggered.
     */
    private BukkitTask updateTask = null;

    /**
     * This variable will protect against spam-click.
     */
    private boolean blockedForDownland;

    /**
     * Stores filtered items.
     */
    private List<LibraryEntry> filterElements;
}
