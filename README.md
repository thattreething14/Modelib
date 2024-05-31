# Modelib 🧪
Modelib enables you to integrate custom models into Minecraft without requiring any additional mods! Unlike other model loading plugins, its primary focus is on achieving high performance regardless of the number of players on the server.

This plugin is a completely free alternative to model engine and is heavily inspired--all be it copied
from [FreeMinecraftModels](https://github.com/MagmaGuy/FreeMinecraftModels/tree/master) created by MagmaGuy, which I highly suggest you check out because the creator is pretty cool (Don't sue pls its only like 40% copied), however is changed 
heavily to work better with more people while providing better server and client side performance.
## Features 🚀
- **Spawn Testing**: Easily spawn mobs with custom attacks and models with ease!
- **Display Items**: Create unique static or animated models for decoration use!
## Getting Started 💡

To start using Modelib in your project, follow these steps:

1. Clone this repository and build the JAR.

2. Add the JAR file to your project dependencies.

   - **Gradle**:
     ```gradle
     implementation files('libs/Modelib-1.0-1.20.1.jar')
     ```

   - **Maven**:
     ```bash
     mvn install:install-file -Dfile=libs/Modelib-1.0-1.20.1.jar -DgroupId=tree -DartifactId=Modelib -Dversion=1.0-1.20.1 -Dpackaging=jar
     ```

     Afterward, initialize it as a dependency in the `pom.xml`:
     ```xml
     <dependency>
       <groupId>tree</groupId>
       <artifactId>Modelib</artifactId>
       <version>1.0-1.20.1</version>
     </dependency>
     ```

## API Usage 💻

### Example:
Here is a simple event listener I've written to make it spawn an entity when an administator throws a pearl.
```java
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType().equals(Material.ENDER_PEARL)) {
            Player player = event.getPlayer();
            if (player.isOp()) {
                // Create a StaticEntity at the pearl's landing location
                Location pearlLandingLocation = event.getPlayer().getEyeLocation();
                StaticEntity staticEntity = StaticEntity.Companion.create("entity_id", pearlLandingLocation);
                if (staticEntity != null) {
                    Bukkit.getLogger().info("Static entity created at " + pearlLandingLocation + "!");
                } else {
                    Bukkit.getLogger().warning("Failed to create static entity.");
                }
            }
        }
    }
```
## Support 💬

For any questions or issues, feel free to reach out to me via [Discord](https://discordapp.com/users/656998951358300191).
