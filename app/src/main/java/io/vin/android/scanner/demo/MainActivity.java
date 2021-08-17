package io.vin.android.scanner.demo;

import android.Manifest;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final RxPermissions rxPermissions = new RxPermissions(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_zbar_scan).setOnClickListener(this);
        findViewById(R.id.btn_mlkit_scan).setOnClickListener(this);
        findViewById(R.id.btn_huawei_scan).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_zbar_scan){
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, ZbarScanActivity.class);
                            startActivity(intent);
                        } else {
                            // Oups permission denied
                        }
                    });
        }else if (id == R.id.btn_mlkit_scan){
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, MLKitScanActivity.class);
                            startActivity(intent);
                        } else {
                            // Oups permission denied
                        }
                    });

        }else if (id == R.id.btn_huawei_scan){
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, HuaweiScanActivity.class);
                            startActivity(intent);
                        } else {
                            // Oups permission denied
                        }
                    });
        }
    }
}
