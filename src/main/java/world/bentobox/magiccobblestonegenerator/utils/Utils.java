//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This class contains some useful methods for addon.
 */
public class Utils
{
// ---------------------------------------------------------------------
// Section: Other
// ---------------------------------------------------------------------


    /**
     * This method returns index of previous page based on current page index and max page index.
     *
     * @param pageIndex Current page index.
     * @param maxPageIndex Maximal page count.
     * @return Integer of previous page index.
     */
    public static int getPreviousPage(int pageIndex, int maxPageIndex)
    {
        // Page 0 is viewed... back arrow = last page ... next arrow = 2
        // Page 1 is viewed... back arrow = 1 ... next arrow = 3
        // Page 2 is viewed... back arrow = 2 ... next arrow = 4
        // Page n is viewed... back arrow = n ... next arrow = n+2
        // Last page is viewed .. back arrow = last... next arrow = 1

        return pageIndex == 0 ? maxPageIndex + 1 : pageIndex;
    }


    /**
     * This method returns index of next page based on current page index and max page index.
     *
     * @param pageIndex Current page index.
     * @param maxPageIndex Maximal page count.
     * @return Integer of next page index.
     */
    public static int getNextPage(int pageIndex, int maxPageIndex)
    {
        // Page 0 is viewed... back arrow = last page ... next arrow = 2
        // Page 1 is viewed... back arrow = 1 ... next arrow = 3
        // Page 2 is viewed... back arrow = 2 ... next arrow = 4
        // Page n is viewed... back arrow = n ... next arrow = n+2
        // Last page is viewed .. back arrow = last... next arrow = 1

        return pageIndex == maxPageIndex ? 1 : pageIndex + 2;
    }


    /**
     * This method assigns Material icon based on input generator type.
     *
     * @param generatorType Generator type which icon should be returned.
     * @return Material for input generator type.
     */
    public static Material getGeneratorTypeMaterial(GeneratorTierObject.GeneratorType generatorType)
    {
        return switch (generatorType) {
            case COBBLESTONE -> Material.COBBLESTONE;
            case STONE -> Material.STONE;
            case BASALT -> Material.BASALT;
            case COBBLESTONE_OR_STONE -> Material.ANDESITE;
            case BASALT_OR_COBBLESTONE -> Material.GRANITE;
            case BASALT_OR_STONE -> Material.BLACKSTONE;
            default -> Material.BEDROCK;
        };
    }


// ---------------------------------------------------------------------
// Section: Permissions
// ---------------------------------------------------------------------


    /**
     * This method gets string value of given permission prefix. If user does not have given permission or it have all
     * (*), then return default value.
     *
     * @param user User who's permission should be checked.
     * @param permissionPrefix Prefix that need to be found.
     * @param defaultValue Default value that will be returned if permission not found.
     * @return String value that follows permissionPrefix.
     */
    public static String getPermissionValue(User user, String permissionPrefix, String defaultValue)
    {
        if (user.isPlayer())
        {
            if (permissionPrefix.endsWith("."))
            {
                permissionPrefix = permissionPrefix.substring(0, permissionPrefix.length() - 1);
            }

            String permPrefix = permissionPrefix + ".";

            List<String> permissions = user.getEffectivePermissions().stream().
                map(PermissionAttachmentInfo::getPermission).
                filter(permission -> permission.startsWith(permPrefix)).
                collect(Collectors.toList());

            for (String permission : permissions)
            {
                if (permission.contains(permPrefix + "*"))
                {
                    // * means all. So continue to search more specific.
                    continue;
                }

                String[] parts = permission.split(permPrefix);

                if (parts.length > 1)
                {
                    return parts[1];
                }
            }
        }

        return defaultValue;
    }


    /**
     * This method gets integer value of given permission prefix. If user does not have given permission or it have all
     * (*), then return default value.
     *
     * @param user User who's permission should be checked.
     * @param permissionPrefix Prefix that need to be found.
     * @param defaultValue Default value that will be returned if permission not found.
     * @return Integer value that follows permissionPrefix.
     */
    public static int getPermissionValue(User user, String permissionPrefix, int defaultValue)
    {
        return user.getPermissionValue(permissionPrefix, defaultValue);
    }


    /**
     * This method replaces "[gamemode] and [number] in permission template with a requested gamemode and empty space
     * accordantly.
     *
     * @param world World where permission is operating.
     * @param permissionTemplate permission template.
     * @return Parsed permission String.
     */
    public static String getPermissionString(World world, String permissionTemplate)
    {
        String permissionPrefix = BentoBox.getInstance().getIWM().getPermissionPrefix(world);

        return permissionPrefix.isEmpty() ? permissionTemplate :
            permissionTemplate.replace("[gamemode].", permissionPrefix);
    }


