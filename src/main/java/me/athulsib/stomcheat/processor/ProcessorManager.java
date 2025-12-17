package me.athulsib.stomcheat.processor;

import lombok.Getter;
import me.athulsib.stomcheat.user.User;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;

//TODO: Custom Processor with no priority
public class ProcessorManager {

    @Getter
    private final List<ProcessorEntry> processorEntries = new ArrayList<>();

    public void registerProcessorsFromPackage(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageName)
                .filterInputsBy(new FilterBuilder().includePackage(packageName))
                .addScanners(Scanners.SubTypes));

        Set<Class<? extends Processor>> processorClassesSet = reflections.getSubTypesOf(Processor.class);
        processorClassesSet.forEach(this::registerProcessor);
    }

    public void registerDefaultProcessors() {
        registerProcessorsFromPackage("me.athulsib.stomcheat.processor.impl");
        sortProcessorsByPriority();
    }

    public void registerProcessor(Class<? extends Processor> processorClass) {
        if (!processorClass.isAnnotationPresent(ProcessorData.class)) return;
        ProcessorEntry entry = new ProcessorEntry(processorClass);

        switch (entry.getType()) {
            case DEFAULT -> {
                boolean duplicatedPriority = processorEntries.stream().anyMatch(p -> p.getPriority() == entry.getPriority());
                if (duplicatedPriority) {
                    System.out.println("Warning: Duplicate priority detected for " +
                            processorClass.getSimpleName() +
                            ". It will not be added to the processors!");
                    return;
                }
                processorEntries.add(entry);
            }
            case CUSTOM -> {
                int targetPriority = entry.getPriority();

                processorEntries.forEach(p -> {
                    if (p.getPriority() >= targetPriority) {
                        p.setPriority(p.getPriority() + 1);
                    }
                });

                processorEntries.add(entry);
                sortProcessorsByPriority();
            }
        }
    }

    public void loadAllProcessorsToPlayer(User user) {
        user.getProcessors().clear();

        List<Processor> processors = new ArrayList<>();
        Map<String, Processor> processorMap = new HashMap<>();
        processorEntries.forEach(entry -> {
            Processor processor = entry.instantiateProcessor(user);
            processors.add(processor);
            processorMap.put(processor.getName(), processor);
        });

        user.setProcessors(processors);
        user.setProcessorMap(processorMap);
    }

    private void sortProcessorsByPriority() {
        processorEntries.sort(Comparator.comparingInt(ProcessorEntry::getPriority));
    }
}
