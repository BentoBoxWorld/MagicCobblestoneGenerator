package world.bentobox.magiccobblestonegenerator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.managers.HooksManager;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;

/**
 * Tests for {@link CustomBlocks} block ID parsing and hook dispatch.
 */
class CustomBlocksTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private HooksManager hooksManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    void testGetProviderParsesKnownPrefixes() {
        assertEquals(CustomBlocks.Provider.ITEMS_ADDER, CustomBlocks.getProvider("itemsadder:ns:ruby_ore"));
        assertEquals(CustomBlocks.Provider.CRAFT_ENGINE, CustomBlocks.getProvider("craftengine:default:topaz"));
        assertEquals(CustomBlocks.Provider.ORAXEN, CustomBlocks.getProvider("oraxen:ruby_ore"));
        assertEquals(CustomBlocks.Provider.NEXO, CustomBlocks.getProvider("nexo:ruby_ore"));
        // Prefix matching is case-insensitive.
        assertEquals(CustomBlocks.Provider.ITEMS_ADDER, CustomBlocks.getProvider("ItemsAdder:ns:ruby_ore"));
    }

    @Test
    void testGetProviderRejectsVanillaAndUnknown() {
        assertNull(CustomBlocks.getProvider("COBBLESTONE"));
        assertNull(CustomBlocks.getProvider("minecraft:cobblestone"));
        assertNull(CustomBlocks.getProvider("someplugin:block"));
        assertNull(CustomBlocks.getProvider(null));
        assertNull(CustomBlocks.getProvider(":oops"));
    }

    @Test
    void testGetCustomIdStripsOnlyProviderPrefix() {
        assertEquals("ns:ruby_ore", CustomBlocks.getCustomId("itemsadder:ns:ruby_ore"));
        assertEquals("ruby_ore", CustomBlocks.getCustomId("oraxen:ruby_ore"));
        // Non-custom IDs pass through unchanged.
        assertEquals("COBBLESTONE", CustomBlocks.getCustomId("COBBLESTONE"));
        assertEquals("minecraft:cobblestone", CustomBlocks.getCustomId("minecraft:cobblestone"));
    }

    @Test
    void testMatchVanilla() {
        assertEquals(Material.COBBLESTONE, CustomBlocks.matchVanilla("COBBLESTONE"));
        assertEquals(Material.COBBLESTONE, CustomBlocks.matchVanilla("minecraft:cobblestone"));
        assertNull(CustomBlocks.matchVanilla("itemsadder:ns:ruby_ore"));
        assertNull(CustomBlocks.matchVanilla("NOT_A_REAL_MATERIAL"));
        assertNull(CustomBlocks.matchVanilla(null));
    }

    @Test
    void testHookAbsentMeansNotRegisteredAndNoPlacement() {
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getHooks()).thenReturn(hooksManager);
        when(hooksManager.getHook(anyString())).thenReturn(Optional.empty());

        assertFalse(CustomBlocks.isRegistered(addon, "itemsadder:ns:ruby_ore"));
        assertFalse(CustomBlocks.canPlace(addon, "itemsadder:ns:ruby_ore"));
        assertFalse(CustomBlocks.place(addon, "itemsadder:ns:ruby_ore", location));
    }

    @Test
    void testPlacementUnsupportedProvidersCannotPlace() {
        // Oraxen and Nexo are rejected before any hook lookup, as core hooks cannot place them yet.
        assertFalse(CustomBlocks.canPlace(addon, "oraxen:ruby_ore"));
        assertFalse(CustomBlocks.canPlace(addon, "nexo:ruby_ore"));
        assertFalse(CustomBlocks.place(addon, "oraxen:ruby_ore", location));
    }

    @Test
    void testVanillaIdsAreNeverPlaceableAsCustom() {
        assertFalse(CustomBlocks.canPlace(addon, "COBBLESTONE"));
        assertFalse(CustomBlocks.isCustom("COBBLESTONE"));
        assertTrue(CustomBlocks.isCustom("craftengine:default:topaz"));
    }

    @Test
    void testGetIconForVanillaBlock() {
        ItemStack icon = CustomBlocks.getIcon(addon, "COBBLESTONE");
        assertEquals(Material.COBBLESTONE, icon.getType());
    }

    @Test
    void testGetIconFallsBackWhenHookMissing() {
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getHooks()).thenReturn(hooksManager);
        when(hooksManager.getHook(anyString())).thenReturn(Optional.empty());

        ItemStack icon = CustomBlocks.getIcon(addon, "itemsadder:ns:ruby_ore");
        assertEquals(Material.NOTE_BLOCK, icon.getType());
    }

    @Test
    void testGetDisplayNameFallsBackToCustomId() {
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getHooks()).thenReturn(hooksManager);
        when(hooksManager.getHook(anyString())).thenReturn(Optional.empty());

        // Fallback icon has no display name, so the plugin-native ID is used.
        assertEquals("ns:ruby_ore", CustomBlocks.getDisplayName(addon, null, "itemsadder:ns:ruby_ore"));
    }
}
