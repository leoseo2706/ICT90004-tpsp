package com.core.tpsp.utils;

import com.core.tpsp.constant.TPSPConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class TpspUtils {

    public static String toJsonString(ObjectMapper mapper, Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            log.warn("Error printing: {}", o);
        }
        return TPSPConstants.EMPTY;
    }

    public static String format(Timestamp d) {
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(TPSPConstants.DATE_FORMAT.FORMAT_dd_MM_yyyy_hh_mm_ss);
        return sdf.format(new Date(d.getTime()));
    }

    public static String format(Timestamp d, String format) {
        if (d == null) {
            return null;
        }
        if (StringUtils.isEmpty(format)) {
            format = TPSPConstants.DATE_FORMAT.FORMAT_dd_MM_yyyy_hh_mm_ss;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(d.getTime()));
    }

    public static String concatenateAddress(String street, String city,
                                            String state, String postalCode) {
        street = !StringUtils.isEmpty(street) ? street : TPSPConstants.EMPTY;
        city = !StringUtils.isEmpty(city) ? city : TPSPConstants.EMPTY;
        state = !StringUtils.isEmpty(state) ? state : TPSPConstants.EMPTY;
        postalCode = !StringUtils.isEmpty(postalCode) ? postalCode : TPSPConstants.EMPTY;

        String address = StringUtils.isEmpty(street)
                ? MessageFormat.format(TPSPConstants.ADDRESS_FORMAT.SHORT_ADDRESS_FORMAT, city, state, postalCode)
                : MessageFormat.format(TPSPConstants.ADDRESS_FORMAT.FULL_ADDRESS_FORMAT, street, city, state, postalCode);

        return address.trim();
    }

    public static String toFullName(String firstName, String lastName) {
        String tmp1 = StringUtils.isEmpty(firstName) ? TPSPConstants.EMPTY : firstName;
        String tmp2 = StringUtils.isEmpty(lastName) ? TPSPConstants.EMPTY : lastName;
        return (firstName + TPSPConstants.SPACE + lastName).trim();
    }

}
