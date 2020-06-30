package world.bentobox.magiccobblestonegenerator.commands.admin;


import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * This class manages {@code /{admin_command} generator} command call.
 */
public class GeneratorAdminCommand extends CompositeCommand
{
	/**
	 * This is simple constructor for initializing /{admin_command} generator command.
	 * @param addon StoneGeneratorAddon addon.
	 * @param parentCommand Parent Command where we hook our command into.
	 */
	public GeneratorAdminCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
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
		this.setPermission("admin.stone-generator");
		this.setParametersHelp("stone-generator.commands.admin.main.parameters");
		this.setDescription("stone-generator.commands.admin.main.description");

		this.setOnlyPlayer(false);

		new ImportCommand(this.getAddon(), this);
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
//		if (args.isEmpty())
//		{
// 			TODO: Need to create and implement admin command.
//			AdminPanel.openPanel(this.getAddon(), this.getWorld(), user);
//		}
//		else
//		{
//			this.showHelp(this, user);
//		}

		this.showHelp(this, user);

		return true;
	}


	// ---------------------------------------------------------------------
	// Section: Subcommadns
	// ---------------------------------------------------------------------


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
			this.setParametersHelp("stone-generator.commands.admin.import.parameters");
			this.setDescription("stone-generator.commands.admin.import.description");

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
			this.askConfirmation(user, user.getTranslation("stone-generator.commands.admin.import.confirmation"),
				() -> this.<StoneGeneratorAddon>getAddon().getImportManager().importFile(user, this.getWorld()));

			return true;
		}
	}
}
