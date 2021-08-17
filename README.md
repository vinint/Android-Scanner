# Android-Scanner

Android scanner widget using zbar decode，Of course you also can realize other decode engine

[中文文档](./README-CN.md)

## Usage

### 1、Decode only with zbar

Add dependencies(Jcenter):

* Gradle

  ```groovy
  compile 'io.vin.android:zbar:1.0.2'
  ```

  

* Maven

  ```java
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>zbar</artifactId>
    <version>1.0.2</version>
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
  compile 'io.vin.android:zbar:1.0.2'
  compile 'io.vin.android:scanner:1.0.11'
  ```

  

* Maven

  ```java
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>zbar</artifactId>
    <version>1.0.2</version>
    <type>pom</type>
  </dependency>
  
  <dependency>
    <groupId>io.vin.android</groupId>
    <artifactId>scanner</artifactId>
    <version>1.0.11</version>
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

    <io.vin.android.scanner.ScannerView2
        android:id="@+id/zbar_scanner_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </io.vin.android.scanner.ScannerView2>
 </RelativeLayout>
```

Activity Code：

```java
public class Camera1TestActivity extends Activity implements View.OnClickListener,
ScannerView2.SingleScanCallBack,ScannerView2.MultipleScanCallBack{

    ScannerView2 mScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        mScannerView = findViewById(R.id.sv_camera);
        initScanner();
    }

    private void initScanner(){
        List<Symbology> formats = new ArrayList<>();
        formats.add(Symbology.CODE128);
        formats.add(Symbology.QRCODE);
      	//Setting support symbology
        mScannerView.setSymbology(formats);
      	//setting camera parameters mode
        mScannerView.setParametersMode(Camera1View.PARAMETERS_MODE_AUTO);
      	//setting autofocus
        mScannerView.setAutoFocus(true);
        mScannerView.setAutoFocusInterval(1000l);
      	//setting Decode Rect
        mScannerView.setDecodeRect(findViewById(R.id.capture_crop_view));
      	//Setting Single scan callback 
        mScannerView.setSingleScanCallBack(this);
				//Setting Multiple scan callback 
        mScannerView.setMultipleScanCallBack(this);
    }

    @Override
    public void singleScan(Result data) {
        //A frame camera data only take one result
    }

    @Override
    public void multipleScan(List<Result> datas) {
      //A frame camera data take multiple result
    }
}
```



## Custom decode Engine

If you are not satisfied with zbar decoding, you can initialize  the "io.vin.android.decodeprotocol.DecodeEngine" interface.Then set the following code.

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