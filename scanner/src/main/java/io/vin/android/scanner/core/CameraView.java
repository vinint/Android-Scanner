package io.vin.android.scanner.core;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import io.vin.android.scanner.util.CameraUtils;


public abstract class CameraView extends FrameLayout implements PreviewCallback {
    public Camera mCamera;
    public CameraPreview mPreview;
    private int mCameraId = 0;

    public CameraView(Context context) {
        super(context);
        setupLayout(context, null);
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupLayout(context, attributeSet);
    }

    public void setupLayout(Context context, AttributeSet attributeSet) {
        this.mPreview = new CameraPreview(context);
        addView(this.mPreview);
    }

    public int getCameraID() {
        return mCameraId;
    }

    public void startCamera() {
        this.mCamera = CameraUtils.getCameraInstance();
        if (this.mCamera != null) {
            this.mPreview.setCamera(this.mCamera, this);
            this.mPreview.initCameraPreview();
        }
    }

    public void stopCamera() {
        if (this.mCamera != null) {
            this.mPreview.stopCameraPreview();
            this.mPreview.setCamera(null, null);
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    // region camera function

    public void setFlash(boolean flag) {
        try {
            if (this.mCamera != null && CameraUtils.isFlashSupported(this.mCamera)) {
                Parameters parameters = this.mCamera.getParameters();
                if (flag) {
                    if (!parameters.getFlashMode().equals("torch")) {
                        parameters.setFlashMode("torch");
                    } else {
                        return;
                    }
                } else if (!parameters.getFlashMode().equals("off")) {
                    parameters.setFlashMode("off");
                } else {
                    return;
                }
                this.mCamera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getFlash() {
        if (this.mCamera != null && CameraUtils.isFlashSupported(this.mCamera) && this.mCamera.getParameters().getFlashMode().equals("torch")) {
            return true;
        }
        return false;
    }

    public void toggleFlash() {
        if (this.mCamera != null && CameraUtils.isFlashSupported(this.mCamera)) {
            Parameters parameters = this.mCamera.getParameters();
            if (parameters.getFlashMode().equals("torch")) {
                parameters.setFlashMode("off");
            } else {
                parameters.setFlashMode("torch");
            }
            this.mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocus(boolean state) {
        if (this.mPreview != null) {
            this.mPreview.setAutoFocus(state);
        }
    }

    public void setAutoFocusInterval(long millisecond){
        if (this.mPreview != null) {
            this.mPreview.setAutoFocusInterval(millisecond);
        }
    }

    // endregion

    public int getDisplayOrientation(){
        return mPreview.getDisplayOrientation();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
