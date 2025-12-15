package me.athulsib.stomcheat.processor.impl;

import lombok.Getter;
import lombok.Setter;
import me.athulsib.stomcheat.processor.Processor;
import me.athulsib.stomcheat.processor.ProcessorData;
import me.athulsib.stomcheat.processor.ProcessorType;
import me.athulsib.stomcheat.user.User;
import me.athulsib.stomcheat.utils.PacketUtil;
import me.athulsib.stomcheat.utils.location.FlyingLocation;
import me.athulsib.stomcheat.wrapper.FlyingWrapper;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.play.ClientEntityActionPacket;

@Getter
@Setter

@ProcessorData(
        name = "movement_processor",
        priority = 0,
        type = ProcessorType.DEFAULT
)
public class MovementProcessor extends Processor {

    private final User data;
    private FlyingLocation to = new FlyingLocation();
    private FlyingLocation from = new FlyingLocation();
    private FlyingLocation fromFrom = new FlyingLocation();
    private FlyingWrapper lastflyingPacket;
    private long lastSprintMillis;
    private boolean sprinting;

    private double deltaX, deltaY, deltaZ, deltaXAbs, deltaZAbs, deltaYAbs, lastDeltaX, lastDeltaY, lastDeltaZ,
            lastDeltaXZ, lastDeltaYaw, lastDeltaPitch, lastDeltaYawAbs, lastDeltaPitchAbs,
            deltaXZ, deltaYaw, deltaPitch, deltaYawAbs, deltaPitchAbs;

    private int tick, sprintTicks;

    public MovementProcessor(User user) {
        super(user);
        this.data = user;
    }

    @Override
    public void onPacket(PlayerPacketEvent event) {
        switch (PacketUtil.toPacketReceive(event)) {
            case CLIENT_POSITION:
            case CLIENT_LOOK:
            case CLIENT_POSITION_LOOK: {
                FlyingWrapper flyingPacket = new FlyingWrapper(event);

                double x = flyingPacket.getX();
                double y = flyingPacket.getY();
                double z = flyingPacket.getZ();

                float pitch = flyingPacket.getPitch();
                float yaw = flyingPacket.getYaw();

                boolean ground = flyingPacket.isOnGround();

                this.fromFrom.setWorld(this.from.getWorld());
                this.from.setWorld(to.getWorld());
                this.to.setWorld(getData().getPlayer().getInstance());

                this.fromFrom.setOnGround(this.from.isOnGround());
                this.from.setOnGround(this.to.isOnGround());
                this.to.setOnGround(ground);

                this.fromFrom.setTick(this.from.getTick());
                this.from.setTick(this.to.getTick());
                this.to.setTick(this.tick);

                if (flyingPacket.hasPositionChanged(lastflyingPacket)) {

                    this.fromFrom.setPosX(this.from.getPosX());
                    this.fromFrom.setPosY(this.from.getPosY());
                    this.fromFrom.setPosZ(this.from.getPosZ());

                    this.from.setPosX(this.to.getPosX());
                    this.from.setPosY(this.to.getPosY());
                    this.from.setPosZ(this.to.getPosZ());

                    this.to.setPosX(x);
                    this.to.setPosY(y);
                    this.to.setPosZ(z);

                    this.lastDeltaX = this.deltaX;
                    this.lastDeltaY = this.deltaY;
                    this.lastDeltaZ = this.deltaZ;

                    this.deltaY = this.to.getPosY() - this.from.getPosY();
                    this.deltaX = this.to.getPosX() - this.from.getPosX();
                    this.deltaZ = this.to.getPosZ() - this.from.getPosZ();

                    this.deltaXAbs = Math.abs(this.deltaX);
                    this.deltaZAbs = Math.abs(this.deltaZ);
                    this.deltaYAbs = Math.abs(this.deltaY);

                    this.lastDeltaXZ = this.deltaXZ;

                    this.deltaXZ = Math.hypot(this.deltaXAbs, this.deltaZAbs);
                }

                if (flyingPacket.hasRotationChanged(lastflyingPacket)) {

                    this.fromFrom.setYaw(this.from.getYaw());
                    this.fromFrom.setPitch(this.from.getPitch());

                    this.from.setYaw(this.to.getYaw());
                    this.from.setPitch(this.to.getPitch());

                    this.to.setPitch(pitch);
                    this.to.setYaw(yaw);

                    this.lastDeltaYaw = this.deltaYaw;
                    this.lastDeltaPitch = this.deltaPitch;

                    this.deltaYaw = this.to.getYaw() - this.from.getYaw();
                    this.deltaPitch = this.to.getPitch() - this.from.getPitch();

                    this.lastDeltaYawAbs = this.deltaYawAbs;
                    this.lastDeltaPitchAbs = this.deltaPitchAbs;

                    this.deltaYawAbs = Math.abs(this.to.getYaw() - this.from.getYaw());
                    this.deltaPitchAbs = Math.abs(this.to.getPitch() - this.from.getPitch());
                }

                lastflyingPacket = flyingPacket;
                ++this.tick;
                if (sprinting) {
                    ++this.sprintTicks;
                } else {
                    this.sprintTicks = 0;
                }
                break;
            }
            case CLIENT_ENTITY_ACTION:
                ClientEntityActionPacket packet = (ClientEntityActionPacket) event.getPacket();
                switch (packet.action()) {
                    case START_SPRINTING -> {
                        sprinting = true;
                        lastSprintMillis = System.currentTimeMillis();
                    }
                    case STOP_SPRINTING -> {
                        sprinting = false;
                    }
                }
                break;
        }
    }

}
