package world.bentobox.magiccobblestonegenerator.commands;


import java.util.List;

import org.bukkit.World;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;

/**
 * This class allows to run /[toplabel] generator level command, that prints current active
 * Magic Generator Tier for caller in target world.
 */
public class CurrentLevelCommand extends CompositeCommand


{
	/**
	 * Sub-command constructor
	 */
	public CurrentLevelCommand(Addon addon, CompositeCommand cmd)
	{
		super(addon, cmd, "level");
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
		this.setDescription("stonegenerator.commands.level.description");
	}


	/**
	 * This method executes /[toplabel] generator level command. This command prints in chat
	 * current active Magic Generator Tier for input world and method caller.
	 */
	@Override
	public boolean execute(User user, String label, List<String> args)
	{
		World world = this.getWorld();

		StoneGeneratorAddon addon = (StoneGeneratorAddon) this.getAddon();
		long islandLevel;
		
		// If flag set then get level from dataBase, Else, get from Level Addon
		if (StoneGeneratorAddon.MAGIC_COBBLESTONE_GENERATOR_OWN_LEVEL.isSetForWorld(world))
			islandLevel = addon.getLevelsData(user.getUniqueId()).getGeneratorLevel();
		else
			islandLevel = addon.getLevelAddon() == null ? 0L : addon.getLevelAddon().getIslandLevel(world, user.getUniqueId());
		
		Settings.GeneratorTier generatorTier = addon.getManager().getGeneratorTier(
			islandLevel,
			world);

		if (generatorTier == null)
		{
			user.sendMessage("stonegenerator.errors.cannot-find-any-generators");
			return false;
		}
		
		AllLevelsCommand.displayTier(user, generatorTier);

		user.sendMessage("stonegenerator.messages.island-level",
			"[level]", Long.toString(islandLevel));

		return true;
	}
}
