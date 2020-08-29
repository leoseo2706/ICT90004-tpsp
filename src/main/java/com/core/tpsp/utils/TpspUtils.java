package com.core.tpsp.utils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TpspUtils {
	
//	public boolean isEmpty(Collection<?> col) {
//		return col == null || col.isEmpty();
//	}
//	
//	public boolean isEmpty(Map<?, ?> map) {
//		return map == null || map.isEmpty();
//	}
//	
//	public boolean isBlank(String in) {
//		return in == null ||  in.trim().isEmpty();
//	}
	
	public static String toJsonString(ObjectMapper mapper, Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (Exception e) {
			log.warn("Error printing: {}", o);
		}
		return "";
	}

}
