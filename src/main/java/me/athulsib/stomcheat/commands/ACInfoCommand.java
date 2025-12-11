package me.athulsib.stomcheat.commands;

import me.athulsib.stomcheat.StomCheat;
import me.athulsib.stomcheat.check.Check;
import me.athulsib.stomcheat.thread.Thread;
import me.athulsib.stomcheat.thread.ThreadManager;
import me.athulsib.stomcheat.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class ACInfoCommand extends Command {

    // Track previous CPU measurements for usage calculation
    private static final Map<String, AtomicLong> previousCpuTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastMeasurements = new ConcurrentHashMap<>();
    private static final long MEASUREMENT_INTERVAL_MS = 1000; // 1 second

    public ACInfoCommand() {
        super("acinfo", "stomcheatinfo", "aci");
        setDefaultExecutor((sender, context) -> sendInfo(sender));
        addSyntax((sender, ctx) -> sendThreadInfo(sender), ArgumentType.Literal("threads"));
    }

    private void sendInfo(CommandSender sender) {
        StomCheat sc = StomCheat.getInstance();
        if (sc == null) {
            sender.sendMessage(Component.text("[!] StomCheat is not initialized.").color(NamedTextColor.RED));
            return;
        }

        int checkCount = sc.getCheckManager() != null ? sc.getCheckManager().getCheckClasses().size() : 0;
        int userCount = sc.getUserManager() != null ? sc.getUserManager().getUserMap().size() : 0;
        ThreadManager tm = sc.getThreadManager();
        int threadCount = tm != null ? tm.getThreads() : 0;

        // Header
        sender.sendMessage(Component.text("\n"));
        sender.sendMessage(Component.text("══════ StomCheat ══════").color(NamedTextColor.AQUA));

        // Summary line
        sender.sendMessage(Component.text("✔ Checks: ").color(NamedTextColor.GRAY)
                .append(Component.text(checkCount).color(NamedTextColor.GREEN))
                .append(Component.text("   "))
                .append(Component.text("👤 Users: ").color(NamedTextColor.GRAY))
                .append(Component.text(userCount).color(NamedTextColor.YELLOW))
                .append(Component.text("   "))
                .append(Component.text("⚙ Threads: ").color(NamedTextColor.GRAY))
                .append(Component.text(threadCount).color(NamedTextColor.GOLD))
        );

        // Thread details
        if (tm != null) {
            sender.sendMessage(Component.text("\n").append(Component.text("Threads (CPU usage)").color(NamedTextColor.AQUA)));
            displayThreadInfo(sender, tm);
        }

        sender.sendMessage(Component.text("\n"));
    }

    private void sendThreadInfo(CommandSender sender) {
        ThreadManager tm = StomCheat.getInstance().getThreadManager();
        if (tm == null) {
            sender.sendMessage(Component.text("ThreadManager not available.").color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("\n").append(Component.text("StomCheat Threads (CPU usage)").color(NamedTextColor.AQUA)));
        displayThreadInfo(sender, tm);
        sender.sendMessage(Component.text("\n"));
    }

    private void displayThreadInfo(CommandSender sender, ThreadManager tm) {
        List<Thread> threads = new ArrayList<>(tm.getUserThreads());
        threads.sort(Comparator.comparing(Thread::getName));
        Map<User, Thread> map = tm.getPlayerThreadMap();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        boolean cpuEnabled = mxBean.isThreadCpuTimeSupported() && mxBean.isThreadCpuTimeEnabled();

        // Initialize CPU tracking for new threads
        long currentTime = System.currentTimeMillis();
        for (Thread th : threads) {
            String threadName = th.getName();
            previousCpuTimes.putIfAbsent(threadName, new AtomicLong(0));
            lastMeasurements.putIfAbsent(threadName, currentTime);
        }

        int barWidth = 20;
        for (Thread th : threads) {
            String threadName = th.getName();
            long userCount = map.values().stream().filter(t -> t == th).count();
            ExecutorService es = th.getExecutorService();
            boolean isTerminated = es.isTerminated();
            boolean isShutdown = es.isShutdown();

            long currentCpuTime = getCpuTimeForWorker(mxBean, tm, threadName);

            // Calculate CPU usage percentage
            double cpuUsage = 0.0;
            String cpuUsageText = "N/A";
            if (cpuEnabled) {
                cpuUsage = calculateCpuUsage(threadName, currentCpuTime, currentTime);
                cpuUsageText = String.format("%.1f%%", cpuUsage);
            }

            // Build usage bar based on percentage
            Component usageBar = buildUsageBar(cpuUsage, barWidth);

            TextColor statusColor = isTerminated ? NamedTextColor.DARK_GRAY :
                    (isShutdown ? NamedTextColor.GRAY : NamedTextColor.GREEN);

            Component line = Component.text("• ").color(NamedTextColor.DARK_GRAY)
                    .append(Component.text(threadName).color(NamedTextColor.GOLD))
                    .append(Component.text("  "))
                    .append(usageBar)
                    .append(Component.text(String.format("  %d users", userCount)).color(NamedTextColor.YELLOW))
                    .append(Component.text("  "))
                    .append(Component.text(cpuUsageText).color(NamedTextColor.WHITE))
                    .append(Component.text("  "))
                    .append(Component.text(isTerminated ? "TERMINATED" :
                            (isShutdown ? "SHUTDOWN" : "ACTIVE")).color(statusColor));

            sender.sendMessage(line);
        }
    }

    private double calculateCpuUsage(String threadName, long currentCpuTime, long currentTime) {
        AtomicLong previousCpuTime = previousCpuTimes.get(threadName);
        Long lastMeasurementTime = lastMeasurements.get(threadName);

        if (previousCpuTime == null || lastMeasurementTime == null) {
            return 0.0;
        }

        long timeDiff = currentTime - lastMeasurementTime;
        if (timeDiff < MEASUREMENT_INTERVAL_MS) {
            return 0.0;
        }

        long cpuDiff = currentCpuTime - previousCpuTime.get();
        if (cpuDiff < 0) {
            // Handle counter reset
            previousCpuTime.set(currentCpuTime);
            lastMeasurements.put(threadName, currentTime);
            return 0.0;
        }

        // Calculate CPU usage percentage
        double cpuUsage = (cpuDiff * 100.0) / (timeDiff * 1_000_000.0); // Convert ms to ns
        cpuUsage = Math.min(100.0, Math.max(0.0, cpuUsage)); // Clamp between 0-100%

        // Update tracking
        previousCpuTime.set(currentCpuTime);
        lastMeasurements.put(threadName, currentTime);

        return cpuUsage;
    }

    private long getCpuTimeForWorker(ThreadMXBean mxBean, ThreadManager tm, String workerName) {
        java.lang.Thread worker = tm.getWorkerThreadMap().get(workerName);
        if (worker == null) return 0L;
        long id = worker.getId();
        try {
            long cpuTime = mxBean.getThreadCpuTime(id);
            return cpuTime == -1 ? 0L : cpuTime;
        } catch (UnsupportedOperationException e) {
            return 0L;
        }
    }

    private Component buildUsageBar(double usagePercentage, int width) {
        int filled = (int) Math.min(width, Math.round(usagePercentage / 100.0 * width));
        int empty = width - filled;
        String bar = "█".repeat(filled) + "░".repeat(empty);

        // Color based on usage level
        TextColor color = usagePercentage < 30 ? NamedTextColor.GREEN :
                (usagePercentage < 70 ? NamedTextColor.YELLOW : NamedTextColor.RED);

        return Component.text("[" + bar + "]").color(color);
    }
}