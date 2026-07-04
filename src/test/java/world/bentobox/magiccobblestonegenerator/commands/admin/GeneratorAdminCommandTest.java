package world.bentobox.magiccobblestonegenerator.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.config.Settings;

class GeneratorAdminCommandTest extends CommonTestSetup {

    @Mock
    private CompositeCommand ac;
    @Mock
    private User user;
    @Mock
    private StoneGeneratorAddon addon;
    @Mock
    private ItemMeta itemMeta;

    private GeneratorAdminCommand gac;
    private Settings settings;
    private CompositeCommand ic;
    private CompositeCommand why;
    private CompositeCommand reset;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

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
        // Item factory (for itemstacks)
        when(itemFactory.getItemMeta(any())).thenReturn(itemMeta);
        // Locales
        when(user.getTranslation(anyString())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        // IWM friendly name for this world
        when(iwm.getFriendlyName(world)).thenReturn("BSkyBlock World");

        gac = new GeneratorAdminCommand(addon, ac);
        ic = gac.getSubCommand("import").get();
        why = gac.getSubCommand("why").get();
        reset = gac.getSubCommand("reset").get();
    }

    @Test
    void testGeneratorAdminCommand() {
        assertNotNull(gac);
    }

    @Test
    void testSetup() {
        assertEquals("admin.stone-generator", gac.getPermission());
        assertEquals("stone-generator.commands.admin.main.parameters", gac.getParameters());
        assertEquals("stone-generator.commands.admin.main.description", gac.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    @Test
    void testSetupImport() {
        assertEquals("admin.stone-generator", ic.getPermission());
        assertEquals("stone-generator.commands.admin.import.parameters", ic.getParameters());
        assertEquals("stone-generator.commands.admin.import.description", ic.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    @Test
    void testSetupWhy() {
        assertEquals("admin.stone-generator.why", why.getPermission());
        assertEquals("stone-generator.commands.admin.why.parameters", why.getParameters());
        assertEquals("stone-generator.commands.admin.why.description", why.getDescription());
        assertFalse(gac.isOnlyPlayer());
    }

    @Test
    void testExecuteUserStringListOfString() {
        assertTrue(gac.execute(user, "bskyblock", List.of()));
    }

    @Test
    void testExecuteUserStringListOfStringImport() {
        assertTrue(ic.execute(user, "bskyblock", List.of()));
    }

    @Test
    void testExecuteUserStringListOfStringHelp() {
        assertTrue(gac.execute(user, "bskyblock", List.of("help")));
        verify(user).sendMessage(
                "commands.help.header",
                "[label]",
                "BSkyBlock World");
        verify(user).getTranslationOrNothing(
                "stone-generator.commands.admin.main.parameters");
        verify(user).getTranslation(
                "stone-generator.commands.admin.main.description");
        verify(user, times(5)).isPlayer();
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator",
                "[description]",
                "stone-generator.commands.admin.main.description");
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator import",
                "[description]",
                "stone-generator.commands.admin.import.description");
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator why",
                "[description]",
                "stone-generator.commands.admin.why.description");
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator database",
                "[description]",
                "stone-generator.commands.admin.database.description");
        verify(user).sendMessage(
                "commands.help.syntax-no-parameters",
                "[usage]",
                "/null generator reset",
                "[description]",
                "stone-generator.commands.admin.reset.description");
        verify(user).sendMessage(
                "commands.help.end");
    }

    @Test
    void testExecuteUserStringListOfStringWhy() {
        assertFalse(why.execute(user, "bskyblock", List.of()));
        verify(user).sendMessage(
                "commands.help.header",
                "[label]",
                "BSkyBlock World");
        verify(user).getTranslationOrNothing(
                "stone-generator.commands.admin.why.parameters");
        verify(user).getTranslation(
                "stone-generator.commands.admin.why.description");
        verify(user).sendMessage(
                "commands.help.end");
    }

    @Test
    void testExecuteUserStringListOfStringWhyPlayer() {
        assertFalse(why.execute(user, "bskyblock", List.of("tastybento")));
        verify(user).sendMessage(
                "stone-generator.conversations.prefixgeneral.errors.player-is-not-owner");
    }

    @Test
    void testSetupReset() {
        assertEquals("admin.stone-generator.reset", reset.getPermission());
        assertEquals("stone-generator.commands.admin.reset.parameters", reset.getParameters());
        assertEquals("stone-generator.commands.admin.reset.description", reset.getDescription());
        assertFalse(reset.isOnlyPlayer());
    }

    @Test
    void testExecuteResetNoArgs() {
        assertFalse(reset.execute(user, "bskyblock", List.of()));
        verify(user).sendMessage(
                "commands.help.header",
                "[label]",
                "BSkyBlock World");
        verify(user).getTranslationOrNothing(
                "stone-generator.commands.admin.reset.parameters");
        verify(user).getTranslation(
                "stone-generator.commands.admin.reset.description");
        verify(user).sendMessage(
                "commands.help.end");
    }

    @Test
    void testExecuteResetPlayerNoIsland() {
        // Target resolves to a UUID but has no island in this world.
        assertFalse(reset.execute(user, "bskyblock", List.of("tastybento")));
        verify(user).sendMessage(
                "stone-generator.conversations.prefixgeneral.errors.player-has-no-island");
    }

}
