package me.athulsib.stomcheat.alert;

import net.kyori.adventure.text.Component;

/**
 * AlertManager is responsible for notifying staff/admins whenever a check is flagged.
 * The way it's meant to work is it broadcasts the alert to all players with a certain permission (e.g stomcheat.alerts).
 * However since Minestom does not have a permission manager by default, you need to create your own implementation of
 * AlertManager that integrates with your server's permission system.
 **/
public interface AlertManager {
    void sendStaffAlert(Component alertComponent);
}
