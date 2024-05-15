package io.agora.rtmsyncmanager.utils;

import androidx.annotation.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MapperUtils {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MapperKey {
        String value();
    }

    public static Map<String, Object> model2Map(@Nullable Object model) {
        HashMap<String, Object> retMap = new HashMap<>();
        if (model == null) {
            return retMap;
        }

        Field[] declaredFields = model.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String fieldName = declaredField.getName();
            Object value;
            try {
                value = declaredField.get(model);
            } catch (IllegalAccessException e) {
                continue;
            }

            try {
                MapperKey key = declaredField.getAnnotation(MapperKey.class);
                if (key != null) {
                    retMap.put(key.value(), value);
                    continue;
                }
            } catch (NullPointerException e) {
                // do nothing
            }

            retMap.put(fieldName, value);
        }

        return retMap;
    }

}
