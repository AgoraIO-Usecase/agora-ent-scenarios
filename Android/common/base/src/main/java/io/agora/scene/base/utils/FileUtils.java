package io.agora.scene.base.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.agora.scene.base.CommonBaseLogger;
import io.agora.scene.base.component.AgoraApplication;

public class FileUtils {
    private static final Executor wokeExecutor = Executors.newSingleThreadExecutor();

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
     * Read data from input stream and write to output stream
     *
     * @param storagePath Target file path
     * @param inputStream Input stream
     */
    private static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[inputStream.available()];
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();
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
     * compression
     */
    public static void ZipCompress(String inputFile, String outputFile) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
        BufferedOutputStream bos = new BufferedOutputStream(out);
        File input = new File(inputFile);
        compress(out, bos, input,null);
        bos.close();
        out.close();
    }
    /**
     * Recursive compression
     * @param name
     */
    public static void compress(ZipOutputStream out, BufferedOutputStream bos, File input, String name) throws IOException {
        if (name == null) {
            name = input.getName();
        }
        if (input.isDirectory()) {
            File[] flist = input.listFiles();

            if (flist.length == 0) {
                out.putNextEntry(new ZipEntry(name + "/"));
            } else {
                for (int i = 0; i < flist.length; i++) {
                    compress(out, bos, flist[i], name + "/" + flist[i].getName());
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(name));
            FileInputStream fos = new FileInputStream(input);
            BufferedInputStream bis = new BufferedInputStream(fos);
            int len=-1;
            byte[] buf = new byte[1024];
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf,0,len);
            }
            bis.close();
            fos.close();
        }
    }

    /**
     * unzip
     */
    public static void ZipUncompress(String inputFile,String destDirPath) throws Exception {
        File srcFile = new File(inputFile);
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        ZipInputStream zIn = new ZipInputStream(new FileInputStream(srcFile));
        ZipEntry entry = null;
        File file = null;
        while ((entry = zIn.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                file = new File(destDirPath, entry.getName());
                if (!file.exists()) {
                    new File(file.getParent()).mkdirs();
                }
                OutputStream out = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                int len = -1;
                byte[] buf = new byte[1024];
                while ((len = zIn.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                bos.close();
                out.close();
            }
        }
    }

    public interface ZipCallback {
        void onFileZipped(String destinationFilePath);

        void onError(Exception e);
    }

    public static void compressFiles(List<String> sourceFilePaths, String destinationFilePath, ZipCallback zipCallback) {
        wokeExecutor.execute(() -> {
            FileUtils.deleteFile(destinationFilePath);
            try {
                // 创建目标压缩文件
                FileOutputStream fos = new FileOutputStream(destinationFilePath);
                ZipOutputStream zipOut = new ZipOutputStream(fos);

                // 逐个将源文件添加到压缩文件中
                for (String sourceFilePath : sourceFilePaths) {
                    File sourceFile = new File(sourceFilePath);
                    if (!sourceFile.exists()) {
                        Log.d("zhangw", "需要压缩的文件不存在：" + sourceFilePath);
                        continue;
                    }
                    // 创建源文件输入流
                    FileInputStream fis = new FileInputStream(sourceFilePath);

                    // 将源文件添加到压缩文件中
                    ZipEntry zipEntry = new ZipEntry(new File(sourceFilePath).getName());
                    zipOut.putNextEntry(zipEntry);

                    // 从源文件输入流读取数据，并写入压缩文件输出流
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }

                    // 关闭流
                    fis.close();
                }

                // 关闭流
                zipOut.close();
                fos.close();

                zipCallback.onFileZipped(destinationFilePath);
                Log.d("zhangw", "文件压缩完成");
            } catch (Exception e) {
                e.printStackTrace();
                zipCallback.onError(e);
            }
        });
    }
}
