package me.athulsib.stomcheat.processor;

import lombok.Getter;
import lombok.Setter;
import me.athulsib.stomcheat.event.Event;
import me.athulsib.stomcheat.user.User;

public class Processor extends Event {
    private final User user;

    @Getter
    private String name;

    @Getter
    private ProcessorType type;

    @Getter
    @Setter
    private int priority;

    public Processor(User user) {
        this.user = user;
        if (getClass().isAnnotationPresent(ProcessorData.class)) {
            ProcessorData processorData = getClass().getAnnotation(ProcessorData.class);

            this.name = processorData.name();
            this.type = processorData.type();
            this.priority = processorData.priority();
        }
    }
}
