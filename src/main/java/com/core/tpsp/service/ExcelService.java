package com.core.tpsp.service;

import com.core.tpsp.payload.ExcelReportDTO;

import java.io.ByteArrayInputStream;

public interface ExcelService {
	
	public ExcelReportDTO loadApplicationFile();

	public ExcelReportDTO loadConvenorRankingFile();

	public ExcelReportDTO loadAllocationFile();

}
