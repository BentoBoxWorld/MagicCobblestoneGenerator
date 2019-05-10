package world.bentobox.magiccobblestonegenerator.commands;


import org.bukkit.Material;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;


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
		World world = this.getWorld();

		StoneGeneratorAddon addon = (StoneGeneratorAddon) this.getAddon();
		long islandLevel = addon.getLevelAddon() == null ? 0L : addon.getLevelAddon().getIslandLevel(world, user.getUniqueId());

		Settings.GeneratorTier generatorTier = addon.getManager().getGeneratorTier(
			islandLevel,
			world);

		if (generatorTier == null)
		{
			user.sendMessage("stonegenerator.errors.cannot-find-any-generators");
			return false;
		}

		int sumChances = generatorTier.getBlockChanceMap().values().stream().mapToInt(Integer::intValue).sum();
		List<Material> materialList = new ArrayList<>(generatorTier.getBlockChanceMap().keySet());
		materialList.sort(Comparator.comparingInt((ToIntFunction<Material>) generatorTier.getBlockChanceMap()::get).thenComparing(material -> material));

		user.sendMessage("stonegenerator.messages.generator-tier",
			"[name]", generatorTier.getName(),
			"[value]", Integer.toString(generatorTier.getMinLevel()));

		for (Material material : materialList)
		{

			user.sendMessage("stonegenerator.messages.material-chance",
				"[name]", material.toString(),
				"[value]", 	Integer.toString((int) Math.floor(generatorTier.getBlockChanceMap().get(material) * 100.0 / sumChances)));
		}

		user.sendMessage("stonegenerator.messages.island-level",
			"[level]", Long.toString(islandLevel));

		return false;
	}
}
