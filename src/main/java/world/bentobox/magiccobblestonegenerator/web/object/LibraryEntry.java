package world.bentobox.magiccobblestonegenerator.web.object;


import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;


/**
 * This objects allows to load each Challenges Catalog library entry.
 */
public class LibraryEntry
{
    /**
     * Empty constructor.
     */
    public LibraryEntry()
    {
    }


    /**
     * Default constructor.
     *
     * @param object Json Object that must be translated to LibraryEntry.
     */
    public LibraryEntry(@NotNull JsonObject object)
    {
        this.name = object.get("name").getAsString();

        Material material = Material.matchMaterial(object.get("icon").getAsString());
        this.icon = (material != null) ? material : Material.PAPER;

        this.description = object.get("description").getAsString();
        this.repository = object.get("repository").getAsString();
        this.language = object.get("language").getAsString();

        this.slot = object.get("slot").getAsInt();

        this.forGameMode = object.get("for").getAsString();
        this.author = object.get("author").getAsString();
        this.version = object.get("version").getAsString();
    }


    /**
     * This method returns the name value.
     *
     * @return the value of name.
     */
    @NotNull
    public String getName()
    {
        return name;
    }


    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * This method returns the icon value.
     *
     * @return the value of icon.
     */
    public Material getIcon()
    {
        return icon;
    }


    /**
     * Sets icon.
     *
     * @param icon the icon
     */
    public void setIcon(Material icon)
    {
        this.icon = icon;
    }


    /**
     * This method returns the description value.
     *
     * @return the value of description.
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * This method returns the repository value.
     *
     * @return the value of repository.
     */
    public String getRepository()
    {
        return repository;
    }


    /**
     * Sets repository.
     *
     * @param repository the repository
     */
    public void setRepository(String repository)
    {
        this.repository = repository;
    }


    /**
     * This method returns the language value.
     *
     * @return the value of language.
     */
    public String getLanguage()
    {
        return language;
    }


// ---------------------------------------------------------------------
// Section: Setters
// ---------------------------------------------------------------------


    /**
     * Sets language.
     *
     * @param language the language
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }


    /**
     * This method returns the slot value.
     *
     * @return the value of slot.
     */
    public int getSlot()
    {
        return slot;
    }


    /**
     * Sets slot.
     *
     * @param slot the slot
     */
    public void setSlot(int slot)
    {
        this.slot = slot;
    }


    /**
     * This method returns the forGameMode value.
     *
     * @return the value of forGameMode.
     */
    public String getForGameMode()
    {
        return forGameMode;
    }


    /**
     * Sets for game mode.
     *
     * @param forGameMode the for game mode
     */
    public void setForGameMode(String forGameMode)
    {
        this.forGameMode = forGameMode;
    }


    /**
     * This method returns the author value.
     *
     * @return the value of author.
     */
    public String getAuthor()
    {
        return author;
    }


    /**
     * Sets author.
     *
     * @param author the author
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }


    /**
     * This method returns the version value.
     *
     * @return the value of version.
     */
    public String getVersion()
    {
        return version;
    }


    /**
     * Sets version.
     *
     * @param version the version
     */
    public void setVersion(String version)
    {
        this.version = version;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Name of entry object
     */
    private String name;

    /**
     * Defaults to {@link Material#PAPER}.
     */
    private Material icon;

    /**
     * Description of entry object.
     */
    private String description;

    /**
     * File name in mcg library.
     */
    private String repository;

    /**
     * Language of content.
     */
    private String language;

    /**
     * Desired slot number.
     */
    private int slot;

    /**
     * Main GameMode for which mcg were created.
     */
    private String forGameMode;

    /**
     * Author (-s) who created current configuration.
     */
    private String author;

    /**
     * Version of MCG Addon, for which generators were created.
     */
    private String version;
}