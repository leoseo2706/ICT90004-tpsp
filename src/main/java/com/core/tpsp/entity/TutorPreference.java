package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class TutorPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer Id;

    @Column(name = "ConvenorId")
    private String convenorId;

    @Column(name = "TutorId")
    private String tutorId;

    @Column(name = "Rating")
    private Integer rating;

}
