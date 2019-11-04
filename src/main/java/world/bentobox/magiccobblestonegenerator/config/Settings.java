package world.bentobox.magiccobblestonegenerator.config;


import java.util.*;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

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
        addon.saveDefaultConfig();

        // Get disabled GameModes
        this.disabledGameModes = new HashSet<>(addon.getConfig().getStringList("disabled-gamemodes"));
        this.offlineGeneration = addon.getConfig().getBoolean("offline-generation");

        // Reads Generator Tiers
        if (addon.getConfig().isSet("tiers"))
        {
            ConfigurationSection section = addon.getConfig().getConfigurationSection("tiers");

            for (String key : section.getKeys(false))
            {
                ConfigurationSection tierSection = section.getConfigurationSection(key);

                if (tierSection != null)
                {
                    GeneratorTier generatorTier = new GeneratorTier(key);
                    generatorTier.setName(tierSection.getString("name"));
                    generatorTier.setMinLevel(tierSection.getInt("min-level"));

                    Map<Material, Integer> blockChances = new EnumMap<>(Material.class);

                    for (String materialKey : tierSection.getConfigurationSection("blocks").getKeys(false))
                    {
                        try
                        {
                            Material material = Material.valueOf(materialKey);
                            blockChances.put(material, tierSection.getInt("blocks." + materialKey, 0));
                        }
                        catch (Exception e)
                        {
                            addon.getLogger().warning(() -> "Unknown material (" + materialKey +
                                    ") in config.yml blocks section for tier " + key + ". Skipping...");
                        }
                    }

                    generatorTier.setBlockChanceMap(blockChances);

                    this.generatorTierMap.put(key, generatorTier);
                }
            }
        }

        // Reads GameMode specific generator tiers.
        if (addon.getConfig().isSet("gamemodes"))
        {
            ConfigurationSection section = addon.getConfig().getConfigurationSection("gamemodes");

            for (String gameMode : section.getKeys(false))
            {
                ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);

                for (String key : gameModeSection.getKeys(false))
                {
                    ConfigurationSection tierSection = gameModeSection.getConfigurationSection(key);

                    GeneratorTier generatorTier = new GeneratorTier(key);
                    generatorTier.setName(tierSection.getString("name"));
                    generatorTier.setMinLevel(tierSection.getInt("min-level"));

                    Map<Material, Integer> blockChances = new EnumMap<>(Material.class);

                    for (String materialKey : tierSection.getConfigurationSection("blocks").getKeys(false))
                    {
                        try
                        {
                            Material material = Material.valueOf(materialKey);
                            blockChances.put(material, tierSection.getInt("blocks." + materialKey, 0));
                        }
                        catch (Exception e)
                        {
                            addon.getLogger().warning(() -> "Unknown material (" + materialKey +
                                    ") in config.yml blocks section for tier " + key +
                                    " in gamemode section for " + gameMode + ". Skipping...");
                        }
                    }

                    generatorTier.setBlockChanceMap(blockChances);

                    this.customGeneratorTierMap.computeIfAbsent(gameMode, k -> new HashMap<>()).put(key, generatorTier);
                }
            }
        }
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
        public Map<Material, Integer> getBlockChanceMap()
        {
            return blockChanceMap;
        }


        /**
         * This method sets the blockChanceMap object value.
         * @param blockChanceMap the blockChanceMap object new value.
         *
         */
        public void setBlockChanceMap(Map<Material, Integer> blockChanceMap)
        {
            this.blockChanceMap = blockChanceMap;
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
        private Map<Material, Integer> blockChanceMap = Collections.emptyMap();
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


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
}
