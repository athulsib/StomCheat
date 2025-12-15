package me.athulsib.stomcheat.check.impl.misc;

import me.athulsib.stomcheat.check.Check;
import me.athulsib.stomcheat.check.CheckData;
import me.athulsib.stomcheat.processor.impl.MovementProcessor;
import me.athulsib.stomcheat.utils.PacketUtil;
import me.athulsib.stomcheat.utils.math.MathUtil;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Explanation
 * <p>
 * Scaffold is one of those cheats where you shouldn't just try value patching or simply verifying block placement mechanics.
 * Those checks are definitely useful but most clients can already bypass them with little effort. So instead of having 15 different
 * checks for validating block placements, we can have fewer heuristic checks that analyze player behavior instead of just physics.
 * <p>
 * This specific check analyzes the player's rotation patterns between placing blocks while bridging to see if it matches human-like aim
 * behavior. The idea is that when a human player places blocks while bridging, their pitch and yaw adjustments won't really vary much
 * but many hack clients will try to snap directly to block position or add weird rotations to seem legit. By using the Gini coefficient, we can
 * measure the inequality in the player's rotation adjustments. A high Gini coefficient indicates that a dataset is evenly distributed,
 * which is a strong sign of non-human behavior since human aim has acceleration and deceleration patterns.
 * <p>
 * This hasn't been tested much but this is minestom so modify & fine tune to add considerations based on your server's features.
 * Not a full check but the core concept is good enough to learn something from.
 * <p>
 * <a href="https://en.wikipedia.org/wiki/Gini_coefficient">...</a>
 * <p>
 * Credit: @Athulsib
 * */
@CheckData(
        name = "Scaffold",
        type = "B",
        description = "Heuristic rotation analysis",
        experimental = true,
        punishmentVL = 10)
public class ScaffoldB extends Check {

    private double threshold;
    private int exemptTeleportTicks;
    private List<Double> pitchRotations = new ArrayList<>();
   // private List<Double> yawRotations = new ArrayList<>();
    private long lastBlockPlaceTime;
    private boolean collectingRotations;

    @Override
    public void onPacket(PlayerPacketEvent event) {
        switch (PacketUtil.toPacketReceive(event)) {
            case CLIENT_POSITION:
                this.exemptTeleportTicks -= Math.min(this.exemptTeleportTicks, 1);
                break;
            case CLIENT_LOOK:
            case CLIENT_POSITION_LOOK:
                this.exemptTeleportTicks -= Math.min(this.exemptTeleportTicks, 1);
                if (collectingRotations) {
                    MovementProcessor movementProcessor = (MovementProcessor) getUser().getProcessor("movement_processor");

                    double deltaPitch = movementProcessor.getDeltaPitchAbs();
                    double deltaYaw = movementProcessor.getDeltaYawAbs();

                    // Only add non-zero rotations
                    if (deltaPitch > 0.001) pitchRotations.add(deltaPitch);
                    //if (deltaYaw > 0.001) yawRotations.add(deltaYaw);
                }
                break;

            case CLIENT_BLOCK_PLACE: {
                this.exemptTeleportTicks -= Math.min(this.exemptTeleportTicks, 1);
                if (this.exemptTeleportTicks > 0
                        || getUser().getPlayer().isAllowFlying()
                        || getUser().getPlayer().getGameMode() != GameMode.SURVIVAL) {
                    this.threshold = 0;
                    return;
                }

                ClientPlayerBlockPlacementPacket packet = (ClientPlayerBlockPlacementPacket) event.getPacket();
                BlockFace blockFace = packet.blockFace();
                int blocky = packet.blockPosition().blockY();
                Point playerPos = getUser().getPlayer().getPosition();
                boolean below = (playerPos.y() - 1.0) >= blocky;  // if block is below player's feet
                boolean inAir = getUser().getPlayer().getInstance().getBlock(packet.blockPosition().sub(0, 1.0, 0)).isAir(); // if block below placed is air (bridging)

                // Check only if player is bridging
                if (blockFace == BlockFace.TOP || !below || !inAir) {
                    collectingRotations = false;
                    return;
                }

                long currentTime = System.currentTimeMillis();
                // Check if within 3 seconds of last block place
                boolean withinTimeWindow = lastBlockPlaceTime != 0 && (currentTime - lastBlockPlaceTime) < 3000;

                // Calculate Gini coefficients if we have samples and within time window
                if (withinTimeWindow && /*!(yawRotations.isEmpty()) ||*/ !(pitchRotations.isEmpty())) {
                    double pitchGini = MathUtil.giniCoefficient(pitchRotations);
//                    double yawGini = MathUtil.giniCoefficient(yawRotations);  // not as relevant for this specific implementation

                    if (pitchGini >= 0.33) {
                        threshold++;
                    } else {
                        threshold -= Math.min(threshold, 0.33);
                    }

//                    if (yawGini >= 0.33) {
//                        threshold++;
//                    } else {
//                        threshold -= Math.min(threshold, 0.33);
//                    }

                    // Maybe would want this to be higher for production along with threshold increment changes. But this is fine for testing.
                    if (threshold >= 10) {
                        fail("Rotation Analysis");
                        threshold -= Math.min(threshold, 3);
                    }
                }

                // Reset for next tracking cycle
                pitchRotations.clear();
               // yawRotations.clear();
                lastBlockPlaceTime = currentTime;
                collectingRotations = true;

                break;
            }
        }
    }


    @Override
    public void onPacket(PlayerPacketOutEvent event) {
        switch (PacketUtil.toPacketSend(event)) {
            case SERVER_POSITION: {
                this.exemptTeleportTicks = 20;
                // Reset tracking state on teleport
                collectingRotations = false;
                pitchRotations.clear();
                //yawRotations.clear();
                lastBlockPlaceTime = 0;
                break;
            }
        }
    }
}