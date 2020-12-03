//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.commands.admin;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This is a debug command for admins. Admins could use to check which generator
 * user is using and could faster find an issue.
 */
public class GeneratorDatabaseCommand extends CompositeCommand
{
	/**
	 * This is simple constructor for initializing /{admin_command} why generator command.
	 * @param addon StoneGeneratorAddon addon.
	 * @param parentCommand Parent Command where we hook our command into.
	 */
	public GeneratorDatabaseCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
	{
		super(addon, parentCommand, "database");
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
		this.setPermission("admin.stone-generator.database");
		this.setParametersHelp(Constants.ADMIN_COMMANDS + "database.parameters");
		this.setDescription(Constants.ADMIN_COMMANDS + "database.description");

		this.setOnlyPlayer(false);

		new ImportCommand(this.getAddon(), this);
		new ExportCommand(this.getAddon(), this);
	}


	/**
	 * Defines what will be executed when this command is run.
	 * @param user the {@link User} who is executing this command.
	 * @param label the label which has been used to execute this command.
	 *              It can be {@link CompositeCommand#getLabel()} or an alias.
	 * @param args the command arguments.
	 * @return {@code true} if the command executed successfully, {@code false} otherwise.
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		this.showHelp(this, user);
		return true;
	}


	/**
	 * This is a confirmation command for importing generators from template file.
	 * It requires confirmation as it removes every data from database.
	 */
	private static class ImportCommand extends ConfirmableCommand
	{
		/**
		 * This is simple constructor for initializing /{admin_command} generator import command.
		 * @param addon StoneGeneratorAddon addon.
		 * @param parentCommand Parent Command where we hook our command into.
		 */
		public ImportCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
		{
			super(addon, parentCommand, "import");
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
			this.setParametersHelp(Constants.ADMIN_COMMANDS + "import-database.parameters");
			this.setDescription(Constants.ADMIN_COMMANDS + "import-database.description");

			this.setOnlyPlayer(false);
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
			if (args.size() != 1)
			{
				this.showHelp(this, user);
				return false;
			}

			this.askConfirmation(user,
				user.getTranslation(Constants.CONVERSATIONS + "prefix") +
					user.getTranslation(Constants.ADMIN_COMMANDS + "import-database.confirmation",
						Constants.GAMEMODE, Utils.getGameMode(this.getWorld())),
				() -> this.<StoneGeneratorAddon>getAddon().getImportManager().importDatabaseFile(user, this.getWorld(), args.get(0)));

			return true;
		}


		/**
		 * Tab Completer for CompositeCommands.
		 * Note that any registered sub-commands will be automatically added to the list.
		 * Use this to add tab-complete for things like names.
		 * @param user the {@link User} who is executing this command.
		 * @param alias alias for command
		 * @param args command arguments
		 * @return List of strings that could be used to complete this command.
		 */
		@Override
		public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
		{
			File localeDir = this.getAddon().getDataFolder();

			File[] files = localeDir.listFiles(pathname ->
				pathname.getName().endsWith(".json") && pathname.isFile());

			if (files.length == 0)
			{
				// Don't show every player on the server. Require at least the first letter
				return Optional.empty();
			}
			else
			{
				return Optional.of(Util.tabLimit(
					Arrays.stream(files).map(File::getName).collect(Collectors.toList()),
					args.get(args.size() - 1)));
			}
		}
	}


	/**
	 * This is a confirmation command for importing generators from template file.
	 * It requires confirmation as it removes every data from database.
	 */
	private static class ExportCommand extends CompositeCommand
	{
		/**
		 * This is simple constructor for initializing /{admin_command} generator import command.
		 * @param addon StoneGeneratorAddon addon.
		 * @param parentCommand Parent Command where we hook our command into.
		 */
		public ExportCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
		{
			super(addon, parentCommand, "export");
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
			this.setParametersHelp(Constants.ADMIN_COMMANDS + "export.parameters");
			this.setDescription(Constants.ADMIN_COMMANDS + "export.description");

			this.setOnlyPlayer(false);
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
			if (args.size() != 1)
			{
				this.showHelp(this, user);
				return false;
			}

			return this.<StoneGeneratorAddon>getAddon().getImportManager().generateDatabaseFile(user,
				this.getWorld(),
				args.get(0));
		}
	}
}
