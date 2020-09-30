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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConvenorId")
    private User convenor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TutorId")
    private User tutor;

    @Column(name = "Rating")
    private Integer rating;

}
