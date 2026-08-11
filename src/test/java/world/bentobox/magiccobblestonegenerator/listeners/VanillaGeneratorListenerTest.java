package world.bentobox.magiccobblestonegenerator.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.TestWorldSettings;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;

/**
 * Tests for {@link VanillaGeneratorListener}, focused on AcidIsland awareness (#173).
 */
class VanillaGeneratorListenerTest extends CommonTestSetup {

    /**
     * World settings that pretend to be AcidIsland ones, i.e. they have the acid damage getter that
     * is read reflectively.
     */
    public static class AcidWorldSettings extends TestWorldSettings {

        private final int acidDamage;

        public AcidWorldSettings(int acidDamage) {
            this.acidDamage = acidDamage;
        }

        @SuppressWarnings("unused")
        public int getAcidDamage() {
            return acidDamage;
        }
    }

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private StoneGeneratorManager manager;
    @Mock
    private MagicGenerator generator;
    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private Block block;
    @Mock
    private BlockState newState;

    private Settings settings;
    private VanillaGeneratorListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        settings = new Settings();
        // Skip the online member and range checks.
        settings.setOfflineGeneration(true);
        settings.setDefaultWorkingRange(0);

        when(addon.getSettings()).thenReturn(settings);
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getAddonManager()).thenReturn(manager);
        when(addon.getIslands()).thenReturn(im);
        when(addon.getGenerator()).thenReturn(generator);

        when(manager.canOperateInWorld(any())).thenReturn(true);
        when(manager.getGeneratorTier(any(), any(), any())).thenReturn(generatorTier);
        when(manager.canGenerateBlock(any(), any())).thenReturn(true);
        when(generator.processBlockReplacement(any(), any(), any())).thenReturn("DIAMOND_ORE");

        when(im.getIslandAt(any())).thenReturn(Optional.of(island));
        when(island.isAllowed(any())).thenReturn(true);

        // Why reporting reads player metadata.
        when(mockPlayer.getMetadata(anyString())).thenReturn(Collections.emptyList());

        // Lava pours into water: the water block turns into stone.
        when(block.isLiquid()).thenReturn(true);
        when(block.getType()).thenReturn(Material.WATER);
        when(block.getWorld()).thenReturn(world);
        when(block.getLocation()).thenReturn(location);
        when(newState.getType()).thenReturn(Material.STONE);

        listener = new VanillaGeneratorListener(addon);
    }

    /**
     * Makes the world belong to a game mode with the given name and world settings.
     */
    private void setGameMode(String name, WorldSettings worldSettings) {
        GameModeAddon gameMode = mock(GameModeAddon.class);
        AddonDescription description = mock(AddonDescription.class);
        when(description.getName()).thenReturn(name);
        when(gameMode.getDescription()).thenReturn(description);
        when(gameMode.getWorldSettings()).thenReturn(worldSettings);
        when(iwm.getAddon(any())).thenReturn(Optional.of(gameMode));
    }

    @Test
    void testStoneInAcidWaterIsNotReplaced() {
        setGameMode("AcidIsland", new AcidWorldSettings(10));

        listener.onBlockFormEvent(new BlockFormEvent(block, newState));

        // AcidIsland reverts this block, so the generator must not touch it.
        verify(newState, never()).setType(any());
    }

    @Test
    void testStoneInAcidWaterIsReplacedIfAcidDamageIsDisabled() {
        setGameMode("AcidIsland", new AcidWorldSettings(0));

        listener.onBlockFormEvent(new BlockFormEvent(block, newState));

        verify(newState).setType(Material.DIAMOND_ORE);
    }

    @Test
    void testStoneInAcidWaterIsReplacedIfOptionIsDisabled() {
        settings.setAcidIslandAware(false);
        setGameMode("AcidIsland", new AcidWorldSettings(10));

        listener.onBlockFormEvent(new BlockFormEvent(block, newState));

        verify(newState).setType(Material.DIAMOND_ORE);
    }

    @Test
    void testStoneInWaterIsReplacedInOtherGameModes() {
        setGameMode("BSkyBlock", new TestWorldSettings());

        listener.onBlockFormEvent(new BlockFormEvent(block, newState));

        verify(newState).setType(Material.DIAMOND_ORE);
    }

    @Test
    void testCobblestoneGeneratorStillWorksInAcidIsland() {
        setGameMode("AcidIsland", new AcidWorldSettings(10));

        // A classic generator forms cobblestone at the lava block, not at the water block.
        when(block.getType()).thenReturn(Material.LAVA);
        when(newState.getType()).thenReturn(Material.COBBLESTONE);

        listener.onBlockFormEvent(new BlockFormEvent(block, newState));

        verify(newState).setType(Material.DIAMOND_ORE);
    }
}
