package world.bentobox.magiccobblestonegenerator;

import org.bukkit.Bukkit;
import java.util.Optional;

import org.bukkit.Material;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.level.Level;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.listeners.VanillaGeneratorListener;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;


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
                this.logWarning("Level add-on not found so Magic Cobblestone Generator will not work correctly!");
                this.levelAddon = null;
            }
            else
            {
                this.levelAddon = (Level) level.get();
            }

            Optional<VaultHook> vault = this.getPlugin().getVault();

            if (!vault.isPresent() || !vault.get().hook())
            {
                this.vaultHook = null;
                this.logWarning("Economy plugin not found so money options will not work!");
            }
            else
            {
                this.vaultHook = vault.get();
            }

            // Register the listener.
            this.registerListener(new VanillaGeneratorListener(this));
            // TODO: fix and implement
            //this.registerListener(new MagicGeneratorListener(this));

            // Register Flags
            this.magicFlag = new Flag.Builder("MAGIC_COBBLESTONE_GENERATOR", Material.DIAMOND_PICKAXE).
                type(Flag.Type.SETTING).
                defaultSetting(true).
                addon(this).
                build();
            this.getPlugin().getFlagsManager().registerFlag(magicFlag);

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
        // TODO.
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
                this.getLogger().info("Magic Cobblestone Generator addon reloaded.");
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
     * Returns the Flag that allows players to toggle on/off the Magic Cobblestone Generator on their islands.
     *
     * @return the Flag that allows players to toggle on/off the Magic Cobblestone Generator on their islands.
     * @since 1.9.0
     */
    public Flag getMagicFlag()
    {
        return magicFlag;
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
     * Flag that toggles on/off the Magic Cobblestone Generator.
     * @since 1.9.0
     */
    private Flag magicFlag;
}
