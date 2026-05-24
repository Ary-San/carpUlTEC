package com.dbp.democarpultec.ride.repository;

import com.dbp.democarpultec.ride.domain.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriver_Id(Long driverId);
}