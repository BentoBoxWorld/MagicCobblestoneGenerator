//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.magiccobblestonegenerator.request;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This request handler returns data about requested generator. Data is not formatted.
 */
public class GeneratorDataRequestHandler extends AddonRequestHandler
{
    /**
     * Default constructor.
     */
    public GeneratorDataRequestHandler(StoneGeneratorAddon addon)
    {
        super("generator-data");
        this.addon = addon;
    }


    /**
     * This method handles addon request.
     */
    @Override
    public Object handle(Map<String, Object> metaData)
    {
        /*
            What we need in the map:
            0. "generator" -> String
            What we will return:
            - null if invalid input
            - Empty map, if there is no generator with given ID.
            - Map<String, Object> with all generator data.
         */

        if (metaData == null || metaData.isEmpty())
        {
            return null;
        }

        Object generatorID = metaData.get(GeneratorDataRequestHandler.GENERATOR);

        // Check for missing data.
        if (!(generatorID instanceof String))
        {
            return null;
        }

        GeneratorTierObject generatorTierObject = this.addon.getAddonManager().getGeneratorByID((String) generatorID);

        if (generatorTierObject == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            Map<String, Object> returnMap = new HashMap<>();

            returnMap.put("uniqueId", generatorTierObject.getUniqueId());
            returnMap.put("friendlyName", generatorTierObject.getFriendlyName());
            returnMap.put("description", generatorTierObject.getDescription());
            returnMap.put("generatorType", generatorTierObject.getGeneratorType().name());

            returnMap.put("generatorIcon", generatorTierObject.getGeneratorIcon());
            returnMap.put("lockedIcon", generatorTierObject.getLockedIcon());

            returnMap.put("defaultGenerator", generatorTierObject.isDefaultGenerator());
            returnMap.put("priority", generatorTierObject.getPriority());

            returnMap.put("requiredMinIslandLevel", generatorTierObject.getRequiredMinIslandLevel());
            returnMap.put("requiredBiomes", generatorTierObject.getRequiredBiomes());
            returnMap.put("requiredPermissions", generatorTierObject.getRequiredPermissions());
            returnMap.put("generatorTierCost", generatorTierObject.getGeneratorTierCost());
            returnMap.put("activationCost", generatorTierObject.getActivationCost());

            returnMap.put("deployed", generatorTierObject.isDeployed());

            returnMap.put("blockChanceMap", generatorTierObject.getBlockChanceMap());
            returnMap.put("treasureItemChanceMap", generatorTierObject.getTreasureItemChanceMap());
            returnMap.put("treasureChance", generatorTierObject.getTreasureChance());
            returnMap.put("maxTreasureAmount", generatorTierObject.getMaxTreasureAmount());

            return returnMap;
        }
    }


    /**
     * Instance of Addon
     */
    private final StoneGeneratorAddon addon;


    /**
     * Player constant.
     */
    private static final String GENERATOR = "generator";
}
