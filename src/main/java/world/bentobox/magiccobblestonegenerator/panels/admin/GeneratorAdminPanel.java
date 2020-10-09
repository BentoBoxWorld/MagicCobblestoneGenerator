//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class creates and manages Admin Panel for MCG.
 */
public class GeneratorAdminPanel extends CommonPanel
{
	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 *
	 * @param addon StoneGeneratorAddon instance.
	 * @param user User who opens panel.
	 * @param world GUI target world.
	 */
	protected GeneratorAdminPanel(StoneGeneratorAddon addon,
		User user,
		World world)
	{
		super(addon, user, world);
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param addon VisitAddon object
	 * @param user User who opens panel
	 */
	public static void openPanel(StoneGeneratorAddon addon,
		World world,
		User user)
	{
		new GeneratorAdminPanel(addon, user, world).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method allows to build panel.
	 */
	@Override
	public void build()
	{
		// PanelBuilder is a BentoBox API that provides ability to easy create Panels.
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "admin-view"));

		GuiUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(10, this.createButton(Action.MANAGE_USERS));
		panelBuilder.item(28, this.createButton(Action.WIPE_USER_DATA));


		panelBuilder.item(12, this.createButton(Action.MANAGE_GENERATOR_TIERS));
		panelBuilder.item(21, this.createButton(Action.MANAGE_GENERATOR_BUNDLES));

		panelBuilder.item(14, this.createButton(Action.IMPORT_TEMPLATE));

		panelBuilder.item(15, this.createButton(Action.WEB_LIBRARY));
		panelBuilder.item(24, this.createButton(Action.EXPORT_FROM_DATABASE));
		panelBuilder.item(33, this.createButton(Action.IMPORT_TO_DATABASE));

		panelBuilder.item(16, this.createButton(Action.SETTINGS));
		panelBuilder.item(34, this.createButton(Action.WIPE_GENERATOR_DATA));

		panelBuilder.item(44, this.createButton(Action.RETURN));
		panelBuilder.build();
	}


	/**
	 * Create button panel item with a given button type.
	 *
	 * @param button the button
	 * @return the panel item
	 */
	private PanelItem createButton(Action button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		String description = this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description");
		
		Material material;
		PanelItem.ClickHandler clickHandler;
		boolean glow = false;
		
		switch (button)
		{
			case MANAGE_USERS:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
//					UserManagePanel.open(this);
					return true;
				};
				material = Material.PLAYER_HEAD;
				break;
			}
			case MANAGE_GENERATOR_TIERS:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					GeneratorManagePanel.open(this);
					return true;
				};
				material = Material.COBBLESTONE;
				break;
			}
			case MANAGE_GENERATOR_BUNDLES:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
//					BundleManagePanel.open(this);
					return true;
				};
				material = Material.CHEST;
				break;
			}
			case SETTINGS:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					SettingsPanel.open(this);
					return true;
				};
				material = Material.CRAFTING_TABLE;
				break;
			}
			case IMPORT_TEMPLATE:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					LibraryPanel.open(this, LibraryPanel.Library.TEMPLATE);
					return true;
				};
				material = Material.BOOKSHELF;
				break;
			}
			case WEB_LIBRARY:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					LibraryPanel.open(this, LibraryPanel.Library.WEB);
					return true;
				};
				material = Material.COBWEB;
				break;
			}
			case EXPORT_FROM_DATABASE:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					// This consumer process file exporting after user input is returned.
					Consumer<String> fileNameConsumer = value -> {
						if (value != null)
						{
							this.addon.getImportManager().generateDatabaseFile(this.user,
								this.world,
								Utils.sanitizeInput(value));
						}

						this.build();
					};

					// This function checks if file can be created.
					Function<String, Boolean> validationFunction = fileName ->
					{
						String sanitizedName =  Utils.sanitizeInput(fileName);
						return !new File(this.addon.getDataFolder(),
							sanitizedName.endsWith(".json") ? sanitizedName : sanitizedName + ".json").exists();
					};

					// Call a conversation API to get input string.
					ConversationUtils.createIDStringInput(fileNameConsumer,
						validationFunction,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "exported-file-name"),
						this.user.getTranslation(Constants.MESSAGE + "database-export-completed",
							Constants.WORLD, world.getName()),
						Constants.ERRORS + "file-name-exist");

					return true;
				};
				material = Material.HOPPER;
				break;
			}
			case IMPORT_TO_DATABASE:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					LibraryPanel.open(this, LibraryPanel.Library.DATABASE);
					return true;
				};
				material = Material.BOOKSHELF;
				glow = true;
				break;
			}
			case WIPE_USER_DATA:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					// Create consumer that accepts value from conversation.
					Consumer<Boolean> consumer = value -> {
						if (value)
						{
							this.addon.getAddonManager().
								wipeIslandData(this.addon.getPlugin().getIWM().getAddon(this.world));
						}

						this.build();
					};
					// Create conversation that gets user acceptance to delete island data.
					ConversationUtils.createConfirmation(
						consumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "confirm-island-data-deletion",
							Constants.GAMEMODE, Utils.getGameMode(this.world)),
						this.user.getTranslation(Constants.MESSAGE + "user-data-removed",
							Constants.GAMEMODE, Utils.getGameMode(this.world)));

					return true;
				};
				
				material = Material.TNT;				
				break;
			}
			case WIPE_GENERATOR_DATA:
			{
				clickHandler = (panel, user1, clickType, slot) -> {
					// Create consumer that accepts value from conversation.
					Consumer<Boolean> consumer = value -> {
						if (value)
						{
							this.addon.getAddonManager().
								wipeGameModeGenerators(this.addon.getPlugin().getIWM().getAddon(this.world));
						}

						this.build();
					};
					// Create conversation that gets user acceptance to delete generator data.
					ConversationUtils.createConfirmation(
						consumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "confirm-generator-data-deletion",
							Constants.GAMEMODE, Utils.getGameMode(this.world)),
						this.user.getTranslation(Constants.MESSAGE + "generator-data-removed",
							Constants.GAMEMODE, Utils.getGameMode(this.world)));

					return true;
				};
				
				material = Material.TNT;
				break;
			}
			case RETURN:
			{
				clickHandler = (panel, user, clickType, i) -> {
					user.closeInventory();
					return true;
				};

				material = Material.OAK_DOOR;

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
			glow(glow).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This enum holds variable that allows to switch between button creation.
	 */
	private enum Action
	{
		MANAGE_USERS,
		MANAGE_GENERATOR_TIERS,
		MANAGE_GENERATOR_BUNDLES,
		
		SETTINGS,
		IMPORT_TEMPLATE,
		
		WEB_LIBRARY,
		EXPORT_FROM_DATABASE,
		IMPORT_TO_DATABASE,
		
		WIPE_USER_DATA,
		WIPE_GENERATOR_DATA,
		
		/**
		 * Return button that exists GUI.
		 */
		RETURN
	}
}
