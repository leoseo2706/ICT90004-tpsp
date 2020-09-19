package com.core.tpsp.controller;

import com.core.tpsp.payload.ExcelReportDTO;
import com.core.tpsp.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @GetMapping("/download/applications")
    public ResponseEntity<Resource> downloadApplicationFile() {
        return buildExcelResponse(excelService.loadApplicationFile());
    }

    @GetMapping("/download/convenor-ranking")
    public ResponseEntity<Resource> downloadConvenorRankingFile() {
        return buildExcelResponse(excelService.loadConvenorRankingFile());
    }

    @GetMapping("/download/allocation")
    public ResponseEntity<Resource> downloadAllocationFile() {
        return buildExcelResponse(excelService.loadAllocationFile());
    }

    private ResponseEntity buildExcelResponse(ExcelReportDTO result) {
        InputStreamResource file = new InputStreamResource(result.getData());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + result.getFileName())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
    }

}
