package world.bentobox.magiccobblestonegenerator.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject.GeneratorType;

/**
 * @author tastybento
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, Config.class, DatabaseSetup.class })
public class StoneGeneratorManagerTest {

    private static AbstractDatabaseHandler<Object> h;
    @Mock
    private BentoBox plugin;
    @Mock
    private Settings settings;
    @Mock
    private StoneGeneratorAddon addon;
    // DUT
    private StoneGeneratorManager sgm;
    @Mock
    private @Nullable World world;
    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private User user;
    private UUID uuid;
    @Mock
    private GeneratorBundleObject generatorBundle;
    @Mock
    private GeneratorDataObject generatorData;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private Location location;
    @Mock
    private @Nullable Island island;
    private world.bentobox.magiccobblestonegenerator.config.Settings s;
    @Mock
    private IslandsManager im;
    @Mock
    private IslandWorldManager iwm;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
	// This has to be done beforeClass otherwise the tests will interfere with each
	// other
	h = mock(AbstractDatabaseHandler.class);
	// Database
	PowerMockito.mockStatic(DatabaseSetup.class);
	DatabaseSetup dbSetup = mock(DatabaseSetup.class);
	when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
	when(dbSetup.getHandler(any())).thenReturn(h);
	when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
    }

    @After
    public void tearDown() throws IOException {
	User.clearUsers();
	Mockito.framework().clearInlineMocks();
	deleteAll(new File("database"));
	deleteAll(new File("database_backup"));
	new File("config.yml").delete();
	deleteAll(new File("addons"));
	deleteAll(new File("panels"));
    }

    private void deleteAll(File file) throws IOException {
	if (file.exists()) {
	    Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	// Set up plugin
	Whitebox.setInternalState(BentoBox.class, "instance", plugin);
	when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
	when(addon.getPlugin()).thenReturn(plugin);

	// The database type has to be created one line before the thenReturn() to work!
	DatabaseType value = DatabaseType.JSON;
	when(plugin.getSettings()).thenReturn(settings);
	when(settings.getDatabaseType()).thenReturn(value);
	// Player
	Player p = mock(Player.class);
	// Sometimes use Mockito.withSettings().verboseLogging()
	when(user.isOp()).thenReturn(false);
	uuid = UUID.randomUUID();
	when(user.getUniqueId()).thenReturn(uuid);
	when(user.getPlayer()).thenReturn(p);
	when(user.getName()).thenReturn("tastybento");
	User.setPlugin(plugin);

	// Generator Tier
	when(this.generatorTier.getFriendlyName()).thenReturn("Basic Tier");
	when(this.generatorTier.getUniqueId()).thenReturn(uuid.toString());

	// Generator Bundle
	when(this.generatorBundle.getFriendlyName()).thenReturn("Basic Bundle");
	when(this.generatorBundle.getUniqueId()).thenReturn(uuid.toString());

	// Locales
	// Return the reference (USE THIS IN THE FUTURE)
	when(user.getTranslation(anyString()))
		.thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));
	when(user.getTranslation(anyString(), anyString(), anyString()))
		.thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

	// Settings
	s = new world.bentobox.magiccobblestonegenerator.config.Settings();
	when(addon.getSettings()).thenReturn(s);

	// Addon Description
	AddonDescription desc = new AddonDescription.Builder("", "MagicCobbleGenerator", "1.2.3").build();
	when(addon.getDescription()).thenReturn(desc);
	when(gameModeAddon.getDescription()).thenReturn(desc);

	// Island manager
	when(addon.getIslands()).thenReturn(im);

	// IWM
	when(plugin.getIWM()).thenReturn(iwm);

	// Location
	when(location.getWorld()).thenReturn(world);

	// RanksManager
	RanksManager rm = new RanksManager();
	when(plugin.getRanksManager()).thenReturn(rm);

	sgm = new StoneGeneratorManager(addon);

	// Addon Manager
	when(addon.getAddonManager()).thenReturn(sgm);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#StoneGeneratorManager(world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon)}.
     */
    @Test
    public void testStoneGeneratorManager() {
	assertNotNull(sgm);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#addWorld(org.bukkit.World)}.
     */
    @Test
    public void testAddWorld() {
	sgm.addWorld(world);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#reload()}.
     * 
     * @throws IntrospectionException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void testReload() throws InstantiationException, IllegalAccessException, InvocationTargetException,
	    ClassNotFoundException, NoSuchMethodException, IntrospectionException {
	sgm.reload();
	verify(addon).log("Loading generator tiers from database...");
	verify(h, atLeast(1)).loadObjects();
	verify(addon).log("Done");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#load()}.
     * 
     * @throws IntrospectionException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void testLoad() throws InstantiationException, IllegalAccessException, InvocationTargetException,
	    ClassNotFoundException, NoSuchMethodException, IntrospectionException {
	sgm.load();
	verify(addon).log("Loading generator tiers from database...");
	verify(h, atLeast(1)).loadObjects();
	verify(addon).log("Done");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorTier() {
	assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
	verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.generator-loaded");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorTierOverwrite() {
	assertTrue(sgm.loadGeneratorTier(generatorTier, true, user));
	verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.generator-loaded");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorTierOverwriteFail() {
	assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
	// Second time, it should fail because it is in the cache now, and overwrite is
	// false
	assertFalse(sgm.loadGeneratorTier(generatorTier, false, user));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorTierOverwritePass() {
	assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
	// Second time, it should pass because it is in the cache, but overwrite is
	// true
	assertTrue(sgm.loadGeneratorTier(generatorTier, true, user));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorBundle() {
	sgm.loadGeneratorBundle(generatorBundle, false, user);
	verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.bundle-loaded");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorBundleOverwrite() {
	sgm.loadGeneratorBundle(generatorBundle, true, user);
	verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.bundle-loaded");
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorBundleOverwriteFail() {
	assertTrue(sgm.loadGeneratorBundle(generatorBundle, false, user));
	// Second time, it should fail because it is in the cache now, and overwrite is
	// false
	assertFalse(sgm.loadGeneratorBundle(generatorBundle, false, user));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadGeneratorBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject, boolean, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testLoadGeneratorBundleOverwritePass() {
	assertTrue(sgm.loadGeneratorBundle(generatorBundle, false, user));
	// Second time, it should pass because it is in the cache, but overwrite is
	// true
	assertTrue(sgm.loadGeneratorBundle(generatorBundle, true, user));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#saveGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testSaveGeneratorTier() {
	sgm.saveGeneratorTier(generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#saveGeneratorBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject)}.
     */
    @Test
    public void testSaveGeneratorBundle() {
	sgm.saveGeneratorBundle(generatorBundle);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#saveGeneratorData(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject)}.
     */
    @Test
    public void testSaveGeneratorData() {
	sgm.saveGeneratorData(generatorData);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#save()}.
     */
    @Test
    public void testSave() {
	sgm.save();
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeGameModeGenerators()}.
     */
    @Test
    public void testWipeGameModeGenerators() {
	sgm.wipeGameModeGenerators(gameModeAddon);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeIslandData(java.util.Optional)}.
     */
    @Test
    public void testWipeIslandData() {
	sgm.wipeIslandData(gameModeAddon);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeGeneratorTier(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testWipeGeneratorTier() {
	sgm.wipeGeneratorTier(generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getGeneratorByID(java.lang.String)}.
     */
    @Test
    public void testGetGeneratorByID() {
	assertNull(sgm.getGeneratorByID(uuid.toString()));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getGeneratorTier(world.bentobox.bentobox.database.objects.Island, org.bukkit.Location, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject.GeneratorType)}.
     */
    @Test
    public void testGetGeneratorTier() {
	assertNull(sgm.getGeneratorTier(island, location, GeneratorType.ANY));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getAllGeneratorTiers(org.bukkit.World)}.
     */
    @Test
    public void testGetAllGeneratorTiers() {
	assertTrue(sgm.getAllGeneratorTiers(world).isEmpty());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getIslandGeneratorTiers(org.bukkit.World, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetIslandGeneratorTiersWorldUser() {
	assertTrue(sgm.getIslandGeneratorTiers(world, user).isEmpty());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getIslandGeneratorTiers(org.bukkit.World, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject)}.
     */
    @Test
    public void testGetIslandGeneratorTiersWorldGeneratorDataObject() {
	assertTrue(sgm.getIslandGeneratorTiers(world, generatorData).isEmpty());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#findDefaultGeneratorList(org.bukkit.World)}.
     */
    @Test
    public void testFindDefaultGeneratorList() {
	assertTrue(sgm.findDefaultGeneratorList(world).isEmpty());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getAllGeneratorBundles(org.bukkit.World)}.
     */
    @Test
    public void testGetAllGeneratorBundles() {
	assertTrue(sgm.getAllGeneratorBundles(world).isEmpty());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getBundleById(java.lang.String)}.
     */
    @Test
    public void testGetBundleById() {
	assertNull(sgm.getBundleById(uuid.toString()));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeBundle(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject)}.
     */
    @Test
    public void testWipeBundle() {
	sgm.wipeBundle(generatorBundle);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#loadUserIslands(java.util.UUID)}.
     */
    @Test
    public void testLoadUserIslands() {
	sgm.loadUserIslands(uuid);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#validateIslandData(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testValidateIslandData() {
	assertNull(sgm.validateIslandData(island));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#checkGeneratorUnlockStatus(world.bentobox.bentobox.database.objects.Island, world.bentobox.bentobox.api.user.User, java.lang.Long)}.
     */
    @Test
    public void testCheckGeneratorUnlockStatus() {
	sgm.checkGeneratorUnlockStatus(island, user, 10L);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getGeneratorData(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetGeneratorDataIsland() {
	assertNotNull(sgm.getGeneratorData(island));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getGeneratorData(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testGetGeneratorDataUserWorld() {
	assertNull(sgm.getGeneratorData(user, world));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#unlockGenerator(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testUnlockGenerator() {
	sgm.unlockGenerator(generatorData, user, island, generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#deactivateGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testDeactivateGenerator() {
	sgm.deactivateGenerator(user, generatorData, generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#canActivateGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testCanActivateGenerator() {
	assertFalse(sgm.canActivateGenerator(user, island, generatorData, generatorTier));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#activateGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testActivateGeneratorUserIslandGeneratorDataObjectGeneratorTierObject() {
	sgm.activateGenerator(user, island, generatorData, generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#activateGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean)}.
     */
    @Test
    public void testActivateGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBoolean() {
	sgm.activateGenerator(user, island, generatorData, generatorTier, false);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#activateGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean)}.
     */
    @Test
    public void testActivateGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBooleanBypass() {
	sgm.activateGenerator(user, island, generatorData, generatorTier, true);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#canPurchaseGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testCanPurchaseGenerator() {
	assertFalse(sgm.canPurchaseGenerator(user, island, generatorData, generatorTier));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#purchaseGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject)}.
     */
    @Test
    public void testPurchaseGeneratorUserIslandGeneratorDataObjectGeneratorTierObject() {
	sgm.purchaseGenerator(user, island, generatorData, generatorTier);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#purchaseGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean)}.
     */
    @Test
    public void testPurchaseGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBoolean() {
	sgm.purchaseGenerator(user, island, generatorData, generatorTier, false);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#purchaseGenerator(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject, world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject, boolean)}.
     */
    @Test
    public void testPurchaseGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBooleanBypass() {
	sgm.purchaseGenerator(user, island, generatorData, generatorTier, true);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeGeneratorData(java.lang.String)}.
     */
    @Test
    public void testWipeGeneratorDataString() {
	sgm.wipeGeneratorData(uuid.toString());
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#wipeGeneratorData(world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject)}.
     */
    @Test
    public void testWipeGeneratorDataGeneratorDataObject() {
	sgm.wipeGeneratorData(generatorData);
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#canOperateInWorld(org.bukkit.World)}.
     */
    @Test
    public void testCanOperateInWorld() {
	assertFalse(sgm.canOperateInWorld(world));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#isMembersOnline(org.bukkit.Location)}.
     */
    @Test
    public void testIsMembersOnline() {
	assertFalse(sgm.isMembersOnline(location));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getIslandLevel(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetIslandLevelIsland() {
	assertEquals(Long.MAX_VALUE, sgm.getIslandLevel(island));
    }

    /**
     * Test method for
     * {@link world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager#getIslandLevel(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetIslandLevelUser() {
	assertEquals(Long.MAX_VALUE, sgm.getIslandLevel(user));
    }

}
