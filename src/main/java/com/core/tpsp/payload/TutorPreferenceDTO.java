package com.core.tpsp.payload;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class TutorPreferenceDTO {

    private Integer Id;

    private UserDTO convenor;

    private UserDTO tutor;

    private Integer rating;
}
