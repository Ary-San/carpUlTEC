package com.dbp.democarpultec.maps.dto;

import com.dbp.democarpultec.maps.domain.MapRideMatchType;
import com.dbp.democarpultec.ride.domain.RideDirection;
import com.dbp.democarpultec.ride.domain.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyRideDto {

    private Long rideId;
    private Long driverId;
    private Long vehicleId;
    private String origin;
    private String destination;
    private LocalDateTime scheduledAt;
    private RideStatus status;
    private RideDirection direction;
    private Integer availableSeats;
    private MapRideMatchType matchType;
    private Double distanceToUtecMeters;
}