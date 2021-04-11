package world.bentobox.magiccobblestonegenerator.request;

import org.bukkit.Bukkit;
import org.bukkit.World;
import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Request for active generator names.
 * Returns a list of active generator friendly names or null.
 * @author AuroraLS3
 */
public class ActiveGeneratorNamesRequestHandler extends AddonRequestHandler
{
    public ActiveGeneratorNamesRequestHandler(StoneGeneratorAddon addon)
    {
        super("magic-generator-type");
        this.addon = addon;
    }


    @Override
    public Object handle(Map<String, Object> map)
    {
        /*
            What we need in the map:
            0. "world-name" -> String
            1. "player" -> UUID
            What we will return:
            - null if invalid input/player has no island
            - empty Collection if the player has no active magic generators on their island or player is offline.
            - List<String> of active magic generator names on their island.
         */

        if (map == null || map.isEmpty())
        {
            return null;
        }

        Object worldName = map.get(WORLD_NAME);
        Object playerUUID = map.get(PLAYER);

        // Check for missing data.
        if (!(worldName instanceof String) || !(playerUUID instanceof UUID))
        {
            return null;
        }

        World world = Bukkit.getWorld((String) worldName);

        // Check for missing world.
        if (world == null)
        {
            return null;
        }

        StoneGeneratorManager addonManager = this.addon.getAddonManager();
        GeneratorDataObject generatorPlayerData = addonManager.getGeneratorData(User.getInstance((UUID) playerUUID), world);

        if (generatorPlayerData == null) 
        {
            return Collections.emptyList();
        }
        else
        {
            return generatorPlayerData.getActiveGeneratorList().stream().
                map(addonManager::getGeneratorByID).
                filter(Objects::nonNull).
                map(GeneratorTierObject::getFriendlyName).
                collect(Collectors.toList());
        }
    }


    /**
     * Instance of Addon
     */
    private final StoneGeneratorAddon addon;

    /**
     * World name constant
     */
    private static final Object WORLD_NAME = "world-name";

    /**
     * Player constant.
     */
    private static final Object PLAYER = "player";
}
