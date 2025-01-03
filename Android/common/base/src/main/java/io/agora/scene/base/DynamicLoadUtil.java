package io.agora.scene.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Utility class for dynamically loading .so files
 */
public class DynamicLoadUtil {
    /**
     * Load .so file
     * @param context Context
     * @param fromPath Source path of .so file, usually download directory
     * @param soName Name of .so file, e.g. libeffect (without .so extension)
     */
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public static void loadSoFile(Context context, String fromPath, String soName) {
        File dir = context.getDir("libs", Context.MODE_PRIVATE);
        if (!isLoadSoFile(dir, soName)) {
            int ret = copy(fromPath, dir.getAbsolutePath(), soName);
            if (ret != 0) {
                Log.e("DynamicLoadUtil", "copy so file failed");
                return;
            }
        }
        Log.d("hugo", "load so file: " + dir.getAbsolutePath() + "/" + soName + ".so");
        System.load(dir.getAbsolutePath() + "/" + soName + ".so");
    }

    /**
     * Check if .so file exists
     * @param dir Target app private directory for .so files
     * @param soName Name of .so file, e.g. libeffect (without .so extension)
     * @return Whether file exists
     */
    public static boolean isLoadSoFile(File dir, String soName) {
        File[] currentFiles = dir.listFiles();
        if (currentFiles == null) {
            return false;
        }
        return Arrays.stream(currentFiles).anyMatch(file -> file.getName().contains(soName));
    }

    /**
     * Copy .so file to app private directory
     * @param fromFile Source path of .so file, usually download directory
     * @param toFile Target app private directory for .so files
     * @param soName Name of .so file, e.g. libeffect (without .so extension)
     * @return Copy result
     */
    private static int copy(String fromFile, String toFile, String soName) {
        File root = new File(fromFile);
        if (!root.exists()) {
            return -1;
        }
        File[] currentFiles = root.listFiles();
        if (currentFiles == null) {
            return -1;
        }
        File targetDir = new File(toFile);
        if (!targetDir.exists()) {
            boolean ret = targetDir.mkdirs();
            Log.d("DynamicLoadUtil", "create dir: " + ret);
        }
        for (File currentFile : currentFiles) {
            if (currentFile.getName().contains(soName)) {
                return copySdcardFile(currentFile.getPath(), toFile + File.separator + currentFile.getName());
            }
        }
        return 0;
    }

    /**
     * Copy file
     * @param fromFile Source path of .so file, usually download directory
     * @param toFile Target app private directory for .so files
     * @return Copy result
     */
    private static int copySdcardFile(String fromFile, String toFile) {
        try (FileInputStream fosFrom = new FileInputStream(fromFile);
             FileOutputStream fosTo = new FileOutputStream(toFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fosFrom.read(buffer)) != -1) {
                fosTo.write(buffer, 0, len);
            }
            return 0;
        } catch (Exception ex) {
            Log.e("DynamicLoadUtil", "copy Sdcard File failed: " + ex.getMessage());
            return -1;
        }
    }
}
