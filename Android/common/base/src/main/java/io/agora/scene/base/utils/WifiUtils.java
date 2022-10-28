package io.agora.scene.base.utils;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.scene.base.component.AgoraApplication;

public class WifiUtils {
    public static int getWifiStatus() {
        WifiManager wifi_service = (WifiManager) AgoraApplication.the().getApplicationContext().getSystemService(AppCompatActivity.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        return wifiInfo.getRssi();
    }
}
