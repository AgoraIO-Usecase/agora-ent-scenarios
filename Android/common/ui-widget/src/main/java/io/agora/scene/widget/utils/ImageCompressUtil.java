package io.agora.scene.widget.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.agora.scene.base.utils.FileUtils;

public class ImageCompressUtil {

    /**
     * 旋转图片摆正显示
     *
     * @param srcPath
     * @param bitmap
     * @return
     */
    public static Bitmap rotateBitmapByExif(String srcPath, Bitmap bitmap) {
        ExifInterface exif;
        Bitmap newBitmap = null;
        try {
            exif = new ExifInterface(srcPath);
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int digree = 0;
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
            }
            if (digree != 0) {
                Matrix m = new Matrix();
                m.postRotate(digree);
                newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                recycleBitmap(bitmap);
                return newBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 回收位图对象
     *
     * @param bitmap
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
            bitmap = null;
        }
    }

    //========================

    public static String displayPath(Activity activity, String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (getBitmapDegree(imagePath) > 0) {
                bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(imagePath));
            }
            //压缩图片
            Bitmap bitmap1 = compressBySampleSize(bitmap, 2, true);
//            Bitmap bitmap1 = compressByQuality(bitmap, 1200);
            //保存图片
            return saveBmp2Gallery(bitmap1, "photo" + ".jpg");
        } else {
            Toast.makeText(activity, "获得图片失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @SuppressLint("Range")
    private String getImagePath(Activity activity, Uri uri, String selection) {
        String path = null;
        Cursor cursor = activity.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }

        return path;
    }

    public static void delBmpCache() {
        String galleryPath = FileUtils.getTempSDPath();
        File dir = new File(galleryPath);
        if (dir.exists()) {
            dir.delete();
        }
    }

    /**
     * @param bmp     获取的bitmap数据
     * @param picName 自定义的图片名
     */
    private static String saveBmp2Gallery(Bitmap bmp, String picName) {

        String fileName = null;
        //系统相册目录
        String galleryPath = FileUtils.getTempSDPath();
        File dir = new File(galleryPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        //通知相册更新
//        MediaStore.Images.Media.insertImage(activity.getContentResolver(),
//                bmp, fileName, null);
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        Uri uri = Uri.fromFile(file);
//        intent.setData(uri);
//        activity.sendBroadcast(intent);
        return file.getAbsolutePath();
    }

    //采样压缩
    private static Bitmap compressBySampleSize(final Bitmap src,
                                               final int sampleSize,
                                               final boolean recycle) {
        if (src == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) {
            src.recycle();
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}