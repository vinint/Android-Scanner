package io.vin.android.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import io.vin.android.DecodeProtocol.DecodeEngine;
import io.vin.android.DecodeProtocol.Symbology;
import io.vin.android.DecodeProtocol.Result;
import java.util.List;

import io.vin.android.ZbarEngine.ZbarDecodeEngine;
import io.vin.android.scanner.core.CameraView;


public class ScannerView extends CameraView {
    private static final String TAG = ScannerView.class.getSimpleName();
    private boolean canScan = true;
    private boolean canVibrate = false;
    private long lastScanTime = -1;
    private Vibrator vibrator;
    private SingleScanCallBack mSingleScanCallBack;
    private MultipleScanCallBack mMultipleScanCallBack;
    private DecodeEngine mDecodeEngine;

    public ScannerView(Context context) {
        super(context);
        this.mDecodeEngine = new ZbarDecodeEngine(context, this);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public ScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDecodeEngine = new ZbarDecodeEngine(context, this);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            camera.getParameters().getPreviewFormat();
            handlePreviewFrame(data, camera);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            camera.setOneShotPreviewCallback(this);
        }
    }

    private void handlePreviewFrame(byte[] data, Camera camera) {
        if (!this.canScan || (mSingleScanCallBack == null && mMultipleScanCallBack == null)) {
            return;
        }
        List<Result> resultList = mDecodeEngine.decode(data, camera.getParameters().getPreviewSize(), getCameraID());
        if (resultList != null && !resultList.isEmpty()) {
            if (mSingleScanCallBack != null) {
                mSingleScanCallBack.singleScan(resultList.get(0));
            }
            if (mMultipleScanCallBack != null) {
                mMultipleScanCallBack.multipleScan(resultList);
            }

            if (canVibrate) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - this.lastScanTime > 2000) {
                    this.vibrator.vibrate(200);
                    this.lastScanTime = nowTime;
                }
            }
        }

    }

    public void startScan() {
        this.canScan = true;
    }

    public void stopScan() {
        this.canScan = false;
    }

    public void enableVibrator(Boolean canVibrate) {
        this.canVibrate = canVibrate;
    }

    // region config decode Engine

    public void setDecoderEngine(DecodeEngine engine) {
        this.mDecodeEngine = engine;
    }

    public void enableCache(Boolean enable) {
        this.mDecodeEngine.enableCache(enable);
    }

    public void setSymbology(List<Symbology> symbologyList) {
        this.mDecodeEngine.setSymbology(symbologyList);
    }

    public void setDecodeRect(Rect decodeRect) {
        this.mDecodeEngine.setDecodeRect(decodeRect);
    }

    public void setDecodeRect(View view) {
        this.mDecodeEngine.setDecodeRect(view);
    }

    // endregion

    // region result callback

    public void setSingleScanCallBack(SingleScanCallBack callBack) {
        this.mSingleScanCallBack = callBack;
    }

    public void removeSingleScanCallBack() {
        this.mSingleScanCallBack = null;
    }

    public void setMultipleScanCallBack(MultipleScanCallBack callBack) {
        this.mMultipleScanCallBack = callBack;
    }

    public void removeMultipleScanCallBack() {
        this.mMultipleScanCallBack = null;
    }

    /**
     * one frame data only decode one result,even if there are multiple results
     * Method     一帧图片数据,返回第一个识别结果(即使有多个)
     * Parameters [callBack]
     * Return     void
     * Author     Vin
     * Mail       vinintg@gmail.com
     * Createtime 2019-07-05 15:45
     * Modifytime 2019-07-05 15:45
     */
    public interface SingleScanCallBack {
        void singleScan(Result data);
    }

    /**
     * one frame data decode multiple results if possible
     * 一帧图片数据,返回多个识别结果
     * Author     Vin
     * Mail       vinintg@gmail.com
     * Createtime 2019-07-02 13:57
     * Modifytime 2019-07-02 13:57
     */
    public interface MultipleScanCallBack {
        void multipleScan(List<Result> datas);
    }

    // endregion

}
