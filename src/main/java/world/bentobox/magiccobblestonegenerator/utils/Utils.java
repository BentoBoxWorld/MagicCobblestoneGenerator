//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.utils;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.*;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import sun.reflect.generics.tree.Tree;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This class contains some useful methods for addon.
 */
public class Utils
{
    /**
     * This method gets string value of given permission prefix. If user does not have
     * given permission or it have all (*), then return default value.
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
     * This method gets integer value of given permission prefix. If user does not have
     * given permission or it have all (*), then return default value.
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
     * This method replaces "[gamemode] and [number] in permission template with a requested
     * gamemode and empty space accordantly.
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
     * This method transforms given World into GameMode name. If world is not a GameMode
     * world then it returns null.
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
     * @param gameModeAddon GameMode which name must be returned.
     * @return GameMode name.
     */
    public static String getGameMode(GameModeAddon gameModeAddon)
    {
        return gameModeAddon.getDescription().getName();
    }


    /**
     * This method allows to get next value from array list after given value.
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
     * @return Map that contains relation from biome name to biome.
     */
    public static Map<String, Biome> getBiomeNameMap()
    {
        Biome[] biomes = Biome.values();

        Map<String, Biome> returnMap = new HashMap<>(biomes.length);

        for (Biome biome : biomes)
        {
            returnMap.put(biome.name(), biome);
        }

        return returnMap;
    }


    /**
     * This method converts given treeMap to pairList.
     * @param treeMap TreeMap that contains elements which must be translated to list.
     * @return PairList of elements from treeMap.
     */
    public static List<Pair<Material, Double>> treeMap2PairList(TreeMap<Double, Material> treeMap)
    {
        List<Pair<Material, Double>> returnList = new ArrayList<>(treeMap.size());
        
        if (treeMap.isEmpty())
        {
            // If map is empty, do not process anymore.
            return returnList;
        }
        
        Double previousValue = 0.0;
        
        while (!treeMap.isEmpty())
        {
            Map.Entry<Double, Material> entry = treeMap.pollFirstEntry();
            returnList.add(new Pair<>(entry.getValue(), entry.getKey() - previousValue));
            previousValue = entry.getKey();
        }
        
        return returnList;
    }


    /**
     * This method converts given pairList to actual TreeMap.
     * @param pairList PairList that contains elements which must be translated to map.
     * @return TreeMap of elements from pairList.
     */
    public static TreeMap<Double, Material> pairList2TreeMap(List<Pair<Material, Double>> pairList)
    {
        TreeMap<Double, Material> treeMap = new TreeMap<>(Double::compareTo);

        if (pairList.isEmpty())
        {
            // Nothing to process
            return treeMap;
        }

        Double nextMax = 0.0;

        for (Pair<Material, Double> pair : pairList)
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
     * Sanitizes the provided input.
     * It replaces spaces and hyphens with underscores and lower cases the input.
     * @param input input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input)
    {
        return input.toLowerCase(Locale.ENGLISH).replace(" ", "_").replace("-", "_");
    }


    /**
     * This method prettify given Biome name to more friendly name.
     * @param user User which translation set will be used.
     * @param biome Biome that requires prettifying.
     * @return Clean and readable biome name.
     */
    public static String prettifyObject(User user, Biome biome)
    {
        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.BIOMES + biome.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   biomes:
        //     [biome]: [name]

        translation = user.getTranslationOrNothing(Constants.BIOMES + biome.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // biomes:
        //   [biome]: [name]

        translation = user.getTranslationOrNothing("biomes." + biome.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }
        
        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(biome.name());
    }


    /**
     * This method prettify given material name to more friendly name.
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
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + material.name().toLowerCase() + ".name");

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
     * This method prettify given entity name to more friendly name.
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


    /**
     * Send given message to user and add prefix to the start of the message.
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
     * @param hasVault Indicates that vault addon is provided.
     * @param available the available
     */
    public static void sendUnlockMessage(UUID uuid, Island island, GeneratorTierObject generator, boolean hasVault, boolean available)
    {
        User user = User.getInstance(uuid);

        WorldSettings settings = BentoBox.getInstance().getIWM().getWorldSettings(island.getWorld());

        if (settings != null && user != null && user.isOnline())
        {
            TextComponent component;

            String command = "/" + settings.getPlayerCommandAliases().split(" ")[0];

            if (!available)
            {
                component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-purchase",
                    Constants.GENERATOR, generator.getFriendlyName(),
                    Constants.NUMBER, String.valueOf(generator.getGeneratorTierCost())));
                command += " generator buy ";
            }
            else
            {
                if (hasVault && generator.getActivationCost() > 0)
                {
                    component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-activate-vault",
                        Constants.GENERATOR, generator.getFriendlyName(),
                        Constants.NUMBER, String.valueOf(generator.getActivationCost())));
                }
                else
                {
                    component = new TextComponent(user.getTranslation(Constants.CONVERSATIONS + "click-text-to-activate",
                        Constants.GENERATOR, generator.getFriendlyName()));
                }

                command += " generator activate ";
            }

            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + generator.getUniqueId()));

            user.getPlayer().spigot().sendMessage(component);
        }
    }
}
