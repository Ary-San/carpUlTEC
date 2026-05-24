package com.dbp.democarpultec.pickup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbp.democarpultec.pickup.domain.PickUp;

import java.util.List;
import java.util.Optional;

public interface PickUpRepository extends JpaRepository<PickUp, Long> {

    boolean existsByRide_IdAndPassenger_Id(Long rideId, Long passengerId);

    boolean existsByRide_IdAndSequence(Long rideId, Integer sequence);

    Optional<PickUp> findByRide_IdAndPassenger_Id(Long rideId, Long passengerId);

    List<PickUp> findByRide_IdOrderBySequenceAsc(Long rideId);
}