package world.bentobox.magiccobblestonegenerator.commands;


import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * This class manage main Magic Cobblestone generator command, that just shows help message.
 */
public class StoneGeneratorMainCommand extends CompositeCommand
{
	/**
	 * Top level command
	 *
	 * @param addon - addon creating the command
	 * @param cmd - parent command
	 */
	public StoneGeneratorMainCommand(StoneGeneratorAddon addon, CompositeCommand cmd)
	{
		super(addon, cmd, "generator");
	}


	/**
	 * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in
	 * this method:
	 * <ul>
	 * <li>Register any of the sub-commands of this command;</li>
	 * <li>Define the permission required to use this command using {@link CompositeCommand#setPermission(String)};</li>
	 * <li>Define whether this command can only be run by players or not using {@link
	 * CompositeCommand#setOnlyPlayer(boolean)};</li>
	 * </ul>
	 */
	@Override
	public void setup()
	{
		this.setPermission("stonegenerator");
		this.setDescription("stonegenerator.commands.main.description");

		new CurrentLevelCommand(this.getAddon(), this);
		new AllLevelsCommand(this.getAddon(), this);
	}


	/**
	 * Defines what will be executed when this command is run.
	 *
	 * @param user the {@link User} who is executing this command.
	 * @param label the label which has been used to execute this command. It can be {@link
	 * CompositeCommand#getLabel()} or an alias.
	 * @param args the command arguments.
	 * @return {@code true} if the command executed successfully, {@code false} otherwise.
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		// Show help
		this.showHelp(this, user);
		return false;
	}
}
