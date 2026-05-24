package com.dbp.democarpultec.pickup.service;

import com.dbp.democarpultec.exception.InsufficientSeatsException;
import com.dbp.democarpultec.exception.InvalidPickupStateException;
import com.dbp.democarpultec.exception.InvalidRideStateException;
import com.dbp.democarpultec.pickup.domain.PickUp;
import com.dbp.democarpultec.pickup.domain.PickUpStatus;
import com.dbp.democarpultec.pickup.dto.PickUpRequestDto;
import com.dbp.democarpultec.pickup.dto.PickUpResponseDto;
import com.dbp.democarpultec.pickup.repository.PickUpRepository;
import com.dbp.democarpultec.ride.domain.Ride;
import com.dbp.democarpultec.ride.domain.RideStatus;
import com.dbp.democarpultec.ride.service.RideService;
import com.dbp.democarpultec.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PickUpService {

    private final PickUpRepository pickUpRepository;
    private final UserService userService;
    private final RideService rideService;

    public List<PickUpResponseDto> findAllByRide(@NonNull Long rideId) {
        return pickUpRepository.findByRide_IdOrderBySequenceAsc(rideId).stream().map(this::toResponseDto).toList();
    }

    public PickUpResponseDto findById(@NonNull Long id) {
        return toResponseDto(findEntityById(id));
    }

    @Transactional
    public PickUpResponseDto create(PickUpRequestDto dto) {
        Ride ride = rideService.findEntityById(dto.getRideId());
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStateException("Completed rides cannot accept pickups");
        }
        if (ride.getAvailableSeats() == null || ride.getAvailableSeats() <= 0) {
            throw new InsufficientSeatsException("No available seats left for ride " + dto.getRideId());
        }

        var passenger = userService.findVerifiedEntityById(dto.getPassengerId());
        if (ride.getDriver().getId().equals(passenger.getId())) {
            throw new InvalidPickupStateException("A driver cannot register as passenger in their own ride");
        }
        if (pickUpRepository.existsByRide_IdAndPassenger_Id(dto.getRideId(), dto.getPassengerId())) {
            throw new InvalidPickupStateException("Passenger is already registered for this ride");
        }
        if (pickUpRepository.existsByRide_IdAndSequence(dto.getRideId(), dto.getSequence())) {
            throw new InvalidPickupStateException("Pickup sequence already exists for this ride");
        }

        rideService.reserveSeat(dto.getRideId());

        PickUp pickUp = PickUp.builder()
                .ride(ride)
                .passenger(passenger)
                .locationName(dto.getLocationName())
                .sequence(dto.getSequence())
                .status(PickUpStatus.PENDING)
                .build();
            PickUp savedPickUp = pickUpRepository.save(pickUp);
            return toResponseDto(savedPickUp);
    }

    @Transactional
    public PickUpResponseDto updateStatus(@NonNull Long pickupId, @NonNull Long driverId, @NonNull PickUpStatus status) {
        PickUp pickUp = findEntityById(pickupId);
        if (!pickUp.getRide().getDriver().getId().equals(driverId)) {
            throw new InvalidPickupStateException("Only the ride owner can update pickup status");
        }
        validateTransition(pickUp.getStatus(), status);
        pickUp.setStatus(status);
        PickUp savedPickUp = pickUpRepository.save(pickUp);
        return toResponseDto(savedPickUp);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!pickUpRepository.existsById(id)) {
            throw new EntityNotFoundException("Pickup not found with id " + id);
        }
        pickUpRepository.deleteById(id);
    }

    public PickUp findEntityById(@NonNull Long id) {
        return pickUpRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pickup not found with id " + id));
    }

    private void validateTransition(PickUpStatus currentStatus, PickUpStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }
        if (currentStatus == PickUpStatus.PENDING && nextStatus == PickUpStatus.ON_BOARD) {
            return;
        }
        if (currentStatus == PickUpStatus.ON_BOARD && nextStatus == PickUpStatus.DROPPED_OFF) {
            return;
        }
        throw new InvalidPickupStateException("Invalid pickup status transition from " + currentStatus + " to " + nextStatus);
    }

    private PickUpResponseDto toResponseDto(PickUp pickUp) {
        return PickUpResponseDto.builder()
                .id(pickUp.getId())
                .rideId(pickUp.getRide().getId())
                .passengerId(pickUp.getPassenger().getId())
                .locationName(pickUp.getLocationName())
                .sequence(pickUp.getSequence())
                .status(pickUp.getStatus())
                .build();
    }
}