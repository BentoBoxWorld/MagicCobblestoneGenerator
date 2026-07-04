package world.bentobox.magiccobblestonegenerator.database.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import world.bentobox.magiccobblestonegenerator.CommonTestSetup;

/**
 * Tests for the requiredGeneratorTiers (#88) and activateOnUnlock (#106) fields of {@link GeneratorTierObject}.
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
    void testSetRequiredGeneratorTiersNullNormalizedToEmpty() {
        GeneratorTierObject tier = new GeneratorTierObject();
        tier.setRequiredGeneratorTiers(null);
        assertTrue(tier.getRequiredGeneratorTiers().isEmpty());
        // clone must not throw on a set that was normalized from null.
        assertTrue(tier.clone().getRequiredGeneratorTiers().isEmpty());
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

    @Test
    void testActivateOnUnlockDefaultsToFalse() {
        GeneratorTierObject tier = new GeneratorTierObject();
        assertFalse(tier.isActivateOnUnlock());
    }

    @Test
    void testSetAndGetActivateOnUnlock() {
        GeneratorTierObject tier = new GeneratorTierObject();
        tier.setActivateOnUnlock(true);
        assertTrue(tier.isActivateOnUnlock());
    }

    @Test
    void testCloneCopiesActivateOnUnlock() {
        GeneratorTierObject tier = new GeneratorTierObject();
        tier.setUniqueId("tier");
        tier.setActivateOnUnlock(true);

        GeneratorTierObject clone = tier.clone();
        assertTrue(clone.isActivateOnUnlock());
    }
}
