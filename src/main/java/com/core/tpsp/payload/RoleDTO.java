package com.core.tpsp.payload;

import lombok.Data;

@Data
public class RoleDTO {

    private String id;

    private String name;

    private String normalizedName;

    private String concurrencyStamp;

    private String description;

    private String creationDate;
}
