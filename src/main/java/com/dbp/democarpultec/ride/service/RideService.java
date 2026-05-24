package com.dbp.democarpultec.ride.service;

import com.dbp.democarpultec.exception.InsufficientSeatsException;
import com.dbp.democarpultec.exception.InvalidRideStateException;
import com.dbp.democarpultec.email.EmailService;
import com.dbp.democarpultec.pickup.repository.PickUpRepository;
import com.dbp.democarpultec.ride.domain.Ride;
import com.dbp.democarpultec.ride.domain.RideStatus;
import com.dbp.democarpultec.ride.dto.RideRequestDto;
import com.dbp.democarpultec.ride.dto.RideResponseDto;
import com.dbp.democarpultec.ride.repository.RideRepository;
import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.user.service.UserService;
import com.dbp.democarpultec.vehicle.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final PickUpRepository pickUpRepository;
    private final EmailService emailService;

    public List<RideResponseDto> findAll() {
        return rideRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public RideResponseDto findById(@NonNull Long id) {
        return toResponseDto(findEntityById(id));
    }

    @Transactional
    public RideResponseDto create(RideRequestDto dto) {
        Ride ride = new Ride();
        updateEntity(ride, dto);
        ride.setStatus(RideStatus.PLANNED);
        ride.setAvailableSeats(ride.getVehicle().getSeats());
        Ride savedRide = rideRepository.save(ride);
        return toResponseDto(savedRide);
    }

    @Transactional
    public RideResponseDto update(@NonNull Long id, RideRequestDto dto) {
        Ride ride = findEntityById(id);
        if (ride.getStatus() != RideStatus.PLANNED) {
            throw new InvalidRideStateException("Only planned rides can be edited");
        }

        int reservedSeats = ride.getVehicle().getSeats() - ride.getAvailableSeats();
        updateEntity(ride, dto);
        int updatedAvailableSeats = ride.getVehicle().getSeats() - reservedSeats;
        if (updatedAvailableSeats < 0) {
            throw new InsufficientSeatsException("Updated vehicle does not have enough capacity for reserved pickups");
        }
        ride.setAvailableSeats(updatedAvailableSeats);
        Ride savedRide = rideRepository.save(ride);
        return toResponseDto(savedRide);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException("Ride not found with id " + id);
        }
        rideRepository.deleteById(id);
    }

    public Ride findEntityById(@NonNull Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found with id " + id));
    }

    @Transactional
    public Ride reserveSeat(@NonNull Long rideId) {
        Ride ride = findEntityById(rideId);
        ensureAcceptingPassengers(ride);
        if (ride.getAvailableSeats() == null || ride.getAvailableSeats() <= 0) {
            throw new InsufficientSeatsException("No available seats left for ride " + rideId);
        }
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        return rideRepository.save(ride);
    }

    @Transactional
    public Ride changeStatus(@NonNull Long id, @NonNull Long driverId, @NonNull RideStatus status) {
        Ride ride = findEntityById(id);
        if (!ride.getDriver().getId().equals(driverId)) {
            throw new InvalidRideStateException("Only the ride owner can change its status");
        }
        validateStatusTransition(ride.getStatus(), status);
        ride.setStatus(status);
        Ride savedRide = rideRepository.save(ride);
        notifyRideStatusChange(savedRide);
        return savedRide;
    }

    @Transactional
    public Ride markActive(@NonNull Long id, @NonNull Long driverId) {
        return changeStatus(id, driverId, RideStatus.ACTIVE);
    }

    @Transactional
    public Ride markCompleted(@NonNull Long id, @NonNull Long driverId) {
        return changeStatus(id, driverId, RideStatus.COMPLETED);
    }

    public void ensureRideOwner(@NonNull Long rideId, @NonNull Long userId) {
        Ride ride = findEntityById(rideId);
        if (!ride.getDriver().getId().equals(userId)) {
            throw new InvalidRideStateException("User is not allowed to modify this ride");
        }
    }

    private void updateEntity(Ride ride, RideRequestDto dto) {
        ride.setDriver(userService.findVerifiedEntityById(dto.getDriverId()));
        ride.setVehicle(vehicleService.findEntityById(dto.getVehicleId()));
        ride.setOrigin(dto.getOrigin());
        ride.setDestination(dto.getDestination());
        ride.setScheduledAt(dto.getScheduledAt());
        ride.setDirection(dto.getDirection());
        if (dto.getStatus() != null) {
            ride.setStatus(dto.getStatus());
        }
    }

    private void ensureAcceptingPassengers(Ride ride) {
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStateException("Completed rides cannot accept new pickups");
        }
    }

    private void validateStatusTransition(RideStatus currentStatus, RideStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        if (currentStatus == RideStatus.PLANNED && newStatus == RideStatus.ACTIVE) {
            return;
        }
        if (currentStatus == RideStatus.ACTIVE && newStatus == RideStatus.COMPLETED) {
            return;
        }
        throw new InvalidRideStateException("Invalid ride status transition from " + currentStatus + " to " + newStatus);
    }

    private RideResponseDto toResponseDto(Ride ride) {
        return RideResponseDto.builder()
                .id(ride.getId())
                .driverId(ride.getDriver().getId())
                .vehicleId(ride.getVehicle().getId())
                .origin(ride.getOrigin())
                .destination(ride.getDestination())
                .scheduledAt(ride.getScheduledAt())
                .status(ride.getStatus())
                .direction(ride.getDirection())
                .availableSeats(ride.getAvailableSeats())
                .build();
    }

    private void notifyRideStatusChange(Ride ride) {
        List<User> passengers = pickUpRepository.findByRide_IdOrderBySequenceAsc(ride.getId()).stream()
                .map(pickUp -> pickUp.getPassenger())
                .distinct()
                .toList();
        List<User> recipients = new java.util.ArrayList<>(passengers);
        recipients.add(ride.getDriver());

        if (ride.getStatus() == RideStatus.ACTIVE) {
            emailService.sendRideStartedEmail(ride, recipients);
        }
        if (ride.getStatus() == RideStatus.COMPLETED) {
            emailService.sendRideCompletedEmail(ride, recipients);
        }
    }
}