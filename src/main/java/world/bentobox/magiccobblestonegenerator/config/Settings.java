package world.bentobox.magiccobblestonegenerator.config;


import java.util.HashSet;
import java.util.Set;


import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;


/**
 * Settings that implements ConfigObject is powerful and dynamic Config Objects that
 * does not need custom parsing. If it is correctly loaded, all its values will be available.
 *
 * Without Getter and Setter this class will not work.
 *
 * To specify location for config object to be stored, you should use @StoreAt(filename="{config file name}", path="{Path to your addon}")
 * To save comments in config file you should use @ConfigComment("{message}") that adds any message you want to be in file.
 */
@StoreAt(filename="config.yml", path="addons/MagicCobblestoneGenerator")
@ConfigComment("MagicCobblestoneGenerator Configuration [version]")
@ConfigComment("This config file is dynamic and saved when the server is shutdown.")
@ConfigComment("")
public class Settings implements ConfigObject
{
    /**
     * Empty constructor.
     */
    public Settings()
    {
        // Default empty constructor.
    }


    // ---------------------------------------------------------------------
    // Section: Getters and Setters
    // ---------------------------------------------------------------------


    /**
     * Method Settings#isOfflineGeneration returns the offlineGeneration of this object.
     *
     * @return the offlineGeneration (type boolean) of this object.
     */
    public boolean isOfflineGeneration()
    {
        return offlineGeneration;
    }


    /**
     * Method Settings#setOfflineGeneration sets new value for the offlineGeneration of this object.
     * @param offlineGeneration new value for this object.
     *
     */
    public void setOfflineGeneration(boolean offlineGeneration)
    {
        this.offlineGeneration = offlineGeneration;
    }


    /**
     * Method Settings#isPhysics returns the physics of this object.
     *
     * @return the physics (type boolean) of this object.
     */
    public boolean isUsePhysics()
    {
        return usePhysics;
    }


    /**
     * Method Settings#setPhysics sets new value for the physics of this object.
     * @param usePhysics new value for this object.
     *
     */
    public void setUsePhysics(boolean usePhysics)
    {
        this.usePhysics = usePhysics;
    }


    /**
     * Method Settings#getWorkingRange returns the workingRange of this object.
     *
     * @return the workingRange (type int) of this object.
     */
    public int getWorkingRange()
    {
        return workingRange;
    }


    /**
     * Method Settings#setWorkingRange sets new value for the workingRange of this object.
     * @param workingRange new value for this object.
     *
     */
    public void setWorkingRange(int workingRange)
    {
        this.workingRange = workingRange;
    }


    /**
     * Method Settings#getDefaultActiveGeneratorCount returns the defaultActiveGeneratorCount of this object.
     *
     * @return the defaultActiveGeneratorCount (type int) of this object.
     */
    public int getDefaultActiveGeneratorCount()
    {
        return defaultActiveGeneratorCount;
    }


    /**
     * Method Settings#setDefaultActiveGeneratorCount sets new value for the defaultActiveGeneratorCount of this object.
     * @param defaultActiveGeneratorCount new value for this object.
     *
     */
    public void setDefaultActiveGeneratorCount(int defaultActiveGeneratorCount)
    {
        this.defaultActiveGeneratorCount = defaultActiveGeneratorCount;
    }


    /**
     * This method returns the disabledGameModes value.
     *
     * @return the value of disabledGameModes.
     */
    public Set<String> getDisabledGameModes()
    {
        return disabledGameModes;
    }


    /**
     * This method sets the disabledGameModes value.
     *
     * @param disabledGameModes the disabledGameModes new value.
     */
    public void setDisabledGameModes(Set<String> disabledGameModes)
    {
        this.disabledGameModes = disabledGameModes;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    @ConfigComment("")
    @ConfigComment("Allows to block addon to generate blocks if all island members are offline.")
    @ConfigEntry(path = "offline-generation")
    private boolean offlineGeneration = false;

    @ConfigComment("")
    @ConfigComment("If physics should be used when placing a block.")
    @ConfigComment("Using physics allow certain redstone machines to work,")
    @ConfigComment("but might have unwanted side effectsAllows to block addon to generate blocks if all island members are offline.")
    @ConfigEntry(path = "use-physic")
    private boolean usePhysics = true;

    @ConfigComment("")
    @ConfigComment("The range in blocks that an island member has to be in to make the generator generate `magic` blocks.")
    @ConfigComment("0 or less will mean that no range is checked.")
    @ConfigComment("Can be changed with a permission `[gamemode].stone-generator.max-range.[number]`.")
    @ConfigEntry(path = "working-range")
    private int workingRange = 0;

    @ConfigComment("")
    @ConfigComment("This allows to define how many generators can be activated at once per each island.")
    @ConfigComment("0 or less will mean that only default generator can be used for island.")
    @ConfigComment("Can be changed with a permission `[gamemode].stone-generator.active-generators.[number]`.")
    @ConfigEntry(path = "default-active-generators")
    private int defaultActiveGeneratorCount = 3;

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which Likes addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();
}
