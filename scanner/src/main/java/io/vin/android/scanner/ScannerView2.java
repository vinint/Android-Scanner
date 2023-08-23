package io.vin.android.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.vin.android.DecodeProtocol.DecodeEngine;
import io.vin.android.DecodeProtocol.Result;
import io.vin.android.DecodeProtocol.Symbology;
import io.vin.android.ZbarEngine.ZbarDecodeEngine;
import io.vin.android.scanner.core.Camera1View;

public class ScannerView2 extends Camera1View {
    public ScannerView2(Context context) {
        super(context);
        initScanner();
    }

    public ScannerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScanner();
    }

    public ScannerView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScanner();
    }

    private static boolean mEnableBarcodeInterval = false;
    private static long mBarcodeInterval = -1;
    private static String mUserDefinedRegex = "^[A-Za-z0-9]+$";
    private boolean mEnableScan = true;
    private boolean mEnableVibrate = false;
    private long mLastScanTime = -1;
    private Vibrator mVibrator;
    private DecodeEngine mDecodeEngine;
    private SingleScanCallBack mSingleScanCallBack;
    private MultipleScanCallBack mMultipleScanCallBack;
    private Camera.PreviewCallback mScanPreviewCallback;

    @Override
    public void setPreviewCallback(Camera.PreviewCallback callback) {
        this.mScanPreviewCallback = callback;
        super.setPreviewCallback((byte[] data, Camera camera) -> {
            try {
                // 1.解码
                handlePreviewFrame(data, camera);
                // 2.回推预览数据
                if (mScanPreviewCallback != null) {
                    mScanPreviewCallback.onPreviewFrame(data, camera);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void initScanner() {
        this.mDecodeEngine = new ZbarDecodeEngine(getContext(), this);
        this.mVibrator = (Vibrator) getContext().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        super.setPreviewCallback((byte[] data, Camera camera) -> {
            try {
                // 1.解码
                handlePreviewFrame(data, camera);
                // 2.回推预览数据
                if (mScanPreviewCallback != null) {
                    mScanPreviewCallback.onPreviewFrame(data, camera);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handlePreviewFrame(byte[] data, Camera camera) {
        if (!this.mEnableScan || (mSingleScanCallBack == null && mMultipleScanCallBack == null)) { return;}
        mDecodeEngine.decode(data, camera.getParameters().getPreviewSize(), getCameraID(), v -> {
            List<Result> resultList = v;
            if (resultList != null && !resultList.isEmpty()) {
                Result item = resultList.get(0);
                callBackData(item, resultList);
                if (mEnableVibrate) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - this.mLastScanTime > 2000) {
                        this.mVibrator.vibrate(200);
                    }
                }
            }
        });

    }

    public static void enableBarcodeInterval(boolean enableBarcodeInterval) {
        mEnableBarcodeInterval = enableBarcodeInterval;
    }

    public static void setBarcodeInterval(long barcodeInterval) {
        mBarcodeInterval = barcodeInterval;
    }

    public static void setContentRegex(String regex) {
        mUserDefinedRegex = regex;
    }

    public void startScan() {
        this.mEnableScan = true;
    }

    public void stopScan() {
        this.mEnableScan = false;
    }

    public void enableVibrator(Boolean canVibrate) {
        this.mEnableVibrate = canVibrate;
    }

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

    private boolean matchUserDefinedRegex(Result item) {
        String content = item.getContents();
        if (item == null || TextUtils.isEmpty(content)){ return false; }
        if (TextUtils.isEmpty(mUserDefinedRegex)) return true;
        if (item.getSymbology() != Symbology.QRCODE){
            return content.matches(mUserDefinedRegex);
        }
        return true;
    }

    private void callBackData(Result singleData, List<Result> multipleData) {
        long currentTime = System.currentTimeMillis();
        if (mEnableBarcodeInterval && currentTime - mLastScanTime > mBarcodeInterval) {
            // 到达时间间隔返回数据
            realCallBackData(singleData,multipleData);
            mLastScanTime = currentTime;
        }else {
            realCallBackData(singleData,multipleData);
        }
    }

    private void realCallBackData(Result singleData, List<Result> multipleData) {
        if (singleData != null && mSingleScanCallBack != null) {
            if (singleData != null && matchUserDefinedRegex(singleData)) { mSingleScanCallBack.singleScan(singleData); }
        }
        if (multipleData != null && mMultipleScanCallBack != null) {
            List newMultipleData = new ArrayList<Result>();
            for (Result item:multipleData) {
                if (item != null && matchUserDefinedRegex(item)){
                    newMultipleData.add(item);
                }
            }
            mMultipleScanCallBack.multipleScan(newMultipleData);
        }
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
}
