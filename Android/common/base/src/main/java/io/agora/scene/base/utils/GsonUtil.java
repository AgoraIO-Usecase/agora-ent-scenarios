package io.agora.scene.base.utils;

import com.google.gson.Gson;

public class GsonUtil {
    public static Gson instance;

    public static Gson getInstance() {
        if (instance == null) {
            instance = new Gson();
        }
        return instance;
    }
}
