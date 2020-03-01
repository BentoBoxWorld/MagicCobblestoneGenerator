package world.bentobox.magiccobblestonegenerator.config;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * This is StoneGeneratorAddon configuration loader.
 * It is not made by latest BentoBox standards and will be changed at some point. It uses the same tricks
 * as in Level addon for easier and faster code writing!
 */
public class Settings
{


    /**
     * Inits Settings file. Use custom YAML parsing in init.
     * @param addon StoneGeneratorAddon
     */
    public Settings(StoneGeneratorAddon addon)
    {
        this.addon = addon;
        addon.saveDefaultConfig();

        // Get disabled GameModes
        this.disabledGameModes = new HashSet<>(addon.getConfig().getStringList("disabled-gamemodes"));
        this.offlineGeneration = addon.getConfig().getBoolean("offline-generation");

        // if physics should be used
        this.physics = addon.getConfig().getBoolean("use-physics", false);

        this.workingRange = addon.getConfig().getInt("working-range", 0);

        // Reads Generator Tiers
        if (addon.getConfig().isSet("tiers"))
        {
            ConfigurationSection section = addon.getConfig().getConfigurationSection("tiers");
            for (String key : Objects.requireNonNull(section).getKeys(false))
            {
                this.generatorTierMap.put(key, addSection(section, key));
            }
        }

        // Reads GameMode specific generator tiers.
        if (addon.getConfig().isSet("gamemodes"))
        {
            ConfigurationSection section = addon.getConfig().getConfigurationSection("gamemodes");

            for (String gameMode : Objects.requireNonNull(section).getKeys(false))
            {
                ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);
                for (String key : Objects.requireNonNull(gameModeSection).getKeys(false))
                {
                    this.customGeneratorTierMap.computeIfAbsent(gameMode, k -> new HashMap<>()).put(key, addSection(gameModeSection, key));
                }

            }
        }
    }

    @NonNull
    private GeneratorTier addSection(ConfigurationSection section, String key) {
        ConfigurationSection tierSection = section.getConfigurationSection(key);
        GeneratorTier generatorTier = new GeneratorTier(key);
        generatorTier.setName(tierSection.getString("name"));
        generatorTier.setMinLevel(tierSection.getInt("min-level"));

        TreeMap<Double, Material> blockChances = new TreeMap<>();
        if (tierSection.isConfigurationSection("blocks")) {
            for (String materialKey : Objects.requireNonNull(tierSection).getConfigurationSection("blocks").getKeys(false))
            {
                try
                {
                    Material material = Material.valueOf(materialKey);
                    double lastEntry = blockChances.isEmpty() ? 0D : blockChances.lastKey();
                    blockChances.put(lastEntry + tierSection.getDouble("blocks." + materialKey, 0), material);
                }
                catch (Exception e)
                {
                    addon.logWarning("Unknown material (" + materialKey +
                            ") in config.yml blocks section for tier " + key + ". Skipping...");
                }
            }

            generatorTier.setBlockChanceMap(blockChances);
        }
        return generatorTier;
    }



    // ---------------------------------------------------------------------
    // Section: Getters
    // ---------------------------------------------------------------------


    /**
     * This method returns the offlineGeneration object.
     * @return the offlineGeneration object.
     */
    public boolean isOfflineGeneration()
    {
        return offlineGeneration;
    }


    /**
     * This method returns the disabledGameModes object.
     * @return the disabledGameModes object.
     */
    public Set<String> getDisabledGameModes()
    {
        return disabledGameModes;
    }


    /**
     * This method returns the defaultGeneratorTierMap object.
     * @return the defaultGeneratorTierMap object.
     */
    public Map<String, GeneratorTier> getDefaultGeneratorTierMap()
    {
        return this.generatorTierMap;
    }


    /**
     * This method returns the customGeneratorTierMap object for given addon.
     * @param addon Addon name which generators should be returned.
     * @return the customGeneratorTierMap object.
     */
    public Map<String, GeneratorTier> getAddonGeneratorTierMap(String addon)
    {
        return this.customGeneratorTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * This method returns if physics should be used when placing a block
     * .
     * @return usePhysics
     */
    public boolean usePhysics()
    {
        return physics;
    }

    /**
     * returns the range the player has to be in to make it work.
     *
     * @return workingRange
     */
    public int getWorkingRange() {
        return workingRange;
    }

    // ---------------------------------------------------------------------
    // Section: Private object
    // ---------------------------------------------------------------------


    /**
     * This class provides ability to easier process Ore Settings
     */
    public class GeneratorTier
    {
        /**
         * Constructor GeneratorTier creates a new GeneratorTier instance.
         *
         * @param id of type String
         */
        GeneratorTier(String id)
        {
            this.id = id;
        }


        // ---------------------------------------------------------------------
        // Section: Methods
        // ---------------------------------------------------------------------


        /**
         * This method returns the name object.
         * @return the name object.
         */
        public String getName()
        {
            return name;
        }


        /**
         * This method sets the name object value.
         * @param name the name object new value.
         *
         */
        public void setName(String name)
        {
            this.name = name;
        }


        /**
         * This method returns the minLevel object.
         * @return the minLevel object.
         */
        public int getMinLevel()
        {
            return minLevel;
        }


        /**
         * This method sets the minLevel object value.
         * @param minLevel the minLevel object new value.
         *
         */
        public void setMinLevel(int minLevel)
        {
            this.minLevel = minLevel;
        }


        /**
         * This method returns the blockChanceMap object.
         * @return the blockChanceMap object.
         */
        public Map<Double, Material> getBlockChanceMap()
        {
            return blockChanceMap;
        }


        /**
         * This method sets the blockChanceMap object value.
         * @param blockChances the blockChanceMap object new value.
         *
         */
        public void setBlockChanceMap(SortedMap<Double, Material> blockChances)
        {
            this.blockChanceMap = blockChances;
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------

        /**
         * Tier ID
         */
        private final String id;

        /**
         * Tier display name
         */
        private String name = "";

        /**
         * Min Island Level to work.
         * -1 means that tier will be always active and is not skipable.
         */
        private int minLevel = -1;

        /**
         * Map that contains chances for each material to be generated.
         */
        private Map<Double, Material> blockChanceMap = Collections.emptyMap();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * Addon
     */
    private StoneGeneratorAddon addon;

    /**
     * Map that links Generator tier ID with object.
     */
    private Map<String, GeneratorTier> generatorTierMap = new HashMap<>();

    /**
     * Map that links GameMode with its custom GameTiers
     */
    private Map<String, Map<String, GeneratorTier>> customGeneratorTierMap = new HashMap<>();

    /**
     * Boolean that indicate if generator should work on islands with offline members.
     */
    private boolean offlineGeneration;

    /**
     * Set that contains all disabled game modes.
     */
    private Set<String> disabledGameModes;

    /**
     * Boolean to indicate if physics should be used when placing a block
     */
    private boolean physics;

    private int workingRange;
}
