package io.vin.android.scanner.util;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import java.util.List;

public class CameraUtils {
    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return camera;
    }

    public static boolean isFlashSupported(Camera camera) {
        if (camera == null) {
            return false;
        }
        Parameters parameters = camera.getParameters();
        if (parameters.getFlashMode() == null) {
            return false;
        }
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || (supportedFlashModes.size() == 1 && ((String) supportedFlashModes.get(0)).equals("off"))) {
            return false;
        }
        return true;
    }
}
