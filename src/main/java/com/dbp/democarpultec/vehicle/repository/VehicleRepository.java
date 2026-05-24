package com.dbp.democarpultec.vehicle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dbp.democarpultec.vehicle.domain.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle>  findByBrand(String brand);
    
}
