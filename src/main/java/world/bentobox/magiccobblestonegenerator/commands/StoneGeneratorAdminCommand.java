package world.bentobox.magiccobblestonegenerator.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;

public class StoneGeneratorAdminCommand extends CompositeCommand {

	public StoneGeneratorAdminCommand(StoneGeneratorAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "generator");
	}
	
	@Override
	public void setup() {
		this.setPermission("admin.stonegenerator");
		this.setOnlyPlayer(false);
		this.setDescription("stonegenerator.commands.main.description");
		
		new AddLevelCommand(this.getAddon(), this);
		new SetLevelCommand(this.getAddon(), this);
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		this.showHelp(this, user);
		return false;
	}
	
}
