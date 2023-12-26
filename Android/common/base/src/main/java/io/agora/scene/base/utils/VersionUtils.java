package io.agora.scene.base.utils;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class VersionUtils {
    public static String getVersion(@NonNull String className) {
        try {
            Class<?> sceneClass = Class.forName(className);
            Field scene1VersionField = sceneClass.getField("VERSION");
            return (String) scene1VersionField.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return "";
        }
    }
}
