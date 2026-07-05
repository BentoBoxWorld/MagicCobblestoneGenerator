package world.bentobox.magiccobblestonegenerator.commands.admin;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.panels.admin.AdminPanel;
import world.bentobox.magiccobblestonegenerator.utils.Constants;
import world.bentobox.magiccobblestonegenerator.utils.Utils;


/**
 * This class manages {@code /{admin_command} generator} command call.
 */
public class GeneratorAdminCommand extends CompositeCommand
{
    /**
     * This is simple constructor for initializing /{admin_command} generator command.
     *
     * @param addon StoneGeneratorAddon addon.
     * @param parentCommand Parent Command where we hook our command into.
     */
    public GeneratorAdminCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
    {
        super(addon,
            parentCommand,
            addon.getSettings().getAdminMainCommand().split(" ")[0],
            addon.getSettings().getAdminMainCommand().split(" "));
    }


    /**
     * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in this
     * method:
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
        this.setParametersHelp(Constants.ADMIN_COMMANDS + "main.parameters");
        this.setDescription(Constants.ADMIN_COMMANDS + "main.description");

        this.setOnlyPlayer(false);

        new ImportCommand(this.getAddon(), this);
        new GeneratorWhyCommand(this.getAddon(), this);
        new GeneratorDatabaseCommand(this.getAddon(), this);
        new ResetCommand(this.getAddon(), this);
    }


    /**
     * Defines what will be executed when this command is run.
     *
     * @param user the {@link User} who is executing this command.
     * @param label the label which has been used to execute this command. It can be {@link CompositeCommand#getLabel()}
     * or an alias.
     * @param args the command arguments.
     * @return {@code true} if the command executed successfully, {@code false} otherwise.
     */
    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        if (args.isEmpty())
        {
            AdminPanel.openPanel(this.getAddon(), this.getWorld(), user);
        }
        else
        {
            this.showHelp(this, user);
        }

