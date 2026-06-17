package com.growvy.dto.req;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SelectApplicantsRequest {
    private List<Long> applicationIds;
}
