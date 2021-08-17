package io.vin.android.DecodeProtocol.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    public static Point getScreenResolution(Context context) {
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        Point screenResolution = new Point();
        if (VERSION.SDK_INT >= 13) {
            display.getSize(screenResolution);
        } else {
            screenResolution.set(display.getWidth(), display.getHeight());
        }
        return screenResolution;
    }

    public static int getScreenOrientation(Context context) {
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getWidth() == display.getHeight()) {
            return 3;
        }
        if (display.getWidth() < display.getHeight()) {
            return Configuration.ORIENTATION_PORTRAIT;
        }
        return Configuration.ORIENTATION_LANDSCAPE;
    }

}
