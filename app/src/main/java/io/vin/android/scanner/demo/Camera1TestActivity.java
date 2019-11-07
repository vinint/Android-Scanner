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
import io.vin.android.zbar.Symbology;

/**
 * 使用重新封装的ScannerView2
 * Author     Vin
 * Mail       vinintg@gmail.com
 */
public class Camera1TestActivity extends Activity implements View.OnClickListener,ScannerView2.SingleScanCallBack{

    ScannerView2 mScannerView;
    TextView mTvMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        mTvMsg = findViewById(R.id.tv_scan_data);
        mScannerView = findViewById(R.id.sv_camera);
        mScannerView.setAutoFocus(true);
        findViewById(R.id.btn_take_picture).setOnClickListener(this);
        initScanner();
    }

    private void initScanner(){
        List<Symbology> formats = new ArrayList<>();
        formats.add(Symbology.CODE128);
        formats.add(Symbology.QRCODE);
        mScannerView.setSymbology(formats);
        mScannerView.setAutoFocus(true);
        mScannerView.setAutoFocusInterval(1000l);
        mScannerView.setSingleScanCallBack(this);
        mScannerView.setDecodeRect(findViewById(R.id.capture_crop_view));
    }

    @Override
    public void singleScan(Result data) {
        mTvMsg.setText(data.getContents());
        Toast.makeText(this,data.getSymbology().getName() +":"+ data.getContents(),Toast.LENGTH_SHORT).show();
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
