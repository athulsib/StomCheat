package me.athulsib.stomcheat.check.impl.movement.speed;


import me.athulsib.stomcheat.check.Check;
import me.athulsib.stomcheat.check.CheckData;
import me.athulsib.stomcheat.processor.impl.MovementProcessor;
import me.athulsib.stomcheat.utils.PacketUtil;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;

@CheckData(
        name = "Speed",
        type = "A",
        description = "Basic air prediction modification check",
        experimental = true,
        punishmentVL = 12)
public class SpeedA extends Check {

    private double threshold;
    private int exemptTeleportTicks;

    @Override
    public void onPacket(PlayerPacketEvent event) {
        switch (PacketUtil.toPacketReceive(event)) {
            case CLIENT_LOOK:
            case CLIENT_POSITION:
            case CLIENT_POSITION_LOOK: {

                this.exemptTeleportTicks -= Math.min(this.exemptTeleportTicks, 1);

                if (this.exemptTeleportTicks > 0
                        || getUser().getPlayer().isAllowFlying()
                        || getUser().getPlayer().getGameMode() != GameMode.SURVIVAL) {
                    this.threshold = 0;
                    return;
                }

                MovementProcessor movementProcessor = (MovementProcessor) getUser().getProcessor("movement_processor");

                boolean ground = movementProcessor.getTo().isOnGround();
                boolean lastGround = movementProcessor.getFrom().isOnGround();

                double deltaXZ = movementProcessor.getDeltaXZ();
                double lastDeltaXZ = movementProcessor.getLastDeltaXZ();

                double previousPredicted = (lastDeltaXZ * 0.91F) + 0.026F;

                if (Math.abs(previousPredicted) < 0.005) {
                    previousPredicted = 0.0;
                }

                double totalDifference = deltaXZ - previousPredicted;

                if (!ground && !lastGround) {
                    if (totalDifference > 1e-12) {
                        if (++this.threshold > 3.5) {
                            this.fail("Not following proper friction in air",
                                    "d=" + totalDifference,
                                    "t=" + this.threshold);
                        }
                    } else {
                        this.threshold -= Math.min(this.threshold, .01);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, .001);
                }

                break;
            }
        }
    }

    @Override
    public void onPacket(PlayerPacketOutEvent event) {
        switch (PacketUtil.toPacketSend(event)) {

            case SERVER_POSITION: {

                //bad way to exempt, not spoon-feeding you code.
                this.exemptTeleportTicks = 20;

                break;
            }
        }
    }
}