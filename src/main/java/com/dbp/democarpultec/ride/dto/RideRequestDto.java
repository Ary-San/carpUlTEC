package com.dbp.democarpultec.ride.dto;

import com.dbp.democarpultec.ride.domain.RideDirection;
import com.dbp.democarpultec.ride.domain.RideStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDto {

	@NotNull
	private Long driverId;

	@NotNull
	private Long vehicleId;

	@NotBlank
	private String origin;

	@NotBlank
	private String destination;

	@NotNull
	private LocalDateTime scheduledAt;

	private RideStatus status;

	@NotNull
	private RideDirection direction;
}
