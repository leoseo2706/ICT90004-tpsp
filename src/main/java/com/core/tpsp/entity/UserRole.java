package com.core.tpsp.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity (name = "AspNetUserRoles")
public class UserRole implements Serializable {

    private static final long serialVersionUID = -5036651355806543544L;

    @EmbeddedId
    UserRoleCompositeKey userRoleKey;

//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "RoleId", referencedColumnName = "Id")
//    private List<Role> roles;

//    @OneToOne(mappedBy = "userRole")
//    private User user;

//    @OneToOne(mappedBy = "userRole", cascade = CascadeType.ALL)
//    private User user;
}
