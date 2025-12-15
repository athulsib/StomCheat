package me.athulsib.stomcheat.processor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessorData {
    String name();
    int priority();
    ProcessorType type();
}
