package world.bentobox.magiccobblestonegenerator.panels.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.panels.CommonPanel;

/**
 * Tests for the prerequisite-generator ({@code REQUIRED_GENERATORS}) and
 * {@code ACTIVATE_ON_UNLOCK} buttons added to {@link GeneratorEditPanel} (#121).
 */
class GeneratorEditPanelTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private StoneGeneratorManager manager;
    @Mock
    private User panelUser;
    @Mock
    private GeneratorTierObject generatorTier;

    private GeneratorEditPanel panel;

    /** Minimal concrete parent panel exposing the (addon, user, world) constructor. */
    private static class ParentPanel extends CommonPanel {
        ParentPanel(StoneGeneratorAddon addon, User user, World world) {
            super(addon, user, world);
        }

        @Override
        protected void build() {
            // no-op
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(addon.getAddonManager()).thenReturn(manager);

        when(panelUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(panelUser.getPlayer()).thenReturn(mockPlayer);
        when(panelUser.getTranslation(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
        // Echo the reference key and append any placeholder values so we can assert on rendered names.
        when(panelUser.getTranslation(anyString(), any(), any())).thenAnswer((Answer<String>) inv -> {
            StringBuilder sb = new StringBuilder(inv.getArgument(0, String.class));
            Object[] args = inv.getArguments();
            for (int i = 1; i < args.length; i++) {
                sb.append('|').append(args[i]);
            }
            return sb.toString();
        });
        when(panelUser.getTranslationOrNothing(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        when(generatorTier.getFriendlyName()).thenReturn("Tier One");

        ParentPanel parent = new ParentPanel(addon, panelUser, world);
        Constructor<GeneratorEditPanel> c =
                GeneratorEditPanel.class.getDeclaredConstructor(CommonPanel.class, GeneratorTierObject.class);
        c.setAccessible(true);
        // Spy so the panel rebuild triggered by click handlers is a no-op (build() reads many unrelated fields).
        panel = Mockito.spy(c.newInstance(parent, generatorTier));
        Mockito.doNothing().when(panel).build();
    }

    private PanelItem button(String buttonName) throws Exception {
        Class<?> buttonEnum = Class.forName(GeneratorEditPanel.class.getName() + "$Button");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object value = Enum.valueOf((Class<Enum>) buttonEnum.asSubclass(Enum.class), buttonName);
        Method m = GeneratorEditPanel.class.getDeclaredMethod("createButton", buttonEnum, Locale.class);
        m.setAccessible(true);
        return (PanelItem) m.invoke(panel, value, Locale.ENGLISH);
    }

    private void click(PanelItem item, ClickType clickType) {
        item.getClickHandler().orElseThrow().onClick(mock(Panel.class), panelUser, clickType, 0);
    }

    // ---------------------------------------------------------------------
    // ACTIVATE_ON_UNLOCK
    // ---------------------------------------------------------------------

    @Test
    void testActivateOnUnlockButtonReflectsEnabledState() throws Exception {
        when(generatorTier.isActivateOnUnlock()).thenReturn(true);

        PanelItem item = button("ACTIVATE_ON_UNLOCK");

        assertTrue(item.isGlow());
    }

    @Test
    void testActivateOnUnlockButtonDisabledState() throws Exception {
        when(generatorTier.isActivateOnUnlock()).thenReturn(false);

        PanelItem item = button("ACTIVATE_ON_UNLOCK");

        assertFalse(item.isGlow());
    }

    @Test
    void testActivateOnUnlockClickToggles() throws Exception {
        when(generatorTier.isActivateOnUnlock()).thenReturn(false);

        click(button("ACTIVATE_ON_UNLOCK"), ClickType.LEFT);

        verify(generatorTier).setActivateOnUnlock(true);
        verify(manager).saveGeneratorTier(generatorTier);
    }

    // ---------------------------------------------------------------------
    // REQUIRED_GENERATORS
    // ---------------------------------------------------------------------

    @Test
    void testRequiredGeneratorsEmptyShowsNone() throws Exception {
        when(generatorTier.getRequiredGeneratorTiers()).thenReturn(new HashSet<>());

        // Description must include the ".none" line; building the button must not throw.
        PanelItem item = button("REQUIRED_GENERATORS");

        assertTrue(item.getDescription().stream()
                .anyMatch(line -> line.contains("required_generators.none")), item.getDescription().toString());
    }

    @Test
    void testRequiredGeneratorsResolvesNamesAndIdFallback() throws Exception {
        when(generatorTier.getRequiredGeneratorTiers())
                .thenReturn(new HashSet<>(List.of("known", "ghost")));
        GeneratorTierObject known = mock(GeneratorTierObject.class);
        when(known.getFriendlyName()).thenReturn("Known Gen");
        when(manager.getGeneratorByID("known")).thenReturn(known);
        when(manager.getGeneratorByID("ghost")).thenReturn(null);

        PanelItem item = button("REQUIRED_GENERATORS");

        List<String> lines = new ArrayList<>(item.getDescription());
        String joined = String.join("\n", lines);
        assertTrue(joined.contains("Known Gen"), joined);
        assertTrue(joined.contains("ghost"), joined);
    }

    @Test
    void testRequiredGeneratorsShiftClickResets() throws Exception {
        when(generatorTier.getRequiredGeneratorTiers())
                .thenReturn(new HashSet<>(List.of("known")));

        click(button("REQUIRED_GENERATORS"), ClickType.SHIFT_LEFT);

        verify(generatorTier).setRequiredGeneratorTiers(new HashSet<>());
        verify(manager).saveGeneratorTier(generatorTier);
    }

    @Test
    void testRequiredGeneratorsClickOpensSelector() throws Exception {
        when(generatorTier.getRequiredGeneratorTiers()).thenReturn(new HashSet<>());
        when(manager.getAllGeneratorTiers(any(World.class))).thenReturn(new ArrayList<>());

        // A plain click opens the MultiGeneratorSelector, which lists all generator tiers.
        click(button("REQUIRED_GENERATORS"), ClickType.LEFT);

        verify(manager, atLeastOnce()).getAllGeneratorTiers(any(World.class));
        // Nothing was reset by a plain click.
        verify(generatorTier, never()).setRequiredGeneratorTiers(any());
    }
}
