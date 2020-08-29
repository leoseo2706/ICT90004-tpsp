package com.core.tpsp.controller;

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

@Controller
@RequestMapping("/excel")
public class ExcelController {

	@Autowired
	private ExcelService excelService;

	@GetMapping("/download")
	public ResponseEntity<Resource> getFile() {
		String filename = "test.xlsx";
		InputStreamResource file = new InputStreamResource(excelService.loadFile());

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
	}

}
