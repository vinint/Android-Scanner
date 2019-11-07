package io.vin.android.scanner.demo;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final RxPermissions rxPermissions = new RxPermissions(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_scan).setOnClickListener(this);
        findViewById(R.id.btn_scan2).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_scan){
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this,ZbarScanTestActivity.class);
                            startActivity(intent);
                        } else {
                            // Oups permission denied
                        }
                    });
        }else if (v.getId() == R.id.btn_scan2){
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this,Camera1TestActivity.class);
                            startActivity(intent);
                        } else {
                            // Oups permission denied
                        }
                    });
        }
    }
}
