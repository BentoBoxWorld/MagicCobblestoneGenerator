package world.bentobox.magiccobblestonegenerator;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class})
public class StoneGeneratorAddonTest {
    
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

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getPlaceholdersManager()).thenReturn(placeholdersManager);
        when(plugin.getFlagsManager()).thenReturn(flagsManager);
        // Bukkit
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getLogger()).thenReturn(logger);
        
        // Addon
        addon = new StoneGeneratorAddon();
        File jFile = new File("addon.jar");
        List<String> lines = Arrays.asList("# MagicCobblestoneGenerator Addon Configuration", "uniqueId: config");
        Path path = Paths.get("config.yml");
        Files.write(path, lines, Charset.forName("UTF-8"));
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(path.toFile())) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                JarEntry entry = new JarEntry(path.toString());
                tempJarOutputStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        File dataFolder = new File("addons/MagicCobblestoneGenerator");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "MagicCobblestoneGenerator", "1.3").description("test").authors("BONNe").build();
        addon.setDescription(desc);
        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);
        CompositeCommand cmd = mock(CompositeCommand.class);
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd );
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
        deleteAll(new File("database"));

    }

    private void deleteAll(File file) throws IOException {
        if (file.exists()) {
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
    public void testOnEnableNoGameMode() {
       /**
    	*when(am.getGameModeAddons()).thenReturn(Collections.emptyList());
        *addon.setState(State.LOADED);
        *addon.onEnable();
        *verify(plugin).logError("[MagicCobblestoneGenerator] Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");
		*/
    }
    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onEnable()}.
     */
    @Test
    public void testOnEnable() {
       /**
        *addon.onLoad();
        *when(plugin.isEnabled()).thenReturn(true);
        *addon.setState(State.LOADED);
        *addon.onEnable();
        *verify(plugin).logWarning("[MagicCobblestoneGenerator] Level add-on not found so Magic Cobblestone Generator will not work correctly!");
        *verify(plugin).logWarning("[MagicCobblestoneGenerator] Economy plugin not found so money options will not work!");
        *verify(plugin, never()).logError("[MagicCobblestoneGenerator] Magic Cobblestone Generator could not hook into any GameMode so will not do anything!");
		*/
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onLoad()}.
     */
    @Test
    public void testOnLoad() {
       /**
        *addon.onLoad();
        *assertTrue(new File("config.yml").exists());
        */
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onReload()}.
     */
    @Test
    public void testOnReload() {
//        addon.onLoad();
//        addon.setState(State.LOADED);
//        addon.onEnable();
//        addon.onReload();
//        verify(logger).info(eq("Magic Cobblestone Generator addon reloaded."));
    }
    
    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#onReload()}.
     */
    @Test
    public void testOnReloadDisabled() {
//        addon.onReload();
//        verify(logger, never()).info(eq("Magic Cobblestone Generator addon reloaded."));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getSettings()}.
     */
    @Test
    public void testGetSettings() {
//        assertNull(addon.getSettings());
//        addon.onLoad();
//        assertNotNull(addon.getSettings());
        
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getGenerator()}.
     */
    @Test
    public void testGetGenerator() {
//        assertNull(addon.getGenerator());
//        testOnEnable();
//        assertNotNull(addon.getGenerator());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getManager()}.
     */
    @Test
    public void testGetManager() {
//        assertNull(addon.getManager());
//        testOnEnable();
//        assertNotNull(addon.getManager());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#getLevelAddon()}.
     */
    @Test
    public void testGetLevelAddon() {
        //assertNull(addon.getLevelAddon());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon#isLevelProvided()}.
     */
    @Test
    public void testIsLevelProvided() {
        //assertFalse(addon.isLevelProvided());
    }

}
