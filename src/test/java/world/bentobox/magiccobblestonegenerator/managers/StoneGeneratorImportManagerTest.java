package world.bentobox.magiccobblestonegenerator.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;

/**
 * Tests that the generator template importer reads the per-tier
 * {@code exhaustion-limit} key.
 *
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BentoBox.class, ItemParser.class, Registry.class })
@SuppressStaticInitializationFor("org.bukkit.Registry")
public class StoneGeneratorImportManagerTest {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private World world;
    @Mock
    private StoneGeneratorManager manager;

    private File dataFolder;
    private StoneGeneratorImportManager im;

    @Before
    public void setUp() throws Exception {
        // Temporary data folder for the addon.
        dataFolder = Files.createTempDirectory("mcg-import-test").toFile();
        when(addon.getDataFolder()).thenReturn(dataFolder);

        // Plugin / IWM wiring so the importer can resolve the game mode for the world.
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameModeAddon));
        AddonDescription desc = new AddonDescription.Builder("", "BSkyBlock", "1.0").build();
        when(gameModeAddon.getDescription()).thenReturn(desc);

        // The manager captures the imported tiers.
        when(addon.getAddonManager()).thenReturn(manager);

        // Icon parsing must not touch a real server.
        PowerMockito.mockStatic(ItemParser.class);
        when(ItemParser.parse(any())).thenReturn(mock(ItemStack.class));

        // getBiomeNameMap() streams Registry.BIOME - override it with an empty registry.
        @SuppressWarnings("unchecked")
        Registry<Biome> biomeRegistry = mock(Registry.class);
        when(biomeRegistry.stream()).thenReturn(Stream.empty());
        Whitebox.setInternalState(Registry.class, "BIOME", biomeRegistry);

        im = new StoneGeneratorImportManager(addon);
    }

    @After
    public void tearDown() throws IOException {
        if (dataFolder != null && dataFolder.exists()) {
            Files.walk(dataFolder.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Writes a template with an explicit {@code exhaustion-limit} on one tier and none
     * on another, then asserts the parsed values (explicit override vs. the -1 default).
     */
    @Test
    public void testImportExhaustionLimit() throws IOException {
        String yaml = "tiers:\n" +
                "  capped_generator:\n" +
                "    name: 'Capped Generator'\n" +
                "    icon: 'STONE:1'\n" +
                "    type: COBBLESTONE\n" +
                "    default: true\n" +
                "    exhaustion-limit: 500\n" +
                "    blocks:\n" +
                "      STONE: 100\n" +
                "  default_generator:\n" +
                "    name: 'Default Generator'\n" +
                "    icon: 'COBBLESTONE:1'\n" +
                "    type: COBBLESTONE\n" +
                "    default: true\n" +
                "    blocks:\n" +
                "      COBBLESTONE: 100\n";
        Files.write(new File(dataFolder, "test.yml").toPath(), yaml.getBytes(StandardCharsets.UTF_8));

        assertTrue(im.importFile(null, world, "test"));

        ArgumentCaptor<GeneratorTierObject> captor = ArgumentCaptor.forClass(GeneratorTierObject.class);
        verify(manager, atLeastOnce()).saveGeneratorTier(captor.capture());
        List<GeneratorTierObject> tiers = captor.getAllValues();

        GeneratorTierObject capped = tiers.stream()
                .filter(t -> t.getUniqueId().endsWith("capped_generator")).findFirst().orElse(null);
        GeneratorTierObject def = tiers.stream()
                .filter(t -> t.getUniqueId().endsWith("default_generator")).findFirst().orElse(null);

        assertNotNull("Capped generator tier should have been imported", capped);
        assertNotNull("Default generator tier should have been imported", def);

        // Explicit per-tier override is read from the template.
        assertEquals(500L, capped.getExhaustionLimit());
        // Absent key falls back to -1, meaning "use the global default".
        assertEquals(-1L, def.getExhaustionLimit());
    }
}
