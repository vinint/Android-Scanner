# Android-Scanner

本扫描控件使用zbar解码,当然你也可以实现其他解码引擎

## 使用

### 1、仅使用zbar

添加依赖：

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

识别代码：
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

**注意**：使用远程依赖方式会加载所有平台的so文件，如果你只想依赖其中某几个平台so文件，那么你需要添加编译配置：

```groovy
defaultConfig {
  ndk {
    abiFilters 'armeabi', 'armeabi-v7a'
	}
}
```

### 2、使用整个控件

添加依赖：

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

布局文件：

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

代码实现：

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
        //必须在这里调用，否则相机不能正常释放。
        mZBarScannerView.stopCamera();
        mZBarScannerView.stopScan();
        mZBarScannerView.removeSingleScanCallBack();
        mZBarScannerView.removeMultipleScanCallBack();
        super.onPause();
    }

    @Override
    public void singleScan(Result data) {
      // 一帧相机数据解析后只取一个结果
        Log.d("",data.getSymbology() +" "+ data.getContents());
        Toast.makeText(this,data.getContents() +" "+ data.getContents(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void multipleScan(List<Result> datas) {
      // 一帧相机数据解析后所有结果
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



## 自定义解码引擎

如果你觉得zbar的解码你不是很满意，那你可以自己实现io.vin.android.scanner.engine.DecodeEngine接口,然后设置

```java
mZBarScannerView.setDecoderEngine(engine);
```

这样在你的业务层面的代码不需要再做其他的改动。



## 参考以下

[https://github.com/ZBar/ZBar](https://github.com/ZBar/ZBar)  

[https://www.gnu.org/software/libiconv](https://www.gnu.org/software/libiconv)  

https://github.com/prathanbomb/android-zbar-sdk

https://github.com/al4fun/SimpleScanner

**PS**.`Zbar`和`libiconv`都是基于`LGPL-2.1`协议开源的。因此本项目也是基于LGPL-2.1协议开源。

