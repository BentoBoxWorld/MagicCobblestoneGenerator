package world.bentobox.magiccobblestonegenerator.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

/**
 * Tests for the shared purchase-with-confirmation helper in {@link CommonPanel} (#109).
 */
class CommonPanelPurchaseTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private StoneGeneratorManager manager;
    @Mock
    private User panelUser;
    @Mock
    private GeneratorDataObject generatorData;
    @Mock
    private GeneratorTierObject generatorTier;

    private Settings settings;
    private TestPanel panel;

    /**
     * Minimal concrete CommonPanel that counts build() calls.
     */
    private static class TestPanel extends CommonPanel {
        int builds = 0;

        TestPanel(StoneGeneratorAddon addon, User user, org.bukkit.World world) {
            super(addon, user, world);
        }

        @Override
        protected void build() {
            this.builds++;
        }

        void purchase(Island island, GeneratorDataObject data, GeneratorTierObject tier) {
            this.purchaseGenerator(island, data, tier);
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        when(addon.getAddonManager()).thenReturn(manager);

        when(panelUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(panelUser.getTranslation(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
        when(panelUser.getTranslationOrNothing(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        when(generatorTier.getFriendlyName()).thenReturn("Basic Tier");

        panel = new TestPanel(addon, panelUser, world);
    }

    @Test
    void testSettingsBuyConfirmationDefaultsToTrue() {
        assertTrue(new Settings().isBuyConfirmation());
    }

    @Test
    void testCannotPurchaseDoesNotBuy() {
        when(manager.canPurchaseGenerator(any(), any(), any(), any())).thenReturn(false);

        panel.purchase(island, generatorData, generatorTier);

        verify(manager, never()).purchaseGenerator(any(), any(), any(), any());
        assertEquals(1, panel.builds);
    }

    @Test
    void testConfirmationDisabledPurchasesDirectly() {
        settings.setBuyConfirmation(false);
        when(manager.canPurchaseGenerator(any(), any(), any(), any())).thenReturn(true);

        try (MockedStatic<ConversationUtils> cu = Mockito.mockStatic(ConversationUtils.class)) {
            panel.purchase(island, generatorData, generatorTier);

            verify(manager).purchaseGenerator(panelUser, island, generatorData, generatorTier);
            cu.verifyNoInteractions();
        }
        assertEquals(1, panel.builds);
    }

    @Test
    void testConfirmationEnabledBuysOnlyAfterConfirm() {
        settings.setBuyConfirmation(true);
        when(manager.canPurchaseGenerator(any(), any(), any(), any())).thenReturn(true);

        List<Consumer<Boolean>> captured = new ArrayList<>();
        try (MockedStatic<ConversationUtils> cu = Mockito.mockStatic(ConversationUtils.class)) {
            cu.when(() -> ConversationUtils.createConfirmation(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        captured.add(inv.getArgument(0));
                        return null;
                    });

            panel.purchase(island, generatorData, generatorTier);

            // Nothing purchased until the player confirms.
            verify(manager, never()).purchaseGenerator(any(), any(), any(), any());
            assertEquals(1, captured.size());

            // Player confirms.
            captured.get(0).accept(true);
            verify(manager).purchaseGenerator(panelUser, island, generatorData, generatorTier);
        }
    }

    @Test
    void testConfirmationEnabledDeclineDoesNotBuy() {
        settings.setBuyConfirmation(true);
        when(manager.canPurchaseGenerator(any(), any(), any(), any())).thenReturn(true);

        List<Consumer<Boolean>> captured = new ArrayList<>();
        try (MockedStatic<ConversationUtils> cu = Mockito.mockStatic(ConversationUtils.class)) {
            cu.when(() -> ConversationUtils.createConfirmation(any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        captured.add(inv.getArgument(0));
                        return null;
                    });

            panel.purchase(island, generatorData, generatorTier);
            // Player declines.
            captured.get(0).accept(false);

            verify(manager, never()).purchaseGenerator(any(), any(), any(), any());
        }
    }
}
