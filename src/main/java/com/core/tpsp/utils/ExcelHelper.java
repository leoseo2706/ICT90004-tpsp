package com.core.tpsp.utils;

import com.core.tpsp.constant.TPSPConstants;
import com.core.tpsp.exception.TpspException;
import com.core.tpsp.payload.ExtraCellDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Component
@Slf4j
public class ExcelHelper {

    @Autowired
    private ObjectMapper mapper;

    public ByteArrayInputStream toExcelReport(List<List<Object>> payloads, List<String> headerCols,
                                              String sheetName, List<List<ExtraCellDTO>> extraRows) {

        if (CollectionUtils.isEmpty(headerCols) || StringUtils.isEmpty(sheetName)) {
            log.info("Empty payload");
            return new ByteArrayInputStream(new byte[0]);
        }

        log.info("Begin creating excel workbook {} ...", sheetName);
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle customBoldStyle = createCustomBoldFont(workbook);
//            int headerSize = headerCols.size();
//            PropertyTemplate pt = new PropertyTemplate();
//            pt.drawBorders(new CellRangeAddress(0, 0, 0, headerSize - 1),
//                    BorderStyle.MEDIUM, BorderExtent.ALL);
//            pt.applyBorders(sheet);

            // row index
            AtomicInteger rowIdx = new AtomicInteger();
            AtomicReference<String> firstColLetterAR = new AtomicReference<String>(null);
            int headerSize = headerCols.size();
            int extraValueColIdx = headerSize - 1;
            int extraLabelColIdx = extraValueColIdx - 1;

            // header creation
            Row headerRow = sheet.createRow(rowIdx.getAndIncrement());
            IntStream.range(0, headerSize).forEach(colIdx -> {

                // header row
                Cell cell = headerRow.createCell(colIdx);
                cell.setCellValue(headerCols.get(colIdx));
                cell.setCellStyle(customBoldStyle);

                // mark letter number
                if (colIdx == 0) {
                    firstColLetterAR.set(getCellLetter(cell));
                }
            });
            log.info("Finished header {}...", TpspUtils.toJsonString(mapper, headerCols));

            // body rows
            if (!CollectionUtils.isEmpty(payloads)) {
                payloads.forEach(cells -> {
                    Row row = sheet.createRow(rowIdx.getAndIncrement());
                    AtomicInteger colIdx = new AtomicInteger();
                    cells.forEach(value -> {
                        Cell cell = row.createCell(colIdx.getAndIncrement());
                        setCellBasedOnType(cell, value, null, null);
                    });
                    log.info("Finished row {}...", TpspUtils.toJsonString(mapper, cells));
                });
            }

            int lastRowIdx = rowIdx.get();
            String firstColLetter = firstColLetterAR.get();

            // total row
            sheet.createRow(rowIdx.getAndIncrement());
            Row totalRow = sheet.createRow(rowIdx.getAndIncrement());

            // total row label
            Cell totalLabel = totalRow.createCell(extraLabelColIdx);
            totalLabel.setCellValue("Total");
            totalLabel.setCellStyle(customBoldStyle);

            // total row formula
            Cell totalValue = totalRow.createCell(extraValueColIdx);
            totalValue.setCellFormula(MessageFormat.format(TPSPConstants.TOTAL_ROW_FORMULA,
                    firstColLetter, TPSPConstants.START_TOTAL_COL, lastRowIdx));

            // extra rows
            if (!CollectionUtils.isEmpty(extraRows)) {

                extraRows.forEach(extraCells -> {
                    Row extraRow = sheet.createRow(rowIdx.getAndIncrement());
                    IntStream.range(0, extraCells.size()).forEach(extraCellIdx -> {
                        ExtraCellDTO extraCellDTO = extraCells.get(extraCellIdx);

                        // set extra label
                        setCellBasedOnType(extraRow.createCell(extraLabelColIdx),
                                extraCellDTO.getLabel(), null, customBoldStyle);

                        // set extra value
                        String value = extraCellDTO.isRequiredColIndex()
                                ? MessageFormat.format(extraCellDTO.getValue(),
                                TPSPConstants.EMPTY, TPSPConstants.EMPTY,
                                TPSPConstants.START_TOTAL_COL, lastRowIdx)
                                : extraCellDTO.getValue();
                        setCellBasedOnType(extraRow.createCell(extraValueColIdx),
                                value, CellType.FORMULA, extraCellDTO.getStyle());
                    });
                });
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            String msg = MessageFormat.format("Encountered error while exporting excel file {0}",
                    e.getMessage());
            log.error(msg);
            throw new TpspException(msg);
        }
    }

    private CellStyle createCustomBoldFont(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String getCellLetter(Cell cell) {
        return CellReference.convertNumToColString(cell.getColumnIndex());
    }

    private void setCellBasedOnType(Cell cell, Object cellValue, CellType type, CellStyle style) {
        try {
            if (cellValue == null) {
                cell.setCellValue(TPSPConstants.EMPTY);
            } else if (CellType.FORMULA == type) {
                cell.setCellFormula((String) cellValue);
            } else if (cellValue instanceof java.lang.Integer) {
                cell.setCellValue((Integer) cellValue);
            } else if (cellValue instanceof java.lang.Long) {
                cell.setCellValue((Long) cellValue);
            } else if (cellValue instanceof java.lang.String) {
                cell.setCellValue((String) cellValue);
            } else if (cellValue instanceof java.lang.Double) {
                cell.setCellValue((Double) cellValue);
            } else if (cellValue instanceof java.lang.Boolean) {
                cell.setCellValue(((Boolean) cellValue)
                        ? TPSPConstants.YES : TPSPConstants.NO);
            }

            if (style != null) {
                cell.setCellStyle(style);
            }
        } catch (Exception e) {
            log.error("Error setting cell {}: {}", cellValue, e);
        }
    }

}
