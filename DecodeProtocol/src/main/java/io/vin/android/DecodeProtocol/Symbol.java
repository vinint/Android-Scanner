package io.vin.android.DecodeProtocol;

public class Symbol {
    /**
     * No symbol decoded.
     */
    public static final int NONE = 0;
    /**
     * Symbol detected but not decoded.
     */
    public static final int PARTIAL = 1;

    /**
     * EAN-8.
     */
    public static final int EAN8 = 8;
    /**
     * UPC-E.
     */
    public static final int UPCE = 9;
    /**
     * ISBN-10 (from EAN-13).
     */
    public static final int ISBN10 = 10;
    /**
     * UPC-A.
     */
    public static final int UPCA = 12;
    /**
     * EAN-13.
     */
    public static final int EAN13 = 13;
    /**
     * ISBN-13 (from EAN-13).
     */
    public static final int ISBN13 = 14;
    /**
     * Interleaved 2 of 5.
     */
    public static final int I25 = 25;
    /**
     * DataBar (RSS-14).
     */
    public static final int DATABAR = 34;
    /**
     * DataBar Expanded.
     */
    public static final int DATABAR_EXP = 35;
    /**
     * Codabar.
     */
    public static final int CODABAR = 38;
    /**
     * Code 39.
     */
    public static final int CODE39 = 39;
    /**
     * PDF417.
     */
    public static final int PDF417 = 57;
    /**
     * QR Code.
     */
    public static final int QRCODE = 64;
    /**
     * Code 93.
     */
    public static final int CODE93 = 93;
    /**
     * Code 128.
     */
    public static final int CODE128 = 128;
}
