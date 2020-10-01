package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "Unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "UnitCode")
    private String unitCode;

    @Column(name = "UnitName")
    private String unitName;

    @Column(name = "Department")
    private Integer department;

    @Column(name = "UnitOwner")
    private String unitOwner;

    @Column(name = "UnitLink")
    private String unitLink;
}
