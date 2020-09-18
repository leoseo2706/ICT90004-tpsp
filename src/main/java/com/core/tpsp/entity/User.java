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

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "SwinburneID")
    private String swinburneId;

    @Column(name = "Street")
    private String street;

    @Column(name = "City")
    private String city;

    @Column(name = "State")
    private String state;

    @Column(name = "PostalCode")
    private String postalCode;

    @Column(name = "Qualification")
    private String qualification;

    @Column(name = "LinkedInProfileUrl")
    private String linkedinUrl;

    @Column(name = "CitizenshipStudyStatus")
    private String citizenshipStudyStatus;

    @Column(name = "AustralianWorkRights")
    private String australianWorkRights;

    @Column(name = "NumberYearsWorkExperience")
    private String numberYearsWorkExperience;

    @Column(name = "PreviousTeachingExperience")
    private String previousTeachingExperience;

    @Column(name = "Publications")
    private String publications;

//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "Id", referencedColumnName = "userRoleKey.userId")
//    private UserRole userRole;

}
