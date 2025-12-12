package me.athulsib.stomcheat.check.impl.misc;


import me.athulsib.stomcheat.check.Check;
import me.athulsib.stomcheat.check.CheckData;
import me.athulsib.stomcheat.utils.PacketUtil;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.instance.block.Block;

@CheckData(
        name = "Scaffold",
        type = "A",
        description = "Placed block without looking at it",
        experimental = true,
        punishmentVL = 10)
public class ScaffoldA extends Check {

    private double threshold;
    private int exemptTeleportTicks;

    @Override
    public void onPacket(PlayerPacketEvent event) {
        switch (PacketUtil.toPacketReceive(event)) {
            case CLIENT_LOOK:
            case CLIENT_POSITION:
            case CLIENT_POSITION_LOOK:
                this.exemptTeleportTicks -= Math.min(this.exemptTeleportTicks, 1);
                break;
            case CLIENT_BLOCK_PLACE: {
                if (this.exemptTeleportTicks > 0
                        || getUser().getPlayer().isAllowFlying()
                        || getUser().getPlayer().getGameMode() != GameMode.SURVIVAL) {
                    this.threshold = 0;
                    return;
                }

                Point blockPosition = getUser().getPlayer().getTargetBlockPosition(5);

                if (blockPosition == null) {
                    if (++this.threshold > 10) {
                        this.fail("Placed block without looking at it p=" + blockPosition + " t=" + this.threshold);
                    }
                } else {
                    this.threshold -= Math.min(this.threshold, 0.5);
                }
                break;
            }
        }
    }

}