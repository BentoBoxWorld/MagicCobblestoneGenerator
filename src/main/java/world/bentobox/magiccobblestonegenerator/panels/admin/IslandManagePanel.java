//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.panels.admin;


import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;
import world.bentobox.magiccobblestonegenerator.panels.ConversationUtils;
import world.bentobox.magiccobblestonegenerator.panels.GuiUtils;
import world.bentobox.magiccobblestonegenerator.utils.Constants;

/**
 * This class manages User islands that are stored in database with some custom data.
 * It also allows to add a custom data to island.
 */
public class IslandManagePanel extends CommonPanel
{
	/**
	 * This is default constructor for all classes that extends CommonPanel.
	 *
	 * @param parentPanel Parent panel of current panel.
	 */
	protected IslandManagePanel(CommonPanel parentPanel)
	{
		super(parentPanel);
		this.elementList = null;
		this.activeTab = Tab.IS_ONLINE;
		this.searchString = "";
	}


	/**
	 * This method build island panel from parent panel.
	 * @param panel ParentPanel.
	 */
	public static void open(CommonPanel panel)
	{
		new IslandManagePanel(panel).build();
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

		GuiUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(3, this.createButton(Tab.IS_ONLINE));
		panelBuilder.item(5, this.createButton(Tab.ALL_ISLANDS));

		// Add search field.
		panelBuilder.item(40, this.createButton(Action.SEARCH));

		this.populateIslandList(panelBuilder);

		panelBuilder.item(44, this.createButton(Action.RETURN));
		panelBuilder.build();
	}


	/**
	 * This method populates panel with all
	 * @param panelBuilder panelBuilder that must be populated.
	 */
	private void populateIslandList(PanelBuilder panelBuilder)
	{
		int MAX_ELEMENTS = 21;

		switch (this.activeTab)
		{
			case IS_ONLINE:
				// Build list only if it does not exist. Yes it will not include members who just joined,
				// but you can always reload panel. :)
				if (this.elementList == null)
				{
					if (!this.searchString.isEmpty())
					{
						// collection to set is required to avoid duplicates.
						this.elementList = this.searchElements(Bukkit.getOnlinePlayers().stream().
							map(player -> this.addon.getIslands().getIsland(this.world, player.getUniqueId())).
							filter(Objects::nonNull).
							collect(Collectors.toSet()));
					}
					else
					{
						// collection to set is required to avoid duplicates.
						this.elementList = new ArrayList<>(Bukkit.getOnlinePlayers().stream().
							map(player -> this.addon.getIslands().getIsland(this.world, player.getUniqueId())).
							filter(Objects::nonNull).
							collect(Collectors.toSet()));
					}

					this.maxPageIndex = (int) Math.ceil(1.0 * this.elementList.size() / MAX_ELEMENTS) - 1;

					// Sort by name.
					this.elementList.sort((o1, o2) -> {
						User u1 = User.getInstance(o1.getOwner());
						User u2 = User.getInstance(o2.getOwner());

						if (u1 == null || !u1.isPlayer())
						{
							return -1;
						}
						else if (u2 == null || !u2.isPlayer())
						{
							return 1;
						}
						else
						{
							return u1.getName().compareTo(u2.getName());
						}
					});
				}

				break;
			case ALL_ISLANDS:
				if (this.elementList == null)
				{
					if (!this.searchString.isEmpty())
					{
						this.elementList = this.searchElements(
							this.addon.getIslands().getIslands(this.world).stream().
								filter(Island::isOwned).
								collect(Collectors.toList()));
					}
					else
					{
						this.elementList = this.addon.getIslands().getIslands(this.world).stream().
							filter(Island::isOwned).
							collect(Collectors.toList());
					}

					this.maxPageIndex = (int) Math.ceil(1.0 * this.elementList.size() / MAX_ELEMENTS) - 1;

					// Sort by name.
					this.elementList.sort((o1, o2) -> {
						User u1 = User.getInstance(o1.getOwner());
						User u2 = User.getInstance(o2.getOwner());

						if (u1 == null || !u1.isPlayer())
						{
							return -1;
						}
						else if (u2 == null || !u2.isPlayer())
						{
							return 1;
						}
						else
						{
							return u1.getName().compareTo(u2.getName());
						}
					});
				}
				break;
			default:
				this.elementList = Collections.emptyList();
		}

		if (this.pageIndex < 0)
		{
			this.pageIndex = this.maxPageIndex;
		}
		else if (this.pageIndex > this.maxPageIndex)
		{
			this.pageIndex = 0;
		}

		if (this.elementList.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18, this.createButton(Action.PREVIOUS));
			panelBuilder.item(26, this.createButton(Action.NEXT));
		}

