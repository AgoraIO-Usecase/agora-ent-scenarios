package io.agora.scene.base.utils;

import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import io.agora.scene.base.CommonBaseLogger;

public final class ZipUtils {
    private static final String TAG = ZipUtils.class.getSimpleName();
    private static final Executor wokeExecutor = Executors.newSingleThreadExecutor();
    private static final android.os.Handler mainThreadHandler = new android.os.Handler(Looper.getMainLooper());

    public static void unzipOnlyPlainXmlFilesAsync(String zipFilePath,
                                                   String targetDirPath,
                                                   UnZipCallback callback) {
        // This is for zipped lyrics file, we only need the plain .xml files
        unZipAsync(zipFilePath, targetDirPath, "^(?!/)[a-zA-Z\\d]+.xml", callback);
    }

    public static void unZipAsync(String zipFilePath,
                                  String targetDirPath,
                                  String filter,
                                  UnZipCallback callback) {
        wokeExecutor.execute(() -> {
            try {
                List<String> outPath = unzipSync(zipFilePath, targetDirPath, filter, false);
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onFileUnZipped(outPath));

                }
            } catch (Exception e) {
                if (callback != null) {
                    mainThreadHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public static List<String> unzipSync(String zipFilePath,
                                         String targetDirPath,
                                         String filter,
                                         boolean reserve) throws Exception {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            CommonBaseLogger.w(TAG, "File " + zipFile + " not found");
            throw new FileNotFoundException(zipFilePath);
        }

        File targetDir = new File(targetDirPath);
        if (targetDir.exists() && targetDir.isFile()) {
            throw new RuntimeException("The target direction is a file!");
        }

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ZipFile zzipFile = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zzipFile.entries();
        List<String> outPath = new ArrayList<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            copyEntry(zzipFile, entry, targetDir, reserve, outPath);
        }
        return outPath;
    }

    private static void copyEntry(ZipFile zipFile,
                                  ZipEntry entry,
                                  File targetDir,
                                  boolean reserve,
                                  List<String> outFilesPath) throws IOException {

        InputStream iStream = null;
        OutputStream oStream = null;
        if (entry.isDirectory()) {
            // 如果条目是目录，创建目录及其所有父目录
            File newDir = new File(targetDir, entry.getName());
            if (!newDir.exists()) {
                boolean result = newDir.mkdirs();
                if (!result && reserve) {
                    // 如果无法创建目录，记录错误
                    CommonBaseLogger.e(TAG, "Failed to create directory: " + newDir.getAbsolutePath());
                }
            }
        } else {
            File oFile = new File(targetDir, entry.getName());
            oFile.deleteOnExit();
            oFile.createNewFile();
            oStream = new FileOutputStream(oFile, false);
            iStream = zipFile.getInputStream(entry);
            outFilesPath.add(oFile.getAbsolutePath());
        }
        if (oStream == null || iStream == null) {
            return;
        }
        try {
            copy(iStream, oStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                oStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = createBuffer();
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    private static final int BUFFER_SIZE = 8192;

    /**
     * Creates a new byte array for buffering reads or writes.
     */
    static byte[] createBuffer() {
        return new byte[BUFFER_SIZE];
    }

    private ZipUtils() {
    }

    public interface UnZipCallback {
        void onFileUnZipped(List<String> unZipFilePaths);

        void onError(Exception e);
    }

    public interface ZipCallback {
        void onFileZipped(String destinationFilePath);

        void onError(Exception e);
    }

    //压缩多个文件
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
