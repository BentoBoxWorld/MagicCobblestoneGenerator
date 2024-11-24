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
    private Addon addon;

    @Override
    public Addon getAddon()
    {
        if (addon == null) {
            addon = new StoneGeneratorAddon();
        }
        return addon;
    }
}
