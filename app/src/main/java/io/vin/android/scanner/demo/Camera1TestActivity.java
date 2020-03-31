package io.vin.android.scanner.demo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.vin.android.scanner.Result;
import io.vin.android.scanner.ScannerView2;
import io.vin.android.scanner.core.Camera1View;
import io.vin.android.zbar.Symbology;

/**
 * 使用重新封装的ScannerView2
 * Author     Vin
 * Mail       vinintg@gmail.com
 */
public class Camera1TestActivity extends Activity implements View.OnClickListener,ScannerView2.SingleScanCallBack,ScannerView2.MultipleScanCallBack{

    ScannerView2 mScannerView;
    TextView mTvMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        mTvMsg = findViewById(R.id.tv_scan_data);
        mScannerView = findViewById(R.id.sv_camera);
        findViewById(R.id.btn_take_picture).setOnClickListener(this);
        initScanner();
    }

    private void initScanner(){
        List<Symbology> formats = new ArrayList<>();
        formats.add(Symbology.CODE128);
        formats.add(Symbology.QRCODE);
        mScannerView.setSymbology(formats);
        mScannerView.setParametersMode(Camera1View.PARAMETERS_MODE_AUTO);
        mScannerView.setAutoFocus(true);
        mScannerView.setAutoFocusInterval(1000l);
        mScannerView.setDecodeRect(findViewById(R.id.capture_crop_view));
        mScannerView.setSingleScanCallBack(this);
        mScannerView.setMultipleScanCallBack(this);
    }

    @Override
    public void singleScan(Result data) {
        // 一帧相机数据解析后只取一个结果
        mTvMsg.setText(data.getContents());
    }

    @Override
    public void multipleScan(List<Result> datas) {
        // 一帧相机数据解析后所有结果
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_take_picture){
            mScannerView.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mTvMsg.setText("拍照成功！");
                }
            });
        }
    }
}
