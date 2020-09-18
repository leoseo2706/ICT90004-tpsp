package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "Class")
@Data
public class Clazz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "UnitId")
    private Integer unitId;

    @Column(name = "ClassType")
    private String classType;

    @Column(name = "TutorAllocated")
    private String tutorAllocated;

    @Column(name = "Allocated")
    private Boolean allocated;

    @Column(name = "Approved")
    private Boolean approved;

    @Column(name = "Location")
    private String location;

    @Column(name = "Room")
    private String room;

    @Column(name = "Year")
    private String year;

    @Column(name = "StudyPeriod")
    private String studyPeriod;

    @Column(name = "DayOfWeek")
    private String dayOfWeek;

    @Column(name = "StartDate")
    private Timestamp startDate;

    @Column(name = "StartTimeScheduled")
    private Timestamp startTimeScheduled;

    @Column(name = "EndTimeScheduled")
    private Timestamp endTimeScheduled;
}
