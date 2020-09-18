package com.core.tpsp.utils;

import com.core.tpsp.exception.TpspException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@Component
@Slf4j
public class ExcelHelper {

    @Autowired
    private ObjectMapper mapper;

    public ByteArrayInputStream toExcelReport(List<List<Object>> payloads, List<String> headerCols,
                                              String sheetName) {

        if (CollectionUtils.isEmpty(payloads) || CollectionUtils.isEmpty(headerCols) || StringUtils.isEmpty(sheetName)) {
            log.info("Empty payload");
            return new ByteArrayInputStream(new byte[0]);
        }

        log.info("Begin creating excel workbook {} ...", sheetName);
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet(sheetName);
            int headerSize = headerCols.size();
            PropertyTemplate pt = new PropertyTemplate();
            pt.drawBorders(new CellRangeAddress(0, 0, 0, headerSize - 1),
                    BorderStyle.MEDIUM, BorderExtent.ALL);

            // header creation
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headerSize; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headerCols.get(col));

                // header style
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                font.setColor(IndexedColors.BLUE.getIndex());
                style.setFont(font);
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(style);
            }
            log.info("Finished header {}...", TpspUtils.toJsonString(mapper, headerCols));

            // other rows
            int rowIdx = 1;
            for (List<Object> cols : payloads) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;

                // writing different columns of a same line
                for (Object colVal : cols) {
                    if (colVal instanceof java.lang.Integer) {
                        row.createCell(colIdx).setCellValue((Integer) colVal);
                    } else if (colVal instanceof java.lang.String) {
                        row.createCell(colIdx).setCellValue((String) colVal);
                    } else if (colVal instanceof java.lang.Double) {
                        row.createCell(colIdx).setCellValue((Double) colVal);
                    } else if (colVal instanceof java.lang.Boolean) {
                        row.createCell(colIdx).setCellValue((
                                (Boolean) colVal) ? "Yes" : "No");
                    }
                    colIdx++;
                }

                log.info("Finished row {}...", TpspUtils.toJsonString(mapper, cols));
            }

            pt.applyBorders(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            String msg = MessageFormat.format("Encountered error while exporting excel file {0}", e.getMessage());
            log.error(msg);
            throw new TpspException(msg);
        }
    }

}
