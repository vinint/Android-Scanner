package io.vin.android.scanner.core;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;


import io.vin.android.scanner.util.DisplayUtils;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;


/**
 * 基于camera1 api的相机控件
 * Author     Vin
 * Mail       vinintg@gmail.com
 */
public class Camera1View extends SurfaceView implements SurfaceHolder.Callback {

    // region construction
    private static final String TAG = "Camera1View";
    private static final int STOPPED = 0;
    private static final int STARTED = 1;
    //
    public static final int PARAMETERS_MODE_FULL_MANUAL = 0;
    public static final int PARAMETERS_MODE_SEMI_AUTO = 1;
    public static final int PARAMETERS_MODE_AUTO = 2;


    public Camera1View(Context context) {
        super(context);
        mSupportCamera = checkCameraHardware(getContext());
        initSurfaceHolder();
    }

    public Camera1View(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSupportCamera = checkCameraHardware(getContext());
        initSurfaceHolder();
    }

    public Camera1View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSupportCamera = checkCameraHardware(getContext());
        initSurfaceHolder();
    }

    // endregion

    // region property

    // 是否支持相机硬件 support the camera or not
    private boolean mSupportCamera = false;
    // 启用相机或者禁用 enable the camera or disable
    private boolean mEnabled = true;
    // Surface是否存在 Surface exist or not
    private boolean mSurfaceExist = false;
    // 相机状态 camera state
    private int mState = STOPPED;
    // 同步对象 Sync Object
    private final Object mSyncObject = new Object();
    private int mCameraId = -1;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private Camera.PreviewCallback mPreviewCallback;
    private int mParametersMode = PARAMETERS_MODE_AUTO;
    private ParametersCallback mParametersCallback;

    private boolean mAutoFocus = false;
    private long mAutoFocusInterval = 1000l;
    private boolean mSupportFocusModeContinuousPicture = false;
    private Handler mAutoFocusHandler;
    private float mAspectTolerance = 0.2f;
    // endregion

    private void initCamera() {
        if (!mSupportCamera) {
            return;
        }
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCameraId = i;
                    mCamera = Camera.open(i);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initSurfaceHolder() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if (getHolder().getSurface() == null) {
            return;
        }
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        /* Do nothing. Wait until surfaceChanged delivered */
        Log.d(TAG, "surfaceCreated");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged");
        synchronized (mSyncObject) {
            if (!mSurfaceExist) {
                mSurfaceExist = true;
                checkCurrentState();
            } else {
                /** Surface changed. We need to stop camera and restart with new parameters */
                /* Pretend that old surface has been destroyed */
                mSurfaceExist = false;
                checkCurrentState();
                /* Now use new surface. Say we have it now */
                mSurfaceExist = true;
                checkCurrentState();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        // empty. Take care of releasing the Camera preview in your activity.
        synchronized (mSyncObject) {
            mSurfaceExist = false;
            checkCurrentState();
        }

    }

    private void checkCurrentState() {
        Log.d(TAG, "call checkCurrentState");
        int targetState;

        if (mSupportCamera && mEnabled && mSurfaceExist && getVisibility() == VISIBLE) {
            targetState = STARTED;
        } else {
            targetState = STOPPED;
        }

        if (targetState != mState) {
            /* The state change detected. Need to exit the current state and enter target state */
            processExitState(mState);
            mState = targetState;
            processEnterState(mState);
        }
    }

    private void processExitState(int state) {
        Log.d(TAG, "call processExitState: " + state);
        switch (state) {
            case STARTED:
                onExitStartedState();
                break;
            case STOPPED:
                onExitStoppedState();
                break;
        }
        ;
    }

    private void processEnterState(int state) {
        Log.d(TAG, "call processEnterState: " + state);
        switch (state) {
            case STARTED:
                onEnterStartedState();
                break;
            case STOPPED:
                onEnterStoppedState();
                break;
        }
        ;
    }

    private void onExitStartedState() {
        synchronized (mSyncObject) {
            if (mCamera != null) {
                try {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void onExitStoppedState() {
    }

    private void onEnterStartedState() {
        if (mHolder.getSurface() != null) {
            initCamera();
            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                // 1.设置绘制预览的 Surface,draw the preview on Surface
                mCamera.setPreviewDisplay(mHolder);
                // 2.设置预览旋转角度
                mCamera.setDisplayOrientation(getDisplayOrientation(getContext(), mCameraId));
                // 3.设置相机参数
                if (mParametersCallback != null) {
                    if (mParametersMode == PARAMETERS_MODE_FULL_MANUAL) {
                        mParametersCallback.setParameters(mCamera);
                    } else if (mParametersMode == PARAMETERS_MODE_SEMI_AUTO) {
                        mParametersCallback.setParameters(mCamera);
                        defultParameters(mCamera);
                    } else if (mParametersMode == PARAMETERS_MODE_AUTO) {
                        defultParameters(mCamera);
                    } else {
                        defultParameters(mCamera);
                    }
                } else {
                    defultParameters(mCamera);
                }

                // 4.开始预览 start preview
                mCamera.startPreview();

                // 5.设置对焦模式
                if (mAutoFocus && !supportFocusModeContinuousPicture(mCamera)) {
                    // 走手动定期调用对焦实现
                    scheduleAutoFocus();
                }


                // 5.设置预览回调
                // 计算当前预览模式下接收预览图片byte[]的大小
                Camera.Parameters parameters = mCamera.getParameters();
                int previewWidth = parameters.getPreviewSize().width;
                int previewHeight = parameters.getPreviewSize().height;
                int bitsPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
                int bufferSize = (previewWidth * previewHeight * bitsPerPixel) / 8 + 1;
                mCamera.addCallbackBuffer(new byte[bufferSize]);
                // set Preview Callback
                mCamera.setPreviewCallbackWithBuffer((byte[] data, Camera camera) -> {
                    // 交给业务层处理
                    if (mPreviewCallback != null) {
                        mPreviewCallback.onPreviewFrame(data, camera);
                    }
                    // 回收缓存
                    mCamera.addCallbackBuffer(data);
                });

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private void onEnterStoppedState() {
        synchronized (mSyncObject) {
            if (mCamera != null) {
                try {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // region public Camera api

    /**
     * Method     getCamera
     * 获取Camera对象，不建议使用，除非你很清楚自己要做什么
     * Parameters []
     * Return     android.hardware.Camera
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * Method     getDisplayOrientation
     * 获取当前camera旋转角度
     * Parameters []
     * Return     int
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public int getDisplayOrientation() {
        int degrees = 0;
        if (mCameraId == -1) {
            degrees = 90;
        } else {
            degrees = getDisplayOrientation(getContext(), mCameraId);
        }
        return degrees;
    }

    /**
     * Method     setParametersMode
     * 设置相机参数模式
     * PARAMETERS_MODE_FULL_MANUAL 全自动，用户控制整个相机参数
     * PARAMETERS_MODE_SEMI_AUTO 半自动，用户自定义部分参数
     * PARAMETERS_MODE_AUTO 默认自动，使用默认参数预览
     * Parameters [mode]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setParametersMode(int mode) {
        this.mParametersMode = mode;
    }

    /**
     * Method     setParametersCallback
     * 相机初始化时设置一些固定的参数，对于聚焦区域，曝光等场景是不适用
     * Parameters [callback]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setParametersCallback(ParametersCallback callback) {
        this.mParametersCallback = callback;
    }

    /**
     * Method     setPreviewCallback
     * 预览数据回调
     * Parameters [cb]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setPreviewCallback(Camera.PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    /**
     * Method     takePicture
     * 拍照方法
     * Parameters [shutter, raw, jpeg]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
        if (isCameraAvailable()) {
            try {
                mCamera.takePicture(shutter, raw, (byte[] data, Camera camera) -> {
                    // 立刻取消自动对焦
                    boolean tmpAutoFocus = mAutoFocus;
                    setAutoFocus(false);
                    // 业务层获取图片数据
                    jpeg.onPictureTaken(data, camera);
                    // 重新预览
                    mCamera.startPreview();
                    // 重新恢复自动对焦
                    setAutoFocus(tmpAutoFocus);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Method     enableView
     * 该方法是为客户端提供的，用来启用摄像机预览。
     * This method is provided for clients, so they can enable the camera privew.
     * Parameters []
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void enableView() {
        synchronized (mSyncObject) {
            mEnabled = true;
            checkCurrentState();
        }
    }

    /**
     * Method     disableView
     * 此方法是为客户端提供的，他们可以禁用摄像头并停止传递帧，即使视图本身没有被销毁仍然停留在屏幕上
     * This method is provided for clients, so they can disable camera  and stop the delivery of frames even though the surface view itself is not destroyed and still stays on the screen
     * Parameters []
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void disableView() {
        synchronized (mSyncObject) {
            mEnabled = false;
            checkCurrentState();
        }
    }

    /**
     * Method     setFlash
     * 设置闪光灯
     * Parameters [flag]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setFlash(boolean flag) {
        synchronized (mSyncObject) {
            if (isCameraAvailable()) {
                try {
                    if (this.mCamera != null && isFlashSupported(this.mCamera)) {
                        Camera.Parameters parameters = this.mCamera.getParameters();
                        if (flag) {
                            if (!parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            } else {
                                return;
                            }
                        } else if (!parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        } else {
                            return;
                        }
                        this.mCamera.setParameters(parameters);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method     setAutoFocus
     * 设置自动对焦
     * Parameters [state]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setAutoFocus(boolean state) {
        if (mAutoFocus == state) {
            return;
        }
        mAutoFocus = state;
        if (mAutoFocus) {
            if (mAutoFocusHandler == null) {
                mAutoFocusHandler = new Handler();
            }
            if (isCameraAvailable() && !supportFocusModeContinuousPicture(mCamera)) {
                scheduleAutoFocus();
            }

        } else {
            if (mAutoFocusHandler != null) {
                mAutoFocusHandler = null;
            }
            if (isCameraAvailable()) {
                try {
                    mCamera.cancelAutoFocus();
                } catch (RuntimeException rex) {
                    rex.printStackTrace();
                }
            }
        }
    }

    /**
     * Method     setAutoFocusInterval
     * 设置自动对焦周期
     * Parameters [millisecond]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    public void setAutoFocusInterval(long millisecond) {
        this.mAutoFocusInterval = millisecond;
    }

    private void scheduleAutoFocus() {
        mAutoFocusHandler.postDelayed(doAutoFocus, mAutoFocusInterval);
    }

    private void safeAutoFocus() {
        if (mAutoFocus && isCameraAvailable()) {
            try {
                mCamera.autoFocus(autoFocusCB);
            } catch (RuntimeException re) {
                // Horrible hack to deal with autofocus errors on Sony devices
                // See https://github.com/dm77/barcodescanner/issues/7 for example
                scheduleAutoFocus(); // wait 1 sec and then do check again
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            safeAutoFocus();
        }
    };

    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if (mAutoFocus) {
                scheduleAutoFocus();
            }
        }
    };


    /**
     * Method     isSupportFocusModeContinuousPicture
     * 检查相机是否支持 FOCUS_MODE_CONTINUOUS_PICTURE 对焦模式
     * 在小米手机上有兼容性问题，其他厂商也不确定（先屏蔽代码）
     * Parameters [camera]
     * Return     boolean
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    private boolean supportFocusModeContinuousPicture(Camera camera) {
        mSupportFocusModeContinuousPicture = false;
        return mSupportFocusModeContinuousPicture;
//        Camera.Parameters parameters = camera.getParameters();
//        if (parameters.getSupportedFocusModes().contains(FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            try {
//                mCamera.cancelAutoFocus();
//                parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
//                mCamera.setParameters(parameters);
//                mSupportFocusModeContinuousPicture = true;
//            } catch (Exception ex) {
//                mSupportFocusModeContinuousPicture = false;
//            }
//        } else {
//            mSupportFocusModeContinuousPicture = false;
//        }
//        return mSupportFocusModeContinuousPicture;
    }

    private boolean isCameraAvailable() {
        synchronized (mSyncObject) {
            boolean result = false;
            int targetState;
            if (mSupportCamera && mEnabled && mSurfaceExist && getVisibility() == VISIBLE) {
                targetState = STARTED;
            } else {
                targetState = STOPPED;
            }
            if (mState == STARTED && targetState == mState) {
                result = true;
            }
            return result;
        }
    }

    private boolean isFlashSupported(Camera camera) {
        if (camera == null) {
            return false;
        }
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || (supportedFlashModes.size() == 1 && ((String) supportedFlashModes.get(0)).equals("off"))) {
            return false;
        }
        return true;
    }

    private void defultParameters(Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        // a.预览图片格式
        /* Image format NV21 causes issues in the Android emulators */
        if (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)) {
            parameters.setPreviewFormat(ImageFormat.YV12);  // "generic" or "android" = android emulator
        } else {
            parameters.setPreviewFormat(ImageFormat.NV21);
        }
        // b.预览图片大小
        Camera.Size optimalPreviewSize = getOptimalPreviewSize2(parameters.getSupportedPreviewSizes(), getWidth(), getHeight());
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);


        // c.拍照图片格式
        List<Integer> pictureFormats = parameters.getSupportedPictureFormats();
        if (pictureFormats != null && pictureFormats.size() > 0 && pictureFormats.contains(ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
        }
        // d.拍照图片大小

        // e.设置预览帧率(基本上帧率是固定值，不同设备不同帧率，一般修改无效）
        //parameters.setPreviewFpsRange();
        //parameters.setPreviewFrameRate();

        camera.setParameters(parameters);
    }

    // endregion

    public interface ParametersCallback {
        //1.设置相机参数
        void setParameters(Camera camera);

    }

    // region Tool method

    private boolean checkOnUIThreadOrNot() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * Method     getDisplayOrientation
     * <p>
     * Parameters [context, cameraId]
     * Return     int
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    private int getDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;
        switch (((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
                break;
        }
        if (info.facing == 1) {
            return (360 - ((info.orientation + degrees) % 360)) % 360;
        }
        return ((info.orientation - degrees) + 360) % 360;
    }

    /**
     * Method     checkCameraHardware
     * Parameters [context]
     * Return     boolean
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Method     getOptimalPreviewSize
     * 根据给出的宽高，寻找最优的预览宽高
     * Find the optimal preview width and height according to the width and height
     * Parameters [sizes, viewWidth, viewHeight]
     * Return     android.hardware.Camera.Size
     * Author     Vin
     * Mail       vinintg@gmail.com
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int viewWidth, int viewHeight) {
        Log.d(TAG, "widgetSize: " + viewWidth + "x" + viewHeight);
        if (sizes == null) {
            return null;
        }
        int w = viewWidth;
        int h = viewHeight;
        Display display = ((WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getWidth() <= display.getHeight()) {
            w = viewHeight;
            h = viewWidth;
        }

        double targetRatio = ((double) w) / ((double) h);
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.1d && ((double) Math.abs(size.height - targetHeight)) < minDiff) {
                optimalSize = size;
                minDiff = (double) Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize != null) {
            Log.d(TAG, "OptimalPreviewSize: " + optimalSize.width + "x" + optimalSize.height);
            return optimalSize;
        }
        minDiff = Double.MAX_VALUE;
        for (Camera.Size size2 : sizes) {
            if (((double) Math.abs(size2.height - targetHeight)) < minDiff) {
                optimalSize = size2;
                minDiff = (double) Math.abs(size2.height - targetHeight);
            }
        }

        Log.d(TAG, "OptimalPreviewSize: " + optimalSize.width + "x" + optimalSize.height);
        return optimalSize;
    }


    private Camera.Size getOptimalPreviewSize2(List<Camera.Size> sizes, int viewWidth, int viewHeight) {
        int w = getWidth();
        int h = getHeight();
        if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            int portraitWidth = h;
            h = w;
            w = portraitWidth;
        }

        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > mAspectTolerance) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize != null) {
            if (targetHeight - optimalSize.height * 2 > 0) {
                optimalSize = null;
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    // endregion
}
