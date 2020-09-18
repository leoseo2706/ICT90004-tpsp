package com.core.tpsp.controller;

import com.core.tpsp.constant.TPSPConstants;
import com.core.tpsp.entity.Role;
import com.core.tpsp.entity.User;
import com.core.tpsp.entity.UserRole;
import com.core.tpsp.exception.TpspException;
import com.core.tpsp.payload.ExcelReportDTO;
import com.core.tpsp.payload.UserDTO;
import com.core.tpsp.repo.RoleRepo;
import com.core.tpsp.repo.UserRepo;
import com.core.tpsp.repo.UserRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.core.tpsp.service.ExcelService;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @GetMapping("/download/applications")
    public ResponseEntity<Resource> downloadApplicationFile() {
        return buildExcelResponse(excelService.loadApplicantUnitFile());
    }

    @GetMapping("/download/convenor-ranking")
    public ResponseEntity<Resource> downloadConvenorRankingFile() {
        return buildExcelResponse(excelService.loadConvenorRankingFile());
    }

    private ResponseEntity buildExcelResponse(ExcelReportDTO result) {
        InputStreamResource file = new InputStreamResource(result.getData());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + result.getFileName())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
    }

}
