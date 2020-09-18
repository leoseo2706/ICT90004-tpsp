package com.core.tpsp.entity;


import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Data
@Embeddable
public class UserRoleCompositeKey implements Serializable {

    /*
        note that in reality, nobody actually implemented a weak entity table
        weak entities are for schema design only.
        I only implement this since this approach is so shitty already
        and changing db cause conflictions for application C# as well
    */
    private static final long serialVersionUID = -6663453344947617855L;

    private String userId;
    private String roleId;
}
