package me.athulsib.stomcheat.user;

import me.athulsib.stomcheat.StomCheat;
import me.athulsib.stomcheat.thread.Thread;
import lombok.Getter;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {
    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    public void addUser(Player player) {
        User user = new User(player);

        // Assign a dedicated thread for this user
        Thread assigned = StomCheat.getInstance().getThreadManager().assignThread(user);
        user.setThread(assigned);

        this.userMap.put(player.getUuid(), user);
        StomCheat.getInstance().getCheckManager().loadAllChecksToPlayer(user);
        StomCheat.getInstance().getProcessorManager().loadAllProcessorsToPlayer(user);
    }

    public void removeUser(Player player) {
        User user = this.userMap.remove(player.getUuid());
        if (user != null) {
            StomCheat.getInstance().getThreadManager().removePlayer(user);
        }
    }

    public User getUser(Player player) {
        return this.userMap.get(player.getUuid());
    }

    public User getUser(UUID uuid) {
        return this.userMap.get(uuid);
    }
}
