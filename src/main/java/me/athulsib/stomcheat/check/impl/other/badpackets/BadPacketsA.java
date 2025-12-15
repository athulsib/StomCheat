package me.athulsib.stomcheat.check.impl.other.badpackets;


import me.athulsib.stomcheat.check.Check;
import me.athulsib.stomcheat.check.CheckData;
import me.athulsib.stomcheat.processor.impl.MovementProcessor;
import me.athulsib.stomcheat.utils.PacketUtil;
import net.minestom.server.event.player.PlayerPacketEvent;

@CheckData(
        name = "BadPackets",
        type = "A",
        description = "Basic impossible pitch check",
        punishmentVL = 3)
public class BadPacketsA extends Check {

    @Override
    public void onPacket(PlayerPacketEvent event) {

        switch (PacketUtil.toPacketReceive(event)) {
            case CLIENT_LOOK:
            case CLIENT_POSITION:
            case CLIENT_POSITION_LOOK: {
                MovementProcessor movementProcessor = (MovementProcessor) getUser().getProcessor("movement_processor");

                double pitch = Math.abs(movementProcessor.getTo().getPitch());

                if (pitch > 90.0) {
                    this.fail("Impossible pitch rotation",
                            "pitch=" + pitch);
                }

                break;
            }
        }
    }
}
