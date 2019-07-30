package io.vin.android.scanner;

import io.vin.android.zbar.Symbology;

public class Result {
    private Symbology mSymbology;
    private String mContents;

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
}
