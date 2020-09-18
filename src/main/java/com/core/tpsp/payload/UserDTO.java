package com.core.tpsp.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private String id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String linkedinUrl;
    private String role;
}
