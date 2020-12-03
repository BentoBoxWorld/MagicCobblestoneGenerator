package world.bentobox.magiccobblestonegenerator.commands.player;


import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.panels.player.GeneratorUserPanel;
import world.bentobox.magiccobblestonegenerator.panels.player.GeneratorViewPanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class process /{player_command} generator command call.
 */
public class GeneratorPlayerCommand extends CompositeCommand
{
	/**
	 * This is simple constructor for initializing /{player_command} generator command.
	 * @param addon Generator addon.
	 * @param parentCommand Parent Command where we hook our command into.
	 */
	public GeneratorPlayerCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
	{
		super(addon, parentCommand, "generator");
	}


	/**
	 * Setups anything that is needed for this command. <br/><br/> It is recommended you
	 * do the following in this method:
	 * <ul>
	 * <li>Register any of the sub-commands of this command;</li>
	 * <li>Define the permission required to use this command using {@link
	 * CompositeCommand#setPermission(String)};</li>
	 * <li>Define whether this command can only be run by players or not using {@link
	 * CompositeCommand#setOnlyPlayer(boolean)};</li>
	 * </ul>
	 */
	@Override
	public void setup()
	{
		this.setPermission("stone-generator");
		this.setParametersHelp(Constants.PLAYER_COMMANDS + "main.parameters");
		this.setDescription(Constants.PLAYER_COMMANDS + "main.description");

		new GeneratorViewPlayerCommand(this);
		new GeneratorBuyPlayerCommand(this);

		this.setOnlyPlayer(true);
	}


	/**
	 * Defines what will be executed when this command is run.
	 *
	 * @param user the {@link User} who is executing this command.
	 * @param label the label which has been used to execute this command. It can be
	 * {@link CompositeCommand#getLabel()} or an alias.
	 * @param args the command arguments.
	 * @return {@code true} if the command executed successfully, {@code false} otherwise.
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		if (args.isEmpty())
		{
			GeneratorUserPanel.openPanel(this.getAddon(), this.getWorld(), user);
		}
		else
		{
			this.showHelp(this, user);
		}

		return true;
	}


	/**
	 * This method creates tab complete for generators.
	 * @param addon instance of StoneGeneratorAddon
	 * @param user User who need to tab complete.
	 * @param world World where tab complete is targeted.
	 * @param args list of string for tab complete
	 * @return possible tab complete results.
	 */
	private static Optional<List<String>> generatorsTabComplete(StoneGeneratorAddon addon,
		User user,
		World world,
		List<String> args)
	{
		if (args.size() > 3)
		{
			// Show generator for first tab.
			return Optional.of(new ArrayList<>());
		}

		final List<String> returnList = new ArrayList<>();

		// Create suggestions with all generators that is available for users.

		addon.getAddonManager().getIslandGeneratorTiers(world, user).
			forEach(generatorTier -> returnList.add(generatorTier.getUniqueId()));

		return Optional.of(Util.tabLimit(returnList, args.get(args.size() - 1)));
	}


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


	/**
	 * This class allows to open GUI for viewing generator directly.
	 */
	private static class GeneratorViewPlayerCommand extends CompositeCommand
	{
		/**
		 * This is simple constructor for initializing /{player_command} generator command.
		 * @param parentCommand Parent Command where we hook our command into.
		 */
		public GeneratorViewPlayerCommand(CompositeCommand parentCommand)
		{
			super(parentCommand.getAddon(), parentCommand, "view");
		}


		/**
		 * Setups anything that is needed for this command. <br/><br/> It is recommended you
		 * do the following in this method:
		 * <ul>
		 * <li>Register any of the sub-commands of this command;</li>
		 * <li>Define the permission required to use this command using {@link
		 * CompositeCommand#setPermission(String)};</li>
		 * <li>Define whether this command can only be run by players or not using {@link
		 * CompositeCommand#setOnlyPlayer(boolean)};</li>
		 * </ul>
		 */
		@Override
		public void setup()
		{
			this.inheritPermission();
			this.setParametersHelp(Constants.PLAYER_COMMANDS + "view.parameters");
			this.setDescription(Constants.PLAYER_COMMANDS + "view.description");

			this.setOnlyPlayer(true);
		}


