package io.agora.scene.widget.utils;

public class UiUtils {
    private static long receiveMessageTime = 0L;
    private static long receiveMessageTime2 = 0L;

    public static boolean isFastClick() {
        if (System.currentTimeMillis() - receiveMessageTime < 1000) {
            return true;
        }
        receiveMessageTime = System.currentTimeMillis();
        return false;
    }

    public static boolean isFastClick(int time) {
        if (System.currentTimeMillis() - receiveMessageTime < time) {
            return true;
        }
        receiveMessageTime = System.currentTimeMillis();
        return false;
    }
    public static boolean isFastClick3(int time) {
        if (System.currentTimeMillis() - receiveMessageTime2 < time) {
            return true;
        }
        receiveMessageTime2 = System.currentTimeMillis();
        return false;
    }
}
