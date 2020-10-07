package world.bentobox.magiccobblestonegenerator.web;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.World;
import java.nio.charset.StandardCharsets;
import java.util.*;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.web.object.LibraryEntry;


/**
 * This class manages content downloading from web repository.
 */
public class WebManager
{
	/**
	 * Default constructor
	 * @param addon StoneGeneratorAddon object.
	 */
	public WebManager(StoneGeneratorAddon addon)
	{
		this.addon = addon;
		this.plugin = addon.getPlugin();

		this.library = new ArrayList<>(0);

		if (this.plugin.getSettings().isGithubDownloadData())
		{
			long connectionInterval = this.plugin.getSettings().getGithubConnectionInterval() * 20L * 60L;

			if (connectionInterval <= 0)
			{
				// If below 0, it means we shouldn't run this as a repeating task.
				this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin,
					() -> this.requestCatalogGitHubData(true),
					600L);
			}
			else
			{
				// Set connection interval to be at least 60 minutes.
				connectionInterval = Math.max(connectionInterval, 60 * 20 * 60L);
				this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin,
					() -> this.requestCatalogGitHubData(true),
					600L,
					connectionInterval);
			}
		}
	}


	/**
	 * This method requests catalog entries from magic cobblestone generator library.
	 * @param clearCache Boolean that indicates if all cached values must be cleared.
	 */
	public void requestCatalogGitHubData(boolean clearCache)
	{
		this.plugin.getWebManager().getGitHub().ifPresent(gitHubWebAPI ->
		{
			if (this.plugin.getSettings().isLogGithubDownloadData())
			{
				this.plugin.log("Downloading data from GitHub...");
			}

			String catalogContent = "";

			// Downloading the data
			try
			{
				catalogContent = gitHubWebAPI.getRepository("BentoBoxWorld", "weblink").
					getContent("mcg/catalog.json").
					getContent().replaceAll("\\n", "");
			}
			catch (IllegalAccessException e)
			{
				if (this.plugin.getSettings().isLogGithubDownloadData())
				{
					this.plugin.log("Could not connect to GitHub.");
				}
			}
			catch (Exception e)
			{
				this.plugin.logError("An error occurred when downloading data from GitHub...");
				this.plugin.logStacktrace(e);
			}

			// People were concerned that the download took ages, so we need to tell them it's over now.
			if (this.plugin.getSettings().isLogGithubDownloadData())
			{
				this.plugin.log("Successfully downloaded data from GitHub.");
			}

			// Decoding the Base64 encoded contents
			catalogContent = new String(Base64.getDecoder().decode(catalogContent),
				StandardCharsets.UTF_8);

			/* Parsing the data */

			// Register the catalog data
			if (!catalogContent.isEmpty())
			{
				if (clearCache)
				{
					this.library.clear();
				}

				JsonObject catalog = new JsonParser().parse(catalogContent).getAsJsonObject();
				catalog.getAsJsonArray("generators").forEach(gamemode ->
					this.library.add(new LibraryEntry(gamemode.getAsJsonObject())));
			}
		});
	}


	/**
	 * This method requests GitHub data for given LibraryEntry object.
	 * @param user User who inits request.
	 * @param world Target world where challenges should be loaded.
	 * @param entry Entry that contains information about requested object.
	 */
	public void requestEntryGitHubData(User user, World world, LibraryEntry entry)
	{
		this.plugin.getWebManager().getGitHub().ifPresent(gitHubWebAPI ->
		{
			if (this.plugin.getSettings().isLogGithubDownloadData())
			{
				this.plugin.log("Downloading data from GitHub...");
			}

			String stoneGeneratorLibrary = "";

			// Downloading the data
			try
			{
				stoneGeneratorLibrary = gitHubWebAPI.getRepository("BentoBoxWorld", "weblink").
					getContent("mcg/library/" + entry.getRepository() + ".json").
					getContent().
					replaceAll("\\n", "");
			}
			catch (IllegalAccessException e)
			{
				if (this.plugin.getSettings().isLogGithubDownloadData())
				{
					this.plugin.log("Could not connect to GitHub.");
				}
			}
			catch (Exception e)
			{
				this.plugin.logError("An error occurred when downloading data from GitHub...");
				this.plugin.logStacktrace(e);
			}

			// People were concerned that the download took ages, so we need to tell them it's over now.
			if (this.plugin.getSettings().isLogGithubDownloadData())
			{
				this.plugin.log("Successfully downloaded data from GitHub.");
			}

			// Decoding the Base64 encoded contents
			stoneGeneratorLibrary = new String(Base64.getDecoder().decode(stoneGeneratorLibrary),
				StandardCharsets.UTF_8);

			/* Parsing the data */

			// Process downloaded library data
			if (!stoneGeneratorLibrary.isEmpty())
			{
				this.addon.getImportManager().processDownloadedFile(user,
					world,
					stoneGeneratorLibrary);
			}
		});
	}


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


	/**
	 * This method returns all library entries that are downloaded.
	 * @return existing Library entries.
	 */
	public List<LibraryEntry> getLibraryEntries()
	{
		List<LibraryEntry> entries = new ArrayList<>(this.library);
		entries.sort(Comparator.comparingInt(LibraryEntry::getSlot));

		return entries;
	}


	/**
	 * This static method returns if GitHub data downloader is enabled or not.
	 * @return {@code true} if data downloader is enabled, {@code false} - otherwise.
	 */
	public static boolean isEnabled()
	{
		return BentoBox.getInstance().getWebManager().getGitHub().isPresent();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * StoneGeneratorAddon variable.
	 */
	private StoneGeneratorAddon addon;

	/**
	 * BentoBox plugin variable.
	 */
	private BentoBox plugin;

	/**
	 * This list contains all entries that were downloaded from GitHub.
	 */
	private List<LibraryEntry> library;
}