		/**
		 * Defines what will be executed when this command is run.
		 *
		 * @param user the {@link User} who is executing this command.
		 * @param label the label which has been used to execute this command. It can be
		 * {@link CompositeCommand#getLabel()} or an alias.
		 * @param args the command arguments.
		 * @return {@code true} if the command executed successfully, {@code false} otherwise.
		 */
		@Override
		public boolean execute(User user, String label, List<String> args)
		{
			if (args.size() == 1)
			{
				GeneratorTierObject generator =
					this.<StoneGeneratorAddon>getAddon().getAddonManager().getGeneratorByID(args.get(0));

				if (generator != null)
				{
					GeneratorViewPanel.openPanel(this.getAddon(), this.getWorld(), user, generator);
				}
				else
				{
					Utils.sendMessage(user,
						user.getTranslation(Constants.ERRORS + "generator-tier-not-found",
							Constants.GENERATOR, args.get(0),
							Constants.GAMEMODE, Utils.getGameMode(this.getWorld())));
				}
			}
			else
			{
				this.showHelp(this, user);
			}

			return false;
		}


		/**
		 * This method adds tab complete for view command.
		 * @param user the {@link User} who is executing this command.
		 * @param alias the label which has been used to execute this command. It can be
		 * {@link CompositeCommand#getLabel()} or an alias.
		 * @param args the command arguments.
		 * @return List with possible generator tier Id's
		 */
		@Override
		public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
		{
			return GeneratorPlayerCommand.generatorsTabComplete(this.getAddon(), user, this.getWorld(), args);
		}
	}


	/**
	 * This class allows to open GUI for viewing generator directly.
	 */
	private static class GeneratorBuyPlayerCommand extends CompositeCommand
	{
		/**
		 * This is simple constructor for initializing /{player_command} generator command.
		 * @param parentCommand Parent Command where we hook our command into.
		 */
		public GeneratorBuyPlayerCommand(CompositeCommand parentCommand)
		{
			super(parentCommand.getAddon(), parentCommand, "buy");
		}


		/**
		 * Setups anything that is needed for this command. <br/><br/> It is recommended you
		 * do the following in this method:
		 * <ul>
		 * <li>Register any of the sub-commands of this command;</li>
		 * <li>Define the permission required to use this command using {@link
		 * CompositeCommand#setPermission(String)};</li>
		 * <li>Define whether this command can only be run by players or not using {@link
		 * CompositeCommand#setOnlyPlayer(boolean)};</li>
		 * </ul>
		 */
		@Override
		public void setup()
		{
			this.inheritPermission();
			this.setParametersHelp(Constants.PLAYER_COMMANDS + "buy.parameters");
			this.setDescription(Constants.PLAYER_COMMANDS + "buy.description");

			this.setOnlyPlayer(true);
		}


		/**
		 * Defines what will be executed when this command is run.
		 *
		 * @param user the {@link User} who is executing this command.
		 * @param label the label which has been used to execute this command. It can be
		 * {@link CompositeCommand#getLabel()} or an alias.
		 * @param args the command arguments.
		 * @return {@code true} if the command executed successfully, {@code false} otherwise.
		 */
		@Override
		public boolean execute(User user, String label, List<String> args)
		{
			if (args.size() == 1)
			{
				StoneGeneratorManager addonManager = this.<StoneGeneratorAddon>getAddon().getAddonManager();
				GeneratorTierObject generator = addonManager.getGeneratorByID(args.get(0));

				if (generator == null)
				{
					Utils.sendMessage(user,
						user.getTranslation(Constants.ERRORS + "generator-tier-not-found",
							Constants.GENERATOR, args.get(0),
							Constants.GAMEMODE, Utils.getGameMode(this.getWorld())));
					return false;
				}


				Island island = this.getAddon().getIslands().getIsland(this.getWorld(), user);

				if (island == null)
				{
					Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
					return false;
				}

				GeneratorDataObject data = addonManager.getGeneratorData(island);

				if (data == null)
				{
					Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
					return false;
				}

				if (addonManager.canPurchaseGenerator(user, island, data, generator))
				{
					addonManager.purchaseGenerator(user, data, generator);
					return true;
				}
			}
			else
			{
				this.showHelp(this, user);
			}

			return false;
		}


		/**
		 * This method adds tab complete for view command.
		 * @param user the {@link User} who is executing this command.
		 * @param alias the label which has been used to execute this command. It can be
		 * {@link CompositeCommand#getLabel()} or an alias.
		 * @param args the command arguments.
		 * @return List with possible generator tier Id's
		 */
		@Override
		public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
		{
			return GeneratorPlayerCommand.generatorsTabComplete(this.getAddon(), user, this.getWorld(), args);
		}
	}
}
