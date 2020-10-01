package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity (name = "AspNetUserRoles")
public class UserRole implements Serializable {

    private static final long serialVersionUID = -5036651355806543544L;

    @EmbeddedId
    UserRoleCompositeKey userRoleKey;

    // feel free to configure the shit as I cannot bother configuring 2 tables with composite keys
    // previous teams did not even include an increment column either

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "RoleId")
//    private Role role;

}
