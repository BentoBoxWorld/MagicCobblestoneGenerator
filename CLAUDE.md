# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=StoneGeneratorManagerTest

# Run a single test method
mvn test -Dtest=StoneGeneratorManagerTest#testMethodName

# Full verify (tests + coverage)
mvn verify
```

## Architecture Overview

**MagicCobblestoneGenerator** is a [BentoBox](https://github.com/BentoBoxWorld/BentoBox) addon that replaces vanilla cobblestone/stone/basalt generation with configurable weighted-random block drops, per-island generator tiers, and economy/level-gated unlocking.

### Core Flow

1. `VanillaGeneratorListener` catches `BlockFormEvent` (lava+water → block)
2. Delegates to `MagicGenerator` task, which uses weighted random selection to pick a replacement block from the active generator's block/treasure lists
3. Active generators per island are stored in `GeneratorDataObject` (database-backed)

### Key Classes

- **`StoneGeneratorAddon`** — main addon class; registers commands, listeners, flags, placeholders, hooks into GameMode addons
- **`StoneGeneratorPladdon`** — Paper plugin loader (`extends Pladdon`) that instantiates the addon
- **`StoneGeneratorManager`** — central singleton; all CRUD for generators/islands, economy integration, activation logic
- **`MagicGenerator`** — performs actual block replacement (weighted random from active tiers)
- **`Settings`** (`config/Settings.java`) — `@ConfigObject`-annotated YAML config

### Data Model (3 `@Table` database objects)

| Class | Purpose |
|---|---|
| `GeneratorTierObject` | Definition: blocks with chances, treasure, requirements (level/perms/cost) |
| `GeneratorDataObject` | Per-island state: which generators are active/unlocked/purchased |
| `GeneratorBundleObject` | Named collections of generator tiers assigned to islands |

`database/objects/` also holds `GeneratorExhaustionData` — a plain (non-`@Table`) data holder, not a persisted database object.

### Package Map

```
commands/admin/     — Admin CLI commands
commands/player/    — Player CLI commands
config/             — Settings (@ConfigObject YAML config)
panels/admin/       — Admin GUI panels (PanelUtils-based)
panels/player/      — Player GUI panels
listeners/          — Event listeners (block form, join/leave, island level)
managers/           — StoneGeneratorManager, StoneGeneratorImportManager
tasks/              — MagicGenerator (block replacement logic)
database/objects/   — GeneratorTierObject, GeneratorDataObject, GeneratorBundleObject, GeneratorExhaustionData
database/adapters/  — Custom DB adapters (GeneratorTierAdapter)
events/             — GeneratorActivationEvent, GeneratorUnlockEvent, GeneratorBuyEvent
request/            — API request handlers for cross-addon data queries
utils/              — Constants, Pair, Utils, Why helpers
web/                — WebManager for online generator library
```

### Optional Integrations

- **Level addon** — island level gates generator unlocking
- **Vault** — economy for purchasing generators
- **Bank addon** — alternative economy backend

### Test Setup

Tests use **JUnit 5 + Mockito 5 + MockBukkit** (`org.mockbukkit.mockbukkit:mockbukkit-v1.21`). There is no PowerMock — static methods are stubbed with Mockito's built-in `mockStatic`, and static singletons (e.g. `BentoBox.instance`, `RanksManager.instance`) are injected via reflection. The Maven Surefire config opens numerous JDK internal modules for the mocking/instrumentation libraries.

Shared scaffolding lives in the addon's test package (`src/test/java/world/bentobox/magiccobblestonegenerator/`):

- **`CommonTestSetup`** — abstract base; subclass it and call `super.setUp()` from a `@BeforeEach`. Spins up a MockBukkit `ServerMock`, mocks `Bukkit`/`Util` statically, injects the `BentoBox` and `RanksManager` singletons, and wires common managers (IWM, islands, players, locales).
- **`TestWorldSettings`** — minimal `WorldSettings` implementation.
- **`WhiteBox`** — reflection helper for setting private static fields.

New tests should follow `StoneGeneratorManagerTest` / `GeneratorAdminCommandTest` (both extend `CommonTestSetup`). A focused test that needs different wiring can manage its own MockBukkit lifecycle instead — see `StoneGeneratorImportManagerTest`.

**Gotchas:**
- `CommonTestSetup` starts the MockBukkit server **before** `MockitoAnnotations.openMocks`, because some database objects (e.g. `GeneratorBundleObject`) build an `ItemStack` in a static initializer that must run against a live server.
- Static `final` fields (e.g. `Registry.BIOME`) can't be overwritten by reflection on Java 21+. Stub the static *method* that reads them instead — the importer test stubs `Utils.getBiomeNameMap()` rather than the field.

## Dependency Source Lookup

When you need to inspect source code for a dependency (e.g., BentoBox, addons):

1. **Check local Maven repo first**: `~/.m2/repository/` — sources jars are named `*-sources.jar`
2. **Check the workspace**: Look for sibling directories or Git submodules that may contain the dependency as a local project (e.g., `../bentoBox`, `../addon-*`)
3. **Check Maven local cache for already-extracted sources** before downloading anything
4. Only download a jar or fetch from the internet if the above steps yield nothing useful

Prefer reading `.java` source files directly from a local Git clone over decompiling or extracting a jar.

In general, the latest version of BentoBox should be targeted.

## Project Layout

Related projects are checked out as siblings under `~/git/`:

**Core:**
- `bentobox/` — core BentoBox framework

**Game modes:**
- `addon-acidisland/` — AcidIsland game mode
- `addon-bskyblock/` — BSkyBlock game mode
- `Boxed/` — Boxed game mode (expandable box area)
- `CaveBlock/` — CaveBlock game mode
- `OneBlock/` — AOneBlock game mode
- `SkyGrid/` — SkyGrid game mode
- `RaftMode/` — Raft survival game mode
- `StrangerRealms/` — StrangerRealms game mode
- `Brix/` — plot game mode
- `parkour/` — Parkour game mode
- `poseidon/` — Poseidon game mode
- `gg/` — gg game mode

**Addons:**
- `addon-level/` — island level calculation
- `addon-challenges/` — challenges system
- `addon-welcomewarpsigns/` — warp signs
- `addon-limits/` — block/entity limits
- `addon-invSwitcher/` / `invSwitcher/` — inventory switcher
- `addon-biomes/` / `Biomes/` — biomes management
- `Bank/` — island bank
- `Border/` — world border for islands
- `Chat/` — island chat
- `CheckMeOut/` — island submission/voting
- `ControlPanel/` — game mode control panel
- `Converter/` — ASkyBlock to BSkyBlock converter
- `DimensionalTrees/` — dimension-specific trees
- `discordwebhook/` — Discord integration
- `Downloads/` — BentoBox downloads site
- `DragonFights/` — per-island ender dragon fights
- `ExtraMobs/` — additional mob spawning rules
- `FarmersDance/` — twerking crop growth
- `GravityFlux/` — gravity addon
- `Greenhouses-addon/` — greenhouse biomes
- `IslandFly/` — island flight permission
- `IslandRankup/` — island rankup system
- `Likes/` — island likes/dislikes
- `Limits/` — block/entity limits
- `lost-sheep/` — lost sheep adventure
- `MagicCobblestoneGenerator/` — custom cobblestone generator
- `PortalStart/` — portal-based island start
- `pp/` — pp addon
- `Regionerator/` — region management
- `Residence/` — residence addon
- `TopBlock/` — top ten for OneBlock
- `TwerkingForTrees/` — twerking tree growth
- `Upgrades/` — island upgrades (Vault)
- `Visit/` — island visiting
- `weblink/` — web link addon
- `CrowdBound/` — CrowdBound addon

**Data packs:**
- `BoxedDataPack/` — advancement datapack for Boxed

**Documentation & tools:**
- `docs/` — main documentation site
- `docs-chinese/` — Chinese documentation
- `docs-french/` — French documentation
- `BentoBoxWorld.github.io/` — GitHub Pages site
- `website/` — website
- `translation-tool/` — translation tool

Check these for source before any network fetch.

## Key Dependencies (source locations)

- `world.bentobox:bentobox` → `~/git/bentobox/src/`
