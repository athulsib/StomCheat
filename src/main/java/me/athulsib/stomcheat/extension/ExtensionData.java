package me.athulsib.stomcheat.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionData {
    String name();
    String author();
    String version();
}
