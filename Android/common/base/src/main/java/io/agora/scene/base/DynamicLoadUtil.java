package io.agora.scene.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * 动态加载 so 文件工具类
 */
public class DynamicLoadUtil {
    /**
     * 加载 so 文件
     * @param context 上下文
     * @param fromPath so 文件路径, 通常是下载目录
     * @param soName so 文件名, 例如 libeffect, 不要带 .so 后缀
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
     * 判断 so 文件是否存在
     * @param dir 目标存放 so 文件的 app 私有目录
     * @param soName so 文件名, 例如 libeffect, 不要带 .so 后缀
     * @return 是否存在
     */
    public static boolean isLoadSoFile(File dir, String soName) {
        File[] currentFiles = dir.listFiles();
        if (currentFiles == null) {
            return false;
        }
        return Arrays.stream(currentFiles).anyMatch(file -> file.getName().contains(soName));
    }

    /**
     * 拷贝 so 文件到 app 私有目录
     * @param fromFile so 文件路径, 通常是下载目录
     * @param toFile 目标存放 so 文件的 app 私有目录
     * @param soName so 文件名, 例如 libeffect, 不要带 .so 后缀
     * @return 拷贝结果
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
     * 拷贝文件
     * @param fromFile so 文件路径, 通常是下载目录
     * @param toFile 目标存放 so 文件的 app 私有目录
     * @return 拷贝结果
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
