package world.bentobox.magiccobblestonegenerator;

import org.bukkit.Bukkit;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.clicklisteners.CycleClick;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.level.Level;
import world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand;
import world.bentobox.magiccobblestonegenerator.commands.player.GeneratorPlayerCommand;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.listeners.JoinLeaveListener;
import world.bentobox.magiccobblestonegenerator.listeners.VanillaGeneratorListener;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorImportManager;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;
import world.bentobox.upgrades.UpgradesAddon;


/**
 * Main addon Class. It starts all processes so addon could properly work.
 * @author BONNe
 */
public class StoneGeneratorAddon extends Addon
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad()
    {
        super.onLoad();
//		// Save default config.yml
        this.saveDefaultConfig();
        // Load Addon Settings
        this.settings = new Config<>(this, Settings.class).loadConfigObject();

        StoneGeneratorAddon.instance = this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable()
    {
        // Check if addon is not disabled before.
        if (this.getState().equals(State.DISABLED))
        {
            Bukkit.getLogger().severe("Magic Cobblestone Generator Addon is not available or disabled!");
            return;
        }

        // Init new import manager.
        this.stoneGeneratorImportManager = new StoneGeneratorImportManager(this);

        // Init new Generator Manager
        this.stoneGeneratorManager = new StoneGeneratorManager(this);

        this.getPlugin().getAddonsManager().getGameModeAddons().stream().
            filter(gameMode -> !settings.getDisabledGameModes().contains(gameMode.getDescription().getName())).
            forEach(gameMode -> {
                if (gameMode.getPlayerCommand().isPresent())
                {
                    // Add Placeholders
                    this.registerPlaceholders(gameMode);

                    // Add GameMode worlds to Generator.
                    this.stoneGeneratorManager.addWorld(gameMode.getOverWorld());

                    if (gameMode.getWorldSettings().isNetherIslands())
                    {
                        this.stoneGeneratorManager.addWorld(gameMode.getNetherWorld());
                    }

                    if (gameMode.getWorldSettings().isEndIslands())
                    {
                        this.stoneGeneratorManager.addWorld(gameMode.getEndWorld());
                    }

                    MAGIC_COBBLESTONE_GENERATOR.addGameModeAddon(gameMode);
                    MAGIC_COBBLESTONE_GENERATOR_PERMISSION.addGameModeAddon(gameMode);

                    // Hook commands
                    gameMode.getPlayerCommand().ifPresent(playerCommand ->
                        new GeneratorPlayerCommand(this, playerCommand));
                    gameMode.getAdminCommand().ifPresent(adminCommand ->
                        new GeneratorAdminCommand(this, adminCommand));

                    // if gamemode does not have any data, load them from template.
                    if (this.stoneGeneratorManager.getAllGeneratorTiers(gameMode.getOverWorld()).isEmpty())
                    {
                        this.stoneGeneratorImportManager.importFile(null, gameMode.getOverWorld());
                    }

                    this.hooked = true;
                }
            });

        if (this.hooked)
        {
            this.generator = new MagicGenerator(this);

            // Try to find Level addon and if it does not exist, display a warning

            Optional<Addon> level = this.getAddonByName("Level");

            if (!level.isPresent())
            {
                this.logWarning("Level add-on not found so Magic Cobblestone Generator, some parts may not work!");
                this.levelAddon = null;
            }
            else
            {
                this.levelAddon = (Level) level.get();
            }

            // Find upgrades addon

            Optional<Addon> upgrades = this.getAddonByName("Upgrades");

            if (!upgrades.isPresent())
            {
//              this.logWarning("Upgrades add-on not found so Magic Cobblestone Generator, some parts may not work!");
                this.upgradesAddon = null;
            }
            else
            {
                this.upgradesAddon = (UpgradesAddon) upgrades.get();
            }

            // Find vault plugin

            Optional<VaultHook> vault = this.getPlugin().getVault();

            if (!vault.isPresent())
            {
                this.vaultHook = null;
                this.logWarning("Vault plugin not found. Economy will not work!");
            }
            else
            {
                this.vaultHook = vault.get();
            }

            // Register the listener.
            this.registerListener(new VanillaGeneratorListener(this));
            // TODO: fix and implement
            //this.registerListener(new MagicGeneratorListener(this));

            this.registerListener(new JoinLeaveListener(this));

            // Register Flags
            this.registerFlag(MAGIC_COBBLESTONE_GENERATOR);
            this.registerFlag(MAGIC_COBBLESTONE_GENERATOR_PERMISSION);

            // Register Request Handlers
//			this.registerRequestHandler(REQUEST_HANDLER);
        }
        else
        {
            this.logError("Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");
            this.setState(State.DISABLED);
        }
    }


    /**
     * Registers the placeholders
     * @param addon GameMode addon where placeholders are added.
     * @since 2.0.0
     */
    private void registerPlaceholders(GameModeAddon addon)
    {
        final String addonName = this.getDescription().getName().toLowerCase();
        final World world = addon.getOverWorld();

        // Placeholder returns currently active count.
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(addon,
            addonName + "_active_generator_count",
            user -> {
                GeneratorDataObject object = this.getAddonManager().getGeneratorData(user, world);
                return String.valueOf(object != null ?
                    object.getActiveGeneratorList().size() : 0);
            });

        // Placeholder returns maximal active generator count, that user can activate.
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(addon,
            addonName + "_max_active_generator_count",
            user -> {
                GeneratorDataObject object = this.getAddonManager().getGeneratorData(user, world);
                return String.valueOf(object != null ?
                    object.getMaxGeneratorCount() : 0);
            });

        // Placeholder returns active generator names separated with ','
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(addon,
            addonName + "_active_generator_names",
            user -> {
                GeneratorDataObject object = this.getAddonManager().getGeneratorData(user, world);

                if (object != null && !object.getActiveGeneratorList().isEmpty())
                {
                    StringBuilder stringBuilder = new StringBuilder();

                    object.getActiveGeneratorList().forEach(generator ->
                        stringBuilder.append(generator.getFriendlyName()).append(","));

                    if (stringBuilder.length() > 0)
                    {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }

                    return stringBuilder.toString();
                }
                else
                {
                    return "";
                }
            });

        // Placeholder returns unlocked generator names separated with ','
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(addon,
            addonName + "_unlocked_generator_names",
            user -> {
                GeneratorDataObject object = this.getAddonManager().getGeneratorData(user, world);

                if (object != null && !object.getUnlockedTiers().isEmpty())
                {
                    StringBuilder stringBuilder = new StringBuilder();

                    object.getUnlockedTiers().stream().sorted().forEach(generator ->
                        stringBuilder.append(generator.getFriendlyName()).append(","));

                    if (stringBuilder.length() > 0)
                    {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }

                    return stringBuilder.toString();
                }
                else
                {
                    return "";
                }
            });

        // Placeholder returns purchased generator names separated with ','
        this.getPlugin().getPlaceholdersManager().registerPlaceholder(addon,
            addonName + "_purchased_generator_names",
            user -> {
                GeneratorDataObject object = this.getAddonManager().getGeneratorData(user, world);

                if (object != null && !object.getPurchasedTiers().isEmpty())
                {
                    StringBuilder stringBuilder = new StringBuilder();

                    object.getPurchasedTiers().stream().sorted().forEach(generator ->
                        stringBuilder.append(generator.getFriendlyName()).append(","));

                    if (stringBuilder.length() > 0)
                    {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }

                    return stringBuilder.toString();
                }
                else
                {
                    return "";
                }
            });
    }


    /**
     * Executes code when disabling the addon.
     */
    @Override
    public void onDisable()
    {
        // Do some stuff...

        if (this.hooked)
        {
            // Save database on disable.
            this.getAddonManager().save();
        }
    }


    /**
     * Executes code when reloading the addon.
     */
    @Override
    public void onReload()
    {
        super.onReload();

        this.settings = new Config<>(this, Settings.class).loadConfigObject();

        if (this.settings == null)
        {
            // If we failed to load Settings then we should not enable addon.
            // We can log error and set state to DISABLED.

            this.logError("MagicCobblestoneGenerator settings could not load! Addon disabled.");
            this.setState(State.DISABLED);
        }
        else
        {
            if (this.hooked)
            {
                this.stoneGeneratorManager.reload();
                this.log("Magic Cobblestone Generator addon reloaded.");
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns the settings object.
     *
     * @return the settings object.
     */
    public Settings getSettings()
    {
        return this.settings;
    }


    /**
     * This method returns Magic Generator.
     *
     * @return Magic Generator object.
     */
    public MagicGenerator getGenerator()
    {
        return this.generator;
    }


    /**
     * This method returns stone manager.
     *
     * @return Stone Generator Manager
     */
    public StoneGeneratorManager getAddonManager()
    {
        return this.stoneGeneratorManager;
    }


    /**
     * This method returns stone import manager.
     *
     * @return Stone Generator Import Manager
     */
    public StoneGeneratorImportManager getImportManager()
    {
        return this.stoneGeneratorImportManager;
    }


    /**
     * This method returns the levelAddon object.
     *
     * @return the levelAddon object.
     */
    public Level getLevelAddon()
    {
        return this.levelAddon;
    }


    /**
     * This method returns the levelProvided object.
     *
     * @return the levelProvided object.
     */
    public boolean isLevelProvided()
    {
        return levelAddon != null;
    }


    /**
     * This method returns the vaultHook is provided.
     *
     * @return the vaultHook object exists.
     */
    public boolean isVaultProvided()
    {
        return vaultHook != null && vaultHook.hook();
    }


    /**
     * This method adds access to vaulthook.
     * @return VaultHook.
     */
    public VaultHook getVaultHook()
    {
        return vaultHook;
    }


    /**
     * This method returns the upgradesAddon object.
     *
     * @return the upgradesAddon object.
     */
    public UpgradesAddon getUpgradesAddon()
    {
        return this.upgradesAddon;
    }


    /**
     * This method returns the upgradesAddon object.
     *
     * @return the upgradesAddon object exists.
     */
    public boolean isUpgradesProvided()
    {
        return upgradesAddon != null;
    }


    /**
     * This method allows to access static addon instance.
     * @return Addon instance.
     */
    public static StoneGeneratorAddon getInstance()
    {
        return instance;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Variable holds settings object.
     */
    private Settings settings;

    /**
     * Variable indicates if addon is hooked in any game mode
     */
    private boolean hooked;

    /**
     * Variable holds Stone Generator Manager object.
     */
    private StoneGeneratorManager stoneGeneratorManager;

    /**
     * Variable holds Stone Generator Manager object.
     */
    private StoneGeneratorImportManager stoneGeneratorImportManager;

    /**
     * Variable holds MagicGenerator object.
     */
    private MagicGenerator generator;

    /**
     * VaultHook that process economy.
     */
    private VaultHook vaultHook;

    /**
     * Level addon.
     */
    private Level levelAddon;

    /**
     * Upgrades addon.
     */
    private UpgradesAddon upgradesAddon;


    /**
     * Static addon isntance.
     */
    private static StoneGeneratorAddon instance;


    // ---------------------------------------------------------------------
    // Section: Flags
    // ---------------------------------------------------------------------


    /**
     * Settings flags allows to modifying parameters of the island.
     *
     * It can be modified by the players (island owner).
     * This is usually an on/off setting.
     *
     * MAGIC_COBBLESTONE_GENERATOR should also be defined in language file under
     * protection.flags section.
     *
     * By default setting is set to false.
     */
    public final static Flag MAGIC_COBBLESTONE_GENERATOR =
        new Flag.Builder("MAGIC_COBBLESTONE_GENERATOR", Material.DIAMOND_PICKAXE).
            type(Flag.Type.SETTING).
            defaultSetting(true).
            build();

    /**
     * This flag allows to change who have access to modify island generator tiers option.
     * Owner can change it from member rank till owner rank.
     * Default value is set to subowner.
     */
    public final static Flag MAGIC_COBBLESTONE_GENERATOR_PERMISSION =
        new Flag.Builder("MAGIC_COBBLESTONE_GENERATOR_PERMISSION", Material.DIAMOND_PICKAXE).
            type(Flag.Type.PROTECTION).
            defaultRank(RanksManager.SUB_OWNER_RANK).
            clickHandler(new CycleClick("MAGIC_COBBLESTONE_GENERATOR_PERMISSION", RanksManager.MEMBER_RANK, RanksManager.OWNER_RANK)).
            build();
}
