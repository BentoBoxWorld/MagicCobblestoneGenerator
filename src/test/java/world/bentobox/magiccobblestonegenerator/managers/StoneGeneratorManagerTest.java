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

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.oneblocks.OneBlockPhase;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
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

    /**
     * Builds a deployed, non-default cobblestone generator tier mock for ordering tests.
     */
    private GeneratorTierObject orderableTier(String id, String name, int priority) {
        GeneratorTierObject t = mock(GeneratorTierObject.class);
        when(t.getUniqueId()).thenReturn(id);
        when(t.getFriendlyName()).thenReturn(name);
        when(t.getPriority()).thenReturn(priority);
        when(t.getGeneratorType()).thenReturn(GeneratorType.COBBLESTONE);
        when(t.isDeployed()).thenReturn(true);
        when(t.isDefaultGenerator()).thenReturn(false);
        return t;
    }

    @Test
    void testGetAllGeneratorTiersEqualPrioritySortsByUniqueIdNotName() {
        sgm.addWorld(world);
        // Same priority + type. Names are reverse of id order to prove the tiebreaker is the id.
        GeneratorTierObject a = orderableTier("magiccobblegenerator_aaa", "Zebra", 10);
        GeneratorTierObject b = orderableTier("magiccobblegenerator_zzz", "Apple", 10);
        sgm.loadGeneratorTier(b, true, null);
        sgm.loadGeneratorTier(a, true, null);

        // Ordered by unique id (aaa before zzz), independent of the friendly names.
        assertEquals(java.util.List.of(a, b), sgm.getAllGeneratorTiers(world));
    }

    @Test
    void testGetAllGeneratorTiersSortsByPriority() {
        sgm.addWorld(world);
        GeneratorTierObject low = orderableTier("magiccobblegenerator_x", "X", 5);
        GeneratorTierObject high = orderableTier("magiccobblegenerator_a", "A", 20);
        sgm.loadGeneratorTier(high, true, null);
        sgm.loadGeneratorTier(low, true, null);

        // Lower priority number comes first, regardless of unique id or name.
        assertEquals(java.util.List.of(low, high), sgm.getAllGeneratorTiers(world));
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

    /**
     * Seeds a deployed, permission-gated generator tier into the cache and returns the freshly created island data with
     * that tier already unlocked and active, simulating a generator that a previous owner had unlocked.
     */
    private GeneratorDataObject seedPermissionGeneratorAndData() {
        sgm.addWorld(world);
        when(island.getUniqueId()).thenReturn("island-133");
        when(island.getWorld()).thenReturn(world);
        when(island.isSpawn()).thenReturn(false);

        when(generatorTier.getUniqueId()).thenReturn("magiccobblegenerator_perm");
        when(generatorTier.isDeployed()).thenReturn(true);
        when(generatorTier.isDefaultGenerator()).thenReturn(false);
        when(generatorTier.getGeneratorType()).thenReturn(GeneratorType.COBBLESTONE);
        when(generatorTier.getRequiredMinIslandLevel()).thenReturn(0L);
        when(generatorTier.getRequiredPermissions())
                .thenReturn(java.util.Set.of("magiccobblegenerator.gen.perm"));
        sgm.loadGeneratorTier(generatorTier, true, null);

        GeneratorDataObject data = sgm.getGeneratorData(island);
        assertNotNull(data);
        data.getUnlockedTiers().add("magiccobblegenerator_perm");
        data.getActiveGeneratorList().add("magiccobblegenerator_perm");
        return data;
    }

    @Test
    void testCheckGeneratorUnlockStatusRevokesPermissionGeneratorWhenOwnerLacksPermission() {
        GeneratorDataObject data = seedPermissionGeneratorAndData();
        // Owner is online but does not have the required permission (new owner scenario, #133).
        when(mockPlayer.isOnline()).thenReturn(true);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertFalse(data.getUnlockedTiers().contains("magiccobblegenerator_perm"));
        assertFalse(data.getActiveGeneratorList().contains("magiccobblegenerator_perm"));
    }

    @Test
    void testCheckGeneratorUnlockStatusKeepsPermissionGeneratorWhenOwnerHasPermission() {
        GeneratorDataObject data = seedPermissionGeneratorAndData();
        when(mockPlayer.isOnline()).thenReturn(true);
        // Owner still has the required permission.
        when(mockPlayer.hasPermission(anyString())).thenReturn(true);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_perm"));
        assertTrue(data.getActiveGeneratorList().contains("magiccobblegenerator_perm"));
    }

    @Test
    void testCheckGeneratorUnlockStatusDoesNotRevokeWhenOwnerOffline() {
        GeneratorDataObject data = seedPermissionGeneratorAndData();
        // Owner is offline, so permissions cannot be checked reliably and nothing is revoked.
        when(mockPlayer.isOnline()).thenReturn(false);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_perm"));
        assertTrue(data.getActiveGeneratorList().contains("magiccobblegenerator_perm"));
    }

    /**
     * Seeds a deployed, level-gated (required level 100) generator that is already unlocked and active, for
     * lose-tiers-on-level-loss tests (#118).
     */
    private GeneratorDataObject seedLevelGeneratorAndData() {
        sgm.addWorld(world);
        when(island.getUniqueId()).thenReturn("island-118");
        when(island.getWorld()).thenReturn(world);
        when(island.isSpawn()).thenReturn(false);

        when(generatorTier.getUniqueId()).thenReturn("magiccobblegenerator_level");
        when(generatorTier.isDeployed()).thenReturn(true);
        when(generatorTier.isDefaultGenerator()).thenReturn(false);
        when(generatorTier.getGeneratorType()).thenReturn(GeneratorType.COBBLESTONE);
        when(generatorTier.getRequiredMinIslandLevel()).thenReturn(100L);
        when(generatorTier.getRequiredPermissions()).thenReturn(java.util.Collections.emptySet());
        sgm.loadGeneratorTier(generatorTier, true, null);

        GeneratorDataObject data = sgm.getGeneratorData(island);
        assertNotNull(data);
        data.getUnlockedTiers().add("magiccobblegenerator_level");
        data.getActiveGeneratorList().add("magiccobblegenerator_level");
        return data;
    }

    @Test
    void testRevokesLevelGeneratorWhenLevelDropped() {
        GeneratorDataObject data = seedLevelGeneratorAndData();
        s.setLoseTiersOnLevelLoss(true);

        // Island level dropped to 10, below the generator's required level of 100.
        sgm.checkGeneratorUnlockStatus(island, null, 10L);

        assertFalse(data.getUnlockedTiers().contains("magiccobblegenerator_level"));
        assertFalse(data.getActiveGeneratorList().contains("magiccobblegenerator_level"));
    }

    @Test
    void testKeepsPurchasedLevelGeneratorWhenLevelDropped() {
        GeneratorDataObject data = seedLevelGeneratorAndData();
        data.getPurchasedTiers().add("magiccobblegenerator_level");
        s.setLoseTiersOnLevelLoss(true);

        sgm.checkGeneratorUnlockStatus(island, null, 10L);

        // Purchased tiers are kept even when the level drops.
        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_level"));
    }

    @Test
    void testDoesNotRevokeLevelGeneratorWhenFeatureDisabled() {
        GeneratorDataObject data = seedLevelGeneratorAndData();
        // Feature is off by default.

        sgm.checkGeneratorUnlockStatus(island, null, 10L);

        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_level"));
    }

    @Test
    void testKeepsLevelGeneratorWhenLevelSufficient() {
        GeneratorDataObject data = seedLevelGeneratorAndData();
        s.setLoseTiersOnLevelLoss(true);

        // Island level is still at or above the requirement.
        sgm.checkGeneratorUnlockStatus(island, null, 200L);

        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_level"));
    }

    /**
     * Builds a deployed, non-default cobblestone generator tier mock with no permission/level requirements, for
     * prerequisite-generator tests.
     */
    private GeneratorTierObject prerequisiteTier(String id, String name, int priority) {
        GeneratorTierObject t = mock(GeneratorTierObject.class);
        when(t.getUniqueId()).thenReturn(id);
        when(t.getFriendlyName()).thenReturn(name);
        when(t.getPriority()).thenReturn(priority);
        when(t.getGeneratorType()).thenReturn(GeneratorType.COBBLESTONE);
        when(t.isDeployed()).thenReturn(true);
        when(t.isDefaultGenerator()).thenReturn(false);
        when(t.getRequiredMinIslandLevel()).thenReturn(0L);
        when(t.getRequiredPermissions()).thenReturn(java.util.Collections.emptySet());
        when(t.getRequiredGeneratorTiers()).thenReturn(java.util.Collections.emptySet());
        return t;
    }

    @Test
    void testCheckGeneratorUnlockStatusUnlocksDependentWhenPrerequisiteUnlocked() {
        sgm.addWorld(world);
        when(island.getUniqueId()).thenReturn("island-88");
        when(island.getWorld()).thenReturn(world);
        when(island.isSpawn()).thenReturn(false);
        s.setNotifyUnlockedGenerators(false);

        GeneratorTierObject gen1 = prerequisiteTier("magiccobblegenerator_gen1", "Gen1", 10);
        GeneratorTierObject gen2 = prerequisiteTier("magiccobblegenerator_gen2", "Gen2", 20);
        when(gen2.getRequiredGeneratorTiers())
                .thenReturn(java.util.Set.of("magiccobblegenerator_gen1"));
        sgm.loadGeneratorTier(gen1, true, null);
        sgm.loadGeneratorTier(gen2, true, null);

        GeneratorDataObject data = sgm.getGeneratorData(island);
        assertNotNull(data);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        // Gen1 has no requirements, so it unlocks; Gen2's prerequisite is then satisfied in the same pass.
        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_gen1"));
        assertTrue(data.getUnlockedTiers().contains("magiccobblegenerator_gen2"));
    }

    @Test
    void testCheckGeneratorUnlockStatusKeepsDependentLockedWhenPrerequisiteLocked() {
        sgm.addWorld(world);
        when(island.getUniqueId()).thenReturn("island-88");
        when(island.getWorld()).thenReturn(world);
        when(island.isSpawn()).thenReturn(false);
        s.setNotifyUnlockedGenerators(false);

        // Gen1 requires a permission the (offline) owner does not have, so it cannot unlock.
        GeneratorTierObject gen1 = prerequisiteTier("magiccobblegenerator_gen1", "Gen1", 10);
        when(gen1.getRequiredPermissions())
                .thenReturn(java.util.Set.of("magiccobblegenerator.gen1"));
        GeneratorTierObject gen2 = prerequisiteTier("magiccobblegenerator_gen2", "Gen2", 20);
        when(gen2.getRequiredGeneratorTiers())
                .thenReturn(java.util.Set.of("magiccobblegenerator_gen1"));
        sgm.loadGeneratorTier(gen1, true, null);
        sgm.loadGeneratorTier(gen2, true, null);

        GeneratorDataObject data = sgm.getGeneratorData(island);
        assertNotNull(data);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        // Gen1 stays locked, so Gen2's prerequisite is unmet and it stays locked too.
        assertFalse(data.getUnlockedTiers().contains("magiccobblegenerator_gen1"));
        assertFalse(data.getUnlockedTiers().contains("magiccobblegenerator_gen2"));
    }

    /**
     * Sets up an AOneBlock world whose island has reached the given block count, plus a phase "Underground" that starts
     * at block 100, and a phase-gated generator. Returns the island's data object (#121).
     */
    private GeneratorDataObject seedPhaseGenerator(String tierId, int islandBlockCount, boolean aOneBlockWorld) {
        sgm.addWorld(world);
        when(island.getUniqueId()).thenReturn("island-121");
        when(island.getWorld()).thenReturn(world);
        when(island.isSpawn()).thenReturn(false);
        s.setNotifyUnlockedGenerators(false);

        if (aOneBlockWorld) {
            AOneBlock aoneBlock = mock(AOneBlock.class);
            when(aoneBlock.getDescription()).thenReturn(
                    new AddonDescription.Builder("", "AOneBlock", "1.0").build());
            OneBlockPhase phase = mock(OneBlockPhase.class);
            when(phase.getBlockNumberValue()).thenReturn(100);
            OneBlocksManager obManager = mock(OneBlocksManager.class);
            when(obManager.getPhase("Underground")).thenReturn(Optional.of(phase));
            when(aoneBlock.getOneBlockManager()).thenReturn(obManager);
            OneBlockIslands obIsland = mock(OneBlockIslands.class);
            when(obIsland.getBlockNumber()).thenReturn(islandBlockCount);
            when(aoneBlock.getOneBlocksIsland(island)).thenReturn(obIsland);
            when(iwm.getAddon(world)).thenReturn(Optional.of(aoneBlock));
        }
        // Otherwise the default (non-AOneBlock) game mode from CommonTestSetup is used.

        GeneratorTierObject tier = prerequisiteTier(tierId, "Phase Gen", 10);
        when(tier.getRequiredPhase()).thenReturn("Underground");
        sgm.loadGeneratorTier(tier, true, null);

        GeneratorDataObject data = sgm.getGeneratorData(island);
        assertNotNull(data);
        return data;
    }

    @Test
    void testUnlocksPhaseGeneratorWhenPhaseReached() {
        // Island at block 150, past the phase's start block of 100.
        GeneratorDataObject data = seedPhaseGenerator("aoneblock_phasegen", 150, true);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertTrue(data.getUnlockedTiers().contains("aoneblock_phasegen"));
    }

    @Test
    void testKeepsPhaseGeneratorLockedWhenPhaseNotReached() {
        // Island at block 50, before the phase's start block of 100.
        GeneratorDataObject data = seedPhaseGenerator("aoneblock_phasegen", 50, true);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertFalse(data.getUnlockedTiers().contains("aoneblock_phasegen"));
    }

    @Test
    void testKeepsPhaseGeneratorLockedInNonAOneBlockWorld() {
        // Not an AOneBlock world: the phase requirement can never be satisfied. The tier id matches the default
        // game mode so it is still evaluated.
        GeneratorDataObject data = seedPhaseGenerator("magiccobblegenerator_phasegen", 150, false);

        sgm.checkGeneratorUnlockStatus(island, null, null);

        assertFalse(data.getUnlockedTiers().contains("magiccobblegenerator_phasegen"));
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

    /**
     * Prepares generatorTier and island for unlock/auto-activation tests and returns a fresh, real data object.
     */
    private GeneratorDataObject prepareUnlockableGenerator() {
        when(generatorTier.isDeployed()).thenReturn(true);
        when(generatorTier.isDefaultGenerator()).thenReturn(false);
        when(island.getUniqueId()).thenReturn("island-106");
        s.setNotifyUnlockedGenerators(false);

        GeneratorDataObject data = new GeneratorDataObject();
        data.setUniqueId("island-106");
        return data;
    }

    @Test
    void testUnlockGeneratorAutoActivatesWhenFlagSet() {
        GeneratorDataObject data = prepareUnlockableGenerator();
        when(generatorTier.isActivateOnUnlock()).thenReturn(true);

        sgm.unlockGenerator(data, user, island, generatorTier);

        assertTrue(data.getUnlockedTiers().contains(uuid.toString()));
        assertTrue(data.getActiveGeneratorList().contains(uuid.toString()));
    }

    @Test
    void testUnlockGeneratorDoesNotAutoActivateWhenFlagUnset() {
        GeneratorDataObject data = prepareUnlockableGenerator();
        when(generatorTier.isActivateOnUnlock()).thenReturn(false);

        sgm.unlockGenerator(data, user, island, generatorTier);

        assertTrue(data.getUnlockedTiers().contains(uuid.toString()));
        assertFalse(data.getActiveGeneratorList().contains(uuid.toString()));
    }

    @Test
    void testAutoActivateRespectsLimitWithoutOverwrite() {
        GeneratorDataObject data = prepareUnlockableGenerator();
        when(generatorTier.isActivateOnUnlock()).thenReturn(true);
        // One active generator already, limit of one, overwrite disabled.
        data.setIslandActiveGeneratorCount(1);
        data.getActiveGeneratorList().add("existing");
        s.setOverwriteOnActive(false);

        sgm.unlockGenerator(data, user, island, generatorTier);

        // Unlocked, but not activated because the active limit is reached.
        assertTrue(data.getUnlockedTiers().contains(uuid.toString()));
        assertFalse(data.getActiveGeneratorList().contains(uuid.toString()));
        assertTrue(data.getActiveGeneratorList().contains("existing"));
    }

    @Test
    void testAutoActivateOverwritesWhenLimitReached() {
        GeneratorDataObject data = prepareUnlockableGenerator();
        when(generatorTier.isActivateOnUnlock()).thenReturn(true);
        data.setIslandActiveGeneratorCount(1);
        data.getActiveGeneratorList().add("existing");
        s.setOverwriteOnActive(true);

        sgm.unlockGenerator(data, user, island, generatorTier);

        // The old generator is replaced by the newly unlocked one.
        assertFalse(data.getActiveGeneratorList().contains("existing"));
        assertTrue(data.getActiveGeneratorList().contains(uuid.toString()));
    }

    @Test
    void testAutoActivateCancelledEventKeepsExistingActiveGenerator() {
        GeneratorDataObject data = prepareUnlockableGenerator();
        when(generatorTier.isActivateOnUnlock()).thenReturn(true);
        data.setIslandActiveGeneratorCount(1);
        data.getActiveGeneratorList().add("existing");
        s.setOverwriteOnActive(true);

        // Cancel any activation event.
        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof world.bentobox.magiccobblestonegenerator.events.GeneratorActivationEvent activation) {
                activation.setCancelled(true);
            }
            return null;
        }).when(pim).callEvent(any(
                world.bentobox.magiccobblestonegenerator.events.GeneratorActivationEvent.class));

        sgm.unlockGenerator(data, user, island, generatorTier);

        // Activation was cancelled before mutating: the existing generator is preserved, the new one is not added.
        assertTrue(data.getUnlockedTiers().contains(uuid.toString()));
        assertTrue(data.getActiveGeneratorList().contains("existing"));
        assertFalse(data.getActiveGeneratorList().contains(uuid.toString()));
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
    void testResetIslandData() {
        when(island.getUniqueId()).thenReturn("island-149");
        assertDoesNotThrow(() -> sgm.resetIslandData(island));
        // The island's stored data is deleted as part of the reset.
        verify(h).deleteID("island-149");
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
