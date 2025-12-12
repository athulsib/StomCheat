package me.athulsib.server;

import me.athulsib.server.commands.GamemodeCommand;
import me.athulsib.stomcheat.StomCheat;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;

public class SCDevServer {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Velocity("minestomdev"));
        TaskManager taskManager = new TaskManager();
        ScoreboardManager scoreboardManager = new ScoreboardManager(taskManager);


        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // Set the ChunkGenerator
        //instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instanceContainer.setChunkLoader(new AnvilLoader("actest"));

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(211, 62, 161));
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            taskManager.runAsync(() -> {
                event.getPlayer().setSkin(PlayerSkin.fromUsername(event.getPlayer().getUsername()));
            });
        });

        // Remove placed blocks after 5 seconds and return the item
        globalEventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            final Instance instance = event.getInstance();
            final var blockPos = event.getBlockPosition();
            final Block placedBlock = event.getBlock();
            final Player player = event.getPlayer();
            final ItemStack handItem = player.getItemInHand(event.getHand());
            final ItemStack refund = handItem != null && !handItem.isAir() ? handItem.withAmount(1) : ItemStack.AIR;

            taskManager.runSyncLater(() -> {
                // Verify the block is still the same
                Block current = instance.getBlock(blockPos);
                if (!current.equals(placedBlock)) return;

                // Remove the block
                instance.setBlock(blockPos, Block.AIR);

                // Return the item or drop it
                if (!refund.isAir()) {
                    boolean success = player.isOnline() && player.getInventory().addItemStack(refund);
                    if (!success) {
                        ItemEntity drop = new ItemEntity(refund);
                        drop.setInstance(instance, new Pos(blockPos.x() + 0.5, blockPos.y() + 0.75, blockPos.z() + 0.5));
                    }
                }
            }, 5000);
        });

        MinecraftServer.getCommandManager().register(new GamemodeCommand());

        StomCheat stomCheat = new StomCheat();
        stomCheat.enable();

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25568);
    }
}
