package world.bentobox.magiccobblestonegenerator.commands;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorData;

public class AddLevelCommand extends CompositeCommand {

	public AddLevelCommand(Addon addon, CompositeCommand cmd) {
		super(addon, cmd, "add");
	}
	
	@Override
	public void setup() {
		this.inheritPermission();
		this.setDescription("stonegenerator.commands.add.description");
	}
	
	public static boolean isInteger(String s, int radix) {
	    Scanner sc = new Scanner(s.trim());
	    if(!sc.hasNextInt(radix)) return false;
	    sc.nextInt(radix);
	    return !sc.hasNext();
	}
	
	@Override
	public boolean canExecute(User user, String label, List<String> args) {
		return getIslands().getIsland(getWorld(), user) != null;
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		if (args.size() != 2) {
			showHelp(this, user);
			return false;
		}
		
		if (!isInteger(args.get(1), 10) || Integer.valueOf(args.get(1)) < 0) {
			user.sendMessage("general.errors.must-be-positive-number", TextVariables.NUMBER, args.get(1));
			return false;
		}
		
		UUID targetUUID = Util.getUUID(args.get(0));
		if (targetUUID == null) {
			user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
			return false;
		}
		
		if (!(getIslands().hasIsland(getWorld(), targetUUID) || getIslands().inTeam(getWorld(), targetUUID))) {
            user.sendMessage("general.errors.player-has-no-island");
            return false;
        }
		
		if (!getIslands().hasIsland(getWorld(), targetUUID)) {
			targetUUID = getIslands().getIsland(getWorld(), targetUUID).getOwner();
		}
		
		StoneGeneratorAddon addon = this.getAddon();
		StoneGeneratorData data = addon.getLevelsData(targetUUID);
		long newLevel = data.getGeneratorLevel() + Integer.valueOf(args.get(1));
		
		data.setGeneratorLevel(newLevel);
		user.sendMessage("stonegenerator.messages.level-add",
				"[player]", args.get(0),
				"[level]", Long.toString(newLevel));
		
		return true;
	}
	
}
