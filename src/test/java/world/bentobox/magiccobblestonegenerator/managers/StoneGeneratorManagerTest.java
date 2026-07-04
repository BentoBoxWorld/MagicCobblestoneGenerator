package world.bentobox.magiccobblestonegenerator.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.magiccobblestonegenerator.CommonTestSetup;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorBundleObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject.GeneratorType;
import world.bentobox.magiccobblestonegenerator.events.GeneratorBuyEvent;
import world.bentobox.magiccobblestonegenerator.events.GeneratorPreBuyEvent;

/**
 * @author tastybento
 */
class StoneGeneratorManagerTest extends CommonTestSetup {

    @Mock
    private StoneGeneratorAddon addon;
    // DUT
    private StoneGeneratorManager sgm;
    @Mock
    private GeneratorTierObject generatorTier;
    @Mock
    private User user;
    @Mock
    private GeneratorBundleObject generatorBundle;
    @Mock
    private GeneratorDataObject generatorData;
    @Mock
    private GameModeAddon gameModeAddon;

    private world.bentobox.magiccobblestonegenerator.config.Settings s;

    private MockedStatic<DatabaseSetup> mockDb;
    private AbstractDatabaseHandler<Object> h;

    @Override
    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();

        // Database
        h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        when(addon.getPlugin()).thenReturn(plugin);

        // Player / user
        when(user.isOp()).thenReturn(false);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(mockPlayer);
        when(user.getName()).thenReturn("tastybento");
        when(user.getLocale()).thenReturn(Locale.ENGLISH);
        User.setPlugin(plugin);

        // Generator Tier
        when(this.generatorTier.getFriendlyName()).thenReturn("Basic Tier");
        when(this.generatorTier.getUniqueId()).thenReturn(uuid.toString());
        when(this.generatorTier.getExhaustionLimit()).thenReturn(-1L);

        // Generator Bundle
        when(this.generatorBundle.getFriendlyName()).thenReturn("Basic Bundle");
        when(this.generatorBundle.getUniqueId()).thenReturn(uuid.toString());

        // Locales
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
        when(im.getIsland(world, uuid)).thenReturn(island);
        when(addon.getIslands()).thenReturn(im);

