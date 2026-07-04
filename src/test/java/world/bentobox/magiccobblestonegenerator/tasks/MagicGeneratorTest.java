package world.bentobox.magiccobblestonegenerator.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.events.GeneratorTreasureDropEvent;

/**
 * Tests for {@link MagicGenerator}, focused on the treasure drop event (#140).
 */
class MagicGeneratorTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private ItemStack treasure;

    private MagicGenerator generator;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Block chance map with a single guaranteed material.
        TreeMap<Double, Material> blockMap = new TreeMap<>();
        blockMap.put(1.0, Material.COBBLESTONE);
        when(generatorTier.getBlockChanceMap()).thenReturn(blockMap);
        when(generatorTier.getMinHeight()).thenReturn(-64);
        when(generatorTier.getMaxHeight()).thenReturn(320);
        when(generatorTier.getMaterialHeightRange(any())).thenReturn(null);
        when(generatorTier.getUniqueId()).thenReturn("basic_tier");
        when(generatorTier.getFriendlyName()).thenReturn("Basic Tier");

        // Guaranteed treasure drop.
        TreeMap<Double, ItemStack> treasureMap = new TreeMap<>();
        treasureMap.put(1.0, treasure);
        when(treasure.clone()).thenReturn(treasure);
        when(generatorTier.getMaxTreasureAmount()).thenReturn(1);
        when(generatorTier.getTreasureChance()).thenReturn(1.0);
        when(generatorTier.getTreasureItemChanceMap()).thenReturn(treasureMap);

        // Location returns the mocked world.
        when(location.getWorld()).thenReturn(world);

        generator = new MagicGenerator(addon);
    }

    @Test
    void testProcessBlockReplacementReturnsMaterial() {
        Material result = generator.processBlockReplacement(generatorTier, location, island);
        assertEquals(Material.COBBLESTONE, result);
    }

    @Test
    void testTreasureDropFiresEventAndDrops() {
        generator.processBlockReplacement(generatorTier, location, island);
        verify(pim).callEvent(any(GeneratorTreasureDropEvent.class));
        verify(world).dropItemNaturally(location, treasure);
    }

    @Test
    void testCancelledTreasureDropEventPreventsDrop() {
        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof GeneratorTreasureDropEvent drop) {
                drop.setCancelled(true);
            }
            return null;
        }).when(pim).callEvent(any(GeneratorTreasureDropEvent.class));

        Material result = generator.processBlockReplacement(generatorTier, location, island);

        // Block is still generated, but no treasure is dropped.
        assertEquals(Material.COBBLESTONE, result);
        verify(world, never()).dropItemNaturally(any(Location.class), any(ItemStack.class));
    }

    @Test
    void testListenerReplacesTreasureItem() {
        ItemStack replacement = mock(ItemStack.class);
        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof GeneratorTreasureDropEvent drop) {
                drop.setItemStack(replacement);
            }
            return null;
        }).when(pim).callEvent(any(GeneratorTreasureDropEvent.class));

        generator.processBlockReplacement(generatorTier, location, island);

        // The modified item is the one that gets dropped.
        verify(world).dropItemNaturally(location, replacement);
        verify(world, never()).dropItemNaturally(location, treasure);
    }
}
