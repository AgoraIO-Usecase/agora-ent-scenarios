package tech.sud.mgp.SudMGPWrapper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * json解析工具
 */
public class SudJsonUtils {

    /**
     * 解析json
     *
     * @return 如果解析出错，则返回空对象
     */
    public static <T> T fromJson(final String json, final Class<T> type) {
        try {
            return getGson().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对象解析成json
     *
     * @param object
     * @return
     */
    public static String toJson(final Object object) {
        return getGson().toJson(object);
    }

    public static Gson getGson() {
        return InnerClass.gson;
    }

    public static class InnerClass {
        public static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    }

}
