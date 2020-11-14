//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;



import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;


/**
 * This class manages settings for Magic Cobblestone Generator Addon.
 */
public class SettingsPanel extends CommonPanel
{
	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 *
	 * @param parentPanel Parent panel of current panel.
	 */
	protected SettingsPanel(CommonPanel parentPanel)
	{
		super(parentPanel);
		this.settings = this.addon.getSettings();
	}


	/**
	 * This method build settings panel from parent panel.
	 * @param panel ParentPanel.
	 */
	public static void open(CommonPanel panel)
	{
		new SettingsPanel(panel).build();
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
			name(this.user.getTranslation(Constants.TITLE + "edit-settings"));

		GuiUtils.fillBorder(panelBuilder, 3, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(10, this.createButton(Action.OFFLINE_GENERATION));
		panelBuilder.item(12, this.createButton(Action.USE_PHYSIC));
		panelBuilder.item(14, this.createButton(Action.WORKING_RANGE));
		panelBuilder.item(16, this.createButton(Action.DEFAULT_ACTIVE_GENERATORS));

		panelBuilder.item(26, this.createButton(Action.RETURN));
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
		List<String> description = new ArrayList<>(2);
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

		Material material;
		PanelItem.ClickHandler clickHandler;
		boolean glow = false;
		int count = 1;

		switch (button)
		{
			case OFFLINE_GENERATION:
			{
				clickHandler = (panel, user, clickType, i) -> {
					this.settings.setOfflineGeneration(!this.settings.isOfflineGeneration());
					this.saveSettings();
					// Update button in panel
					this.build();

					return true;
				};

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.settings.isOfflineGeneration())));

				material = Material.REDSTONE_TORCH;
				glow = this.settings.isOfflineGeneration();

				break;
			}
			case USE_PHYSIC:
			{
				clickHandler = (panel, user, clickType, i) -> {
					this.settings.setUsePhysics(!this.settings.isUsePhysics());
					this.saveSettings();
					// Update button in panel
					this.build();

					return true;
				};

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.settings.isUsePhysics())));

				material = Material.PISTON;
				glow = this.settings.isUsePhysics();

				break;
			}
			case WORKING_RANGE:
			{
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setDefaultWorkingRange(number.intValue());
							this.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						0,
						2000);

					return true;
				};

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.settings.getDefaultWorkingRange())));

				material = Material.OAK_TRAPDOOR;
				count = this.settings.getDefaultWorkingRange();

				break;
			}
			case DEFAULT_ACTIVE_GENERATORS:
			{
				clickHandler = (panel, user, clickType, i) -> {
					Consumer<Number> numberConsumer = number -> {
						if (number != null)
						{
							this.settings.setDefaultActiveGeneratorCount(number.intValue());
							this.saveSettings();
						}

						// reopen panel
						this.build();
					};

					ConversationUtils.createNumericInput(numberConsumer,
						this.user,
						this.user.getTranslation(Constants.QUESTIONS + "input-number"),
						1,
						2000);

					return true;
				};

				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					Constants.VALUE, String.valueOf(this.settings.getDefaultActiveGeneratorCount())));

				material = Material.BOOK;
				count = this.settings.getDefaultActiveGeneratorCount();

				break;
			}
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
			default:
				return PanelItem.empty();
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(new ItemStack(material, count == 0 ? 1 : count)).
			clickHandler(clickHandler).
			glow(glow).
			build();
	}


	/**
	 * This method saves current settings.
	 */
	private void saveSettings()
	{
		new Config<>(this.addon, Settings.class).saveConfigObject(this.settings);
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This enum holds all possible actions in current GUI.
	 */
	private enum Action
	{
		/**
		 * Process Offline Generation Action.
		 */
		OFFLINE_GENERATION,
		/**
		 * Process Use Physic Action.
		 */
		USE_PHYSIC,
		/**
		 * Process Working Range Action.
		 */
		WORKING_RANGE,
		/**
		 * Process Default Active Generators Action.
		 */
		DEFAULT_ACTIVE_GENERATORS,
		/**
		 * Process Return Action.
		 */
		RETURN
	}


	/**
	 * Settings object to allow to change settings.
	 */
	private final Settings settings;
}
