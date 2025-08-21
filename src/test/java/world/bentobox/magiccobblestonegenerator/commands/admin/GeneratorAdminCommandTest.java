package world.bentobox.magiccobblestonegenerator.commands.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, DatabaseSetup.class, RanksManager.class })
public class GeneratorAdminCommandTest {

    @Mock
    private BentoBox plugin;
    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private LocalesManager lm;
    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private World world;
    @Mock
    private IslandsManager im;
    @Mock
    private @Nullable Island island;
    @Mock
    private IslandWorldManager iwm;

    private GeneratorAdminCommand gac;
    private Settings settings;
    @Mock
    private ItemMeta itemMeta;
    @Mock
    private Player mockPlayer;
    @Mock
    private PlayersManager pm;
    private CompositeCommand ic;
    private CompositeCommand why;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        world.bentobox.bentobox.Settings bbSettings = new world.bentobox.bentobox.Settings();
        when(plugin.getSettings()).thenReturn(bbSettings);
        when(plugin.getPlayers()).thenReturn(pm);
        // Parent command has no aliases
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());
        when(ac.getWorld()).thenReturn(world);
        when(ac.getAddon()).thenReturn(addon);
        // Addon
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);
        when(addon.getIslands()).thenReturn(im);
        // user
        when(user.getLocale()).thenReturn(Locale.ENGLISH);
        // Target bill - default target. Non Op, online, no ban prevention permission
        UUID uuid = UUID.randomUUID();
        when(pm.getUUID(anyString())).thenReturn(uuid);
        when(mockPlayer.getName()).thenReturn("bill");
        when(mockPlayer.getDisplayName()).thenReturn("&Cbill");
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        when(mockPlayer.isOp()).thenReturn(false);
        when(mockPlayer.isOnline()).thenReturn(true);
        when(mockPlayer.hasPermission(anyString())).thenReturn(false);
        User.getInstance(mockPlayer);
        when(user.getPlayer()).thenReturn(mockPlayer);
        // Mock item factory (for itemstacks)
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);
        // Locales
        LocalesManager lm = mock(LocalesManager.class);
        when(lm.get(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));
        when(plugin.getLocalesManager()).thenReturn(lm);
        PlaceholdersManager phm = mock(PlaceholdersManager.class);
        when(phm.replacePlaceholders(any(), any())).thenAnswer(invocation -> invocation.getArgument(1, String.class));
        // Placeholder manager
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        // IWM
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getFriendlyName(world)).thenReturn("BSkyBlock World");

        gac = new GeneratorAdminCommand(addon, ac);
        ic = gac.getSubCommand("import").get();
        why = gac.getSubCommand("why").get();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
        deleteAll(new File("database"));
        deleteAll(new File("database_backup"));
    }

    private void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }

    }


    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand#GeneratorAdminCommand(world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon, world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testGeneratorAdminCommand() {
        assertNotNull(gac);
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("admin.stone-generator", gac.getPermission());
        assertEquals("stone-generator.commands.admin.main.parameters", gac.getParameters());
        assertEquals("stone-generator.commands.admin.main.description", gac.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand.GeneratorImportCommand#setup()}.
     */
    @Test
    public void testSetupImport() {
        assertEquals("admin.stone-generator", ic.getPermission());
        assertEquals("stone-generator.commands.admin.import.parameters", ic.getParameters());
        assertEquals("stone-generator.commands.admin.import.description", ic.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand.GeneratorWhyCommand#setup()}.
     */
    @Test
    public void testSetupWhy() {
        assertEquals("admin.stone-generator.why", why.getPermission());
        assertEquals("stone-generator.commands.admin.why.parameters", why.getParameters());
        assertEquals("stone-generator.commands.admin.why.description", why.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        assertTrue(gac.execute(user, "bskyblock", List.of()));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringImport() {
        assertTrue(ic.execute(user, "bskyblock", List.of()));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringHelp() {
        assertTrue(gac.execute(user, "bskyblock", List.of("help")));
        verify(user).sendMessage(
                "commands.help.header",
                "[label]",
                "BSkyBlock World");
        verify(user).getTranslationOrNothing(
                "stone-generator.commands.admin.main.parameters"
                );
        verify(user).getTranslation(
                "stone-generator.commands.admin.main.description"
                );
        verify(user, times(4)).isPlayer();
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator",
                "[description]",
                "stone-generator.commands.admin.main.description"
                );
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator import",
                "[description]",
                "stone-generator.commands.admin.import.description"
                );
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator why",
                "[description]",
                "stone-generator.commands.admin.why.description"
                );
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator database",
                "[description]",
                "stone-generator.commands.admin.database.description"
                );
        verify(user).sendMessage(
                "commands.help.end"
                );
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand.GeneratorWhyCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringWhy() {
        assertFalse(why.execute(user, "bskyblock", List.of()));
        verify(user).sendMessage(
                "commands.help.header",
                "[label]",
                "BSkyBlock World");
        verify(user).getTranslationOrNothing(
                "stone-generator.commands.admin.why.parameters"
                );
        verify(user).getTranslation(
                "stone-generator.commands.admin.why.description"
                );
        verify(user).sendMessage(
                "commands.help.end"
                );
    }
    
    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.commands.admin.GeneratorAdminCommand.GeneratorWhyCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringWhyPlayer() {
        assertFalse(why.execute(user, "bskyblock", List.of("tastybento")));
        verify(user).sendMessage(
                "stone-generator.conversations.prefixgeneral.errors.player-is-not-owner"
                );
    }

}
