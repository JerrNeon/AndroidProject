package com.jn.kiku.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import androidx.annotation.ColorInt;
import android.widget.ScrollView;

import androidx.annotation.IntRange;

import com.jn.common.util.ContextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @version V1.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (图片工具类)
 * @create by: chenwei
 * @date 2016/7/26 9:34
 */
public class ImageUtil {

    private static final String APP_CACHE_DIR = "cache";//应用缓存根目录
    private static final String IMAGE_CACHE_DIR = "image";//图片缓存目录
    private static final String VIDEO_CACHE_DIR = "video";//视频缓存目录
    private static final String FILE_CACHE_DIR = "file";//文件缓存目录
    private static final String CRASH_CACHE_DIR = "crash";//异常信息目录

    private static final String IMAGE_COMPRESSTEMP_FILENAME = "temp";//图片压缩临时文件名

    /**
     * 获取缓存根目录
     *
     * @return
     */
    public static File getCacheRootFile() {
        return getCacheRootFile(ContextUtils.getInstance().getApplication());
    }

    /**
     * 获取图片压缩临时路径
     *
     * @return
     */
    public static String getImageCompressTempCachePath() {
        return getImageCachePath(IMAGE_COMPRESSTEMP_FILENAME);
    }

    /**
     * 获取图片缓存路径
     *
     * @param imageName 图片名
     * @return 图片路径
     */
    public static String getImageCachePath(String imageName) {
        String path = getImageCacheFile().getAbsolutePath() + "/" + imageName + ".png";
        File file = new File(path);
        file.deleteOnExit();
        return path;
    }

    /**
     * 获取视频缓存路径
     *
     * @return
     */
    public static String getVideoCachePath() {
        return getVideoCacheFile().getAbsolutePath();
    }

    /**
     * 获取异常信息路径
     *
     * @param logName 文件名
     * @return log文件
     */
    public static String getCrashPath(String logName) {
        return getCrashFile().getAbsolutePath() + "/" + logName + ".txt";
    }

    /**
     * 获取图片缓存目录
     *
     * @return File
     */
    public static File getImageCacheFile() {
        File result = new File(
                getCacheRootFile().getAbsoluteFile() + "/" + APP_CACHE_DIR + "/" + IMAGE_CACHE_DIR);
        if (!result.exists())
            result.mkdirs();
        return result;
    }

    /**
     * 获取视频缓存目录
     *
     * @return
     */
    public static File getVideoCacheFile() {
        File result = new File(
                getCacheRootFile().getAbsoluteFile() + "/" + APP_CACHE_DIR + "/" + VIDEO_CACHE_DIR);
        if (!result.exists())
            result.mkdirs();
        return result;
    }

    /**
     * 获取文件缓存目录
     *
     * @return File
     */
    public static File getFileCacheFile() {
        File result = new File(
                getCacheRootFile().getAbsoluteFile() + "/" + APP_CACHE_DIR + "/" + FILE_CACHE_DIR);
        if (!result.exists())
            result.mkdirs();
        return result;
    }

    /**
     * 获取异常信息目录
     *
     * @return File
     */
    public static File getCrashFile() {
        File result = new File(
                getCacheRootFile().getAbsoluteFile() + "/" + APP_CACHE_DIR + "/" + CRASH_CACHE_DIR);
        if (!result.exists())
            result.mkdirs();
        return result;
    }

