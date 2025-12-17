package me.athulsib.stomcheat.config;

public record Config(
        boolean loadDefaultChecks,
        boolean loadDefaultProcessors,
        int threadCount,
        String alertMessage,
        String experimental,
        String hover,
        String broadcast,
        String kickMessage
) {}