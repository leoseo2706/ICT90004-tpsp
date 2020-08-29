package com.core.tpsp.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.core.tpsp.exception.TpspException;
import com.core.tpsp.payload.ExcelReportDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExcelHelper {
	
	@Value("#{'${report.excel.header}'.split(',')}")
	private List<String> excelHeaderCols;

	@Value("${report.excel.sheet}")
	private String sheetName;
	
	@Autowired
	private ObjectMapper mapper;

	public ByteArrayInputStream toExcelReport(List<ExcelReportDTO> payloads) {
		
		if (CollectionUtils.isEmpty(payloads)) {
			log.info("Empty payload");
			return new ByteArrayInputStream(new byte[0]);
		}
		
		log.info("Begin creating excel workbook {}...", sheetName);
		try (Workbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			
			Sheet sheet = workbook.createSheet(sheetName);
			
			// header creation
			Row headerRow = sheet.createRow(0);
			for (int col = 0; col<excelHeaderCols.size(); col ++) {
				Cell cell = headerRow.createCell(col);
		        cell.setCellValue(excelHeaderCols.get(col));
			}
			log.info("Finished header {}...", TpspUtils.toJsonString(mapper, excelHeaderCols));
			
			// other rows
			int rowIdx = 1;
			for (ExcelReportDTO payload: payloads) {
				Row row = sheet.createRow(rowIdx++);
				
				row.createCell(0).setCellValue(payload.getUnit());
				// etc.
				
				log.info("Finished row {}...", TpspUtils.toJsonString(mapper, payload));
			}
			
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
			
		} catch (IOException e) {
			String msg = MessageFormat.format("Encountered error while exporting excel file {0}", e.getMessage());
			log.error(msg);
			throw new TpspException(msg);
		}
	}

}
