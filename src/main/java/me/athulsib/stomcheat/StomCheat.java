package me.athulsib.stomcheat;

import me.athulsib.stomcheat.config.ACConfig;
import me.athulsib.stomcheat.config.ConfigLoader;
import me.athulsib.stomcheat.minestom.MinestomListener;
import me.athulsib.stomcheat.check.CheckManager;
import me.athulsib.stomcheat.packet.PacketListener;
import me.athulsib.stomcheat.processor.ProcessorManager;
import me.athulsib.stomcheat.thread.ThreadManager;
import me.athulsib.stomcheat.user.UserManager;
import lombok.Getter;
import me.athulsib.stomcheat.commands.ACInfoCommand;
import net.minestom.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class StomCheat {

    @Getter
    private static StomCheat instance;
    private ProcessorManager processorManager;
    private CheckManager checkManager;

    private ConfigLoader configLoader;
    private ACConfig acConfig;
    private StomCheatConfig config;

    private final ThreadManager threadManager = new ThreadManager();
    private final UserManager userManager = new UserManager();
    private final ScheduledExecutorService checkService = Executors.newSingleThreadScheduledExecutor();

    public void enable() {
        try {
            instance = this;

            this.configLoader = new ConfigLoader();
            this.acConfig = this.configLoader.loadConfig();

            this.config = new StomCheatConfig();

            new PacketListener();

            //Register listener for join and quit events.
            new MinestomListener();

            this.processorManager = new ProcessorManager();
            this.checkManager = new CheckManager();

            this.processorManager.registerDefaultProcessors();

            //Load the checks separate from the player to make it more accessible
            this.checkManager.registerDefaultChecks();

            //TODO Add more commands
            // Register commands
            MinecraftServer.getCommandManager().register(new ACInfoCommand());

            //Reset all check violation over time
            this.checkService.scheduleAtFixedRate(() -> getUserManager().getUserMap().forEach((uuid, user) ->
                            user.getChecks().forEach(check -> check.setViolations(0))),
                    1L, 10L, TimeUnit.MINUTES);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void disable() {
        this.checkService.shutdown();
        this.threadManager.shutdown();
    }
}
