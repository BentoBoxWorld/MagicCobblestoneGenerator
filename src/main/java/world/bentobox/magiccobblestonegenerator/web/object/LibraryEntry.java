package world.bentobox.magiccobblestonegenerator.web.object;


import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;


/**
 * The type Library entry.
 *
 * @param name Name of the library entry
 * @param icon Icon of the library entry
 * @param description Description of the library entry
 * @param repository Link to the repository
 * @param language Language of the entry
 * @param slot order of the entry
 * @param gameMode Made primary for gamemode
 * @param author Author of the entry
 * @param version version of the challenges.
 */
public record LibraryEntry(String name, Material icon, String description, String repository,
                           String language, int slot, String gameMode, String author, String version)
{
    /**
     * Default constructor.
     *
     * @param object Json Object that must be translated to LibraryEntry.
     */
    public static LibraryEntry fromJson(@NotNull JsonObject object)
    {
        Material material = Material.matchMaterial(object.get("icon").getAsString());

        return new LibraryEntry(object.get("name").getAsString(),
            (material != null) ? material : Material.PAPER,
            object.get("description").getAsString(),
            object.get("repository").getAsString(),
            object.get("language").getAsString(),
            object.get("slot").getAsInt(),
            object.get("for").getAsString(),
            object.get("author").getAsString(),
            object.get("version").getAsString());
    }


    public static LibraryEntry fromTemplate(String name, Material icon)
    {
        return new LibraryEntry(name, icon, "", "", "", 0, "", "", "");
    }
}