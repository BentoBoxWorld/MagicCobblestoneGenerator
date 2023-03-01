package world.bentobox.magiccobblestonegenerator.panels.utils;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class contains all necessary things that allows to select single block from all ingame blocks. Selected
 * block will be returned via BiConsumer.
 */
public class SingleBlockSelector extends PagedSelector<Material>
{
	private SingleBlockSelector(User user, Mode mode, Set<Material> excluded, BiConsumer<Boolean, Material> consumer)
	{
		super(user);
		this.consumer = consumer;

		// Current GUI cannot display air blocks. It crashes with null-pointer
		excluded.add(Material.AIR);
		excluded.add(Material.CAVE_AIR);
		excluded.add(Material.VOID_AIR);

		// Piston head and moving piston is not necessary. useless.
		excluded.add(Material.PISTON_HEAD);
		excluded.add(Material.MOVING_PISTON);

		// Barrier cannot be accessible to user.
		excluded.add(Material.BARRIER);
		excluded.add(Material.STRUCTURE_VOID);

		this.elements = Arrays.stream(Material.values()).
			filter(material -> !excluded.contains(material)).
			filter(material -> {
				switch (mode)
				{
					case BLOCKS -> {
						return material.isBlock();
					}
					case ITEMS -> {
						return material.isItem();
					}
					default -> {
						return true;
					}
				}
			}).
			// Sort by name
			sorted(Comparator.comparing(Material::name)).
			collect(Collectors.toList());
		// Init without filters applied.
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, Mode mode, Set<Material> excluded, BiConsumer<Boolean, Material> consumer)
	{
		new SingleBlockSelector(user, mode, excluded, consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, BiConsumer<Boolean, Material> consumer)
	{
		new SingleBlockSelector(user, Mode.ANY, new HashSet<>(), consumer).build();
	}


	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, Mode mode, BiConsumer<Boolean, Material> consumer)
	{
		new SingleBlockSelector(user, mode, new HashSet<>(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "select-block"));

		PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(4, this.createButton());

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
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
					return element.name().toLowerCase().contains(this.searchString.toLowerCase());
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton()
	{
		final String reference = Constants.BUTTON + "return.";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon = new ItemStack(Material.IRON_DOOR);
		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) ->
		{
			this.consumer.accept(false, null);
			return true;
		};

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates button for given material.
	 * @param material material which button must be created.
	 * @return new Button for material.
	 */
	@Override
	protected PanelItem createElementButton(Material material)
	{
		final String reference = Constants.BUTTON + "material-icon.";

		List<String> description = new ArrayList<>();

		String descriptionText = Utils.prettifyDescription(material, this.user);

		if (!descriptionText.isBlank())
		{
			description.add(descriptionText);
		}

		description.add(this.user.getTranslation(reference + "description",
			"[id]", material.name()));
		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-choose"));

		return new PanelItemBuilder().
			name(this.user.getTranslation(reference + "name", "[material]",
				Utils.prettifyObject(material, this.user))).
			icon(PanelUtils.getMaterialItem(material)).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(true, material);
				return true;
			}).
			build();
	}


// ---------------------------------------------------------------------
// Section: Mode
// ---------------------------------------------------------------------


	public enum Mode
	{
		BLOCKS,
		ITEMS,
		ANY
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<Material> elements;

	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, Material> consumer;

	/**
	 * Stores filtered items.
	 */
	private List<Material> filterElements;
}
