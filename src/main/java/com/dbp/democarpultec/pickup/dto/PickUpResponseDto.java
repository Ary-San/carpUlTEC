package com.dbp.democarpultec.pickup.dto;

import com.dbp.democarpultec.pickup.domain.PickUpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickUpResponseDto {

    private Long id;
    private Long rideId;
    private Long passengerId;
    private String locationName;
    private Integer sequence;
    private PickUpStatus status;
}