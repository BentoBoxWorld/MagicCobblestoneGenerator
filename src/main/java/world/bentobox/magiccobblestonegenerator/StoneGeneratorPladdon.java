//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.magiccobblestonegenerator;


import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


/**
 * @author tastybento
 */
@Plugin(name="MagicCobblestoneGenerator", version="1.0")
@ApiVersion(ApiVersion.Target.v1_17)
public class StoneGeneratorPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new StoneGeneratorAddon();
    }
}