        return true;
    }


    // ---------------------------------------------------------------------
    // Section: Subcommands
    // ---------------------------------------------------------------------


    /**
     * Base class for admin subcommands that take a single player name argument. It validates the argument, resolves the
     * target player UUID and provides player name tab-completion, delegating the actual work to
     * {@link #executeForTarget(User, String, UUID)}.
     */
    private abstract static class PlayerTargetCommand extends ConfirmableCommand
    {
        protected PlayerTargetCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand, String label)
        {
            super(addon, parentCommand, label);
        }


        @Override
        public boolean execute(User user, String label, List<String> args)
        {
            // If args are not right, show help
            if (args.size() != 1)
            {
                this.showHelp(this, user);
                return false;
            }

            // Get target
            UUID targetUUID = Util.getUUID(args.get(0));

            if (targetUUID == null)
            {
                Utils.sendMessage(user,
                    user.getTranslation("general.errors.unknown-player", TextVariables.NAME, args.get(0)));
                return false;
            }

            return this.executeForTarget(user, args.get(0), targetUUID);
        }


        /**
         * Runs the subcommand for the resolved target player.
         *
         * @param user the user running the command.
         * @param targetName the player name argument as typed.
         * @param targetUUID the resolved target player UUID.
         * @return {@code true} if the command executed successfully.
         */
        protected abstract boolean executeForTarget(User user, String targetName, UUID targetUUID);


        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            if (args.isEmpty())
            {
                // Don't show every player on the server. Require at least the first letter
                return Optional.empty();
            }

            return Optional.of(Util.tabLimit(
                new ArrayList<>(Util.getOnlinePlayerList(user)),
                args.get(args.size() - 1)));
        }
    }


    /**
     * This is a confirmation command for importing generators from template file. It requires confirmation as it
     * removes every data from database.
     */
    private static class ImportCommand extends ConfirmableCommand
    {
        /**
         * This is simple constructor for initializing /{admin_command} generator import command.
         *
         * @param addon StoneGeneratorAddon addon.
         * @param parentCommand Parent Command where we hook our command into.
         */
        public ImportCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
        {
            super(addon, parentCommand, "import");
        }


        /**
         * Setups anything that is needed for this command. <br/><br/> It is recommended you do the following in this
         * method:
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
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "import.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "import.description");

            this.setOnlyPlayer(false);
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
            this.askConfirmation(user,
                user.getTranslation(Constants.CONVERSATIONS + "prefix") +
                    user.getTranslation(Constants.ADMIN_COMMANDS + "import.confirmation",
                        Constants.GAMEMODE, Utils.getGameMode(this.getWorld())),
                () -> this.<StoneGeneratorAddon>getAddon().getImportManager().importFile(user, this.getWorld()));

            return true;
        }
    }


    /**
     * This is a debug command for admins. Admins could use to check which generator user is using and could faster find
     * an issue.
     */
    private static class GeneratorWhyCommand extends PlayerTargetCommand
    {
        /**
         * This is simple constructor for initializing /{admin_command} why generator command.
         *
         * @param addon StoneGeneratorAddon addon.
         * @param parentCommand Parent Command where we hook our command into.
         */
        public GeneratorWhyCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
        {
            super(addon, parentCommand, "why");
        }


        @Override
        public void setup()
        {
            this.setPermission("admin.stone-generator.why");
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "why.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "why.description");

            this.setOnlyPlayer(false);
        }


        @Override
        protected boolean executeForTarget(User user, String targetName, UUID targetUUID)
        {
            // Set meta data on player
            Island island = this.getAddon().getIslands().getIsland(this.getWorld(), targetUUID);

            if (island == null || island.getOwner() == null || !island.getOwner().equals(targetUUID))
            {
                Utils.sendMessage(user,
                    user.getTranslation("general.errors.player-is-not-owner"));
                return false;
            }

            User target = User.getInstance(island.getOwner());

            if (!target.isOnline())
            {
                Utils.sendMessage(user,
                    user.getTranslation("general.errors.offline-player"));
                return false;
            }

            // Determine the debug mode and toggle if required
            boolean newValue = !target.getPlayer().getMetadata(getWorld().getName() + "_why_debug_generator").stream().
                filter(p -> getPlugin().equals(p.getOwningPlugin())).
                findFirst().
                map(MetadataValue::asBoolean).
                orElse(false);

            if (newValue)
            {
                Utils.sendMessage(user,
                    user.getTranslation("commands.admin.why.turning-on",
                        TextVariables.NAME, target.getName()));
            }
            else
            {
                Utils.sendMessage(user,
                    user.getTranslation("commands.admin.why.turning-off",
                        TextVariables.NAME, target.getName()));
            }

            // Set the debug meta
            target.getPlayer().setMetadata(getWorld().getName() + "_why_debug_generator",
                new FixedMetadataValue(this.getPlugin(), newValue));

            if (user.isPlayer())
            {
                target.getPlayer().setMetadata(getWorld().getName() + "_why_debug_generator_issuer",
                    new FixedMetadataValue(this.getPlugin(), user.getUniqueId().toString()));
            }

            return true;
        }
    }


    /**
     * This command resets a single player's island generator data (unlocked, purchased and active generators) without
     * touching the rest of the database. Requires confirmation as it is destructive.
     */
    private static class ResetCommand extends PlayerTargetCommand
    {
        /**
         * This is simple constructor for initializing /{admin_command} generator reset command.
         *
         * @param addon StoneGeneratorAddon addon.
         * @param parentCommand Parent Command where we hook our command into.
         */
        public ResetCommand(StoneGeneratorAddon addon, CompositeCommand parentCommand)
        {
            super(addon, parentCommand, "reset");
        }


        @Override
        public void setup()
        {
            this.setPermission("admin.stone-generator.reset");
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "reset.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "reset.description");

            this.setOnlyPlayer(false);
        }


        @Override
        protected boolean executeForTarget(User user, String targetName, UUID targetUUID)
        {
            Island island = this.getAddon().getIslands().getIsland(this.getWorld(), targetUUID);

            if (island == null)
            {
                Utils.sendMessage(user, user.getTranslation("general.errors.player-has-no-island"));
                return false;
            }

            // Fall back to the typed argument if the server cannot resolve a name for the UUID.
            final String resolvedName = this.getPlayers().getName(targetUUID);
            final String displayName = resolvedName == null || resolvedName.isEmpty() ? targetName : resolvedName;

            this.askConfirmation(user,
                user.getTranslation(Constants.CONVERSATIONS + "prefix") +
                    user.getTranslation(Constants.ADMIN_COMMANDS + "reset.confirmation",
                        Constants.PLAYER, displayName),
                () ->
                {
                    this.<StoneGeneratorAddon>getAddon().getAddonManager().resetIslandData(island);
                    Utils.sendMessage(user,
                        user.getTranslation(Constants.MESSAGES + "generator-data-reset",
                            Constants.PLAYER, displayName));
                });

            return true;
        }
    }
}
