package com.dbp.democarpultec.maps.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapLocationRequestDto {

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @Positive
    private Integer radiusMeters;

    @Positive
    private Integer maxResults;
}