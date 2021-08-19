package io.vin.android.MLKitEngine;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import io.vin.android.DecodeProtocol.DecodeEngine;
import io.vin.android.DecodeProtocol.Symbology;
import io.vin.android.DecodeProtocol.Result;
import io.vin.android.DecodeProtocol.utils.DisplayUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MLKitDecodeEngine implements DecodeEngine {
    private final WeakReference<Context> mContext;
    private final WeakReference<View> mView;
    private WeakReference<View> mDecodeAreaView;
    private Rect mDecodeAreaViewRect;
    //当前界面是否固定
    private volatile boolean isUILock = false;
    private volatile boolean isDecoding = false;
    private int mCameraID = 0;
    private int previewWidth = -1;
    private int previewHeight = -1;
    private int displayOrientation = 0;
    // 最终相对于预览图片的矩形框
    private Rect mDecodeRect;//与预览图片旋转保持同步
    private Rect mScaledRect;
    //支持条码、二维码的格式
    private List<Symbology> mSymbologyList;
    //解码结果
    private List<Result> mScanResultList = new ArrayList<>();

    //Google MLKit解码
    private BarcodeScannerOptions mScannerOptions;
    private BarcodeScanner mScanner;

    public MLKitDecodeEngine(Context context, View view) {
        this.mContext = new WeakReference<>(context);
        this.mView = new WeakReference<>(view);
        this.mSymbologyList = Symbology.ALL;
        configDecoder();
    }

    @Override
    public void enableCache(Boolean enable) {
    }

    @Override
    public void setDecodeRect(Rect decodeAreaViewRect) {
        this.mDecodeAreaViewRect = decodeAreaViewRect;
    }

    @Override
    public void setDecodeRect(View view) {
        mDecodeAreaView = new WeakReference<>(view);
    }

    @Override
    public List<Result> decode(byte[] data, Camera camera, int cameraID) {
        if (!isUILock) {
            this.mCameraID = cameraID;
            this.previewWidth = camera.getParameters().getPreviewSize().width;
            this.previewHeight = camera.getParameters().getPreviewSize().height;
            this.displayOrientation = getDisplayOrientation(0);
            //计算相对于预览图片的解码区域
            mDecodeRect = getFinalDecodeRect(mDecodeAreaView.get());
        }
        InputImage image = InputImage.fromByteArray(
                data,
                previewWidth,
                previewHeight,
                displayOrientation,
                InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        );

        Task<List<Barcode>> resultTask = mScanner.process(image);
        List<Barcode> barcodeList = resultTask.getResult();
        //若能扫描到条码，说明UI已经固定
        if (!barcodeList.isEmpty()) {
            isUILock = true;
        }
        mScanResultList.clear();
        for (Barcode item : barcodeList) {
            if (containsRect(item.getBoundingBox(), mScaledRect)) {
            Result resultItem = new Result();
            resultItem.setContents(item.getRawValue());
            mScanResultList.add(resultItem);
            }
        }
        return mScanResultList;
    }

    @Override
    public void decode(byte[] data, Camera camera, int cameraID, DecodeCallback callback) {
        if (!isUILock) {
            this.mCameraID = cameraID;
            this.previewWidth = camera.getParameters().getPreviewSize().width;
            this.previewHeight = camera.getParameters().getPreviewSize().height;
            this.displayOrientation = getDisplayOrientation(0);
            //计算相对于预览图片的解码区域
            mDecodeRect = getFinalDecodeRect(mDecodeAreaView.get());
        }

        if (!isDecoding) {
            InputImage image = InputImage.fromByteArray(
                    data,
                    previewWidth,
                    previewHeight,
                    displayOrientation,
                    InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
            );

            mScanner.process(image)
                    .addOnFailureListener(v-> isDecoding = false)
                    .addOnCompleteListener(v -> {
                        isDecoding = false;
                        List<Barcode> barcodeList = v.getResult();
                        //若能扫描到条码，说明UI已经固定
                        if (!barcodeList.isEmpty()) {
                            isUILock = true;
                        }
                        mScanResultList.clear();
                        for (Barcode item : barcodeList) {
                            if (containsRect(item.getBoundingBox(), mScaledRect)) {
                                Result resultItem = new Result();
                                resultItem.setContents(item.getDisplayValue());
                                resultItem.setRect(item.getBoundingBox());
                                resultItem.setSymbology(barcode2Symbology(item.getFormat()));
                                mScanResultList.add(resultItem);
                            }
                        }
                        callback.onDecodeCallback(mScanResultList);
                    });
            isDecoding = true;
        }
    }

    @Override
    public void setSymbology(List<Symbology> symbologys) {
        this.mSymbologyList = symbologys;
        configDecoder();
    }

    private void configDecoder() {
        mScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_CODE_128, convertSymbology(mSymbologyList))
                .build();
        mScanner = BarcodeScanning.getClient(mScannerOptions);
    }

    private boolean containsRect(Rect child, Rect parent) {
        if (child.top >= parent.top &&
                child.left >= parent.left &&
                parent.bottom >= child.bottom &&
                parent.right >= child.right) {
            return true;
        }
        return false;
    }

    private int[] convertSymbology(List<Symbology> symbologyList) {
        int[] barcodeArray = new int[symbologyList.size()];
        for (int i = 0; i < symbologyList.size(); i++) {
            Symbology oldItem = symbologyList.get(i);
            barcodeArray[i] = symbology2Barcode(oldItem);
        }
        return barcodeArray;
    }

    private int symbology2Barcode(Symbology oldItem) {
        int barcode = Barcode.FORMAT_CODE_128;
        if (Symbology.PARTIAL.equals(oldItem)) {
        } else if (Symbology.EAN8.equals(oldItem)) {
            barcode = Barcode.FORMAT_EAN_8;
        } else if (Symbology.UPCE.equals(oldItem)) {
            barcode = Barcode.FORMAT_UPC_E;
        } else if (Symbology.ISBN10.equals(oldItem)) {
            barcode = Barcode.FORMAT_EAN_13;
        } else if (Symbology.UPCA.equals(oldItem)) {
            barcode = Barcode.FORMAT_UPC_A;
        } else if (Symbology.EAN13.equals(oldItem)) {
            barcode = Barcode.FORMAT_EAN_13;
        } else if (Symbology.ISBN13.equals(oldItem)) {
            barcode = Barcode.FORMAT_EAN_13;
        } else if (Symbology.I25.equals(oldItem)) {
        } else if (Symbology.DATABAR.equals(oldItem)) {
            barcode = Barcode.FORMAT_DATA_MATRIX;
        } else if (Symbology.DATABAR_EXP.equals(oldItem)) {
            barcode = Barcode.FORMAT_DATA_MATRIX;
        } else if (Symbology.CODABAR.equals(oldItem)) {
            barcode = Barcode.FORMAT_CODABAR;
        } else if (Symbology.CODE39.equals(oldItem)) {
            barcode = Barcode.FORMAT_CODE_39;
        } else if (Symbology.PDF417.equals(oldItem)) {
            barcode = Barcode.FORMAT_PDF417;
        } else if (Symbology.QRCODE.equals(oldItem)) {
            barcode = Barcode.FORMAT_QR_CODE;
        } else if (Symbology.CODE93.equals(oldItem)) {
            barcode = Barcode.FORMAT_CODE_93;
        } else if (Symbology.CODE128.equals(oldItem)) {
            barcode = Barcode.FORMAT_CODE_128;
        }
        return barcode;
    }

    private Symbology barcode2Symbology(int barcode) {
        Symbology symbology = Symbology.CODE128;
        if (Barcode.FORMAT_EAN_8 == barcode) {
            symbology = Symbology.EAN8;
        } else if (Barcode.FORMAT_UPC_E == barcode) {
            symbology = Symbology.UPCE;
        } else if (Barcode.FORMAT_UPC_A == barcode) {
            symbology = Symbology.UPCA;
        } else if (Barcode.FORMAT_EAN_13 == barcode) {
            symbology = Symbology.EAN13;
        }else if (Barcode.FORMAT_DATA_MATRIX == barcode) {
            symbology = Symbology.DATABAR_EXP;
        } else if (Barcode.FORMAT_CODABAR == barcode) {
            symbology = Symbology.CODABAR;
        } else if (Barcode.FORMAT_CODE_39 ==barcode) {
            symbology = Symbology.CODE39;
        } else if (Barcode.FORMAT_PDF417 == barcode) {
            symbology = Symbology.PDF417;
        } else if (Barcode.FORMAT_QR_CODE == barcode) {
            symbology = Symbology.QRCODE;
        } else if (Barcode.FORMAT_CODE_93 == barcode) {
            symbology = Symbology.CODE93;
        } else if (Barcode.FORMAT_CODE_128 == barcode) {
            symbology = Symbology.CODE128;
        }
        return symbology;
    }

    public int getDisplayOrientation(int cameraID) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);
        int degrees = 0;
        switch (((WindowManager) mContext.get().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
                break;
        }
        if (info.facing == 1) {
            return (360 - ((info.orientation + degrees) % 360)) % 360;
        }
        return ((info.orientation - degrees) + 360) % 360;
    }

    private Rect getFinalDecodeRect(View view) {
        Rect decodeViewRect = getDecodeView2CameraViewRect(view);
        Rect scaledRect = getCameraView2PreviewScaledRect(decodeViewRect, previewWidth, previewHeight);
        mScaledRect = scaledRect;
        Rect rotatedRect = getPreviewRotatedRect(scaledRect, previewWidth, previewHeight);
        return rotatedRect;
    }

    private Rect getDecodeView2CameraViewRect(View view) {
        Rect decodeArea = new Rect();

        //1.通过getGlobalVisibleRect计算Rect
        Rect cropRect = new Rect();
        view.getGlobalVisibleRect(cropRect);

        Rect scanerRect = new Rect();
        mView.get().getGlobalVisibleRect(scanerRect);

        if (cropRect.width() == 0 ||
                cropRect.height() == 0 ||
                scanerRect.width() == 0 ||
                scanerRect.height() == 0) {
            //若view未渲染,直接放弃计算
            decodeArea = null;
        } else {
            // 矩形A中心点坐标
            int aCenterX = (cropRect.left + cropRect.right) / 2;
            int aCenterY = (cropRect.top + cropRect.bottom) / 2;

            // 矩形B中心点坐标
            int bCenterX = (scanerRect.left + scanerRect.right) / 2;
            int bCenterY = (scanerRect.top + scanerRect.bottom) / 2;

            // 两个中心点之间的宽高
            int twoPointWidth = Math.abs(aCenterX - bCenterX);
            int twoPointHeight = Math.abs(aCenterY - bCenterY);

            if (twoPointWidth < (cropRect.width() + scanerRect.width()) / 2
                    && twoPointHeight < (cropRect.height() + scanerRect.height()) / 2) {
                //两个矩形相交
                Rect intersectRect = new Rect();
                intersectRect.left = Math.max(cropRect.left, scanerRect.left);
                intersectRect.top = Math.max(cropRect.top, scanerRect.top);
                intersectRect.right = Math.min(cropRect.right, scanerRect.right);
                intersectRect.bottom = Math.min(cropRect.bottom, scanerRect.bottom);

                //计算相交区域，相对于camera区域的相对坐标
                decodeArea.left = intersectRect.left - scanerRect.left;
                decodeArea.right = intersectRect.right - scanerRect.left;
                decodeArea.top = intersectRect.top - scanerRect.top;
                decodeArea.bottom = intersectRect.bottom - scanerRect.top;

                Log.d("ZbarDecodeEngine", "cropRect:" + cropRect.left + "," + cropRect.top + "," + cropRect.right + "," + cropRect.bottom);
                Log.d("ZbarDecodeEngine", "scanerRect:" + scanerRect.left + "," + scanerRect.top + "," + scanerRect.right + "," + scanerRect.bottom);
                Log.d("ZbarDecodeEngine", "intersectRect:" + intersectRect.left + "," + intersectRect.top + "," + intersectRect.right + "," + intersectRect.bottom);
                Log.d("ZbarDecodeEngine", "decodeArea:" + decodeArea.left + "," + decodeArea.top + "," + decodeArea.right + "," + decodeArea.bottom);

            } else {
                //两个矩形不相交
                decodeArea = null;
            }
        }

        return decodeArea;
    }

    private Rect getCameraView2PreviewScaledRect(Rect framingRect, int previewWidth, int previewHeight) {
        int scanerViewWidth = this.mView.get().getWidth();
        int scanerViewHeight = this.mView.get().getHeight();

        int width, height;
        if (DisplayUtils.getScreenOrientation(mContext.get()) == Configuration.ORIENTATION_PORTRAIT//竖屏使用
                && previewHeight < previewWidth) {
            width = previewHeight;
            height = previewWidth;
        } else if (DisplayUtils.getScreenOrientation(mContext.get()) == Configuration.ORIENTATION_LANDSCAPE//横屏使用
                && previewHeight > previewWidth) {
            width = previewHeight;
            height = previewWidth;
        } else {
            width = previewWidth;
            height = previewHeight;
        }

        Rect scaledRect = new Rect(framingRect);
        scaledRect.left = scaledRect.left * width / scanerViewWidth;
        scaledRect.right = scaledRect.right * width / scanerViewWidth;
        scaledRect.top = scaledRect.top * height / scanerViewHeight;
        scaledRect.bottom = scaledRect.bottom * height / scanerViewHeight;

        return scaledRect;
    }

    private Rect getPreviewRotatedRect(Rect rect, int previewWidth, int previewHeight) {
        int orientation = getDisplayOrientation(0);
        Rect rotatedRect = new Rect(rect);

        if (orientation == 90) {//若相机图像需要顺时针旋转90度，则将扫码框逆时针旋转90度
            rotatedRect.left = rect.top;
            rotatedRect.top = previewHeight - rect.right;
            rotatedRect.right = rect.bottom;
            rotatedRect.bottom = previewHeight - rect.left;
        } else if (orientation == 180) {//若相机图像需要顺时针旋转180度,则将扫码框逆时针旋转180度
            rotatedRect.left = previewWidth - rect.right;
            rotatedRect.top = previewHeight - rect.bottom;
            rotatedRect.right = previewWidth - rect.left;
            rotatedRect.bottom = previewHeight - rect.top;
        } else if (orientation == 270) {//若相机图像需要顺时针旋转270度，则将扫码框逆时针旋转270度
            rotatedRect.left = previewWidth - rect.bottom;
            rotatedRect.top = rect.left;
            rotatedRect.right = previewWidth - rect.top;
            rotatedRect.bottom = rect.right;
        }

        return rotatedRect;
    }
}
