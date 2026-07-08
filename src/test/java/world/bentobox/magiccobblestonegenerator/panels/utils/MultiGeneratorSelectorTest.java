package world.bentobox.magiccobblestonegenerator.panels.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

/**
 * Tests for {@link MultiGeneratorSelector} — the panel used to pick prerequisite generators.
 */
class MultiGeneratorSelectorTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private StoneGeneratorManager manager;
    @Mock
    private User panelUser;

    private final List<GeneratorTierObject> allTiers = new ArrayList<>();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(addon.getAddonManager()).thenReturn(manager);
        when(manager.getAllGeneratorTiers(any(World.class))).thenReturn(allTiers);

        when(panelUser.getTranslation(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
        when(panelUser.getTranslation(anyString(), any(), any()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));
        when(panelUser.getTranslationOrNothing(anyString()))
                .thenAnswer((Answer<String>) inv -> inv.getArgument(0, String.class));

        // PanelBuilder.build() opens the inventory for the player.
        when(panelUser.getPlayer()).thenReturn(mockPlayer);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private GeneratorTierObject tier(String id, String name, boolean deployed, boolean isDefault) {
        GeneratorTierObject t = mock(GeneratorTierObject.class);
        when(t.getUniqueId()).thenReturn(id);
        when(t.getFriendlyName()).thenReturn(name);
        when(t.isDeployed()).thenReturn(deployed);
        when(t.isDefaultGenerator()).thenReturn(isDefault);
        when(t.getGeneratorIcon()).thenReturn(new ItemStack(Material.STONE));
        return t;
    }

    @SuppressWarnings("unchecked")
    private MultiGeneratorSelector newSelector(GeneratorTierObject excluded,
            Set<String> selectedIds,
            Consumer<Set<String>> consumer) throws Exception {
        Constructor<MultiGeneratorSelector> c = MultiGeneratorSelector.class.getDeclaredConstructor(
                User.class, StoneGeneratorAddon.class, World.class,
                GeneratorTierObject.class, Set.class, Consumer.class);
        c.setAccessible(true);
        return c.newInstance(panelUser, addon, world, excluded, selectedIds, consumer);
    }

    @SuppressWarnings("unchecked")
    private List<GeneratorTierObject> field(MultiGeneratorSelector s, String name) throws Exception {
        Field f = MultiGeneratorSelector.class.getDeclaredField(name);
        f.setAccessible(true);
        return (List<GeneratorTierObject>) f.get(s);
    }

    private List<String> ids(List<GeneratorTierObject> list) {
        return list.stream().map(GeneratorTierObject::getUniqueId).toList();
    }

    private PanelItem invokeCreateButton(MultiGeneratorSelector s, String action) throws Exception {
        Class<?> actionEnum = Class.forName(MultiGeneratorSelector.class.getName() + "$Action");
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object value = Enum.valueOf((Class<Enum>) actionEnum.asSubclass(Enum.class), action);
        Method m = MultiGeneratorSelector.class.getDeclaredMethod("createButton", actionEnum);
        m.setAccessible(true);
        return (PanelItem) m.invoke(s, value);
    }

    // ---------------------------------------------------------------------
    // Constructor filtering
    // ---------------------------------------------------------------------

    @Test
    void testOnlyDeployedNonDefaultShown() throws Exception {
        allTiers.add(tier("a", "Alpha", true, false)); // shown
        allTiers.add(tier("b", "Beta", false, false)); // undeployed - hidden
        allTiers.add(tier("c", "Gamma", true, true)); // default - hidden

        MultiGeneratorSelector s = newSelector(null, new HashSet<>(), set -> {});

        assertEquals(List.of("a"), ids(field(s, "elements")));
    }

    @Test
    void testExcludedGeneratorNeverShown() throws Exception {
        GeneratorTierObject excluded = tier("a", "Alpha", true, false);
        allTiers.add(excluded);
        allTiers.add(tier("b", "Beta", true, false));

        MultiGeneratorSelector s = newSelector(excluded, new HashSet<>(), set -> {});

        assertEquals(List.of("b"), ids(field(s, "elements")));
    }

    @Test
    void testAlreadySelectedButUndeployedStillShown() throws Exception {
        // An undeployed/default generator that is already selected must remain visible so it can be deselected.
        allTiers.add(tier("a", "Alpha", false, false)); // undeployed but selected -> shown
        allTiers.add(tier("b", "Beta", true, true)); // default but selected -> shown
        allTiers.add(tier("c", "Gamma", false, false)); // undeployed, not selected -> hidden

        MultiGeneratorSelector s = newSelector(null, new LinkedHashSet<>(Arrays.asList("a", "b")), set -> {});

        assertEquals(List.of("a", "b"), ids(field(s, "elements")));
    }

    @Test
    void testElementsSortedByFriendlyName() throws Exception {
        allTiers.add(tier("a", "Zeta", true, false));
        allTiers.add(tier("b", "Alpha", true, false));
        allTiers.add(tier("c", "Mu", true, false));

        MultiGeneratorSelector s = newSelector(null, new HashSet<>(), set -> {});

        assertEquals(List.of("b", "c", "a"), ids(field(s, "elements")));
    }

    // ---------------------------------------------------------------------
    // updateFilters
    // ---------------------------------------------------------------------

    @Test
    void testBlankFilterShowsAllElements() throws Exception {
        allTiers.add(tier("a", "Alpha", true, false));
        allTiers.add(tier("b", "Beta", true, false));
        MultiGeneratorSelector s = newSelector(null, new HashSet<>(), set -> {});

        setSearch(s, "   ");
        invokeUpdateFilters(s);

        assertSame(field(s, "elements"), field(s, "filterElements"));
    }

    @Test
    void testNonBlankFilterMatchesNameCaseInsensitively() throws Exception {
        allTiers.add(tier("a", "Alpha", true, false));
        allTiers.add(tier("b", "Beta", true, false));
        MultiGeneratorSelector s = newSelector(null, new HashSet<>(), set -> {});

        setSearch(s, "ALPH");
        invokeUpdateFilters(s);

        assertEquals(List.of("a"), ids(field(s, "filterElements")));
    }

    @Test
    void testTurkishLocaleDoesNotBreakFilter() throws Exception {
        // Locale.ROOT folding must find "I" regardless of the JVM default locale.
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            allTiers.add(tier("a", "Iron", true, false));
            MultiGeneratorSelector s = newSelector(null, new HashSet<>(), set -> {});

            setSearch(s, "iron");
            invokeUpdateFilters(s);

            assertEquals(List.of("a"), ids(field(s, "filterElements")));
        } finally {
            Locale.setDefault(previous);
        }
    }

    private void setSearch(MultiGeneratorSelector s, String value) throws Exception {
        Field f = PagedSelector.class.getDeclaredField("searchString");
        f.setAccessible(true);
        f.set(s, value);
    }

    private void invokeUpdateFilters(MultiGeneratorSelector s) throws Exception {
        Method m = MultiGeneratorSelector.class.getDeclaredMethod("updateFilters");
        m.setAccessible(true);
        m.invoke(s);
    }

    // ---------------------------------------------------------------------
    // Element buttons
    // ---------------------------------------------------------------------

    @Test
    void testUnselectedElementButtonClickSelects() throws Exception {
        GeneratorTierObject a = tier("a", "Alpha", true, false);
        allTiers.add(a);
        Set<String> selected = new LinkedHashSet<>();
        MultiGeneratorSelector s = newSelector(null, selected, set -> {});

        PanelItem item = s.createElementButton(a);
        assertFalse(item.isGlow());

        clickElement(item);

        assertTrue(readSelectedIds(s).contains("a"));
    }

    @Test
    void testSelectedElementButtonGlowsAndClickDeselects() throws Exception {
        GeneratorTierObject a = tier("a", "Alpha", true, false);
        allTiers.add(a);
        MultiGeneratorSelector s = newSelector(null, new LinkedHashSet<>(List.of("a")), set -> {});

        PanelItem item = s.createElementButton(a);
        assertTrue(item.isGlow());

        clickElement(item);

        assertFalse(readSelectedIds(s).contains("a"));
    }

    private void clickElement(PanelItem item) {
        Panel panel = mock(Panel.class);
        Inventory inventory = mock(Inventory.class);
        when(panel.getInventory()).thenReturn(inventory);
        item.getClickHandler().orElseThrow().onClick(panel, panelUser, ClickType.LEFT, 10);
    }

    @SuppressWarnings("unchecked")
    private Set<String> readSelectedIds(MultiGeneratorSelector s) throws Exception {
        Field f = MultiGeneratorSelector.class.getDeclaredField("selectedIds");
        f.setAccessible(true);
        return (Set<String>) f.get(s);
    }

    // ---------------------------------------------------------------------
    // Accept / cancel buttons
    // ---------------------------------------------------------------------

    @Test
    void testAcceptButtonReturnsSelectedIds() throws Exception {
        allTiers.add(tier("a", "Alpha", true, false));
        Set<String> selected = new LinkedHashSet<>(List.of("a"));
        AtomicReference<Set<String>> result = new AtomicReference<>();
        MultiGeneratorSelector s = newSelector(null, selected, result::set);

        PanelItem accept = invokeCreateButton(s, "ACCEPT_GENERATOR");
        accept.getClickHandler().orElseThrow().onClick(mock(Panel.class), panelUser, ClickType.LEFT, 39);

        assertEquals(Set.of("a"), result.get());
    }

    @Test
    void testCancelButtonReturnsNull() throws Exception {
        AtomicReference<Set<String>> result = new AtomicReference<>(Set.of("sentinel"));
        MultiGeneratorSelector s = newSelector(null, new LinkedHashSet<>(), result::set);

        PanelItem cancel = invokeCreateButton(s, "RETURN");
        cancel.getClickHandler().orElseThrow().onClick(mock(Panel.class), panelUser, ClickType.LEFT, 44);

        assertNull(result.get());
    }

    // ---------------------------------------------------------------------
    // build() / open()
    // ---------------------------------------------------------------------

    @Test
    void testBuildDoesNotThrowWithSelection() throws Exception {
        allTiers.add(tier("a", "Alpha", true, false));
        allTiers.add(tier("b", "Beta", true, false));
        MultiGeneratorSelector s = newSelector(null, new LinkedHashSet<>(List.of("a")), set -> {});

        Method build = MultiGeneratorSelector.class.getDeclaredMethod("build");
        build.setAccessible(true);
        build.invoke(s);
    }

    @Test
    void testOpenBuildsPanelWithoutInvokingConsumer() {
        allTiers.add(tier("a", "Alpha", true, false));
        AtomicReference<Set<String>> result = new AtomicReference<>(Set.of("sentinel"));

        MultiGeneratorSelector.open(panelUser, addon, world, null, new HashSet<>(), result::set);

        // Opening the panel must not select or cancel anything yet.
        assertEquals(Set.of("sentinel"), result.get());
    }
}
