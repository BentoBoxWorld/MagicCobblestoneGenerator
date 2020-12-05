# Magic Cobblestone Generator Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/MagicCobblestoneGenerator)](https://ci.codemc.org/job/BentoBoxWorld/job/MagicCobblestoneGenerator/)

Add-on for BentoBox to provide ability to generate additional block types in cobblestone generators for any BentoBox GameMode. For example, have diamond ore generated, or iron ore. It's magic!  

## Where to find

You can download it from [Release tab](https://github.com/BentoBoxWorld/MagicCobblestoneGenerator/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/MagicCobblestoneGenerator/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/MagicCobblestoneGenerator/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

## What to expect

Magic Cobblestone Generator is changed a lot since it first creation, and since Minecraft 1.16, it now operates with all 3 lava based block generations:
- Classic cobblestone generation
- Stone generation
- New basalt generation

Addon also does not work in static progression based on island level. Now it allows users to toggle which generator they want to activate.

Server owner can specify different block generation for each generator, as well as set up small chance to generate treasure on block generation.

Generators can also be limited by a biome and permissions, as well as old island level. 

Monetary system is implemented and now users can purchase generators. Server owners can also enable payment cost for activating each generator.

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml and generatorTemplate.yml
4. Edit the config.yml and generatorTemplate.yml how you want.
5. Restart the server

To change generators, you can simply edit generatorTemplate.yml at any point. However, to apply changes for GameMode addon, you need to write `/[gamemode_admin_command] generator import`.
Admins can also add/edit/remove generators using AdminGUI.

Magic Cobblestone Generator does not limit how many generators are active. You can activate multiple generators at the same time, but only best suitable for given location will be used.
Users have a nice GUI that allows interacting with generators by executing command: `/[gamemode_user_command] generator`.
By right clicking on GUI element, users can see detailed information about each generator. However, the same view can be accessed with `/[gamemode_user_command] generator view <generator>` command.

There are 2 new permissions that could be useful to customize experience:
- `[gamemode].stone-generator.active-generators.[NUMBER]` - permission for island owner that allows increasing active generator number.
- `[gamemode].stone-generator.max-range.[NUMBER]` - permission for island owner that allows increasing range in which generator will work. Be aware, it must be enabled in the config.
- `[gamemode].stone-generator.bundle.[bundle_id]` - permission for island owner that allows to set specific generator bundle that will work on his island.

## Bundles

Bundles are a new feature in this addon. It allows specifying generators that can be used per island. Players will be able to see only generators that are assigned to the bundle.
Bundles can be created with template file, or using new Admin Menu.

## Placeholders

Magic Cobblestone Generator have 5 placeholders:
- `[gamemode]_magiccobblestonegenerator_active_generator_count` - Returns number of currently active generator tiers.
- `[gamemode]_magiccobblestonegenerator_max_active_generator_count` - Returns number of maximal amount of active generator tiers.
- `[gamemode]_magiccobblestonegenerator_active_generator_names` - Returns text that contains all active generator names separated with `,`.
- `[gamemode]_magiccobblestonegenerator_unlocked_generator_names` - Returns text that contains all unlocked generator names separated with `,`.
- `[gamemode]_magiccobblestonegenerator_purchased_generator_names` - Returns text that contains all purchased generator names separated with `,`.

## Compatibility

- [x] BentoBox - 1.15 version

## Config.yml

The config.yml has the following sections:

* **Offline Generation** - ability to disable addon processing on islands where none of members are online.
* **Physic Usage** - ability to specify if block generation should use game physic. Disabling will allow floating gravel, however some redstone machines may stop working.
* **Working Range** - specify default working range for the generator to work. If players are not in the given range, then generator will not work.
* **Active Generator Count** - specify default amount of generators users can activate at the same time.
* **Notify On Unlock** - allows to toggle if message about unlocked generators should be send to island members.
* **Show Filters in GUI** - allows to toggle if in player generator selection view filters should be visible.
* **User GUI border block** - allows to change border block around player menus. 
* **Border block name** - allows to change border block name in player menus.

As well as addon contains section that allows to change command aliases. Changing them requires server restart.

## generatorTemplate.yml

This is just a template that shows how each generator is set up.
Only generator ID must be specified. Other parts can be skipped.
```
  # Unique Id for generator. Used in internal storage and accessing to each generator data.
  generator_unique_id: 
    # Display name for users. Supports colour codes.
    # Default value: generator_unique_id without _
    name: "Something fancy"
    # Description in lore message. Supports colour codes.
    # Can be defined empty by replacing eveything with [].
    # Default value: []
    description: -|
      First Line Of lore Message
      &2Second Line Of lore Message
    # Icon used in GUI's. Number at the end allows to specify stack size for item.
    # Default value: Paper.
    icon: "PAPER:1"
    # Generator type: COBBLESTONE, STONE or BASALT. Self explanatory.
    # Default value: COBBLESTONE
    type: COBBLESTONE
    # Indicates if genertor is default generator. Default generators ignores requirement section.
    # It is activated for each new island. Can be only one per each generator type.
    # Default value: false
    default: false
    # Users selects active generators.
    # Priority indicates which generator will be used
    # if multiple of them fulfills requirements.
    # Default value: 1
    priority: 1
    # There are several requirements that can be defined here.
    requirements:
      # Can define minimal island level for generator to work. Required Level Addon.
      # Default value: 0
      island-level: 10
      # List of required permissions for users to select this generator.
      # Default value: []
      required-permissions: []
      # List of required biomes for generator to work.
      # Empty means that there is no limitation in which biome generator works.
      # Default value: [].
      required-biomes: []
      # Cost for purchasing this generator. Requires Vault and any economy plugin.
      # Currently implemented by clicking on purchase icon in generator view GUI.
      # Default value: 0
      purchase-cost: 5.0
    # Cost for activating current generator tier. Requires Vault and any economy plugin.
    # Will be payed only on active switching between generators.
    # Default value: 0.
    activation-cost: 0.0
    # Materials and their chances. Use actual blocks please.
    # Chance supports any positive number, including double value.
    # Everything in the end will be normalized.
    # Default value: []
    blocks:
      FIRST_BLOCK_NAME_ID: NUMBER
      SECOND_BLOCK_NAME_ID: NUMBER
    # Treasure that has a chance to be dropped when block is generated.
    # ONLY on generation, not on block break.
    # Default value: []
    treasure:
      # Chance from 0 till 1. 0 - will not be possible to get a treasure.
      # Default value: 0
      chance: 0.001
      # Materials that can be dropped. Applies to the same rules as block section.
      # Default value: []
      material:
        FIRST_BLOCK_NAME_ID: NUMBER
        SECOND_BLOCK_NAME_ID: NUMBER
      # Maximal amount of items dropped.
      # It will be from 1 till defined amount.
      # Default value: 1
      amount: 1
```

Bundles can be created in using the same template file in the section below all generators.
Bundles are optimal, and addon can work without them.
```
  # bundle_id
  bundle_unique_id:
    # Display name for users
    name: "Something fancy"
    # Description in lore message. Supports colour codes.
    # Can be defined empty by replacing eveything with [].
    # Default value: []
    description: -|
      First Line Of lore Message
      &2Second Line Of lore Message
    # Icon used in GUI's. Number at the end allows to specify stack size for item.
    # Default value: Paper.
    icon: "PAPER:1"
    # List of generators that bundle will work have access.
    generators:
      - generator_id_1
      - generator_id_2
```

## Information

More information can be found in [Wiki Pages](https://docs.bentobox.world/en/latest/addons/MagicCobblestoneGenerator/).
