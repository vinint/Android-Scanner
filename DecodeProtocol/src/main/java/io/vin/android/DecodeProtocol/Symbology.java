package io.vin.android.DecodeProtocol;

import java.util.ArrayList;
import java.util.List;

public class Symbology {
    private int mId;
    private String mName;

    public static final Symbology NONE = new Symbology(Symbol.NONE, "NONE");
    public static final Symbology PARTIAL = new Symbology(Symbol.PARTIAL, "PARTIAL");
    public static final Symbology EAN8 = new Symbology(Symbol.EAN8, "EAN8");
    public static final Symbology UPCE = new Symbology(Symbol.UPCE, "UPCE");
    public static final Symbology ISBN10 = new Symbology(Symbol.ISBN10, "ISBN10");
    public static final Symbology UPCA = new Symbology(Symbol.UPCA, "UPCA");
    public static final Symbology EAN13 = new Symbology(Symbol.EAN13, "EAN13");
    public static final Symbology ISBN13 = new Symbology(Symbol.ISBN13, "ISBN13");
    public static final Symbology I25 = new Symbology(Symbol.I25, "I25");
    public static final Symbology DATABAR = new Symbology(Symbol.DATABAR, "DATABAR");
    public static final Symbology DATABAR_EXP = new Symbology(Symbol.DATABAR_EXP, "DATABAR_EXP");
    public static final Symbology CODABAR = new Symbology(Symbol.CODABAR, "CODABAR");
    public static final Symbology CODE39 = new Symbology(Symbol.CODE39, "CODE39");
    public static final Symbology PDF417 = new Symbology(Symbol.PDF417, "PDF417");
    public static final Symbology QRCODE = new Symbology(Symbol.QRCODE, "QRCODE");
    public static final Symbology CODE93 = new Symbology(Symbol.CODE93, "CODE93");
    public static final Symbology CODE128 = new Symbology(Symbol.CODE128, "CODE128");

    public static final List<Symbology> ALL = new ArrayList<Symbology>();

    static {
        ALL.add(Symbology.PARTIAL);
        ALL.add(Symbology.EAN8);
        ALL.add(Symbology.UPCE);
        ALL.add(Symbology.ISBN10);
        ALL.add(Symbology.UPCA);
        ALL.add(Symbology.EAN13);
        ALL.add(Symbology.ISBN13);
        ALL.add(Symbology.I25);
        ALL.add(Symbology.DATABAR);
        ALL.add(Symbology.DATABAR_EXP);
        ALL.add(Symbology.CODABAR);
        ALL.add(Symbology.CODE39);
        ALL.add(Symbology.PDF417);
        ALL.add(Symbology.QRCODE);
        ALL.add(Symbology.CODE93);
        ALL.add(Symbology.CODE128);
    }

    public Symbology(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public static Symbology getFormatById(int id) {
        for(Symbology format : ALL) {
            if(format.getId() == id) {
                return format;
            }
        }
        return Symbology.NONE;
    }
}