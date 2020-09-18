package com.core.tpsp.service;

import com.core.tpsp.payload.ExcelReportDTO;

import java.io.ByteArrayInputStream;

public interface ExcelService {
	
	public ExcelReportDTO loadAllocationFile();

	public ExcelReportDTO loadConvenorRankingFile();

	public ExcelReportDTO loadUnitFile();

}
