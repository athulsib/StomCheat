package me.athulsib.stomcheat.extension;

import lombok.Getter;
import me.athulsib.stomcheat.StomCheat;

import java.util.ArrayList;
import java.util.List;

public class ExtensionManager {
    @Getter
    private final List<ExtensionInfo> loadedExtensions = new ArrayList<>();

    public void loadExtensions(StomCheatExtension... extensions) {
        for (StomCheatExtension extension : extensions) {
            extension.init(StomCheat.getInstance());
            if (!extension.getClass().isAnnotationPresent(ExtensionData.class)) {
                throw new RuntimeException(
                        "Extension " + extension.getClass().getName()
                                + " is missing the required @ExtensionData annotation."
                );
            }

            ExtensionData extensionData = extension.getClass().getAnnotation(ExtensionData.class);
            loadedExtensions.add(new ExtensionInfo(extensionData.name(), extensionData.author(), extensionData.version()));
        }
    }

}
