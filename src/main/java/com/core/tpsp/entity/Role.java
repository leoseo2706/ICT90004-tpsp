package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "AspNetRoles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private String id;

    @Column(name = "Name")
    private String name;

    @Column(name = "NormalizedName")
    private String normalizedName;

    @Column(name = "ConcurrencyStamp")
    private String concurrencyStamp;

    @Column(name = "Description")
    private String description;

    @Column(name = "CreationDate")
    private Timestamp creationDate;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleId")
    private List<UserRole> userRoles = new ArrayList<>();
}
