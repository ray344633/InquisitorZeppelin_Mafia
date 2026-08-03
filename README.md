
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

## Architecture & Directory Structure
This mod follows an advanced **Package-by-Feature (Domain-Driven)** architectural pattern, which is considered the standard for highly scalable, large Minecraft mods (e.g. Create, Mekanism). 

This approach groups related classes by their actual in-game feature or system, rather than strictly by their technical type (like putting all items in one folder and all blocks in another).

When adding new files, please follow these guidelines on where to place them inside `src/main/java/com/scir4y/zeppelinmurdermod/`:

- **`content/`**: **Core Game Features**. Everything that exists in the world or inventories goes here, sub-divided by feature domain.
  - Examples: `content/elevator/` (holds elevator blocks, items, entities, logic), `content/note/`, `content/role/`.
- **`registry/`**: **Registries**. All central `DeferredRegister` classes (e.g., `ModBlocks`, `ModItems`). If you create a new item in `content/elevator/item/`, register it in `registry/ModItems.java`.
- **`system/`**: **Core Logic & Mechanics**. Cross-cutting game systems that manage state or mechanics (e.g., `system/game/GameState.java`, task managers, capabilities/attachments).
- **`client/`**: **Client-Side Only**. All rendering, models, GUI screens, and client event handlers. Mirrors the content structure (e.g., `client/renderer/elevator/`, `client/gui/`). This prevents dedicated server crashes.
- **`network/`**: Network packets (`payload/`) and handlers (`handler/`).
- **`command/`**: Server commands.
- **`config/`**: Configuration files.

For example:
- If you're adding a **new elevator button block**, place the class in `content/elevator/block/` and register it in `registry/ModBlocks.java`.
- If you're adding a **renderer** for that block, place it in `client/renderer/elevator/`.
