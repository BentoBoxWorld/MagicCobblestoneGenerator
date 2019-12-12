package world.bentobox.magiccobblestonegenerator.listeners;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorManager;
import world.bentobox.magiccobblestonegenerator.tasks.MagicGenerator;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class})
public class MainGeneratorListenerTest {

    @Mock
    private BentoBox plugin;
    @Mock
    private StoneGeneratorAddon addon;

    private MainGeneratorListener mgl;
    @Mock
    private Block block;
    @Mock
    private Block block2;
    @Mock
    private StoneGeneratorManager sgm;
    @Mock
    private World world;
    @Mock
    private Location location;
    @Mock
    private Location location2;
    @Mock
    private MagicGenerator mg;
    @Mock
    private BukkitScheduler scheduler;
    @Mock
    private Block block3;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);
        when(addon.getPlugin()).thenReturn(plugin);

        // Bukkit
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getScheduler()).thenReturn(scheduler);

        // Stone Generator Manager
        when(addon.getManager()).thenReturn(sgm);
        when(sgm.canOperateInWorld(any())).thenReturn(true);
        when(sgm.isMembersOnline(any())).thenReturn(true);

        // Blocks
        when(block.getWorld()).thenReturn(world);
        when(block2.getWorld()).thenReturn(world);
        when(block.getLocation()).thenReturn(location);
        when(block2.getLocation()).thenReturn(location2);
        when(location.getWorld()).thenReturn(world);
        when(location2.getWorld()).thenReturn(world);
        when(block.getType()).thenReturn(Material.LAVA);
        when(block2.getType()).thenReturn(Material.WATER);
        when(block2.getRelative(any())).thenReturn(block3);
        when(block3.getType()).thenReturn(Material.LAVA);

        // Generator
        when(addon.getGenerator()).thenReturn(mg);
        when(mg.isReplacementGenerated(any())).thenReturn(true);
        when(mg.isReplacementGenerated(any(), anyBoolean())).thenReturn(true);

        mgl = new MainGeneratorListener(addon);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccess() {
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertTrue(event.isCancelled());
        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessAir() {
        when(block2.getType()).thenReturn(Material.AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessVoidAir() {
        when(block2.getType()).thenReturn(Material.VOID_AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessCaveAir() {
        when(block2.getType()).thenReturn(Material.CAVE_AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessWaterAirLava() {
        when(block.getType()).thenReturn(Material.WATER);
        when(block2.getType()).thenReturn(Material.AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertTrue(event.isCancelled());
        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessWaterVoidAirLava() {
        when(block.getType()).thenReturn(Material.WATER);
        when(block2.getType()).thenReturn(Material.VOID_AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertTrue(event.isCancelled());
        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSuccessWaterCaveAirLava() {
        when(block.getType()).thenReturn(Material.WATER);
        when(block2.getType()).thenReturn(Material.CAVE_AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertTrue(event.isCancelled());
        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventNotInWorld() {
        when(sgm.canOperateInWorld(any())).thenReturn(false);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventNotOnline() {
        when(sgm.isMembersOnline(any())).thenReturn(false);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSameBlock() {
        BlockFromToEvent event = new BlockFromToEvent(block, block);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventWrongType() {
        when(block.getType()).thenReturn(Material.AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventSameType() {
        when(block2.getType()).thenReturn(Material.LAVA);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }

    /**
     * Test method for {@link world.bentobox.magiccobblestonegenerator.listeners.MainGeneratorListener#onBlockFromToEvent(org.bukkit.event.block.BlockFromToEvent)}.
     */
    @Test
    public void testOnBlockFromToEventLava2AirAboveAir() {
        when(block2.getType()).thenReturn(Material.AIR);
        when(block3.getType()).thenReturn(Material.AIR);
        BlockFromToEvent event = new BlockFromToEvent(block, block2);
        mgl.onBlockFromToEvent(event);
        assertFalse(event.isCancelled());
        verify(scheduler, never()).runTask(eq(plugin), any(Runnable.class));
    }


}
