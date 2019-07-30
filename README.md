# android-scanner

Android scanner widget using zbar decode，Of course you also can realize other decode engine

[中文文档](./README-CN.md)

## Usage

### 1、Decode only with zbar

Add dependencies:

* Gradle

  ```groovy
  compile 'io.vin.android:zbar:1.0.1'
  ```

  

* Maven

  ```java
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>zbar</artifactId>
    <version>1.0.1</version>
    <type>pom</type>
  </dependency>
  ```

Decoding code:
```java
// 1.initialize ImageScanner
ImageScanner mDecoder = new ImageScanner();
mDecoder.setConfig(Symbol.NONE, Config.X_DENSITY, 3);
mDecoder.setConfig(Symbol.NONE, Config.Y_DENSITY, 3);
//Disable all the Symbols
 mDecoder.setConfig(Symbol.NONE, Config.ENABLE, 0);
// set support Symbols
mDecoder.setConfig(Symbology.CODE128, Config.ENABLE, 1)
mDecoder.setConfig(Symbology.QRCODE, Config.ENABLE, 1)

// 2.set image data
Image image = new Image(width, height, "Y800");
image.setData(data);
//image.setCrop(rotatedRect.left, rotatedRect.top, rotatedRect.width(), rotatedRect.height());

// 3.decode results
int result = this.mDecoder.scanImage(image);
if (result != 0) {
  SymbolSet resultSet = this.mDecoder.getResults();
}
```

**Note**: The use of remote dependencies will load all the files so the platform, if you just want to rely on some of these platforms, then you need to add the compilation configuration:

```groovy
defaultConfig {
  ndk {
    abiFilters 'armeabi', 'armeabi-v7a'
	}
}
```

### 2、Using Scanner widget

Add dependencies:

* Gradle

  ```java
  compile 'io.vin.android:zbar:1.0.1'
  compile 'io.vin.android:scanner:1.0.1'
  ```

  

* Maven

  ```java
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>zbar</artifactId>
    <version>1.0.1</version>
    <type>pom</type>
  </dependency>
  
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>scanner</artifactId>
    <version>1.0.1</version>
    <type>pom</type>
  </dependency>
  ```

Layout xml：

```java
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <io.vin.android.scanner.ScannerView
        android:id="@+id/zbar_scanner_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </io.vin.android.scanner.ScannerView>
 </RelativeLayout>
```

Activity Code：

```java
public class ZbarScanTestActivity extends Activity  implements ScannerView.SingleScanCallBack , ScannerView.MultipleScanCallBack{
    private ScannerView mZBarScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zbar_scan_test);
        mZBarScannerView = findViewById(R.id.zbar_scanner_view);
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
       // Must be called here, otherwise the camera should not be released properly.
        mZBarScannerView.stopCamera();
        mZBarScannerView.stopScan();
        mZBarScannerView.removeSingleScanCallBack();
        mZBarScannerView.removeMultipleScanCallBack();
        super.onPause();
    }

    @Override
    public void singleScan(Result data) {
      // One frame of camera data is parsed and only one result is taken.
        Log.d("",data.getSymbology() +" "+ data.getContents());
        Toast.makeText(this,data.getContents() +" "+ data.getContents(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void multipleScan(List<Result> datas) {
      // One frame of camera data is parsed and all results are taken.
        for (Result item :datas){
            Log.d("",item.getContents());
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
}
```



## Custom decode Engine

If you are not satisfied with zbar decoding, you can initialize  the "io.vin.android.scanner.engine.DecodeEngine" interface.Then set the following code.

```java
mZBarScannerView.setDecoderEngine(engine);
```

This way the code at your business level does not need to be changed.



## Reference below

[https://github.com/ZBar/ZBar](https://github.com/ZBar/ZBar)  

[https://www.gnu.org/software/libiconv](https://www.gnu.org/software/libiconv)  

https://github.com/prathanbomb/android-zbar-sdk

https://github.com/al4fun/SimpleScanner



**PS**: Zbar and libiconv are based on `LGPL-2.1` open source.Therefore, this project is also based on the LGPL-2.1 protocol open source.