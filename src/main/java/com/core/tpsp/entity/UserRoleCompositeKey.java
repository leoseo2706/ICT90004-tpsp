package com.core.tpsp.entity;


import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Data
@Embeddable
public class UserRoleCompositeKey implements Serializable {

    private static final long serialVersionUID = -6663453344947617855L;
    private String userId;
    private String roleId;
}
