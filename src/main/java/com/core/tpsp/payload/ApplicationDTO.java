package com.core.tpsp.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {

    private Integer applicationId;

    private Integer id;

    private String applicant;

    private Integer appliedClass;

    private Boolean pa;

    private Boolean approved;

    private Integer preference;
}
