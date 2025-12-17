package me.athulsib.stomcheat.check;

import lombok.Getter;
import me.athulsib.stomcheat.StomCheat;
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

    public void registerChecksFromPackage(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageName)
                .addScanners(Scanners.SubTypes));

        Set<Class<? extends Check>> checkClassesSet = reflections.getSubTypesOf(Check.class);
        checkClasses.addAll(checkClassesSet);
    }

    public void registerDefaultChecks() {
        StomCheat stomCheat = StomCheat.getInstance();
        if (stomCheat.getConfig().loadDefaultChecks()) {
            registerChecksFromPackage("me.athulsib.stomcheat.check.impl");
        }
    }

    public void registerCheck(Class<? extends Check> checkClass) {
        if (!checkClasses.contains(checkClass)) {
            checkClasses.add(checkClass);
        }
    }

    public void loadAllChecksToPlayer(User user) {
        user.getChecks().clear();
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

    public void loadCheckToPlayer(User user, Class<? extends Check> checkClass) {
        try {
            Check check = checkClass.getDeclaredConstructor().newInstance();
            check.setUser(user);
            user.getChecks().add(check);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadChecks() {
        checkClasses.clear();
        registerDefaultChecks();

    }
}
