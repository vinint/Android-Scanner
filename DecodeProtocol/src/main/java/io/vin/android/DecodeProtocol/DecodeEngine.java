package io.vin.android.DecodeProtocol;

import android.graphics.Rect;
import android.hardware.Camera;
import android.view.View;

import java.util.List;

/**
 *decode engine interface
 *Author     Vin
 *Mail       vinintg@gmail.com
 *Createtime 2019-07-26 14:11
 *Modifytime 2019-07-26 14:11
 */
public interface DecodeEngine {

    /**
     *Method     enableCache
     * Enable or disable the inter-image result cache (default disabled).
     * Mostly useful for scanning video frames, the cache filters duplicate
     * results from consecutive images, while adding some consistency
     * checking and hysteresis to the results.  Invoking this method also
     * clears the cache.
     *Parameters [enable]
     *Return     void
     *Author     Vin
     *Mail       vinintg@gmail.com
     *Createtime 2019-07-26 14:21
     *Modifytime 2019-07-26 14:21
     */
    void enableCache(Boolean enable);

    /**
     *Method    setSymbology
     * setting the support Symbology to decode.
     * QRCODE 、CODE128 and so on
     *Parameters [symbologys]
     *Return     void
     *Author     Vin
     *Mail       vinintg@gmail.com
     *Createtime 2019-07-26 14:13
     *Modifytime 2019-07-26 14:13
     */
    void setSymbology(List<Symbology> symbologys);

    /**
     *Method     setDecodeRect
     * setting the area which need to decode
     *Parameters [decodeViewRect]
     *Return     void
     *Author     Vin
     *Mail       vinintg@gmail.com
     *Createtime 2019-07-26 14:13
     *Modifytime 2019-07-26 14:13
     */
    void setDecodeRect(Rect decodeViewRect);

    /**
     *Method     setDecodeRect
     * setting the area which need to decode
     *Parameters [view]
     *Return     void
     *Author     Vin
     *Mail       vinintg@gmail.com
     *Createtime 2019-07-26 14:13
     *Modifytime 2019-07-26 14:13
     */
    void setDecodeRect(View view);

    /**
     *Method     decode 后续要废弃
     * decode camera frames data
     *Parameters [data, camera]
     *Return     java.util.List<io.vin.android.scanner.Result>
     *Author     Vin
     *Mail       vinintg@gmail.com
     *Createtime 2019-07-26 14:13
     *Modifytime 2019-07-26 14:13
     */
    List<Result> decode(byte[] data, Camera.Size previewSize,int cameraID);

    void decode(byte[] data, Camera.Size previewSize,int cameraID,DecodeCallback callback);

    public interface DecodeCallback{
        void onDecodeCallback(List<Result> resultList);
    }
}


