//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;
import world.bentobox.magiccobblestonegenerator.web.object.LibraryEntry;


/**
 * This panel is used to display different type of libraries.
 * 		WEB mode displays libraries from BentoBox.weblib repo.
 * 		DATABASE mode displays locally exported database files located in addon directory (.json)
 * 		TEMPLATE mode displays template files located in addon directory (.yml)
 * On clicking element, addon attempts to download (for web only) and install library.
 * User must enter confirm in chat to do it.
 */
public class LibraryPanel extends CommonPanel
{
	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 *
	 * @param parentPanel Parent panel of current panel.
	 */
	protected LibraryPanel(CommonPanel parentPanel, Library mode)
	{
		super(parentPanel);
		this.mode = mode;

		switch (mode)
		{
			case WEB:
				this.libraryEntries = this.addon.getWebManager().getLibraryEntries();
				break;
			case DATABASE:
				this.libraryEntries = this.generateDatabaseEntries();
				break;
			case TEMPLATE:
				this.libraryEntries = this.generateTemplateEntries();
				break;
			default:
				this.libraryEntries = Collections.emptyList();
		}

		// Stores how many elements will be in display.
		this.rowCount = this.libraryEntries.size() > 14 ? 3 : this.libraryEntries.size() > 7 ? 2 : 1;
	}



	/**
	 * This method is used to open LibraryPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param parentPanel CommonPanel parentPanel
	 * @param mode Library which should be opened.
	 */
	public static void open(CommonPanel parentPanel, Library mode)
	{
		new LibraryPanel(parentPanel, mode).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method generates list of database file entries.
	 * @return List of entries for database files.
	 */
	private List<LibraryEntry> generateDatabaseEntries()
	{
		File localeDir = this.addon.getDataFolder();
		File[] files = localeDir.listFiles(pathname ->
			pathname.getName().endsWith(".json") && pathname.isFile());

		if (files.length == 0)
		{
			// No
			return Collections.emptyList();
		}

		return Arrays.stream(files).
			map(file ->
			{
				LibraryEntry entry = new LibraryEntry();
				entry.setName(file.getName().substring(0, file.getName().length() - 5));
				entry.setIcon(Material.PAPER);
				return entry;
			}).
			collect(Collectors.toList());
	}


	/**
	 * This method generates list of template file entries.
	 * @return List of entries for template files.
	 */
	private List<LibraryEntry> generateTemplateEntries()
	{
		File localeDir = this.addon.getDataFolder();
		File[] files = localeDir.listFiles(pathname ->
			pathname.getName().endsWith(".yml") &&
				pathname.isFile() &&
				!pathname.getName().equals("config.yml"));

		if (files.length == 0)
		{
			// No
			return Collections.emptyList();
		}

		return Arrays.stream(files).
			map(file ->
			{
				LibraryEntry entry = new LibraryEntry();
				entry.setName(file.getName().substring(0, file.getName().length() - 5));
				entry.setIcon(Material.PAPER);
				return entry;
			}).
			collect(Collectors.toList());
	}


	/**
	 * This method allows to build panel.
	 */
	@Override
	public void build()
	{
		if (this.libraryEntries.isEmpty())
		{
			this.user.sendMessage(Constants.ERRORS + "no-library-entries");
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

		GuiUtils.fillBorder(panelBuilder, this.rowCount + 2, Material.MAGENTA_STAINED_GLASS_PANE);
		
		this.fillLibraryEntries(panelBuilder);

		panelBuilder.item((this.rowCount + 2) * 9 - 1, this.createButton(Action.RETURN));
		
		panelBuilder.build();
	}


	/**
	 * This method fills panel builder empty spaces with library entries and adds previous
	 * next buttons if necessary.
	 * @param panelBuilder PanelBuilder that is necessary to populate.
	 */
	private void fillLibraryEntries(PanelBuilder panelBuilder)
	{
		int MAX_ELEMENTS = this.rowCount * 7;

		final int correctPage;

		if (this.pageIndex < 0)
		{
			correctPage = this.libraryEntries.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.libraryEntries.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		if (this.libraryEntries.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(9, this.createButton(Action.PREVIOUS));
			panelBuilder.item(17, this.createButton(Action.NEXT));
		}

		int generatorIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (generatorIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			generatorIndex < this.libraryEntries.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createLibraryButton(this.libraryEntries.get(generatorIndex++)));
			}

			index++;
		}
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Action button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		String description = this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description");
		PanelItem.ClickHandler clickHandler;
		Material material;

		switch (button)
		{
			case RETURN:
			{
				clickHandler = (panel, user, clickType, i) -> {
					if (this.parentPanel != null)
					{
						this.parentPanel.build();
					}
					else
					{
						user.closeInventory();
					}

					return true;
				};

				material = Material.OAK_DOOR;

				break;
			}
			case PREVIOUS:
			{
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
				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				material = Material.ARROW;
				break;
			}
			default:
				return PanelItem.empty();
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(false).
			build();
	}


	/**
	 * This method creates button for library entry.
	 * @param libraryEntry LibraryEntry which button must be created.
	 * @return PanelItem for library entry.
	 */
	private PanelItem createLibraryButton(LibraryEntry libraryEntry)
	{
		PanelItemBuilder itemBuilder = new PanelItemBuilder().
			name(libraryEntry.getName()).
			description(this.generateEntryDescription(libraryEntry)).
			icon(libraryEntry.getIcon()).
			glow(false);

		itemBuilder.clickHandler((panel, user1, clickType, i) -> {
			this.generateConfirmationInput(libraryEntry);
			return true;
		});

		return itemBuilder.build();
	}


	/**
	 * This method generated description for LibraryEntry object.
	 * @param entry LibraryEntry object which description must be generated.
	 * @return List of strings that will be placed in ItemStack lore message.
	 */
	private List<String> generateEntryDescription(LibraryEntry entry)
	{
		List<String> description = new ArrayList<>();

		if (entry.getAuthor() != null)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "library-author",
				Constants.AUTHOR,
				entry.getAuthor()));
		}

