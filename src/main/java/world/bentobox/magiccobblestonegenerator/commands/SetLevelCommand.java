package world.bentobox.magiccobblestonegenerator.commands;

import java.util.List;
import java.util.UUID;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorData;


/**
 * This class allows to run /[AdminLabel] generator set [player] [level] command,
 * It set [level] to [player]'s island
 *
 */
public class SetLevelCommand extends CompositeCommand {

	/**
	 * Sub-command constructor
	 * @param addon
	 * @param cmd
	 */
	public SetLevelCommand(Addon addon, CompositeCommand cmd) {
		super(addon, cmd, "set");
	}
	
	/**
	 * Set parameters for command:
	 * Inherit permission from top command
	 * Can be run by anythings
	 * Set description
	 */
	@Override
	public void setup() {
		this.inheritPermission();
		this.setOnlyPlayer(false);
		this.setDescription("stonegenerator.commands.set.description");
	}
	
	@Override
	public boolean canExecute(User user, String label, List<String> args) {
		return getIslands().getIsland(getWorld(), user) != null;
	}
	
	/**
	 * Execute command
	 * Check before run:
	 *   Valid arguments
	 *   Known player
	 *   Player has island
	 * From user's island, get Owner UUID
	 */
	@Override
	public boolean execute(User user, String label, List<String> args) {
		
		// Check for number of arguments
		if (args.size() != 2) {
			showHelp(this, user);
			return false;
		}
		
		// Check for [level] argument validity
		if (!Util.isInteger(args.get(1), true) || Integer.valueOf(args.get(1)) < 0) {
			user.sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, args.get(1));
			return false;
		}
		
		// Check for [player] argument validity
		UUID targetUUID = Util.getUUID(args.get(0));
		if (targetUUID == null) {
			user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
			return false;
		}
		
		// Check if [Player] has an island
		if (!(getIslands().hasIsland(getWorld(), targetUUID) || getIslands().inTeam(getWorld(), targetUUID))) {
            user.sendMessage("general.errors.player-has-no-island");
            return false;
        }
		
		// Get UUID of island owner
		if (!getIslands().hasIsland(getWorld(), targetUUID)) {
			targetUUID = getIslands().getIsland(getWorld(), targetUUID).getOwner();
		}
		
		StoneGeneratorAddon addon = this.getAddon();
		StoneGeneratorData data = addon.getLevelsData(targetUUID);
		// Get new level
		long newLevel = Integer.valueOf(args.get(1));
		
		// Set new level in dataBase
		data.setGeneratorLevel(newLevel);
		// Send info message
		user.sendMessage("stonegenerator.messages.level-set",
				"[player]", args.get(0),
				"[level]", Long.toString(newLevel));
		
		return true;
	}
	
}
