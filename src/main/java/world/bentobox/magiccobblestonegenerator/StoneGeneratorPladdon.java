//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.magiccobblestonegenerator;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


/**
 * @author tastybento
 */
public class StoneGeneratorPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new StoneGeneratorAddon();
    }
}
