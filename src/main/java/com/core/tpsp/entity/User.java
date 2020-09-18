package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "AspNetUsers")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private String id;

    @Column(name = "UserName")
    private String userName;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email")
    private String email;

    @Column(name = "LinkedInProfileUrl")
    private String linkedinUrl;

//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "Id", referencedColumnName = "userRoleKey.userId")
//    private UserRole userRole;

}
