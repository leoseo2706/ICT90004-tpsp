package com.core.tpsp.service;

import com.core.tpsp.payload.ExcelReportDTO;

public interface ExcelService {
	
	public ExcelReportDTO loadApplicationFile();

	public ExcelReportDTO loadConvenorRankingFile();

	public ExcelReportDTO loadAllocationFile();

	public ExcelReportDTO loadTutorListFile();

}