        // IWM
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameModeAddon));

        sgm = new StoneGeneratorManager(addon);

        // Addon Manager
        when(addon.getAddonManager()).thenReturn(sgm);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        deleteAll(new java.io.File("addons"));
        deleteAll(new java.io.File("panels"));
        new java.io.File("config.yml").delete();
    }

    @Test
    void testStoneGeneratorManager() {
        assertNotNull(sgm);
    }

    @Test
    void testAddWorld() {
        assertDoesNotThrow(() -> sgm.addWorld(world));
    }

    @Test
    void testReload() throws Exception {
        sgm.reload();
        verify(addon).log("Loading generator tiers from database...");
        verify(h, atLeast(1)).loadObjects();
        verify(addon).log("Done");
    }

    @Test
    void testLoad() throws Exception {
        sgm.load();
        verify(addon).log("Loading generator tiers from database...");
        verify(h, atLeast(1)).loadObjects();
        verify(addon).log("Done");
    }

    @Test
    void testLoadGeneratorTier() {
        assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
        verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.generator-loaded");
    }

    @Test
    void testLoadGeneratorTierOverwrite() {
        assertTrue(sgm.loadGeneratorTier(generatorTier, true, user));
        verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.generator-loaded");
    }

    @Test
    void testLoadGeneratorTierOverwriteFail() {
        assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
        // Second time, it should fail because it is in the cache now, and overwrite is false
        assertFalse(sgm.loadGeneratorTier(generatorTier, false, user));
    }

    @Test
    void testLoadGeneratorTierOverwritePass() {
        assertTrue(sgm.loadGeneratorTier(generatorTier, false, user));
        // Second time, it should pass because it is in the cache, but overwrite is true
        assertTrue(sgm.loadGeneratorTier(generatorTier, true, user));
    }

    @Test
    void testLoadGeneratorBundle() {
        sgm.loadGeneratorBundle(generatorBundle, false, user);
        verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.bundle-loaded");
    }

    @Test
    void testLoadGeneratorBundleOverwrite() {
        sgm.loadGeneratorBundle(generatorBundle, true, user);
        verify(user).sendMessage("stone-generator.conversations.prefixstone-generator.messages.bundle-loaded");
    }

    @Test
    void testLoadGeneratorBundleOverwriteFail() {
        assertTrue(sgm.loadGeneratorBundle(generatorBundle, false, user));
        // Second time, it should fail because it is in the cache now, and overwrite is false
        assertFalse(sgm.loadGeneratorBundle(generatorBundle, false, user));
    }

    @Test
    void testLoadGeneratorBundleOverwritePass() {
        assertTrue(sgm.loadGeneratorBundle(generatorBundle, false, user));
        // Second time, it should pass because it is in the cache, but overwrite is true
        assertTrue(sgm.loadGeneratorBundle(generatorBundle, true, user));
    }

    @Test
    void testSaveGeneratorTier() {
        CompletableFuture<Boolean> cf = sgm.saveGeneratorTier(generatorTier);
        assertTrue(cf.isDone());
    }

    @Test
    void testSaveGeneratorBundle() {
        CompletableFuture<Boolean> cf = sgm.saveGeneratorBundle(generatorBundle);
        assertTrue(cf.isDone());
    }

    @Test
    void testSaveGeneratorData() {
        CompletableFuture<Boolean> cf = sgm.saveGeneratorData(generatorData);
        assertTrue(cf.isDone());
    }

    @Test
    void testSave() {
        CompletableFuture<Boolean> cf = sgm.save();
        assertTrue(cf.isDone());
    }

    @Test
    void testWipeGameModeGenerators() {
        sgm.wipeGameModeGenerators(gameModeAddon);
        verify(addon).log("All generators for magiccobblegenerator are removed!");
        verify(addon).log("All bundles for magiccobblegenerator are removed!");
    }

    @Test
    void testWipeIslandData() {
        sgm.wipeIslandData(gameModeAddon);
        verify(addon).log("All island data for MagicCobbleGenerator are removed!");
    }

    @Test
    void testWipeGeneratorTier() {
        // No observable side effect to verify; assert the call completes cleanly.
        assertDoesNotThrow(() -> sgm.wipeGeneratorTier(generatorTier));
    }

    @Test
    void testGetGeneratorByID() {
        assertNull(sgm.getGeneratorByID(uuid.toString()));
    }

    @Test
    void testGetGeneratorTier() {
        assertNull(sgm.getGeneratorTier(island, location, GeneratorType.ANY));
    }

    @Test
    void testGetAllGeneratorTiers() {
        assertTrue(sgm.getAllGeneratorTiers(world).isEmpty());
    }

    @Test
    void testGetIslandGeneratorTiersWorldUser() {
        assertTrue(sgm.getIslandGeneratorTiers(world, user).isEmpty());
    }

    @Test
    void testGetIslandGeneratorTiersWorldGeneratorDataObject() {
        assertTrue(sgm.getIslandGeneratorTiers(world, generatorData).isEmpty());
    }

    @Test
    void testFindDefaultGeneratorList() {
        assertTrue(sgm.findDefaultGeneratorList(world).isEmpty());
    }

    @Test
    void testGetAllGeneratorBundles() {
        assertTrue(sgm.getAllGeneratorBundles(world).isEmpty());
    }

    @Test
    void testGetBundleById() {
        assertNull(sgm.getBundleById(uuid.toString()));
    }

    @Test
    void testWipeBundle() {
        sgm.loadGeneratorBundle(generatorBundle, false, user);
        sgm.wipeBundle(generatorBundle);
        verify(h).deleteID(uuid.toString());
    }

    @Test
    void testLoadUserIslands() {
        sgm.addWorld(world);
        sgm.loadUserIslands(uuid);
        verify(island, times(18)).getOwner();
    }

    @Test
    void testValidateIslandData() {
        sgm.addWorld(world);
        @Nullable
        GeneratorDataObject gdo = sgm.validateIslandData(island);
        assertNotNull(gdo);
    }

    @Test
    void testCheckGeneratorUnlockStatus() {
        sgm.checkGeneratorUnlockStatus(island, user, 10L);
        verify(island, times(2)).isSpawn();
    }

    @Test
    void testGetGeneratorDataIsland() {
        assertNotNull(sgm.getGeneratorData(island));
    }

    @Test
    void testGetGeneratorDataUserWorld() {
        assertNull(sgm.getGeneratorData(user, world));
    }

    @Test
    void testCanGenerateBlockNoLimit() {
        s.setGeneratorExhaustionLimit(0);
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
    }

    @Test
    void testCanGenerateBlockUnderLimit() {
        s.setGeneratorExhaustionLimit(2);
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
    }

    @Test
    void testCanGenerateBlockExhausted() {
        s.setGeneratorExhaustionLimit(1);
        s.setGeneratorExhaustionCooldown(60);
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
        // Limit is now reached, next call should be blocked and generator put on cooldown.
        assertFalse(sgm.canGenerateBlock(island, generatorTier));
        assertFalse(sgm.canGenerateBlock(island, generatorTier));
    }

    @Test
    void testCanGenerateBlockTierOverridesLimit() {
        s.setGeneratorExhaustionLimit(1000);
        when(this.generatorTier.getExhaustionLimit()).thenReturn(1L);
        assertTrue(sgm.canGenerateBlock(island, generatorTier));
        assertFalse(sgm.canGenerateBlock(island, generatorTier));
    }

    @Test
    void testUnlockGenerator() {
        sgm.unlockGenerator(generatorData, user, island, generatorTier);
        verify(user).sendMessage(
                "stone-generator.conversations.prefixstone-generator.messages.generator-cannot-be-unlocked");
    }

    @Test
    void testDeactivateGenerator() {
        assertFalse(sgm.deactivateGenerator(user, generatorData, generatorTier));
    }

    @Test
    void testCanActivateGenerator() {
        assertFalse(sgm.canActivateGenerator(user, island, generatorData, generatorTier));
    }

    @Test
    void testActivateGeneratorUserIslandGeneratorDataObjectGeneratorTierObject() {
        assertDoesNotThrow(() -> sgm.activateGenerator(user, island, generatorData, generatorTier));
    }

    @Test
    void testActivateGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBoolean() {
        assertDoesNotThrow(() -> sgm.activateGenerator(user, island, generatorData, generatorTier, true));
    }

    @Test
    void testCanPurchaseGenerator() {
        assertFalse(sgm.canPurchaseGenerator(user, island, generatorData, generatorTier));
    }

    @Test
    void testPurchaseGeneratorUserIslandGeneratorDataObjectGeneratorTierObject() {
        assertDoesNotThrow(() -> sgm.purchaseGenerator(user, island, generatorData, generatorTier));
    }

    @Test
    void testPurchaseGeneratorUserIslandGeneratorDataObjectGeneratorTierObjectBoolean() {
        assertDoesNotThrow(() -> sgm.purchaseGenerator(user, island, generatorData, generatorTier, true));
    }

    @Test
    void testPurchaseGeneratorFiresPreBuyEvent() {
        when(generatorData.getUniqueId()).thenReturn(uuid.toString());
        sgm.purchaseGenerator(user, island, generatorData, generatorTier, true);
        verify(pim).callEvent(any(GeneratorPreBuyEvent.class));
    }

    @Test
    void testPurchaseGeneratorCancelledPreBuyEventStopsPurchase() {
        when(generatorData.getUniqueId()).thenReturn(uuid.toString());
        // Cancel any GeneratorPreBuyEvent that is fired.
        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof GeneratorPreBuyEvent preBuy) {
                preBuy.setCancelled(true);
            }
            return null;
        }).when(pim).callEvent(any(GeneratorPreBuyEvent.class));

        sgm.purchaseGenerator(user, island, generatorData, generatorTier, true);

        // Purchase must be aborted: the tier is never added and the post-purchase event never fires.
        verify(generatorData, Mockito.never()).getPurchasedTiers();
        verify(pim, Mockito.never()).callEvent(any(GeneratorBuyEvent.class));
    }

    @Test
    void testWipeGeneratorDataString() {
        assertDoesNotThrow(() -> sgm.wipeGeneratorData(uuid.toString()));
    }

    @Test
    void testWipeGeneratorDataGeneratorDataObject() {
        assertDoesNotThrow(() -> sgm.wipeGeneratorData(generatorData));
    }

    @Test
    void testCanOperateInWorld() {
        assertFalse(sgm.canOperateInWorld(world));
    }

    @Test
    void testIsMembersOnline() {
        assertFalse(sgm.isMembersOnline(location));
    }

    @Test
    void testGetIslandLevelIsland() {
        assertEquals(Long.MAX_VALUE, sgm.getIslandLevel(island));
    }

    @Test
    void testGetIslandLevelUser() {
        assertEquals(Long.MAX_VALUE, sgm.getIslandLevel(user));
    }

}