    /**
     * This method returns if given user has all required permissions.
     *
     * @param user User who must be checked.
     * @param permissions List of permissions that must be checked.
     * @return {@code true} if player has all required permissions, {@code flase} otherwise.
     */
    public static boolean matchAllPermissions(User user, Collection<String> permissions)
    {
        return permissions.isEmpty() ||
            user.isOp() ||
            permissions.stream().allMatch(user::hasPermission);
    }


    /**
     * This method transforms given World into GameMode name. If world is not a GameMode world then it returns null.
     *
     * @param world World which gameMode name must be found out.
     * @return GameMode name or null.
     */
    public static String getGameMode(World world)
    {
        return BentoBox.getInstance().getIWM().getAddon(world).
            map(gameModeAddon -> gameModeAddon.getDescription().getName()).
            orElse(null);
    }


    /**
     * This method transforms given GameMode into name.
     *
     * @param gameModeAddon GameMode which name must be returned.
     * @return GameMode name.
     */
    public static String getGameMode(GameModeAddon gameModeAddon)
    {
        return gameModeAddon.getDescription().getName();
    }


    /**
     * This method allows to get next value from array list after given value.
     *
     * @param values Array that should be searched for given value.
     * @param currentValue Value which next element should be found.
     * @param <T> Instance of given object.
     * @return Next value after currentValue in values array.
     */
    public static <T> T getNextValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i + 1 == values.length)
                {
                    return values[0];
                }
                else
                {
                    return values[i + 1];
                }
            }
        }

        return currentValue;
    }


    /**
     * This method allows to get previous value from array list after given value.
     *
     * @param values Array that should be searched for given value.
     * @param currentValue Value which previous element should be found.
     * @param <T> Instance of given object.
     * @return Previous value before currentValue in values array.
     */
    public static <T> T getPreviousValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i > 0)
                {
                    return values[i - 1];
                }
                else
                {
                    return values[values.length - 1];
                }
            }
        }

        return currentValue;
    }


    /**
     * This method returns map that contains biomes name as key and biome as value.
     *
     * @return Map that contains relation from biome name to biome.
     */
    public static Map<String, Biome> getBiomeNameMap() {
        return Registry.BIOME.stream().collect(Collectors.toMap(biome -> biome.name(), biome -> biome));
    }


    /**
     * This method converts given treeMap to pairList.
     *
     * @param treeMap TreeMap that contains elements which must be translated to list.
     * @return PairList of elements from treeMap.
     */
    public static <T> List<Pair<T, Double>> treeMap2PairList(TreeMap<Double, T> treeMap)
    {
        List<Pair<T, Double>> returnList = new ArrayList<>(treeMap.size());

        if (treeMap.isEmpty())
        {
            // If map is empty, do not process anymore.
            return returnList;
        }

        Double previousValue = 0.0;

        while (!treeMap.isEmpty())
        {
            Map.Entry<Double, T> entry = treeMap.pollFirstEntry();
            returnList.add(new Pair<>(entry.getValue(), entry.getKey() - previousValue));
            previousValue = entry.getKey();
        }

        return returnList;
    }


    /**
     * This method converts given pairList to actual TreeMap.
     *
     * @param pairList PairList that contains elements which must be translated to map.
     * @return TreeMap of elements from pairList.
     */
    public static <T> TreeMap<Double, T> pairList2TreeMap(List<Pair<T, Double>> pairList)
    {
        TreeMap<Double, T> treeMap = new TreeMap<>(Double::compareTo);

        if (pairList.isEmpty())
        {
            // Nothing to process
            return treeMap;
        }

        Double nextMax = 0.0;

        for (Pair<T, Double> pair : pairList)
        {
            // drop 0 and negative values
            if (pair.getValue() > 0.0)
            {
                nextMax += pair.getValue();
                treeMap.put(nextMax, pair.getKey());
            }
        }

        return treeMap;
    }


    /**
     * Sanitizes the provided input. It replaces spaces and hyphens with underscores and lower cases the input.
     * This code also removes all color codes from the input.
     * @param input input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input)
    {
        return ChatColor.stripColor(
            Util.translateColorCodes(input.toLowerCase(Locale.ENGLISH).
                replace(" ", "_").
                replace("-", "_")));
    }


// ---------------------------------------------------------------------
// Section: Biome Type Resolver
// ---------------------------------------------------------------------

    /**
     * This method returns if current biome is locally detected as snowy biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is snowy biome, {@code false} otherwise.
     */
    public static boolean isSnowyBiome(Biome biome)
    {
        return biome == Biome.SNOWY_PLAINS || biome == Biome.SNOWY_TAIGA || biome == Biome.ICE_SPIKES
                || biome == Biome.FROZEN_RIVER || biome == Biome.SNOWY_BEACH;
    }


    /**
     * This method returns if current biome is locally detected as cold biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cold biome, {@code false} otherwise.
     */
    public static boolean isColdBiome(Biome biome)
    {
        return biome == Biome.WINDSWEPT_HILLS || biome == Biome.WINDSWEPT_GRAVELLY_HILLS
                || biome == Biome.WINDSWEPT_FOREST || biome == Biome.TAIGA || biome == Biome.OLD_GROWTH_PINE_TAIGA
                || biome == Biome.OLD_GROWTH_SPRUCE_TAIGA || biome == Biome.STONY_SHORE;
    }


    /**
     * This method returns if current biome is locally detected as temperate biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is temperate biome, {@code false} otherwise.
     */
    public static boolean isTemperateBiome(Biome biome)
    {
        return biome == Biome.PLAINS || biome == Biome.SUNFLOWER_PLAINS || biome == Biome.FOREST
                || biome == Biome.FLOWER_FOREST || biome == Biome.BIRCH_FOREST || biome == Biome.OLD_GROWTH_BIRCH_FOREST
                || biome == Biome.DARK_FOREST || biome == Biome.SWAMP || biome == Biome.JUNGLE
                || biome == Biome.SPARSE_JUNGLE || biome == Biome.BAMBOO_JUNGLE || biome == Biome.RIVER
                || biome == Biome.BEACH || biome == Biome.MUSHROOM_FIELDS;
    }


    /**
     * This method returns if current biome is locally detected as warm biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is warm biome, {@code false} otherwise.
     */
    public static boolean isWarmBiome(Biome biome)
    {
        return biome == Biome.DESERT || biome == Biome.SAVANNA || biome == Biome.WINDSWEPT_SAVANNA
                || biome == Biome.BADLANDS || biome == Biome.ERODED_BADLANDS || biome == Biome.WOODED_BADLANDS
                || biome == Biome.SAVANNA_PLATEAU;
    }


    /**
     * This method returns if current biome is locally detected as aquatic biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is aquatic biome, {@code false} otherwise.
     */
    public static boolean isAquaticBiome(Biome biome)
    {
        return biome == Biome.WARM_OCEAN || biome == Biome.LUKEWARM_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN
                || biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.COLD_OCEAN
                || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.FROZEN_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN;
    }


    /**
     * This method returns if current biome is locally detected as neutral biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is neutral biome, {@code false} otherwise.
     */
    public static boolean isNeutralBiome(Biome biome)
    {
        return biome == Biome.THE_VOID;
    }


    /**
     * This method returns if current biome is locally detected as cave biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is cave biome, {@code false} otherwise.
     */
    public static boolean isCaveBiome(Biome biome) {
        return biome == Biome.LUSH_CAVES || biome == Biome.DRIPSTONE_CAVES;
    }


    /**
     * This method returns if current biome is locally detected as nether biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is nether biome, {@code false} otherwise.
     */
    public static boolean isNetherBiome(Biome biome) {
        return biome == Biome.NETHER_WASTES || biome == Biome.SOUL_SAND_VALLEY || biome == Biome.CRIMSON_FOREST
                || biome == Biome.WARPED_FOREST || biome == Biome.BASALT_DELTAS;
    }


    /**
     * This method returns if current biome is locally detected as the end biome.
     *
     * @param biome Biome that must be checked.
     * @return {@code true} if I think it is the end biome, {@code false} otherwise.
     */
    public static boolean isTheEndBiome(Biome biome) {
        return biome == Biome.THE_END || biome == Biome.SMALL_END_ISLANDS || biome == Biome.END_MIDLANDS
                || biome == Biome.END_HIGHLANDS || biome == Biome.END_BARRENS;
    }


