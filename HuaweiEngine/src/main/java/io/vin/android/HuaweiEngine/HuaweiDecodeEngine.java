package io.vin.android.HuaweiEngine;

import android.graphics.Rect;
import android.hardware.Camera;
import android.view.View;

import java.util.List;

import io.vin.android.DecodeProtocol.DecodeEngine;
import io.vin.android.DecodeProtocol.Result;
import io.vin.android.DecodeProtocol.Symbology;

public class HuaweiDecodeEngine implements DecodeEngine {
    @Override
    public void enableCache(Boolean enable) {

    }

    @Override
    public void setSymbology(List<Symbology> symbologys) {

    }

    @Override
    public void setDecodeRect(Rect decodeViewRect) {

    }

    @Override
    public void setDecodeRect(View view) {

    }

    @Override
    public List<Result> decode(byte[] data, Camera camera, int cameraID) {
        return null;
    }

    @Override
    public void decode(byte[] data, Camera camera, int cameraID, DecodeCallback callback) {

    }
}
