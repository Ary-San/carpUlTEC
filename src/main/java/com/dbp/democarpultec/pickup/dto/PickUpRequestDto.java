package com.dbp.democarpultec.pickup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickUpRequestDto {

    @NotNull
    private Long rideId;

    @NotNull
    private Long passengerId;

    @NotBlank
    private String locationName;

    @NotNull
    @Min(1)
    private Integer sequence;
}