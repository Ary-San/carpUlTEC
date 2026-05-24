package com.dbp.democarpultec.ride.domain;

import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.vehicle.domain.Vehicle;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rides")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Ride {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	private Long version;

	@ManyToOne(optional = false)
	@JoinColumn(name = "driver_id", nullable = false)
	private User driver;

	@ManyToOne(optional = false)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@Column(nullable = false)
	private String origin;

	@Column(nullable = false)
	private String destination;

	@Column(nullable = false)
	private LocalDateTime scheduledAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private RideStatus status = RideStatus.PLANNED;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RideDirection direction;

	@Column(nullable = false)
	private Integer availableSeats;

	@PrePersist
	void onCreate() {
		if (status == null) {
			status = RideStatus.PLANNED;
		}
		if (vehicle != null && availableSeats == null) {
			availableSeats = vehicle.getSeats();
		}
	}
}
