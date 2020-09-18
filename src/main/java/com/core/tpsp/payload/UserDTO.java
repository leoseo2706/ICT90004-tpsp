package com.core.tpsp.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private String id;

    private String userName;

    private String role;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String swinburneId;

    private String street;

    private String city;

    private String state;

    private String postalCode;

    private String qualification;

    private String linkedinUrl;

    private String citizenshipStudyStatus;

    private String australianWorkRights;

    private String numberYearsWorkExperience;

    private String previousTeachingExperience;

    private String publications;
}
