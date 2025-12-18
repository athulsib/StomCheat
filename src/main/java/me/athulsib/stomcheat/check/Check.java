package me.athulsib.stomcheat.check;

import me.athulsib.stomcheat.StomCheat;
import me.athulsib.stomcheat.event.Event;
import me.athulsib.stomcheat.user.User;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Getter
@Setter
public abstract class Check extends Event {

    @Setter
    private User user;
    private CheckData data;
    private double violations;
    private double punishmentVL;
    private String checkName, checkType;
    private boolean enabled;
    private boolean experimental;
    public double buffer;

    public Check() {
        if (getClass().isAnnotationPresent(CheckData.class)) {
            this.data = getClass().getAnnotation(CheckData.class);

            this.punishmentVL = this.data.punishmentVL();
            this.checkName = this.data.name();
            this.checkType = this.data.type();
            this.enabled = this.data.enabled();
            this.experimental = this.data.experimental();
        }
    }


    public void fail(String... data) {
        this.violations += 1.0;

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : data) {
            stringBuilder.append(s).append(", ");
        }

        int ping = this.user.getPlayer().getLatency();

        String alert = StomCheat.getInstance().getConfig().alertMessage()
                .replace("%player%", this.user.getUserName())
                .replace("%check%", this.checkName)
                .replace("%type%", this.checkType)
                .replace("%vl%", String.valueOf(this.violations))
                .replace("%punishvl%", String.valueOf(this.punishmentVL))
                .replace("%experimental%", this.experimental ? StomCheat.getInstance().getConfig().experimental() : " ");

        String hoverTemplate = StomCheat.getInstance().getConfig().hover()
                .replace("%check%", this.checkName)
                .replace("%type%", this.checkType)
                .replace("%vl%", String.valueOf(this.violations))
                .replace("%ping%", String.valueOf(ping))
                .replace("%description%", this.data.description())
                .replace("%data%", stringBuilder.toString());

        Component hoverMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(hoverTemplate);


        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(alert)
                .hoverEvent(HoverEvent.showText(hoverMessage))
                .replaceText(builder -> builder.matchLiteral(String.valueOf(this.violations)).replacement(Component.text(String.valueOf(this.violations)).color(getViolationColor())))
                .replaceText(builder -> builder.matchLiteral(String.valueOf(this.punishmentVL)).replacement(Component.text(String.valueOf(this.punishmentVL)).color(getViolationColor())));

        // Send alerta via alert manager implementation
        StomCheat.getInstance().getAlertManager().sendStaffAlert(message);

        if (this.violations >= this.punishmentVL) {
            punish();
        }
    }

    public void punish() {
        String broadcast = StomCheat.getInstance().getConfig().broadcast();
        String kickMessage = StomCheat.getInstance().getConfig().kickMessage();


       // getUser().getPlayer().kick(LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage));

        StomCheat.getInstance().getUserManager().getUserMap().values().parallelStream().forEach(user ->
                user.getPlayer().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(String.format(broadcast, this.user.getUserName()))));

        // reset violations
        getUser().getChecks().parallelStream().forEach(check -> check.setViolations(0));
        getUser().getChecks().parallelStream().forEach(check -> check.setBuffer(0));
    }

    public TextColor getViolationColor() {
        double ratio = this.violations / this.punishmentVL;
        int red, green;

        if (ratio <= 0.5) {
            red = (int) (255 * (ratio / 0.5));
            green = 255;
        } else {
            red = 255;
            green = (int) (255 * ((1 - ratio) / 0.5));
        }

        return TextColor.color(red, green, 0);
    }
}