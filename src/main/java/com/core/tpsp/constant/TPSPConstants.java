package com.core.tpsp.constant;

public class TPSPConstants {

    public interface ADDRESS_FORMAT {
        String FULL_ADDRESS_FORMAT = "{0}, {1} {2} {3}";
        String SHORT_ADDRESS_FORMAT = "{0} {1} {2}";
    }

    public interface DATE_FORMAT {
        String FORMAT_dd_MM_yyyy_hh_mm_ss = "dd/MM/yyyy HH:mm:ss";
    }

    public static Boolean APPROVED = true;

    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String HYPHEN = "-";

    public final static String FILE_EXTENSION = ".xlsx";

    public final static String YES = "Yes";
    public final static String NO = "No";

    public final static String START_TOTAL_COL = "2";

    public final static String TOTAL_ROW_FORMULA = "ROWS({0}{1}:{0}{2})";
    public final static String COUNT_IF_FORMULA = "COUNTIF({0}{2}:{0}{3}, \"{1}\")";
}