		int elementIndex = MAX_ELEMENTS * this.pageIndex;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (elementIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
			elementIndex < this.elementList.size() &&
			index < 36)
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index,
					this.createIslandButton(this.elementList.get(elementIndex++)));
			}

			index++;
		}
	}


	/**
	 * This method filters out islands that do not contains search field.
	 * @param islandCollection Collection of the islands from which it should search.
	 * @return List of Islands that contains searched field.
	 */
	private List<Island> searchElements(Collection<Island> islandCollection)
	{
		return islandCollection.stream().
			filter(island -> {
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

					if (member != null &&
						member.isPlayer() &&
						member.getName().toLowerCase().contains(this.searchString.toLowerCase()))
					{
						return true;
					}
				}

				// Island do not contains filter field.
				return false;
			}).
			collect(Collectors.toList());
	}


	/**
	 * This method creates button for given island.
	 * @param island Island which button must be created.
	 * @return PanelItem button for given island.
	 */
	private PanelItem createIslandButton(Island island)
	{
		List<String> description = new ArrayList<>();

		UUID ownerId = island.getOwner();
		String ownerName = this.addon.getPlayers().getName(ownerId);
		description.add(ownerName);

		description.add(island.getUniqueId());

		ImmutableSet<UUID> members = island.getMemberSet();

		if (members.size() > 1)
		{
			members.forEach(uuid -> {
				if (uuid != ownerId)
				{
					description.add(ChatColor.AQUA + this.addon.getPlayers().getName(uuid));
				}
			});
		}

		String name = island.getName() != null ? island.getName() : ownerName;

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			IslandEditPanel.open(this, island);
			// Always return true.
			return true;
		};

		return new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "island", Constants.ISLAND, name)).
			description(description).
			icon(ownerName).
			clickHandler(clickHandler).
			build();
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
			case PREVIOUS:
			{
				clickHandler = (panel, user, clickType, i) -> {
					this.pageIndex--;
					this.build();
					return true;
				};

				// Page 0 is viewed... back arrow = last page ... next arrow = 2
				// Page 1 is viewed... back arrow = 1 ... next arrow = 3
				// Page 2 is viewed... back arrow = 2 ... next arrow = 4
				// Page n is viewed... back arrow = n ... next arrow = n+2
				// Last page is viewed .. back arrow = last... next arrow = 1

				if (this.pageIndex == 0)
				{
					count = this.maxPageIndex + 1;
				}
				else
				{
					count = this.pageIndex;
				}

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

				// Page 0 is viewed... back arrow = last page ... next arrow = 2
				// Page 1 is viewed... back arrow = 1 ... next arrow = 3
				// Page 2 is viewed... back arrow = 2 ... next arrow = 4
				// Page n is viewed... back arrow = n ... next arrow = n+2
				// Last page is viewed .. back arrow = last... next arrow = 1

				if (this.pageIndex == this.maxPageIndex)
				{
					count = 1;
				}
				else
				{
					count = this.pageIndex + 2;
				}

				material = Material.ARROW;
				break;
			}
			case SEARCH:
			{
				material = Material.PAPER;
				description.add(this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".search",
					Constants.VALUE, this.searchString));

				clickHandler = (panel, user, clickType, slot) -> {
					if (clickType.isRightClick())
					{
						// Clear string.
						this.searchString = "";
						this.elementList = null;
						// Rebuild gui.
						this.build();
					}
					else
					{
						// Create consumer that process description change
						Consumer<String> consumer = value ->
						{
							if (value != null)
							{
								this.searchString = value;
								this.elementList = null;
							}

							this.build();
						};

						// start conversation
						ConversationUtils.createStringInput(consumer,
							user,
							user.getTranslation(Constants.QUESTIONS + "write-search"),
							user.getTranslation(Constants.MESSAGE + "search-updated"));
					}

					return true;
				};

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
	 * This method creates panel item for given button type.
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createButton(Tab button)
	{
		String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
		description.add("");
		description.add(this.user.getTranslation(Constants.DESCRIPTION + "click-to-see"));

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
			this.activeTab = button;
			this.pageIndex = 0;
			this.elementList = null;

			this.build();
			return true;
		};

		Material material;

		switch (button)
		{
			case IS_ONLINE:
				material = Material.WRITTEN_BOOK;
				break;
			case ALL_ISLANDS:
				material = Material.CHEST;
				break;
			default:
				material = Material.PAPER;
		}

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(this.activeTab == button).
			build();
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
	 * This enum holds all possible actions in current GUI.
	 */
	private enum Action
	{
		/**
		 * Process search function.
		 */
		SEARCH,
		/**
		 * Allows to select previous bundles in multi-page situation.
		 */
		PREVIOUS,
		/**
		 * Allows to select next bundles in multi-page situation.
		 */
		NEXT,
		/**
		 * Process Return Action.
		 */
		RETURN
	}


	/**
	 * This list contains currently displayed island list.
	 */
	private List<Island> elementList;

	/**
	 * Stores current string for searching.
	 */
	private String searchString;

	/**
	 * This variable holds current pageIndex for multi-page generator choosing.
	 */
	private int pageIndex;

	/**
	 * This variable stores maximal page index for previous/next page.
	 */
	private int maxPageIndex;

	/**
	 * Allows to switch between active tabs.
	 */
	private Tab activeTab;
}
