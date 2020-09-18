package com.core.tpsp.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitDTO {

    private Integer Id;

    private String unitCode;

    private String unitName;

    private Integer department;

    private String unitOwner;

    private String unitLink;
}
