package world.bentobox.magiccobblestonegenerator.panels;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

/**
 * Tests for the requirement-description rendering in {@link CommonPanel}, covering the
 * OneBlock phase, block-count and prerequisite-generator requirements (#117, #121).
 */
class CommonPanelDescriptionTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private StoneGeneratorManager manager;

    private User panelUser;
    private DescPanel panel;

    /**
     * Concrete CommonPanel exposing the protected description helper and swallowing build().
     */
    private static class DescPanel extends CommonPanel {
        DescPanel(StoneGeneratorAddon addon, User user, org.bukkit.World world) {
            super(addon, user, world);
        }

        @Override
        protected void build() {
            // no-op
        }

        List<String> describe(GeneratorTierObject generator, boolean isUnlocked) {
            return this.generateGeneratorDescription(generator, false, isUnlocked, false, 0L);
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        Settings settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        when(addon.getAddonManager()).thenReturn(manager);
        when(addon.isLevelProvided()).thenReturn(false);

        // Echo translation references and interpolate placeholder pairs (flattening varargs) so we can
        // assert on what the requirement lore renders.
        Answer<Object> translating = inv -> {
            String method = inv.getMethod().getName();
            if (method.equals("getLocale")) {
                return java.util.Locale.ENGLISH;
            }
            if (method.equals("getTranslation") || method.equals("getTranslationOrNothing")) {
                List<Object> flat = new ArrayList<>();
                for (Object arg : inv.getArguments()) {
                    if (arg instanceof Object[] array) {
                        flat.addAll(Arrays.asList(array));
                    } else {
                        flat.add(arg);
                    }
                }
                if (flat.isEmpty()) {
                    return "";
                }
                StringBuilder sb = new StringBuilder(String.valueOf(flat.get(0)));
                for (int i = 1; i + 1 < flat.size(); i += 2) {
                    sb.append('|').append(flat.get(i)).append('=').append(flat.get(i + 1));
                }
                return sb.toString();
            }
            return Mockito.RETURNS_DEFAULTS.answer(inv);
        };
        panelUser = mock(User.class, translating);

        panel = new DescPanel(addon, panelUser, world);
    }

    @Test
    void testPhaseRequirementShownWhenLocked() {
        GeneratorTierObject generator = new GeneratorTierObject();
        generator.setRequiredPhase("Nether");

        String description = String.join("\n", panel.describe(generator, false));

        assertTrue(description.contains("phase|[name]=Nether"), description);
    }

    @Test
    void testBlockCountRequirementShownWhenLocked() {
        GeneratorTierObject generator = new GeneratorTierObject();
        generator.setRequiredBlockCount(500);

        String description = String.join("\n", panel.describe(generator, false));

        assertTrue(description.contains("block-count|[number]=500"), description);
    }

    @Test
    void testRequiredGeneratorsResolveNamesAndFallBackToId() {
        GeneratorTierObject known = mock(GeneratorTierObject.class);
        when(known.getFriendlyName()).thenReturn("Known Gen");
        when(manager.getGeneratorByID("known")).thenReturn(known);
        when(manager.getGeneratorByID("ghost")).thenReturn(null);

        GeneratorTierObject generator = new GeneratorTierObject();
        generator.setRequiredGeneratorTiers(new java.util.LinkedHashSet<>(List.of("known", "ghost")));

        String description = String.join("\n", panel.describe(generator, false));

        // Resolved name for the known generator, raw id fallback for the deleted one.
        assertTrue(description.contains("[generator]=Known Gen"), description);
        assertTrue(description.contains("[generator]=ghost"), description);
    }

    @Test
    void testRequirementsHiddenWhenUnlocked() {
        GeneratorTierObject generator = new GeneratorTierObject();
        generator.setRequiredPhase("Nether");
        generator.setRequiredBlockCount(500);
        generator.setRequiredGeneratorTiers(new java.util.LinkedHashSet<>(List.of("known")));

        String description = String.join("\n", panel.describe(generator, true));

        // When already unlocked, none of the requirement placeholders carry values.
        assertFalse(description.contains("[name]=Nether"), description);
        assertFalse(description.contains("[number]=500"), description);
    }
}
