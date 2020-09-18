package com.core.tpsp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class TpspUtils {

    public static final String FORMAT_dd_MM_yyyy_hh_mm_ss = "dd/MM/yyyy HH:mm:ss";

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

    public static String format(Timestamp d) {
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_dd_MM_yyyy_hh_mm_ss);
        return sdf.format(new Date(d.getTime()));
    }

    public static String format(Timestamp d, String format) {
        if (d == null) {
            return null;
        }
        if (StringUtils.isEmpty(format)) {
            format = FORMAT_dd_MM_yyyy_hh_mm_ss;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(d.getTime()));
    }

}
