package world.bentobox.magiccobblestonegenerator.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;

/**
 * Tests for {@link GeneratorPreBuyEvent}.
 */
@ExtendWith(MockitoExtension.class)
class GeneratorPreBuyEventTest {

    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private User user;

    private final UUID uuid = UUID.randomUUID();
    private final String islandId = UUID.randomUUID().toString();

    private GeneratorPreBuyEvent event;

    @BeforeEach
    void setUp() {
        when(generatorTier.getFriendlyName()).thenReturn("Basic Tier");
        when(generatorTier.getUniqueId()).thenReturn("basic_tier");
        when(user.getUniqueId()).thenReturn(uuid);
        event = new GeneratorPreBuyEvent(generatorTier, user, islandId);
    }

    @Test
    void testEventPopulatesFieldsFromArguments() {
        assertEquals("Basic Tier", event.getGenerator());
        assertEquals("basic_tier", event.getGeneratorID());
        assertEquals(uuid, event.getTargetPlayer());
        assertEquals(islandId, event.getIslandUUID());
    }

    @Test
    void testEventIsNotCancelledByDefault() {
        assertFalse(event.isCancelled());
    }

    @Test
    void testSetCancelled() {
        event.setCancelled(true);
        assertTrue(event.isCancelled());
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    void testSetters() {
        UUID newPlayer = UUID.randomUUID();
        event.setTargetPlayer(newPlayer);
        event.setIslandUUID("other-island");
        event.setGenerator("Other");
        event.setGeneratorID("other");
        assertEquals(newPlayer, event.getTargetPlayer());
        assertEquals("other-island", event.getIslandUUID());
        assertEquals("Other", event.getGenerator());
        assertEquals("other", event.getGeneratorID());
    }

    @Test
    void testHandlers() {
        assertNotNull(event.getHandlers());
        assertNotNull(GeneratorPreBuyEvent.getHandlerList());
    }
}
