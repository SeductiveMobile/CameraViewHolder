package com.seductive.tools.cameraholder.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides common methods for getting front/back facing camera ids,
 * checking availability of front/back facing cameras, receiving available output sizes for cameras
 * and converting output previews between formats.
 * Class allows to work both with camera1 api and camera2 APIs. Camera2 API is available for
 * Android Lollipop and above; to use any on methods below, you should check current Android
 * version using {@link android.os.Build.VERSION and android.os.Build.VERSION_CODES}.
 *
 * @see Camera
 * @see CameraManager
 * @see CameraCharacteristics
 */
public final class CameraUtils {

    /**
     * Is used for Camera1 API.
     * Returns id of back facing camera using {@link Camera} and
     * {@link android.hardware.Camera.CameraInfo}.
     *
     * @return id of back facing camera if exists; -1 otherwise.
     */
    public static int getBackFacingCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Is used for Camera1 API.
     * Returns id of front facing camera using {@link Camera} and
     * {@link android.hardware.Camera.CameraInfo}.
     *
     * @return id of front facing camera if exists; -1 otherwise.
     */
    public static int getFrontFacingCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Is used for Camera1 API.
     * Checks if front facing camera is exists.
     *
     * @return true if exists, false otherwise.
     */
    public static boolean isFrontFacingCameraAvailable() {
        return CameraUtils.getFrontFacingCameraId() != -1;
    }

    /**
     * Is used for Camera1 API.
     * Returns list of supported video sizes for front facing camera. If camera is not exists or
     * busy by other process, returned list will be empty.
     *
     * @return list of {@link android.hardware.Camera.Size} for front facing camera
     */
    public static List<Camera.Size> getSupportedVideoSizesForFrontCamera() {
        return getSupportedVideoSizesForCameraId(getFrontFacingCameraId());
    }

    /**
     * Is used for Camera1 API.
     * Returns list of supported video sizes for back facing camera. If camera is not exists or
     * busy by other process, returned list will be empty.
     *
     * @return list of {@link android.hardware.Camera.Size} for back facing camera
     */
    public static List<Camera.Size> getSupportedVideoSizesForBackCamera() {
        return getSupportedVideoSizesForCameraId(getBackFacingCameraId());
    }

    /**
     * Is used for Camera2 API only.
     * Returns list of supported video sizes for back facing camera. If camera is not exists or
     * busy by other process, returned list will be empty.
     *
     * @param context current Context instance
     * @return list of {@link Size} for back facing camera
     */
    public static List<Size> getSupportedOutputSizesForBackCamera2(Context context) {
        return getSupportedOutputSizesForCamera2Id(context, getBackFacingCameraId());
    }

    /**
     * Is used for Camera2 API only.
     * Returns list of supported video sizes for front facing camera. If camera is not exists or
     * busy by other process, returned list will be empty.
     *
     * @param context current Context instance
     * @return list of {@link Size} for front facing camera
     */
    public static List<Size> getSupportedOutputSizesForFrontCamera2(Context context) {
        return getSupportedOutputSizesForCamera2Id(context, getFrontFacingCameraId());
    }

