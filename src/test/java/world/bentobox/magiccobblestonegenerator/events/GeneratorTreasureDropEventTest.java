package world.bentobox.magiccobblestonegenerator.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;

/**
 * Tests for {@link GeneratorTreasureDropEvent}.
 */
@ExtendWith(MockitoExtension.class)
class GeneratorTreasureDropEventTest {

    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private Location location;
    @Mock
    private ItemStack itemStack;

    private GeneratorTreasureDropEvent event;

    @BeforeEach
    void setUp() {
        when(generatorTier.getFriendlyName()).thenReturn("Basic Tier");
        when(generatorTier.getUniqueId()).thenReturn("basic_tier");
        event = new GeneratorTreasureDropEvent(generatorTier, "island-1", location, itemStack);
    }

    @Test
    void testEventPopulatesFieldsFromArguments() {
        assertEquals("Basic Tier", event.getGenerator());
        assertEquals("basic_tier", event.getGeneratorID());
        assertEquals("island-1", event.getIslandUUID());
        assertSame(location, event.getLocation());
        assertSame(itemStack, event.getItemStack());
    }

    @Test
    void testEventIsNotCancelledByDefault() {
        assertFalse(event.isCancelled());
    }

    @Test
    void testSetCancelled() {
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    void testSetItemStackAllowsModification() {
        ItemStack other = mock(ItemStack.class);
        event.setItemStack(other);
        assertSame(other, event.getItemStack());
    }

    @Test
    void testSetters() {
        Location newLocation = mock(Location.class);
        event.setIslandUUID("island-2");
        event.setGenerator("Other");
        event.setGeneratorID("other");
        event.setLocation(newLocation);
        assertEquals("island-2", event.getIslandUUID());
        assertEquals("Other", event.getGenerator());
        assertEquals("other", event.getGeneratorID());
        assertSame(newLocation, event.getLocation());
    }

    @Test
    void testHandlers() {
        assertNotNull(event.getHandlers());
        assertNotNull(GeneratorTreasureDropEvent.getHandlerList());
    }
}
