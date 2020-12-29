package world.bentobox.magiccobblestonegenerator;


import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class, DatabaseSetup.class})
public class StoneGeneratorAddonTest
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getPlaceholdersManager()).thenReturn(placeholdersManager);
        when(plugin.getFlagsManager()).thenReturn(flagsManager);
        when(plugin.getIWM()).thenReturn(iwm);
        // Bukkit
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        when(Bukkit.getLogger()).thenReturn(logger);

        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);


        // Addon
        addon = new StoneGeneratorAddon();
        File jFile = new File("addon.jar");
        List<String> lines = Arrays.asList("# MagicCobblestoneGenerator Addon Configuration", "uniqueId: config");
        Path path = Paths.get("config.yml");
        Files.write(path, lines, Charset.forName("UTF-8"));
        Path template = Paths.get("src/main/resources/generatorTemplate.yml");
        Path copyTo = Paths.get("generatorTemplate.yml");
        Files.copy(template, copyTo);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile)))
        {
            //Added the new files to the jar.
            addFileToJar(path, tempJarOutputStream);
            addFileToJar(copyTo, tempJarOutputStream);
        }
        File dataFolder = new File("addons/MagicCobblestoneGenerator");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc =
            new AddonDescription.Builder("bentobox", "MagicCobblestoneGenerator", "1.3").description("test")
                .authors("BONNe").build();
        addon.setDescription(desc);
        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 =
            new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);
        when(gameMode.getWorldSettings()).thenReturn(mock(WorldSettings.class));
        CompositeCommand cmd = mock(CompositeCommand.class);
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd);
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
        // IWM
        when(iwm.getAddon(any())).thenReturn(Optional.of(gameMode));
    }


    private void addFileToJar(Path path, JarOutputStream tempJarOutputStream) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(path.toFile()))
        {

            byte[] buffer = new byte[1024];
            int bytesRead;
            JarEntry entry = new JarEntry(path.toString());
            tempJarOutputStream.putNextEntry(entry);
            while ((bytesRead = fis.read(buffer)) != -1)
            {
                tempJarOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        new File("addon.jar").delete();
        new File("config.yml").delete();
        new File("generatorTemplate.yml").delete();
        deleteAll(new File("addons"));
        deleteAll(new File("database"));
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }


    private void deleteAll(File file) throws IOException
    {
        if (file.exists())
        {
            Files.walk(file.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onEnable()}.
     */
    @Test
    public void testOnEnableNoGameMode()
    {
        when(am.getGameModeAddons()).thenReturn(Collections.emptyList());
        addon.setState(State.LOADED);
        addon.onEnable();
        verify(plugin).logError(
            "[MagicCobblestoneGenerator] Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onEnable()}.
     */
    @Test
    public void testOnEnable()
    {
        addon.onLoad();
        when(plugin.isEnabled()).thenReturn(true);
        addon.setState(State.LOADED);
        addon.onEnable();
        verify(plugin).log("[MagicCobblestoneGenerator] Loading generator tiers from database...");
        verify(plugin).log("[MagicCobblestoneGenerator] Done");

        verify(plugin).logWarning(
            "[MagicCobblestoneGenerator] Level add-on not found so Magic Cobblestone Generator, some parts may not work!");
//      verify(plugin).logWarning("[MagicCobblestoneGenerator] Upgrades add-on not found so Magic Cobblestone Generator, some parts may not work!");
        verify(plugin).logWarning("[MagicCobblestoneGenerator] Vault plugin not found. Economy will not work!");
        //verify(plugin, never()).logError("[MagicCobblestoneGenerator] Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");

    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onLoad()}.
     */
    @Test
    public void testOnLoad()
    {
        addon.onLoad();
        assertTrue(new File("config.yml").exists());
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onReload()}.
     */
    @Test
    public void testOnReload()
    {
        addon.onLoad();
        addon.setState(State.LOADED);
        addon.onEnable();
        addon.onReload();
        verify(plugin).log(eq("[MagicCobblestoneGenerator] Magic Cobblestone Generator addon reloaded."));
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onReload()}.
     */
    @Test
    public void testOnReloadDisabled()
    {
        addon.onReload();
        verify(plugin).log(eq("No config.yml found. Creating it..."));
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getSettings()}.
     */
    @Test
    public void testGetSettings()
    {
        assertNull(addon.getSettings());
        addon.onLoad();
        assertNotNull(addon.getSettings());
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getGenerator()}.
     */
    @Test
    public void testGetGenerator()
    {
        assertNull(addon.getGenerator());
        testOnEnable();
        assertNotNull(addon.getGenerator());
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getAddonManager()}.
     */
    @Test
    public void testGetManager()
    {
        assertNull(addon.getAddonManager());
        testOnEnable();
        assertNotNull(addon.getAddonManager());
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getLevelAddon()}.
     */
    @Test
    public void testGetLevelAddon()
    {
        assertNull(addon.getLevelAddon());
    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#isLevelProvided()}.
     */
    @Test
    public void testIsLevelProvided()
    {
        assertFalse(addon.isLevelProvided());
    }


    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass()
    {
        // This has to be done beforeClass otherwise the tests will interfere with each other
        handler = mock(AbstractDatabaseHandler.class);
        // Database
        PowerMockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(handler);
    }

    private StoneGeneratorAddon addon;

    @Mock
    private BentoBox plugin;

    @Mock
    private AddonsManager am;

    @Mock
    private Logger logger;

    @Mock
    private GameModeAddon gameMode;

    @Mock
    private PlaceholdersManager placeholdersManager;

    @Mock
    private FlagsManager flagsManager;

    @Mock
    private Settings pluginSettings;

    @Mock
    private IslandWorldManager iwm;

    @Mock
    private static AbstractDatabaseHandler<Object> handler;
}
