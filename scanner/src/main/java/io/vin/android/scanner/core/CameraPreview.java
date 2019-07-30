package io.vin.android.scanner.core;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import java.util.List;

import io.vin.android.scanner.util.DisplayUtils;

public class CameraPreview extends SurfaceView implements Callback {
    private static final String TAG = "CameraPreview";
    AutoFocusCallback autoFocusCB = new MyAutoFocusCallback();
    private Runnable doAutoFocus = new AutoFocusRunable();
    private boolean mAutoFocus = true;
    private Handler mAutoFocusHandler;
    private Camera mCamera;
    private PreviewCallback mPreviewCallback;
    private boolean mPreviewing = true;
    private boolean mSurfaceCreated = false;

    private long autoFocusInterval = 1000l;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCamera(Camera camera, PreviewCallback previewCallback) {
        this.mCamera = camera;
        this.mPreviewCallback = previewCallback;
        this.mAutoFocusHandler = new Handler();
    }

    public void initCameraPreview() {
        if (this.mCamera != null) {
            getHolder().addCallback(this);
            getHolder().setType(3);
            if (this.mPreviewing) {
                requestLayout();
            } else {
                showCameraPreview();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.mSurfaceCreated = true;
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (surfaceHolder.getSurface() != null) {
            stopCameraPreview();
            showCameraPreview();
        }
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if (this.mCamera != null) {
            try {
                LayoutParams params = getLayoutParams();
                Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Point outSize = new Point();
                if (VERSION.SDK_INT >= 13) {
                    display.getSize(outSize);
                }
                params.width = outSize.x;
                params.height = outSize.y;
                setLayoutParams(params);
                this.mPreviewing = true;
                setupCameraParameters();
                this.mCamera.setPreviewDisplay(getHolder());
                this.mCamera.setDisplayOrientation(getDisplayOrientation());
                this.mCamera.setOneShotPreviewCallback(this.mPreviewCallback);
                this.mCamera.startPreview();
                if (this.mAutoFocus) {
                    this.mCamera.autoFocus(this.autoFocusCB);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void stopCameraPreview() {
        if (this.mCamera != null) {
            try {
                this.mPreviewing = false;
                this.mCamera.cancelAutoFocus();
                this.mCamera.setOneShotPreviewCallback(null);
                this.mCamera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void setupCameraParameters() {
        Size optimalSize = getOptimalPreviewSize();
        Parameters parameters = this.mCamera.getParameters();
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        this.mCamera.setParameters(parameters);
    }

    public int getDisplayOrientation() {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        int degrees = 0;
        switch (((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
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

    private Size getOptimalPreviewSize() {
        if (this.mCamera == null) {
            return null;
        }
        List<Size> sizes = this.mCamera.getParameters().getSupportedPreviewSizes();
        Point screenResolution = DisplayUtils.getScreenResolution(getContext());
        int w = 0;
        int h = 0;
        Log.i(TAG, "screen ---------> w: " + screenResolution.x + "  h: " + screenResolution.y);
        Log.i(TAG, "view ---------> w: " + getWidth() + "  h: " + getHeight());
        if (null == null || null == null) {
            w = screenResolution.x;
            h = screenResolution.y;
            if (DisplayUtils.getScreenOrientation(getContext()) == 1) {
                w = screenResolution.y;
                h = screenResolution.x;
            }
        }
        double targetRatio = ((double) w) / ((double) h);
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.1d && ((double) Math.abs(size.height - targetHeight)) < minDiff) {
                optimalSize = size;
                minDiff = (double) Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize != null) {
            return optimalSize;
        }
        minDiff = Double.MAX_VALUE;
        for (Size size2 : sizes) {
            if (((double) Math.abs(size2.height - targetHeight)) < minDiff) {
                optimalSize = size2;
                minDiff = (double) Math.abs(size2.height - targetHeight);
            }
        }
        return optimalSize;
    }

    public void setAutoFocus(boolean state) {
        if (this.mCamera != null && this.mPreviewing && state != this.mAutoFocus) {
            this.mAutoFocus = state;
            if (this.mAutoFocus) {
                Log.v(TAG, "Starting autofocus");
                this.mCamera.autoFocus(this.autoFocusCB);
                return;
            }
            Log.v(TAG, "Cancelling autofocus");
            this.mCamera.cancelAutoFocus();
        }
    }

    public void setAutoFocusInterval(long millisecond){
        this.autoFocusInterval = millisecond;
    }

    class AutoFocusRunable implements Runnable {
        AutoFocusRunable() {
        }

        public void run() {
            if (CameraPreview.this.mCamera != null && CameraPreview.this.mPreviewing && CameraPreview.this.mAutoFocus && CameraPreview.this.mSurfaceCreated) {
                try {
                    CameraPreview.this.mCamera.autoFocus(CameraPreview.this.autoFocusCB);
                } catch (RuntimeException e) {
                    CameraPreview.this.mAutoFocusHandler.postDelayed(CameraPreview.this.doAutoFocus, autoFocusInterval);
                }
            }
        }
    }

    class MyAutoFocusCallback implements AutoFocusCallback {
        MyAutoFocusCallback() {
        }

        public void onAutoFocus(boolean success, Camera camera) {
            CameraPreview.this.mAutoFocusHandler.postDelayed(CameraPreview.this.doAutoFocus, autoFocusInterval);
            Log.d(CameraPreview.TAG, "autoFocusCB");
        }
    }

}
