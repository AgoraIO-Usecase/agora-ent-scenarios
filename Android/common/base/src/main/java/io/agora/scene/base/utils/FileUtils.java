package io.agora.scene.base.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.CommonBaseLogger;
import io.agora.scene.base.component.AgoraApplication;

public class FileUtils {
    public static String getBaseStrPath() {
        return AgoraApplication.the().getExternalFilesDir("media").getAbsolutePath() + File.separator;
    }

    public static String getTempSDPath() {
        return getBaseStrPath() + "ag" + File.separator;
    }

    public static final String SEPARATOR = File.separator;

    public static String copyFileFromAssets(Context context, String assetsFilePath, String storagePath) {
        if (TextUtils.isEmpty(storagePath)) {
            return null;
        } else if (storagePath.endsWith(SEPARATOR)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }

        if (TextUtils.isEmpty(assetsFilePath) || assetsFilePath.endsWith(SEPARATOR)) {
            return null;
        }

        String storageFilePath = storagePath + SEPARATOR + assetsFilePath;

        AssetManager assetManager = context.getAssets();
        try {
            File file = new File(storageFilePath);
            if (file.exists()) {
                return storageFilePath;
            }
            file.getParentFile().mkdirs();
            InputStream inputStream = assetManager.open(assetsFilePath);
            readInputStream(storageFilePath, inputStream);
        } catch (IOException e) {
            CommonBaseLogger.e("FileUtils", e.toString());
            return null;
        }
        return storageFilePath;
    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    private static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (IOException e) {
            CommonBaseLogger.e("FileUtils", e.toString());
        }
    }


    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取指定目录下的所有文件
     * @param directory
     * @return
     */
    public static List<File> findInnermostFiles(File directory) {
        List<File> innermostFiles = new ArrayList<>();
        findInnermostFilesRecursively(directory, innermostFiles);
        return innermostFiles;
    }

    private static void findInnermostFilesRecursively(File directory, List<File> innermostFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            boolean hasSubdirectory = false;
            for (File file : files) {
                if (file.isDirectory()) {
                    hasSubdirectory = true;
                    findInnermostFilesRecursively(file, innermostFiles);
                }
            }
            if (!hasSubdirectory) {
                for (File file : files) {
                    if (file.isFile()) {
                        innermostFiles.add(file);
                    }
                }
            }
        }
    }

    /**
     * 获取指定目录下的所有图片文件
     * @param directoryPath
     * @return
     */
    public static List<String> loadImages(String directoryPath) {
        List<String > imagePaths = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isImageFile(file)) {
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return imagePaths;
    }

    private static boolean isImageFile(File file) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        for (String extension : imageExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static byte[] getFileMD5(final File file) {
        if (file == null) return null;
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (!(dis.read(buffer) > 0)) break;
            }
            md = dis.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
