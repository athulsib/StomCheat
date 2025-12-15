package me.athulsib.stomcheat.processor;

import lombok.Getter;
import lombok.Setter;
import me.athulsib.stomcheat.user.User;

import java.lang.reflect.Constructor;

public class ProcessorEntry {

    @Getter
    private final Class<? extends Processor> processorClass;

    @Getter
    @Setter
    private int priority;

    @Getter
    private final ProcessorType type;

    public ProcessorEntry(Class<? extends Processor> processorClass) {
        ProcessorData data = processorClass.getAnnotation(ProcessorData.class);

        this.processorClass = processorClass;
        this.priority = data.priority();
        this.type = data.type();
    }

    public Processor instantiateProcessor(User user) {
        try {
            Constructor<? extends Processor> constructor =
                    processorClass.getDeclaredConstructor(User.class);

            return constructor.newInstance(user);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
