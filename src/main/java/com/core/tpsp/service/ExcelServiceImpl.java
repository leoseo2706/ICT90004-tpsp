package com.core.tpsp.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.core.tpsp.payload.ExcelReportDTO;
import com.core.tpsp.utils.ExcelHelper;

@Service
public class ExcelServiceImpl implements ExcelService {
	
	@Autowired
	private ExcelHelper excelHelper;
	
	@Override
	public ByteArrayInputStream loadFile() {
		
		// test
		List<ExcelReportDTO> payloads = test();
		
		return excelHelper.toExcelReport(payloads);
	}
	
	private List<ExcelReportDTO> test() {
		return new ArrayList<ExcelReportDTO>() {{
			add(new ExcelReportDTO("ahihi"));
			add(new ExcelReportDTO("ahoho"));
		}};
	}

}
