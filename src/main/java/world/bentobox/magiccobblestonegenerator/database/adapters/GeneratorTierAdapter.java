//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.database.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;


/**
 * This class allows to serialize generator tiers with their uniqueId and deserialize them.
 */
public class GeneratorTierAdapter implements JsonSerializer<Set<GeneratorTierObject>>, JsonDeserializer<Set<GeneratorTierObject>>
{
	@Override
	public JsonElement serialize(Set<GeneratorTierObject> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonArray generatorIds = new JsonArray();

		for (GeneratorTierObject generatorTierObject : src)
		{
			generatorIds.add(generatorTierObject.getUniqueId());
		}

		return generatorIds;
	}


	@Override
	public Set<GeneratorTierObject> deserialize(JsonElement json,
		Type typeOfT,
		JsonDeserializationContext context)
		throws JsonParseException
	{
		JsonArray jsonObject = json.getAsJsonArray();

		Set<GeneratorTierObject> generatorSet = new HashSet<>();

		jsonObject.forEach(jsonElement ->
		{
			GeneratorTierObject object =
				StoneGeneratorAddon.getInstance().getAddonManager().getGeneratorByID(jsonElement.getAsString());

			if (object != null)
			{
				generatorSet.add(object);
			}
		});

		return generatorSet;
	}
}