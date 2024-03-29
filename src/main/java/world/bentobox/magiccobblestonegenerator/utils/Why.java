//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.magiccobblestonegenerator.utils;


import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;


/**
 * Separate class that manages reports from Why Command.
 */
public class Why
{
    public static void report(@NotNull Location location, String why)
    {
        BentoBox.getInstance().getIslands().getIslandAt(location).ifPresent(island -> {
            if (island.getOwner() != null)
            {
                User owner = User.getInstance(island.getOwner());
                Why.report(StoneGeneratorAddon.getInstance(), owner, location, why);
            }
        });
    }


    public static void report(@NotNull Island island, Location location, String why)
    {
        if (island.getOwner() != null)
        {
            User owner = User.getInstance(island.getOwner());
            Why.report(StoneGeneratorAddon.getInstance(), owner, location, why);
        }
    }


    /**
     * This method prints debug messages about specific user.
     *
     * @param addon Addon which are called
     * @param user User who calls the command.
     * @param location Location where something is happening.
     * @param why Reason and its value for why.
     */
    public static void report(Addon addon, @Nullable User user, @NotNull Location location, String why)
    {
        // A quick way to debug flag listener unit tests is to add this line here: System.out.println(why.name()); NOSONAR
        if (user != null &&
            user.isPlayer() &&
            user.getPlayer().getMetadata(location.getWorld().getName() + "_why_debug_generator").stream().
                filter(p -> p.getOwningPlugin().equals(addon.getPlugin())).
                findFirst().map(MetadataValue::asBoolean).
                orElse(false))
        {
            String whyEvent = "Why: MagicCobblestoneGenerator in world " + location.getWorld().getName() + " at " +
                Util.xyz(location.toVector());
            String whyBypass = "Why: " + user.getName() + " - " + why;

            addon.log(whyEvent);
            addon.log(whyBypass);

            // See if there is a player that issued the debug
            String issuerUUID =
                user.getPlayer().getMetadata(location.getWorld().getName() + "_why_debug_generator_issuer").stream().
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
}
