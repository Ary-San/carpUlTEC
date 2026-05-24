package com.dbp.democarpultec.tracking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingLocationDto {

    @NotNull
    private Long rideId;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;
}