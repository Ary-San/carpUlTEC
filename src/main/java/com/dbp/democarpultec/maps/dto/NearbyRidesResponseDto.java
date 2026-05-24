package com.dbp.democarpultec.maps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyRidesResponseDto {

    private String referenceName;
    private Double userLat;
    private Double userLng;
    private Double referenceLat;
    private Double referenceLng;
    private Double distanceToReferenceMeters;
    private Integer radiusMeters;
    private boolean withinRadius;
    private List<NearbyRideDto> rides;
}