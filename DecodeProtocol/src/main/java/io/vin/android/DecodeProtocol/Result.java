package io.vin.android.DecodeProtocol;


import android.graphics.Rect;

public class Result {
    private Symbology mSymbology;
    private String mContents;
    private Rect mRect;

    public void setContents(String contents) {
        this.mContents = contents;
    }

    public void setSymbology(Symbology symbology) {
        this.mSymbology = symbology;
    }

    public Symbology getSymbology() {
        return this.mSymbology;
    }

    public String getContents() {
        return this.mContents;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }
}
