package io.vin.android.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.vin.android.ZbarEngine.ZbarDecodeEngine;
import io.vin.android.DecodeProtocol.DecodeEngine;
import io.vin.android.DecodeProtocol.Symbology;
import io.vin.android.DecodeProtocol.Result;
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

    private Map<String,Long> mBarcodeCacheMap = new HashMap<>();
    private static long mBarcodeInterval = -1;
    private static boolean mEnableBarcodeInterval = false;

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
        super.setPreviewCallback((byte[] data, Camera camera)-> {
            try {
                // 1.解码
                handlePreviewFrame(data,camera);
                // 2.回推预览数据
                if (mScanPreviewCallback!=null){
                    mScanPreviewCallback.onPreviewFrame(data,camera);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private void initScanner(){
        this.mDecodeEngine = new ZbarDecodeEngine(getContext(),this);
        this.mVibrator = (Vibrator) getContext().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        super.setPreviewCallback((byte[] data, Camera camera)-> {
            try {
                // 1.解码
                handlePreviewFrame(data,camera);
                // 2.回推预览数据
                if (mScanPreviewCallback!=null){
                    mScanPreviewCallback.onPreviewFrame(data,camera);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private void handlePreviewFrame(byte[] data, Camera camera) {
        if (!this.mEnableScan || (mSingleScanCallBack == null && mMultipleScanCallBack == null)) {
            return;
        }
        mDecodeEngine.decode(data, camera.getParameters().getPreviewSize(), getCameraID(),v->{
            List<Result> resultList = v;
            if (resultList != null && !resultList.isEmpty()) {
                if (mSingleScanCallBack != null) {
                    Result item = resultList.get(0);
                    if (mEnableBarcodeInterval
                            && mBarcodeCacheMap.containsKey(item.getContents())) {
                        if (System.currentTimeMillis() - mBarcodeCacheMap.get(item.getContents()) > mBarcodeInterval) {
                            //到达时间间隔返回数据
                            mSingleScanCallBack.singleScan(item);
                            mBarcodeCacheMap.put(item.getContents(),System.currentTimeMillis());
                        }
                    } else {
                        mSingleScanCallBack.singleScan(item);
                        mBarcodeCacheMap.put(item.getContents(),System.currentTimeMillis());
                    }
                    //清除超过时间间很久的数据
                    removeTimeoutData();
                }

                if (mMultipleScanCallBack != null) {
                    mMultipleScanCallBack.multipleScan(resultList);
                }

                if (mEnableVibrate) {
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - this.mLastScanTime > 2000) {
                        this.mVibrator.vibrate(200);
                        this.mLastScanTime = nowTime;
                    }
                }
            }
        });

    }

    private void removeTimeoutData(){
        long nowTime = System.currentTimeMillis();
        for (Iterator<Map.Entry<String, Long>> it = mBarcodeCacheMap.entrySet().iterator(); it.hasNext();){
            Map.Entry<String, Long> item = it.next();
            if (nowTime - item.getValue() > mBarcodeInterval){
                it.remove();
            }
        }
    }

    public static void enableBarcodeInterval(boolean enableBarcodeInterval){
        mEnableBarcodeInterval = enableBarcodeInterval;
    }

    public static void setBarcodeInterval(long barcodeInterval){
        mBarcodeInterval = barcodeInterval;
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