    /**
     * Is used for Camera1 API.
     * Returns list of supported video sizes for camera with cameraId. Method use
     * {@link Camera} to access required hardware camera. If camera is busy by other
     * process, RuntimeException is thrown and method returns empty list.
     *
     * @param cameraId the hardware camera to access, between 0 and {number of available
     *                 cameras - 1};
     * @return list of available {@link android.hardware.Camera.Size} if
     * {@link Camera was opened correctly}, empty list - otherwise.
     */
    public static List<Camera.Size> getSupportedVideoSizesForCameraId(int cameraId) {
        List<Camera.Size> sizes = new ArrayList<>();
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
            Camera.Parameters params = camera.getParameters();
            sizes = params.getSupportedPreviewSizes();
            camera.release();
            camera = null;
        } catch (RuntimeException e) {
            //failed to connect to camera service, return empty list
            if (camera != null) {
                camera.release();
            }
        }
        return sizes;
    }

    /**
     * Is used for Camera2 API ony.
     * Returns list of supported video sizes for camera with cameraId. Method use
     * {@link CameraManager} to access required {@link CameraCharacteristics}.
     * CameraCharacteristics instance for camera contains {@link StreamConfigurationMap}. With method
     * StreamConfigurationMap#getOutputSizes(Class<T> klass) we receive list of available video
     * output sizes and add only values less or equals to device screen heigth/width values.
     * According to usage of this method, if we allow to return any of available output sized,
     * some of high-quality resolutions could have problems with rendering when it'll set as
     * preview size.
     * If camera is busy by other process, RuntimeException is thrown and method returns empty list.
     *
     * @param context  Context to get camera system service with.
     * @param cameraId the hardware camera to access, between 0 and {number of available
     *                 cameras - 1};
     * @return list of available {@link Size} if
     * {@link android.hardware.camera2.CameraAccessException} won't be thrown while open camera,
     * empty list - otherwise.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static List<Size> getSupportedOutputSizesForCamera2Id(Context context, int cameraId) {
        List<Size> result = new ArrayList<>();
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                if (Integer.valueOf(id) == cameraId) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] sizes = streamConfigurationMap != null ?
                            streamConfigurationMap.getOutputSizes(SurfaceTexture.class) : null;
                    if (sizes != null) {
                        for (Size size : sizes) {
                            if (size.getHeight() <= UIUtils.getScreenWidth(context)
                                    && size.getWidth() <= UIUtils.getScreenHeight(context)) {
                                result.add(size);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Is used for Camera2 API ony.
     * See {@link android.hardware.camera2.CameraCharacteristics.Key#INFO_SUPPORTED_HARDWARE_LEVEL_FULL}
     * and {@link }
     *
     * @param context  Context to get camera system service with.
     * @param cameraId the hardware camera to access, between 0 and {number of available
     *                 cameras - 1};
     * @return true if SDK version >= {@link Build.VERSION_CODES#LOLLIPOP} and support advanced
     * features
     */
    public static boolean isCameraFocusAvailable(Context context, int cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                if (Integer.valueOf(id) == cameraId) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                    Object infoSuppHardwareLevelObj = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

                    return infoSuppHardwareLevelObj != null &&
                            (int) infoSuppHardwareLevelObj ==
                                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method for saving byte array into file;
     *
     * @param data     the byte array which should be saved.
     * @param fileName the path to destination file.
     * @throws IOException
     */
    public static void saveImageBytes(byte[] data, String fileName) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
        bos.write(data);
        bos.flush();
        bos.close();
    }

    public static void toRGB565(byte[] yuvs, int width, int height, byte[] rgbs) {
        //the end of the luminance data
        final int lumEnd = width * height;
        //points to the next luminance value pair
        int lumPtr = 0;
        //points to the next chromiance value pair
        int chrPtr = lumEnd;
        //points to the next byte output pair of RGB565 value
        int outPtr = 0;
        //the end of the current luminance scanline
        int lineEnd = width;

        while (true) {

            //skip back to the start of the chromiance values when necessary
            if (lumPtr == lineEnd) {
                if (lumPtr == lumEnd) break; //we've reached the end
                //division here is a bit expensive, but's only done once per scanline
                chrPtr = lumEnd + ((lumPtr >> 1) / width) * width;
                lineEnd += width;
            }

            //read the luminance and chromiance values
            final int Y1 = yuvs[lumPtr++] & 0xff;
            final int Y2 = yuvs[lumPtr++] & 0xff;
            final int Cr = (yuvs[chrPtr++] & 0xff) - 128;
            final int Cb = (yuvs[chrPtr++] & 0xff) - 128;
            int R, G, B;

            //generate first RGB components
            B = Y1 + ((454 * Cb) >> 8);
            if (B < 0) B = 0;
            else if (B > 255) B = 255;
            G = Y1 - ((88 * Cb + 183 * Cr) >> 8);
            if (G < 0) G = 0;
            else if (G > 255) G = 255;
            R = Y1 + ((359 * Cr) >> 8);
            if (R < 0) R = 0;
            else if (R > 255) R = 255;
            //NOTE: this assume little-endian encoding
            rgbs[outPtr++] = (byte) (((G & 0x3c) << 3) | (B >> 3));
            rgbs[outPtr++] = (byte) ((R & 0xf8) | (G >> 5));

            //generate second RGB components
            B = Y2 + ((454 * Cb) >> 8);
            if (B < 0) B = 0;
            else if (B > 255) B = 255;
            G = Y2 - ((88 * Cb + 183 * Cr) >> 8);
            if (G < 0) G = 0;
            else if (G > 255) G = 255;
            R = Y2 + ((359 * Cr) >> 8);
            if (R < 0) R = 0;
            else if (R > 255) R = 255;
            //NOTE: this assume little-endian encoding
            rgbs[outPtr++] = (byte) (((G & 0x3c) << 3) | (B >> 3));
            rgbs[outPtr++] = (byte) ((R & 0xf8) | (G >> 5));
        }
    }

    public static int[] decodeYUV420SP(byte[] yuv420sp, int width,
                                       int height, Rect rect) {
        if (rect == null) {
            rect = new Rect(0, 0, width, height);
        }
        final int frameSize = width * height;
        int ow = rect.right - rect.left;
        int oh = rect.bottom - rect.top;
        int rgb[] = new int[ow * oh];
        for (int j = 0, yp = 0, ic = 0; j < height; j++) {
            if (j <= rect.top) {
                yp += width;
                continue;
            }
            if (j > rect.bottom)
                break;
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                if (i <= rect.left || i > rect.right)
                    continue;
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                int rx = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                rgb[ic] = rx;
                ic++;
            }
        }
        return rgb;
    }

    static public void decodeYUV420SP(int[] rgba, byte[] yuv420sp, int width,
                                      int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                // 0xff00) | ((b >> 10) & 0xff);
                // rgba, divide 2^10 ( >> 10)
                rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
                        | ((b >> 2) | 0xff00);
            }
        }
    }

    /**
     * Converts YUV image in byte array to bitmap by converting every pixel
     *
     * @param data   YUV image data
     * @param width  destination image width
     * @param height destination image heigth
     * @param crop   target image params
     * @return converted image in Bitmap
     */
    public static Bitmap convertYUV(byte[] data, int width, int height, Rect crop) {
        if (crop == null) {
            crop = new Rect(0, 0, width, height);
        }
        Bitmap image = Bitmap.createBitmap(crop.width(), crop.height(), Bitmap.Config.ARGB_8888);
        int yv = 0, uv = 0, vv = 0;

        for (int y = crop.top; y < crop.bottom; y += 1) {
            for (int x = crop.left; x < crop.right; x += 1) {
                yv = data[y * width + x] & 0xff;
                uv = (data[width * height + (x / 2) * 2 + (y / 2) * width + 1] & 0xff) - 128;
                vv = (data[width * height + (x / 2) * 2 + (y / 2) * width] & 0xff) - 128;
                image.setPixel(x, y, convertPixel(yv, uv, vv));
            }
        }
        return image;
    }

    public static int convertPixel(int y, int u, int v) {
        int r = (int) (y + 1.13983f * v);
        int g = (int) (y - .39485f * u - .58060f * v);
        int b = (int) (y + 2.03211f * u);
        r = (r > 255) ? 255 : (r < 0) ? 0 : r;
        g = (g > 255) ? 255 : (g < 0) ? 0 : g;
        b = (b > 255) ? 255 : (b < 0) ? 0 : b;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static byte[] convertN21ToJpeg(byte[] bytesN21, int w, int h) {
        byte[] rez = new byte[0];

        YuvImage yuv_image = new YuvImage(bytesN21, ImageFormat.NV21, w, h, null);
        Rect rect = new Rect(0, 0, w, h);
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
        yuv_image.compressToJpeg(rect, 100, output_stream);
        rez = output_stream.toByteArray();
        return rez;
    }

    /**
     * Converts YUV420 NV21 to Y888 (RGB8888). The gray scale image still holds 3 bytes on the pixel.
     *
     * @param pixels output array with the converted array o grayscale pixels
     * @param data   byte array on YUV420 NV21 format.
     * @param width  pixels width
     * @param height pixels height
     */
    public static void applyGrayScale(int[] pixels, byte[] data, int width, int height) {
        int p;
        int size = width * height;
        for (int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            pixels[i] = 0xff000000 | p << 16 | p << 8 | p;
        }
    }
}
