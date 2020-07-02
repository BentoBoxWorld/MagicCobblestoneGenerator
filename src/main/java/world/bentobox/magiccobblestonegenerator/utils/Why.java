//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.utils;


import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.metadata.MetadataValue;
import java.util.UUID;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;


/**
 * Separate class that manages reports from Why Command.
 */
public class Why
{
	/**
	 * This method prints debug messages about specific user.
	 * @param addon Addon which are called
	 * @param user User who calls the command.
	 * @param location Location where something is happening.
	 * @param why Reason and its value for why.
	 */
	public static void report(Addon addon, @Nullable User user, @NotNull Location location, Reason why)
	{
		// A quick way to debug flag listener unit tests is to add this line here: System.out.println(why.name()); NOSONAR
		if (user != null &&
			user.isPlayer() &&
			user.getPlayer().getMetadata(location.getWorld().getName() + "_why_debug_generator").stream().
				filter(p -> p.getOwningPlugin().equals(addon.getPlugin())).
				findFirst().map(MetadataValue::asBoolean).
				orElse(false))
		{
			String whyEvent = "Why: " + why.getKey() + " in world " + location.getWorld().getName() + " at " + Util.xyz(location.toVector());
			String whyBypass = "Why: " + user.getName() + " - " + why.getValue();

			addon.log(whyEvent);
			addon.log(whyBypass);

			// See if there is a player that issued the debug
			String issuerUUID = user.getPlayer().getMetadata(location.getWorld().getName() + "_why_debug_generator_issuer").stream().
				filter(p -> addon.getPlugin().equals(p.getOwningPlugin())).
				findFirst().
				map(MetadataValue::asString).
				orElse("");

			if (!issuerUUID.isEmpty())
			{
				User issuer = User.getInstance(UUID.fromString(issuerUUID));

				if (issuer != null && issuer.isPlayer())
				{
					user.sendRawMessage(whyEvent);
					user.sendRawMessage(whyBypass);
				}
			}
		}
	}


	/**
	 * This is a general Reason enum that contains all possible values for reporting an debug message.
	 */
	public enum Reason
	{
		UNDEFINED("name", "value");


		Reason(String key, String value)
		{
			this.key = key;
			this.value = value;
		}


		public String getKey()
		{
			return key;
		}


		public String getValue()
		{
			return value;
		}


		/**
		 * This variable stores string text for key.
		 */
		private String key;

		/**
		 * This variable stores text.
		 */
		private String value;
	}
}
