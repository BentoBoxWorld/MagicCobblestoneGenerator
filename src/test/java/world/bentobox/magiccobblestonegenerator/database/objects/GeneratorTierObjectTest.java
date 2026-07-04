package world.bentobox.magiccobblestonegenerator.database.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import world.bentobox.magiccobblestonegenerator.CommonTestSetup;

/**
 * Tests for the requiredGeneratorTiers field of {@link GeneratorTierObject} (#88).
 */
class GeneratorTierObjectTest extends CommonTestSetup {

    @Test
    void testRequiredGeneratorTiersDefaultsToEmpty() {
        GeneratorTierObject tier = new GeneratorTierObject();
        assertTrue(tier.getRequiredGeneratorTiers().isEmpty());
    }

    @Test
    void testSetAndGetRequiredGeneratorTiers() {
        GeneratorTierObject tier = new GeneratorTierObject();
        tier.setRequiredGeneratorTiers(new HashSet<>(Set.of("gen1", "gen2")));
        assertEquals(Set.of("gen1", "gen2"), tier.getRequiredGeneratorTiers());
    }

    @Test
    void testCloneCopiesRequiredGeneratorTiersIndependently() {
        GeneratorTierObject tier = new GeneratorTierObject();
        tier.setUniqueId("tier");
        tier.setRequiredGeneratorTiers(new HashSet<>(Set.of("gen1")));

        GeneratorTierObject clone = tier.clone();
        assertEquals(Set.of("gen1"), clone.getRequiredGeneratorTiers());

        // The clone must hold an independent copy.
        clone.getRequiredGeneratorTiers().add("gen2");
        assertFalse(tier.getRequiredGeneratorTiers().contains("gen2"));
    }
}
