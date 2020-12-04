package world.bentobox.magiccobblestonegenerator.config;


import org.bukkit.Material;
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
    public int getDefaultWorkingRange()
    {
        return defaultWorkingRange;
    }


    /**
     * Method Settings#setWorkingRange sets new value for the workingRange of this object.
     * @param defaultWorkingRange new value for this object.
     *
     */
    public void setDefaultWorkingRange(int defaultWorkingRange)
    {
        this.defaultWorkingRange = defaultWorkingRange;
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


    /**
     * Gets player main command.
     *
     * @return the player main command
     */
    public String getPlayerMainCommand()
    {
        return playerMainCommand;
    }


    /**
     * Sets player main command.
     *
     * @param playerMainCommand the player main command
     */
    public void setPlayerMainCommand(String playerMainCommand)
    {
        this.playerMainCommand = playerMainCommand;
    }


    /**
     * Gets player view command.
     *
     * @return the player view command
     */
    public String getPlayerViewCommand()
    {
        return playerViewCommand;
    }


    /**
     * Sets player view command.
     *
     * @param playerViewCommand the player view command
     */
    public void setPlayerViewCommand(String playerViewCommand)
    {
        this.playerViewCommand = playerViewCommand;
    }


    /**
     * Gets player buy command.
     *
     * @return the player buy command
     */
    public String getPlayerBuyCommand()
    {
        return playerBuyCommand;
    }


    /**
     * Sets player buy command.
     *
     * @param playerBuyCommand the player buy command
     */
    public void setPlayerBuyCommand(String playerBuyCommand)
    {
        this.playerBuyCommand = playerBuyCommand;
    }


    /**
     * Gets player activate command.
     *
     * @return the player activate command
     */
    public String getPlayerActivateCommand()
    {
        return playerActivateCommand;
    }


    /**
     * Sets player activate command.
     *
     * @param playerActivateCommand the player activate command
     */
    public void setPlayerActivateCommand(String playerActivateCommand)
    {
        this.playerActivateCommand = playerActivateCommand;
    }


    /**
     * Gets admin main command.
     *
     * @return the admin main command
     */
    public String getAdminMainCommand()
    {
        return adminMainCommand;
    }


    /**
     * Sets admin main command.
     *
     * @param adminMainCommand the admin main command
     */
    public void setAdminMainCommand(String adminMainCommand)
    {
        this.adminMainCommand = adminMainCommand;
    }


    /**
     * Is show filters boolean.
     *
     * @return the boolean
     */
    public boolean isShowFilters()
    {
        return showFilters;
    }


    /**
     * Sets show filters.
     *
     * @param showFilters the show filters
     */
    public void setShowFilters(boolean showFilters)
    {
        this.showFilters = showFilters;
    }


    /**
     * Gets border block.
     *
     * @return the border block
     */
    public Material getBorderBlock()
    {
        return borderBlock;
    }


    /**
     * Sets border block.
     *
     * @param borderBlock the border block
     */
    public void setBorderBlock(Material borderBlock)
    {
        this.borderBlock = borderBlock;
    }


    /**
     * Gets border block name.
     *
     * @return the border block name
     */
    public String getBorderBlockName()
    {
        return borderBlockName;
    }


    /**
     * Sets border block name.
     *
     * @param borderBlockName the border block name
     */
    public void setBorderBlockName(String borderBlockName)
    {
        this.borderBlockName = borderBlockName;
    }


    /**
     * Is notify unlocked generators boolean.
     *
     * @return the boolean
     */
    public boolean isNotifyUnlockedGenerators()
    {
        return notifyUnlockedGenerators;
    }


    /**
     * Sets notify unlocked generators.
     *
     * @param notifyUnlockedGenerators the notify unlocked generators
     */
    public void setNotifyUnlockedGenerators(boolean notifyUnlockedGenerators)
    {
        this.notifyUnlockedGenerators = notifyUnlockedGenerators;
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
    @ConfigEntry(path = "use-physic", needsRestart = true)
    private boolean usePhysics = true;

    @ConfigComment("")
    @ConfigComment("The range in blocks that an island member has to be in to make the generator generate `magic` blocks.")
    @ConfigComment("0 or less will mean that no range is checked.")
    @ConfigComment("Can be changed with a permission `[gamemode].stone-generator.max-range.[number]`.")
    @ConfigEntry(path = "working-range")
    private int defaultWorkingRange = 0;

    @ConfigComment("")
    @ConfigComment("This allows to define how many generators can be activated at once per each island.")
    @ConfigComment("0 or less will mean that there is no limitation.")
    @ConfigComment("Can be changed with a permission `[gamemode].stone-generator.active-generators.[number]`.")
    @ConfigEntry(path = "default-active-generators")
    private int defaultActiveGeneratorCount = 3;

    @ConfigComment("")
    @ConfigComment("Send a notification message when player unlocks a new generator.")
    @ConfigComment("3 messages that will be showed:")
    @ConfigComment("stone-generator.conversations.click-text-to-purchase - if generator is unlocked but is not purchased.")
    @ConfigComment("stone-generator.conversations.click-text-to-activate-vault - if generator is unlocked but requires activation cost.")
    @ConfigComment("stone-generator.conversations.click-text-to-activate - if generator is unlocked and can be activated.")
    @ConfigEntry(path = "notify-on-unlock")
    private boolean notifyUnlockedGenerators = true;

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which the addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes", needsRestart = true)
    private Set<String> disabledGameModes = new HashSet<>();

    @ConfigComment("This allows to toggle if filters in Generator User Panel should be showed.")
    @ConfigEntry(path = "gui.show-filters")
    private boolean showFilters = true;

    @ConfigComment("This allows to change main border block in player panel.")
    @ConfigEntry(path = "gui.border-block")
    private Material borderBlock = Material.MAGENTA_STAINED_GLASS_PANE;

    @ConfigComment("This allows to change border block display name.")
    @ConfigEntry(path = "gui.border-block-name")
    private String borderBlockName = " ";

    @ConfigComment("Player main sub-command to access the addon.")
    @ConfigComment("This command label will be required to write after gamemode player command label, f.e. /[label] generator")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.main", needsRestart = true)
    private String playerMainCommand = "generator";

    @ConfigComment("Player view sub-command that allows to see detailed generator view.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] generator view")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.view", needsRestart = true)
    private String playerViewCommand = "view";

    @ConfigComment("Player buy sub-command that allows to buy generator with a command.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] generator buy")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.buy", needsRestart = true)
    private String playerBuyCommand = "buy";

    @ConfigComment("Player activate sub-command that allows to activate or deactivate generator with a command.")
    @ConfigComment("This command label will be required to write after player main command, f.e. /[label] generator activate")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.player.activate", needsRestart = true)
    private String playerActivateCommand = "activate";

    @ConfigComment("Admin main sub-command to access the addon.")
    @ConfigComment("This command label will be required to write after gamemode admin command label, f.e. /[label] generator")
    @ConfigComment("Each alias must be separated with an empty space.")
    @ConfigEntry(path = "commands.admin.main", needsRestart = true)
    private String adminMainCommand = "generator";
}