		if (entry.getDescription() != null)
		{
			description.add(entry.getDescription());
		}

		if (entry.getForGameMode() != null)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "library-gamemode",
				Constants.GAMEMODE,
				entry.getForGameMode()));
		}

		if (entry.getLanguage() != null)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "library-lang",
				Constants.LANG,
				entry.getLanguage()));
		}

		if (entry.getVersion() != null)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "library-version",
				Constants.VERSION,
				entry.getVersion()));
		}

		return description;
	}


	/**
	 * This method generates consumer and calls ConversationAPI
	 * for confirmation that processes file downloading, importing
	 * and gui opening or closing.
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
					case TEMPLATE:
						this.addon.getImportManager().importFile(this.user,
							this.world,
							libraryEntry.getName());

						if (this.parentPanel != null)
						{
							this.parentPanel.build();
						}
						else
						{
							this.user.closeInventory();
						}

						break;
					case DATABASE:
						this.addon.getImportManager().importDatabaseFile(this.user,
							this.world,
							libraryEntry.getName());

						if (this.parentPanel != null)
						{
							this.parentPanel.build();
						}
						else
						{
							this.user.closeInventory();
						}

						break;
					case WEB:
						if (!this.blockedForDownland)
						{
							this.blockedForDownland = true;

							this.user.sendMessage(Constants.MESSAGE + "start-downloading");

							// Run download task after 5 ticks.
							this.addon.getPlugin().getServer().getScheduler().
								runTaskLaterAsynchronously(
									this.addon.getPlugin(),
									() -> this.addon.getWebManager()
										.requestEntryGitHubData(this.user, this.world, libraryEntry),
									5L);

							if (this.parentPanel != null)
							{
								if (this.updateTask != null)
								{
									this.updateTask.cancel();
								}

								this.parentPanel.build();
							}
							else
							{
								if (this.updateTask != null)
								{
									this.updateTask.cancel();
								}

								this.user.closeInventory();
							}
						}
						break;
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
			this.user.getTranslation(Constants.QUESTIONS + "confirm-data-replacement",
				Constants.GAMEMODE, Utils.getGameMode(this.world)),
			this.user.getTranslation(Constants.MESSAGE + "new-generators-imported",
				Constants.GAMEMODE, Utils.getGameMode(this.world)));
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


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
	
	
	/**
	 * This enum holds variable that allows to switch between button creation.
	 */
	private enum Action
	{
		/**
		 * Return button that exists GUI.
		 */
		RETURN,
		/**
		 * Allows to select previous library in multi-page situation.
		 */
		PREVIOUS,
		/**
		 * Allows to select next library in multi-page situation.
		 */
		NEXT
	}
	
	
	/**
	 * Stores active library that must be searched.
	 */
	private final Library mode;

	/**
	 * List of library elements.
	 */
	private final List<LibraryEntry> libraryEntries;

	/**
	 * Stores update task that is triggered.
	 */
	private BukkitTask updateTask = null;

	/**
	 * This variable will protect against spam-click.
	 */
	private boolean blockedForDownland;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * Stores how many elements will be in display.
	 */
	private final int rowCount;
}
