package world.bentobox.magiccobblestonegenerator.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;

/**
 * @author tastybento
 *
 */
public class SettingsTest {
    
    @Mock
    private BentoBox plugin;

    @Mock
    private StoneGeneratorAddon addon;

    private Settings s;
    private YamlConfiguration config;

    private String skygrid = "SkyGrid";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        config = new YamlConfiguration();
        File file = new File("src/main/resources", "config.yml");
        config.load(file);
        addon = mock(StoneGeneratorAddon.class);
        when(addon.getConfig()).thenReturn(config);
        s = new Settings(addon);
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#isOfflineGeneration()}.
     */
    @Test
    public void testIsOfflineGeneration() {
        assertFalse(s.isOfflineGeneration());
    }
    
    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#isOfflineGeneration()}.
     */
    @Test
    public void testIsOfflineGenerationOnline() {
        config.set("offline-generation", true);
        s = new Settings(addon);
        assertTrue(s.isOfflineGeneration());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#getDisabledGameModes()}.
     */
    @Test
    public void testGetDisabledGameModes() {
        assertTrue(s.getDisabledGameModes().isEmpty());
    }
    
    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#getDisabledGameModes()}.
     */
    @Test
    public void testGetDisabledGameModesSomeDisabled() {
        config.set("disabled-gamemodes", Collections.singletonList("BSkyBlock"));
        s = new Settings(addon);
        assertFalse(s.getDisabledGameModes().isEmpty());
        assertTrue(s.getDisabledGameModes().contains("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#getDefaultGeneratorTierMap()}.
     */
    @Test
    public void testGetDefaultGeneratorTierMap() {
        assertEquals(7, s.getDefaultGeneratorTierMap().size());
        assertTrue(s.getDefaultGeneratorTierMap().containsKey("default"));
        for (int i = 1; i < 7; i++) {
            assertTrue(s.getDefaultGeneratorTierMap().containsKey("tier" + String.valueOf(i)));
        }
        assertEquals("Stone Level", s.getDefaultGeneratorTierMap().get("default").getName());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.config.Settings#getAddonGeneratorTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonGeneratorTierMap() {
        assertEquals(1, s.getAddonGeneratorTierMap(skygrid).size());
        assertEquals("Diamond Level", s.getAddonGeneratorTierMap(skygrid).get("default").getName());
    }

}
