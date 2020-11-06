package world.bentobox.magiccobblestonegenerator.panels.utils;


import org.bukkit.Material;
import java.util.*;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class contains all necessary things that allows to select single or multiple blocks from all
 * game blocks.
 */
public class SelectBlocksPanel
{
	/**
	 * Instantiates a new Select blocks panel.
	 *
	 * @param user the user
	 * @param singleSelect the single select
	 * @param blockSelect the block select
	 * @param excludedMaterial the excluded material
	 * @param consumer the consumer
	 */
	private SelectBlocksPanel(User user,
		boolean singleSelect,
		boolean blockSelect,
		Set<Material> excludedMaterial,
		Consumer<Set<Material>> consumer)
	{
		this.consumer = consumer;
		this.user = user;
		this.singleSelect = singleSelect;

		// Current GUI cannot display air blocks. It crashes with null-pointer
		excludedMaterial.add(Material.AIR);
		excludedMaterial.add(Material.CAVE_AIR);
		excludedMaterial.add(Material.VOID_AIR);

		// Piston head and moving piston is not necessary. useless.
		excludedMaterial.add(Material.PISTON_HEAD);
		excludedMaterial.add(Material.MOVING_PISTON);

		// Barrier cannot be accessible to user.
		excludedMaterial.add(Material.BARRIER);

		this.elements = new ArrayList<>();
		this.selectedMaterials = new HashSet<>();

		for (Material material : Material.values())
		{
			if ((!blockSelect || material.isBlock()) && !material.isLegacy() && !excludedMaterial.contains(material))
			{
				this.elements.add(material);
			}
		}

		this.pageIndex = 0;
	}


	/**
	 * Opens panel that with all blocks from choose.
	 * @param user the user
	 * @param singleSelect the single select
	 * @param consumer the consumer
	 */
	public static void open(User user, boolean singleSelect, Consumer<Set<Material>> consumer)
	{
		new SelectBlocksPanel(user, singleSelect, false, new HashSet<>(), consumer).build();
	}


	/**
	 * Opens panel that with all blocks from choose.
	 * @param user the user
	 * @param singleSelect the single select
	 * @param blockSelect the block select
	 * @param consumer the consumer
	 */
	public static void open(User user, boolean singleSelect, boolean blockSelect, Consumer<Set<Material>> consumer)
	{
		new SelectBlocksPanel(user, singleSelect, blockSelect, new HashSet<>(), consumer).build();
	}


	/**
	 * Opens panel that with all blocks from choose.
	 * @param user the user
	 * @param singleSelect the single select
	 * @param blockSelect the block select
	 * @param excludedMaterial the excluded material
	 * @param consumer the consumer
	 */
	public static void open(User user,
		boolean singleSelect,
		boolean blockSelect,
		Set<Material> excludedMaterial,
		Consumer<Set<Material>> consumer)
	{
		new SelectBlocksPanel(user, singleSelect, blockSelect, excludedMaterial, consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "select-block"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		final int MAX_ELEMENTS = 21;

		if (this.pageIndex < 0)
		{
			this.pageIndex = this.elements.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.elements.size() / MAX_ELEMENTS))
		{
			this.pageIndex = 0;
		}

		int materialIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (materialIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			materialIndex < this.elements.size())
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createMaterialButton(this.elements.get(materialIndex++)));
			}

			index++;
		}

		if (this.elements.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary


			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		panelBuilder.item(40, this.createButton(Action.ACCEPT));
		panelBuilder.item(44, this.createButton(Action.RETURN));

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem that represents given material.
	 * Some materials is not displayable in Inventory GUI, so they are replaced with "placeholder" items.
	 * @param material Material which icon must be created.
	 * @return PanelItem that represents given material.
	 */
	private PanelItem createMaterialButton(Material material)
	{
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.MATERIALS + material.name() + ".description"));

		// Add empty line
		if (!description.get(0).isEmpty())
		{
			description.add("");
		}

		if (this.selectedMaterials.contains(material))
		{
			description.add(this.user.getTranslationOrNothing(Constants.DESCRIPTION + "click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslationOrNothing(Constants.DESCRIPTION + "click-to-select"));
		}

		return new PanelItemBuilder().
			name(Utils.prettifyObject(this.user, material)).
			description(description).
			icon(GuiUtils.getMaterialItem(material)).
			clickHandler((panel, user1, clickType, slot) -> {
				if (this.singleSelect)
				{
					this.consumer.accept(Collections.singleton(material));
				}
				else if (!this.selectedMaterials.remove(material))
				{
					this.selectedMaterials.add(material);
				}

				// update icons
				panel.getInventory().setItem(slot, this.createMaterialButton(material).getItem());
				panel.getInventory().setItem(40, this.createButton(Action.ACCEPT).getItem());
				return true;
			}).
			glow(this.selectedMaterials.contains(material)).
			build();
	}


	/**
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Action button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));

		PanelItem.ClickHandler clickHandler;
		Material icon;

		switch (button)
		{
			case RETURN:
			{
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-cancel"));

				clickHandler = (panel, user, clickType, i) -> {
					// Return NULL.
					this.consumer.accept(null);
					return true;
				};

				icon = Material.OAK_DOOR;

				break;
			}
			case PREVIOUS:
			{
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-previous"));

				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				icon = Material.ARROW;
				break;
			}
			case NEXT:
			{
				// add empty line
				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-next"));

				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex++;
					this.build();
					return true;
				};

				icon = Material.ARROW;
				break;
			}
			case ACCEPT:
			{
				if (!this.selectedMaterials.isEmpty())
				{
					description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-values"));

					for (Material material : this.selectedMaterials)
					{
						description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value-list",
							Constants.VALUE, Utils.prettifyObject(this.user, material)));
					}
				}

				description.add("");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-accept"));

				clickHandler = (panel, user, clickType, i) -> {
					// Return selected biomes.
					this.consumer.accept(this.selectedMaterials);
					return true;
				};

				icon = Material.FILLED_MAP;

				break;
			}
			default:
				return PanelItem.empty();
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(icon).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Stores all available actions for Panel.
	 */
	private enum Action
	{
		PREVIOUS,
		NEXT,
		RETURN,
		ACCEPT
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<Material> elements;

	/**
	 * Set that contains selected materials.
	 */
	private final Set<Material> selectedMaterials;

	/**
	 * This variable stores consumer.
	 */
	private final Consumer<Set<Material>> consumer;

	/**
	 * User who runs GUI.
	 */
	private final User user;

	/**
	 * This indicate that return set must contain only single item.
	 */
	private final boolean singleSelect;

	/**
	 * Page index.
	 */
	private int pageIndex;
}
