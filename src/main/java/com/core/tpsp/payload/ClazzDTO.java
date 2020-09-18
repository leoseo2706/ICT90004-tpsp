package com.core.tpsp.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClazzDTO {

    private Integer id;

    private Integer unitId;

    private String classType;

    private String tutorAllocated;

    private Boolean allocated;

    private Boolean approved;

    private String location;

    private String room;

    private String year;

    private String studyPeriod;

    private String dayOfWeek;

    private String startDate;

    private String startTimeScheduled;

    private String endTimeScheduled;

    private UnitDTO unitDTO;
}
