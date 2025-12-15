package me.athulsib.stomcheat.processor.impl;

import me.athulsib.stomcheat.processor.Processor;
import me.athulsib.stomcheat.processor.ProcessorData;
import me.athulsib.stomcheat.processor.ProcessorType;
import me.athulsib.stomcheat.user.User;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;

@ProcessorData(
        name = "Test",
        priority = 1,
        type = ProcessorType.DEFAULT
)
public class TestProcessor extends Processor {

    public TestProcessor(User user) {
        super(user);
    }

    @Override
    public void onPacket(PlayerPacketEvent event) {
        super.onPacket(event);
    }

    @Override
    public void onPacket(PlayerPacketOutEvent event) {
        super.onPacket(event);
    }
}
