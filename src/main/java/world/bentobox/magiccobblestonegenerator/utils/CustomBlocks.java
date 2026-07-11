package world.bentobox.magiccobblestonegenerator.utils;


import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.CraftEngineHook;
import world.bentobox.bentobox.hooks.ItemsAdderHook;
import world.bentobox.bentobox.hooks.OraxenHook;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Helper for custom blocks provided by third-party plugins (ItemsAdder, CraftEngine, Oraxen, Nexo).
 * <p>
 * Generator block entries are stored as plain strings. A vanilla entry is a {@link Material} name
 * (e.g. {@code COBBLESTONE}), while a custom entry is prefixed with the provider, e.g.
 * {@code itemsadder:iasurvival:ruby_ore}, {@code craftengine:default:topaz_ore} or {@code oraxen:ruby_ore}.
 * <p>
 * All plugin access is routed through BentoBox core hooks, so this addon never depends on the
 * third-party plugins directly. Providers whose hook does not support placement yet (Oraxen, Nexo)
 * are recognised but will not generate until BentoBox core adds support.
 */
public final class CustomBlocks
{
    private CustomBlocks()
    {
    }


    /**
     * Custom block providers supported by this addon via BentoBox hooks.
     */
    public enum Provider
    {
        ITEMS_ADDER("itemsadder", "ItemsAdder", true),
        CRAFT_ENGINE("craftengine", "CraftEngine", true),
        ORAXEN("oraxen", "Oraxen", false),
        NEXO("nexo", "Nexo", false);


        Provider(String prefix, String pluginName, boolean placementSupported)
        {
            this.prefix = prefix;
            this.pluginName = pluginName;
            this.placementSupported = placementSupported;
        }


        /**
         * @return prefix used in block IDs, e.g. {@code itemsadder}.
         */
        public String getPrefix()
        {
            return this.prefix;
        }


        /**
         * @return plugin name used to look up the BentoBox hook.
         */
        public String getPluginName()
        {
            return this.pluginName;
        }


        /**
         * @return {@code true} if the BentoBox core hook for this provider can place blocks.
         */
        public boolean isPlacementSupported()
        {
            return this.placementSupported;
        }


        private final String prefix;

        private final String pluginName;

        private final boolean placementSupported;
    }


    /**
     * Returns the custom block provider for the given block ID, or {@code null} if the ID does not
     * carry a known provider prefix (i.e. it is a vanilla material name).
     *
     * @param blockId block ID to parse.
     * @return provider or {@code null}.
     */
    public static @Nullable Provider getProvider(@Nullable String blockId)
    {
        if (blockId == null)
        {
            return null;
        }

        int separator = blockId.indexOf(':');

        if (separator <= 0)
        {
            return null;
        }

        String prefix = blockId.substring(0, separator).toLowerCase();

        for (Provider provider : Provider.values())
        {
            if (provider.getPrefix().equals(prefix))
            {
                return provider;
            }
        }

        return null;
    }


    /**
     * Returns if given block ID references a custom block from a known provider.
     *
     * @param blockId block ID to check.
     * @return {@code true} if the ID has a known provider prefix.
     */
    public static boolean isCustom(@Nullable String blockId)
    {
        return getProvider(blockId) != null;
    }


    /**
     * Strips the provider prefix from a custom block ID, returning the plugin-native ID.
     *
     * @param blockId provider-prefixed block ID.
     * @return the ID without the provider prefix, or the input unchanged if it has none.
     */
    public static String getCustomId(String blockId)
    {
        return getProvider(blockId) == null ? blockId : blockId.substring(blockId.indexOf(':') + 1);
    }


    /**
     * Parses a vanilla material from the given block ID.
     *
     * @param blockId block ID to parse.
     * @return the matching {@link Material}, or {@code null} if the ID is a custom block or unknown.
     */
    public static @Nullable Material matchVanilla(@Nullable String blockId)
    {
        if (blockId == null || isCustom(blockId))
        {
            return null;
        }

        return Material.matchMaterial(blockId);
    }


    /**
     * Returns if the BentoBox hook for the given provider is present (i.e. the plugin is installed).
     *
     * @param addon addon instance used to reach the hook manager.
     * @param provider custom block provider.
     * @return {@code true} if the hook is registered.
     */
    public static boolean isHooked(StoneGeneratorAddon addon, Provider provider)
    {
        return addon.getPlugin().getHooks().getHook(provider.getPluginName()).isPresent();
    }


