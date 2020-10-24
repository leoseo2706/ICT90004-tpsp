package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity(name = "Applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApplicationId")
    private Integer applicationId;

    @Column(name = "Applicant")
    private String applicant;

    @Column(name = "AppliedClass")
    private Integer appliedClass;

    @Column(name = "ProvisionallyAllocated")
    private Boolean pa;

    @Column(name = "Approved")
    private Boolean approved;

    @Column(name = "Preference")
    private Integer preference;

}
