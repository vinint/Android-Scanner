package io.vin.android.scanner;

public class ScannerSDK {
    private static DecodeEngineEnum mDecodeEngineType = DecodeEngineEnum.ZBAR;
    public static void setDecodeEngine(DecodeEngineEnum type){
        mDecodeEngineType = type;
    }

    public static DecodeEngineEnum getDecodeEngine(){
        return mDecodeEngineType;
    }
}
