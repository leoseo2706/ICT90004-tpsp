package com.core.tpsp.payload;

import lombok.*;

import java.io.ByteArrayInputStream;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelReportDTO {

    private String fileName;
    private ByteArrayInputStream data;

}
