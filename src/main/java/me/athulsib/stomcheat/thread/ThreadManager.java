package me.athulsib.stomcheat.thread;

import me.athulsib.stomcheat.StomCheat;
import me.athulsib.stomcheat.user.User;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

@Getter
public class ThreadManager {
    private final int threads = StomCheat.getInstance() != null && StomCheat.getInstance().getConfig() != null
            ? StomCheat.getInstance().getConfig().threadCount()
            : Math.min(Runtime.getRuntime().availableProcessors(), 16);
    private final List<Thread> userThreads = new CopyOnWriteArrayList<>();
    private final Map<User, Thread> playerThreadMap = new ConcurrentHashMap<>();
    private final Map<String, java.lang.Thread> workerThreadMap = new ConcurrentHashMap<>();

    public ThreadManager() {
        initializeThreads();
    }

    private void initializeThreads() {
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            String workerName = "StomCheat-Worker-" + finalI;
            java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                java.lang.Thread t = new java.lang.Thread(r);
                t.setName(workerName);
                t.setDaemon(true);
                workerThreadMap.put(workerName, t);
                return t;
            });
            Thread thread = new Thread(executor, workerName);
            userThreads.add(thread);
        }
    }

    public Thread assignThread(User playerData) {
        Thread assignedThread = getLeastLoadedThread();
        playerThreadMap.put(playerData, assignedThread);
        return assignedThread;
    }

    public Thread getPlayerThread(User playerData) {
        return playerThreadMap.get(playerData);
    }

    public void removePlayer(User playerData) {
        playerThreadMap.remove(playerData);
    }

    private Thread getLeastLoadedThread() {
        return userThreads.stream()
                .min((t1, t2) -> {
                    long count1 = playerThreadMap.values().stream().filter(t -> t == t1).count();
                    long count2 = playerThreadMap.values().stream().filter(t -> t == t2).count();
                    return Long.compare(count1, count2);
                })
                .orElse(userThreads.isEmpty() ? null : userThreads.get(0));
    }

    public void shutdown() {
        userThreads.forEach(thread -> thread.getExecutorService().shutdown());
        playerThreadMap.clear();
        workerThreadMap.clear();
    }
}