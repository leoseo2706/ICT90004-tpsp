package com.core.tpsp.constant;

public class TPSPConstants {

    public interface ROLE {
        String CONVENOR = "Convenor";
    }

    public interface ADDRESS_FORMAT {
        String FULL_ADDRESS_FORMAT = "{0}, {1} {2} {3}";
        String SHORT_ADDRESS_FORMAT = "{0} {1} {2}";
    }

    public interface DATE_FORMAT {
        String FORMAT_dd_MM_yyyy_hh_mm_ss = "dd/MM/yyyy HH:mm:ss";
    }

    public static Boolean APPROVED = true;

    public static final String EMPTY = "";

    public final static String FILE_EXTENSION = ".xlsx";
}
