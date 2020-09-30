package com.core.tpsp.payload;

import lombok.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;

@Data
@Builder
public class ExtraCellDTO {

    private String label;
    private String value;
    private CellType type;
    private CellStyle style;
    private boolean requiredColIndex;
    private Object criterion;
}
