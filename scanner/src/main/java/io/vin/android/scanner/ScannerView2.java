package io.vin.android.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

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


    private boolean canScan = true;
    private boolean canVibrate = false;
    private long lastScanTime = -1;
    private Vibrator mVibrator;
    private SingleScanCallBack mSingleScanCallBack;
    private MultipleScanCallBack mMultipleScanCallBack;
    private DecodeEngine mDecodeEngine;
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
        if (!this.canScan || (mSingleScanCallBack == null && mMultipleScanCallBack == null)) {
            return;
        }
        mDecodeEngine.decode(data, camera,getCameraID(),v->{
            List<Result> resultList = v;
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
                        this.mVibrator.vibrate(200);
                        this.lastScanTime = nowTime;
                    }
                }
            }
        });

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
