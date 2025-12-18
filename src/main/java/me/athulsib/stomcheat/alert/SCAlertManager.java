package me.athulsib.stomcheat.alert;

import me.athulsib.stomcheat.StomCheat;
import net.kyori.adventure.text.Component;

/**
* Default implementation of AlertManager that broadcasts alerts to the whole server.
**/
public class SCAlertManager implements AlertManager {

    @Override
    public void sendStaffAlert(Component alert) {
        StomCheat.getInstance().getUserManager().getUserMap().values().forEach(user -> {
            user.getPlayer().sendMessage(alert);
        });
    }
}