// ---------------------------------------------------------------------
// Section: Prettify Object translations
// ---------------------------------------------------------------------


    /**
     * Prettify Material object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for Material.
     */
    public static String prettifyObject(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        return LangUtilsHook.getMaterialName(object, user);
    }


    /**
     * Prettify Material object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for Material.
     */
    public static String prettifyDescription(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * This method prettify given Biome name to more friendly name.
     *
     * @param user User which translation set will be used.
     * @param biome Biome that requires prettifying.
     * @return Clean and readable biome name.
     */
    public static String prettifyObject(Biome biome, User user)
    {
        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.BIOMES + biome.getKey().getKey() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]: [name]

        translation = user.getTranslationOrNothing(Constants.BIOMES + biome.getKey().getKey());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // biomes:
        //   [biome]: [name]

        translation = user.getTranslationOrNothing("biomes." + biome.getKey().getKey());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(biome.getKey().getKey());
    }


    /**
     * This method prettify given material name to more friendly name.
     *
     * @param user User which translation set will be used.
     * @param material material that requires prettifying.
     * @return Clean and readable material name.
     */
    public static String prettifyObject(User user, Material material)
    {
        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       name: [name]
        String translation =
            user.getTranslationOrNothing(Constants.MATERIALS + material.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + material.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // biomes:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + material.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(material.name());
    }


    /**
     * This method prettify given itemStack name to more friendly name.
     *
     * @param user User which translation set will be used.
     * @param itemStack material that requires prettifying.
     * @return Clean and readable material name.
     */
    public static String prettifyObject(User user, ItemStack itemStack)
    {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null)
        {
            // If item has a valid display name, return it.

            ItemMeta itemMeta = itemStack.getItemMeta();
            return itemMeta.getDisplayName().isEmpty() ?
                Utils.prettifyObject(user, itemStack.getType()) :
                itemMeta.getDisplayName();
        }
        else
        {
            // Otherwise return material object name:
            return Utils.prettifyObject(user, itemStack.getType());
        }
    }


    /**
     * This method prettify given entity name to more friendly name.
     *
     * @param user User which translation set will be used.
     * @param entity entity that requires prettifying.
     * @return Clean and readable entity name.
     */
    public static String prettifyObject(User user, EntityType entity)
    {
        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [entity]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.ENTITIES + entity.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [entity]: [name]

        translation = user.getTranslationOrNothing(Constants.ENTITIES + entity.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // biomes:
        //   [entity]: [name]

        translation = user.getTranslationOrNothing("entities." + entity.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(entity.name());
    }


// ---------------------------------------------------------------------
// Section: Messages
// ---------------------------------------------------------------------


    /**
     * Send given message to user and add prefix to the start of the message.
     *
     * @param user User who need to receive message.
     * @param message String of message that must be send.
     */
    public static void sendMessage(User user, String message)
    {
        user.sendMessage(user.getTranslation(Constants.CONVERSATIONS + "prefix") + message);
    }


    /**
     * Send unlock message for user with given UUID.
     *
     * @param uuid the uuid
     * @param island the island
     * @param generator the generator
     * @param addon instance of stone generator addon.
     * @param available the available
     */
    public static void sendUnlockMessage(UUID uuid,
        Island island,
        GeneratorTierObject generator,
        StoneGeneratorAddon addon,
        boolean available)
    {
        User user = User.getInstance(uuid);

        WorldSettings settings = addon.getPlugin().getIWM().getWorldSettings(island.getWorld());

        if (settings != null && user != null && user.isOnline())
        {
            TextComponent component;

            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("/");
            commandBuilder.append(settings.getPlayerCommandAliases().split(" ")[0]);
            commandBuilder.append(" ");
            commandBuilder.append(addon.getSettings().getPlayerMainCommand().split(" ")[0]);
            commandBuilder.append(" ");

            if (!available)
            {
                component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-purchase",
                    Constants.GENERATOR, generator.getFriendlyName(),
                    Constants.NUMBER, String.valueOf(generator.getGeneratorTierCost())));

                commandBuilder.append(addon.getSettings().getPlayerBuyCommand().split(" ")[0]);
            }
            else
            {
                if (addon.isVaultProvided() && generator.getActivationCost() > 0)
                {
                    component =
                        new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-activate-vault",
                            Constants.GENERATOR, generator.getFriendlyName(),
                            Constants.NUMBER, String.valueOf(generator.getActivationCost())));
                }
                else
                {
                    component =
                        new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-activate",
                            Constants.GENERATOR, generator.getFriendlyName()));
                }

                commandBuilder.append(addon.getSettings().getPlayerActivateCommand().split(" ")[0]);
            }

            commandBuilder.append(" ");
            commandBuilder.append(generator.getUniqueId());

            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandBuilder.toString()));

            user.getPlayer().spigot().sendMessage(component);
        }
    }
}