    /**
     * Returns if the given custom block ID is registered by its provider plugin on this server.
     *
     * @param addon addon instance used to reach the hook manager.
     * @param blockId provider-prefixed block ID.
     * @return {@code true} if the provider plugin is hooked and knows the ID.
     */
    public static boolean isRegistered(StoneGeneratorAddon addon, String blockId)
    {
        Provider provider = getProvider(blockId);

        if (provider == null || !isHooked(addon, provider))
        {
            return false;
        }

        String customId = getCustomId(blockId);

        return switch (provider)
        {
            case ITEMS_ADDER -> ItemsAdderHook.isInRegistry(customId);
            case CRAFT_ENGINE -> CraftEngineHook.exists(customId);
            case ORAXEN -> OraxenHook.exists(customId);
            // No BentoBox core hook for Nexo yet.
            case NEXO -> false;
        };
    }


    /**
     * Returns if the given custom block ID can actually be placed on this server: the provider
     * plugin is hooked, the ID is registered, and the BentoBox hook supports block placement.
     *
     * @param addon addon instance used to reach the hook manager.
     * @param blockId provider-prefixed block ID.
     * @return {@code true} if {@link #place(StoneGeneratorAddon, String, Location)} can succeed.
     */
    public static boolean canPlace(StoneGeneratorAddon addon, String blockId)
    {
        Provider provider = getProvider(blockId);

        return provider != null &&
            provider.isPlacementSupported() &&
            isRegistered(addon, blockId);
    }


    /**
     * Places the given custom block through its BentoBox hook.
     *
     * @param addon addon instance used to reach the hook manager.
     * @param blockId provider-prefixed block ID.
     * @param location target location.
     * @return {@code true} if the block was placed.
     */
    public static boolean place(StoneGeneratorAddon addon, String blockId, Location location)
    {
        if (!canPlace(addon, blockId))
        {
            return false;
        }

        String customId = getCustomId(blockId);

        return switch (getProvider(blockId))
        {
            case ITEMS_ADDER ->
            {
                ItemsAdderHook.place(customId, location);
                yield true;
            }
            case CRAFT_ENGINE -> CraftEngineHook.placeBlock(location, customId);
            // canPlace already filters these out; kept for exhaustiveness.
            case ORAXEN, NEXO -> false;
        };
    }


    /**
     * Returns an icon ItemStack for the given block ID, for use in GUI panels. Vanilla IDs map to
     * their material; custom IDs use the provider's item (correct texture / model data) when the
     * hook is available, otherwise a fallback icon.
     *
     * @param addon addon instance used to reach the hook manager.
     * @param blockId block ID.
     * @return icon ItemStack, never null.
     */
    public static ItemStack getIcon(StoneGeneratorAddon addon, String blockId)
    {
        Material vanilla = matchVanilla(blockId);

        if (vanilla != null)
        {
            return new ItemStack(vanilla.isItem() ? vanilla : Material.PAPER);
        }

        Provider provider = getProvider(blockId);

        if (provider != null && isHooked(addon, provider))
        {
            String customId = getCustomId(blockId);

            Optional<ItemStack> icon = switch (provider)
            {
                case ITEMS_ADDER -> ItemsAdderHook.getItemStack(customId);
                case CRAFT_ENGINE -> CraftEngineHook.getItemStack(customId);
                // Oraxen hook exposes icons only via Oraxen classes; Nexo has no hook yet.
                case ORAXEN, NEXO -> Optional.empty();
            };

            if (icon.isPresent())
            {
                return icon.get().clone();
            }
        }

        return new ItemStack(Material.NOTE_BLOCK);
    }


    /**
     * Returns a user-facing display name for the given block ID. Vanilla IDs are translated via the
     * locale files; custom IDs use the provider item's display name when available, otherwise the
     * plugin-native ID.
     *
     * @param addon addon instance used to reach the hook manager.
     * @param user user for translations.
     * @param blockId block ID.
     * @return display name, never null.
     */
    public static String getDisplayName(StoneGeneratorAddon addon, User user, String blockId)
    {
        Material vanilla = matchVanilla(blockId);

        if (vanilla != null)
        {
            return Utils.prettifyObject(user, vanilla);
        }

        if (isCustom(blockId))
        {
            ItemStack icon = getIcon(addon, blockId);
            ItemMeta meta = icon.getItemMeta();

            if (meta != null && meta.hasDisplayName())
            {
                return meta.getDisplayName();
            }

            return getCustomId(blockId);
        }

        return blockId;
    }
}
