package world.bentobox.magiccobblestonegenerator;


import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.level.Level;
import world.bentobox.magiccobblestonegenerator.commands.StoneGeneratorMainCommand;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;


/**
 * Main addon Class. It starts all processes so addon could properly work.
 * @author BONNe
 */
public class StoneGeneratorAddon extends Addon {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad() {
        super.onLoad();
//		// Save default config.yml
        this.saveDefaultConfig();
        // Load Addon Settings
        this.settings = new Settings(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        // Check if addon is not disabled before.
        if (this.getState().equals(State.DISABLED)) {
            Bukkit.getLogger().severe("Magic Cobblestone Generator Addon is not available or disabled!");
            return;
        }

        List<String> hookedGameModes = new ArrayList<>();

        getPlugin().getAddonsManager().getGameModeAddons().stream()
                .filter(g -> !settings.getDisabledGameModes().contains(g.getDescription().getName()))
                .forEach(g -> {
                    if (g.getPlayerCommand().isPresent())
                    {
                        new StoneGeneratorMainCommand(this, g.getPlayerCommand().get());
                        this.hooked = true;

                        hookedGameModes.add(g.getDescription().getName());
                    }
                });

        if (this.hooked) {
            this.stoneGeneratorManager = new StoneGeneratorManager(this);
            this.stoneGeneratorManager.addGameModes(hookedGameModes);
            this.generator = new MagicGenerator(this);

            // Try to find Level addon and if it does not exist, display a warning

            Optional<Addon> level = this.getAddonByName("Level");

            if (!level.isPresent()) {
                this.logWarning("Level add-on not found so Magic Cobblestone Generator will not work correctly!");
                this.levelAddon = null;
            }
            else {
                this.levelAddon = (Level) level.get();
            }

            Optional<VaultHook> vault = this.getPlugin().getVault();

            if (!vault.isPresent() || !vault.get().hook()) {
                this.vaultHook = null;
                this.logWarning("Economy plugin not found so money options will not work!");
            }
            else {
                this.vaultHook = vault.get();
            }


            // Register the listener.
            this.registerListener(new MainGeneratorListener(this));

            // Register Flags
//			this.getPlugin().getFlagsManager().registerFlag(FLAG_NAME);

            // Register Request Handlers
//			this.registerRequestHandler(REQUEST_HANDLER);
        } else {
            this.logError("Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");
            this.setState(State.DISABLED);
        }
    }


    /**
     * Executes code when disabling the addon.
     */
    @Override
    public void onDisable() {
        // Do some stuff...
    }


    /**
     * Executes code when reloading the addon.
     */
    @Override
    public void onReload() {
        super.onReload();

        if (this.hooked) {
            this.settings = new Settings(this);
            this.getLogger().info("Magic Cobblestone Generator addon reloaded.");
        }
    }

    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns the settings object.
     * @return the settings object.
     */
    public Settings getSettings() {
        return this.settings;
    }


    /**
     * This method returns Magic Generator.
     * @return Magic Generator object.
     */
    public MagicGenerator getGenerator() {
        return this.generator;
    }


    /**
     * This method returns stone manager.
     * @return Stone Generator Manager
     */
    public StoneGeneratorManager getManager() {
        return this.stoneGeneratorManager;
    }


    /**
     * This method returns the levelAddon object.
     * @return the levelAddon object.
     */
    public Level getLevelAddon() {
        return this.levelAddon;
    }


    /**
     * This method returns the levelProvided object.
     * @return the levelProvided object.
     */
    public boolean isLevelProvided() {
        return levelAddon != null;
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
}
