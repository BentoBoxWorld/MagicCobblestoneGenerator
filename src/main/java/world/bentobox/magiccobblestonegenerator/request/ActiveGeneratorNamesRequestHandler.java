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
 *
 * @author AuroraLS3
 */
public class ActiveGeneratorNamesRequestHandler extends AddonRequestHandler {

    private static final Object WORLD_NAME = "world-name";
    private static final Object PLAYER = "player";
    private final StoneGeneratorAddon addon;

    public ActiveGeneratorNamesRequestHandler(StoneGeneratorAddon addon) {
        super("magic-generator-type");
        this.addon = addon;
    }

    @Override
    public Object handle(Map<String, Object> map) {
        /*
            What we need in the map:
            0. "world-name" -> String
            1. "player" -> UUID
            What we will return:
            - null if invalid input/player has no island
            - empty Collection if the player has no active magic generators on their island or player is offline.
            - Collection<String> of active magic generator names on their island.
         */

        if (map == null || map.isEmpty()) return null;
        Object worldName = map.get(WORLD_NAME);
        Object playerUUID = map.get(PLAYER);
        if (!(worldName instanceof String) || !(playerUUID instanceof UUID)) { // instanceof covers null cases
            return null;
        }
        World world = Bukkit.getWorld((String) worldName);
        if (world == null) return null;

        StoneGeneratorManager addonManager = addon.getAddonManager();
        GeneratorDataObject generatorData = addonManager.getGeneratorData(User.getInstance((UUID) playerUUID), world);

        if (generatorData == null) return Collections.emptySet();

        return generatorData.getActiveGeneratorList().stream()
                .map(addonManager::getGeneratorByID)
                .filter(Objects::nonNull)
                .map(GeneratorTierObject::getFriendlyName)
                .collect(Collectors.toSet());
    }
}