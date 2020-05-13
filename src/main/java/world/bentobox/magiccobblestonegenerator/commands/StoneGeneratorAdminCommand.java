package world.bentobox.magiccobblestonegenerator.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;

/**
 * This class manage admin Magic Cobblestone generator command
 *
 */
public class StoneGeneratorAdminCommand extends CompositeCommand {

	/**
	 * Top level command
	 * 
	 * @param addon
	 * @param cmd
	 */
	public StoneGeneratorAdminCommand(StoneGeneratorAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "generator");
	}
	
	/**
	 * Set parameters for command & setup sub-command
	 * Set permission to admin.stonegenerator
	 * Can be run by anythings
	 * Set description
	 */
	@Override
	public void setup() {
		this.setPermission("admin.stonegenerator");
		this.setOnlyPlayer(false);
		this.setDescription("stonegenerator.commands.main.description");
		
		//setup sub-command
		new AddLevelCommand(this.getAddon(), this);
		new SetLevelCommand(this.getAddon(), this);
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		this.showHelp(this, user);
		return false;
	}
	
}