    /**
     * 获取缓存根目录
     * <p>
     * 有SDcard放在/mnt/sdcard/Android目录下
     * 没有sdcard 放在 data/data/packagename/file 目录下
     * </P>
     *
     * @param application Application
     * @return
     */
    public static File getCacheRootFile(Application application) {
        File result = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// SD卡已经挂载
            result = application.getExternalFilesDir(null);
        } else {
            result = application.getFilesDir();
        }
        if (result == null)
            result = application.getFilesDir();
        return result;
    }

    /**
     * 压缩图片
     *
     * @param filePath   原图片路径
     * @param targetPath 目标图片路径
     */
    public static String compressImage(String filePath, String targetPath) {
        return compressImage(filePath, targetPath, 80);
    }

    /**
     * 压缩图片
     *
     * @param filePath   原图片路径
     * @param targetPath 目标图片路径
     * @param quality    质量
     */
    public static String compressImage(String filePath, String targetPath, int quality) {
        Bitmap bm = getSmallBitmap(filePath);//获取一定尺寸的图片
        int degree = readPictureDegree(filePath);//获取相片拍摄角度
        if (degree != 0) {//旋转照片角度，防止头像横着显示
            bm = rotateBitmap(bm, degree);
        }
        File outputFile = new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
            } else {
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
        } catch (Exception e) {
        }
        return outputFile.getPath();
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 800, 800);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }


    /**
     * 获取照片角度
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
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
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转照片
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 保存本地资源文件到手机中
     *
     * @param context
     * @param resourceId
     * @param fileName
     * @return
     */
    public static String saveLocalResourceFile(Context context, int resourceId, String fileName) {
        Bitmap bitmap = BitmapFactory.decodeStream(context.getResources().openRawResource(resourceId));
        return saveBitmap(bitmap, fileName);
    }

    /**
     * 保存Bitmap到手机中
     *
     * @param bitmap
     * @param fileName
     * @return
     */
    public static String saveBitmap(Bitmap bitmap, String fileName) {
        try {
            String path = ImageUtil.getImageCachePath(fileName);
            File outputFile = new File(ImageUtil.getImageCachePath(fileName));
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
            } else {
                outputFile.delete();
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存Scrollview中的内容到手机中
     *
     * @param scrollView      ScrollView
     * @param backgroundColor 图片背景色
     * @param fileName        图片名
     * @return
     */
    public static String saveBitmap(ScrollView scrollView, @ColorInt int backgroundColor, String fileName) {
        int height = 0;
        Bitmap bitmap;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            height += scrollView.getChildAt(i).getHeight();
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), height, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundColor);
        scrollView.draw(canvas);
        return saveBitmap(bitmap, fileName);
    }

    /**
     * 清除用户上传图片时保存的临时文件
     */
    public static void clearTempImage() {
        if (getImageCacheFile().exists())
            FileUtils.deleteAllFiles(getImageCacheFile());
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final int newWidth,
                                         final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src       The source of bitmap.
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @param recycle   True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final int newWidth,
                                         final int newHeight,
                                         final boolean recycle) {
        return scale(src, newWidth, newHeight, recycle);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @return the compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final float scaleWidth,
                                         final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return he compressed bitmap
     */
    public static Bitmap compressByScale(final Bitmap src,
                                         final float scaleWidth,
                                         final float scaleHeight,
                                         final boolean recycle) {
        return scale(src, scaleWidth, scaleHeight, recycle);
    }

    /**
     * Return the scaled bitmap
     *
     * @param src         The source of bitmap.
     * @param scaleWidth  The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return the scaled bitmap
     */
    public static Bitmap scale(final Bitmap src,
                               final float scaleWidth,
                               final float scaleHeight,
                               final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src     The source of bitmap.
     * @param quality The quality.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           @IntRange(from = 0, to = 100) final int quality) {
        return compressByQuality(src, quality, false);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src     The source of bitmap.
     * @param quality The quality.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           @IntRange(from = 0, to = 100) final int quality,
                                           final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src         The source of bitmap.
     * @param maxByteSize The maximum size of byte.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize) {
        return compressByQuality(src, maxByteSize, false);
    }

    /**
     * Return the compressed bitmap using quality.
     *
     * @param src         The source of bitmap.
     * @param maxByteSize The maximum size of byte.
     * @param recycle     True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src,
                                           final long maxByteSize,
                                           final boolean recycle) {
        if (isEmptyBitmap(src) || maxByteSize <= 0) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes;
        if (baos.size() <= maxByteSize) {
            bytes = baos.toByteArray();
        } else {
            baos.reset();
            src.compress(Bitmap.CompressFormat.JPEG, 0, baos);
            if (baos.size() >= maxByteSize) {
                bytes = baos.toByteArray();
            } else {
                // find the best quality using binary search
                int st = 0;
                int end = 100;
                int mid = 0;
                while (st < end) {
                    mid = (st + end) / 2;
                    baos.reset();
                    src.compress(Bitmap.CompressFormat.JPEG, mid, baos);
                    int len = baos.size();
                    if (len == maxByteSize) {
                        break;
                    } else if (len > maxByteSize) {
                        end = mid - 1;
                    } else {
                        st = mid + 1;
                    }
                }
                if (end == mid - 1) {
                    baos.reset();
                    src.compress(Bitmap.CompressFormat.JPEG, st, baos);
                }
                bytes = baos.toByteArray();
            }
        }
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
