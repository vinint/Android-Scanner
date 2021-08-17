package io.vin.android.scanner.demo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import io.vin.android.DecodeProtocol.Result;
import io.vin.android.DecodeProtocol.Symbology;
import io.vin.android.scanner.ScannerView;

public class ZbarScanTestActivity extends Activity  implements ScannerView.SingleScanCallBack , ScannerView.MultipleScanCallBack{

    private int setDecodeRectCount = 0;
    private ScannerView mZBarScannerView;
    private RelativeLayout mRlDecodeAreaView;
    private ImageView mIvLine;
    private ValueAnimator mScanAnimator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zbar_scan_test);
        mZBarScannerView = findViewById(R.id.zbar_scanner_view);
        mRlDecodeAreaView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        mIvLine = (ImageView) findViewById(R.id.capture_scan_line);
        initScanner();

    }

    @Override
    protected void onResume() {
        mZBarScannerView.startCamera();
        mZBarScannerView.startScan();
        mZBarScannerView.setSingleScanCallBack(this);
        mZBarScannerView.setMultipleScanCallBack(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mZBarScannerView.stopCamera();
        mZBarScannerView.stopScan();
        mZBarScannerView.removeSingleScanCallBack();
        mZBarScannerView.removeMultipleScanCallBack();
        stopScanAnimator();
        super.onPause();
    }

    @Override
    public void singleScan(Result data) {
        Log.d("新ZBAR测试",data.getSymbology() +" "+ data.getContents());
        Toast.makeText(this,data.getSymbology().getName() +":"+ data.getContents(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void multipleScan(List<Result> datas) {
        for (Result item :datas){
            Log.d("新ZBAR测试2",item.getContents());
        }

    }

    private void initScanner(){
        List<Symbology> formats = new ArrayList<>();
        formats.add(Symbology.CODE128);
        formats.add(Symbology.QRCODE);
        formats.add(Symbology.CODE39);
        mZBarScannerView.setSymbology(formats);
        mZBarScannerView.setAutoFocus(true);
        mZBarScannerView.setAutoFocusInterval(500l);

    }

    private void setIdentifyArea(){
        Rect decodeArea = new Rect();

        //1.通过getGlobalVisibleRect计算Rect
        Rect cropRect = new Rect();
        mRlDecodeAreaView.getGlobalVisibleRect(cropRect);

        Rect scanerRect = new Rect();
        mZBarScannerView.getGlobalVisibleRect(scanerRect);

        decodeArea = cropRect;
        decodeArea.top = cropRect.top - scanerRect.top;
        decodeArea.bottom = cropRect.bottom - scanerRect.top;




        //2.通过getLocationOnScreen计算Rect
//        int width = mRlDecodeAreaView.getWidth();
//        int height = mRlDecodeAreaView.getHeight();
//        int[] cropViewOnScreen = new int[2];
//        mRlDecodeAreaView.getLocationOnScreen(cropViewOnScreen);
//        int[] scannerViewOnScreen = new int[2];
//        mZBarScannerView.getLocationOnScreen(scannerViewOnScreen);
//
//        int left = cropViewOnScreen[0];
//        int top = cropViewOnScreen[1] - scannerViewOnScreen[1];
//        int right = left+ width;
//        int bottom = top + height;
//
//        decodeArea.left = left;
//        decodeArea.top = top;
//        decodeArea.right = right;
//        decodeArea.bottom = bottom;



        mZBarScannerView.setDecodeRect(decodeArea);

    }

    private void startScanAnimator() {
        if (mScanAnimator == null) {
            int height = mRlDecodeAreaView.getHeight() - 25;
            mScanAnimator = ObjectAnimator.ofFloat(mIvLine, "translationY", 0F, height).setDuration(3000);
            mScanAnimator.setInterpolator(new LinearInterpolator());
            mScanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mScanAnimator.setRepeatMode(ValueAnimator.REVERSE);
        }
        mScanAnimator.start();
    }

    private void stopScanAnimator(){
        if (mScanAnimator !=null){
            mScanAnimator.cancel();
        }
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        startScanAnimator();
        if (setDecodeRectCount == 0){
//            setIdentifyArea();
            mZBarScannerView.setDecodeRect(mRlDecodeAreaView);
            setDecodeRectCount++;
        }
    }
}
