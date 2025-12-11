package me.athulsib.stomcheat.check;

import lombok.Getter;
import me.athulsib.stomcheat.user.User;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CheckManager {

    @Getter
    public final List<Class<? extends Check>> checkClasses = new ArrayList<>();

    // yes im that lazy
    public void loadChecks() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("me.athulsib.stomcheat.check.impl")
                .addScanners(Scanners.SubTypes));

        Set<Class<? extends Check>> checkClassesSet = reflections.getSubTypesOf(Check.class);
        checkClasses.addAll(checkClassesSet);
    }

    public void loadToPlayer(User user) {
        List<Check> userChecks = new ArrayList<>();
        for (Class<? extends Check> checkClass : checkClasses) {
            try {
                Check check = checkClass.getDeclaredConstructor().newInstance();
                check.setUser(user);
                userChecks.add(check);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        user.getChecks().addAll(userChecks);
    }
}
