package com.dbp.democarpultec.vehicle.controller;

import com.dbp.democarpultec.vehicle.dto.VehicleRequestDto;
import com.dbp.democarpultec.vehicle.dto.VehicleResponseDto;
import com.dbp.democarpultec.vehicle.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public Page<VehicleResponseDto> findAll(Pageable pageable) {
        return vehicleService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public VehicleResponseDto findById(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDto> create(@Valid @RequestBody VehicleRequestDto vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(vehicle));
    }

    @PutMapping("/{id}")
    public VehicleResponseDto update(@PathVariable Long id, @Valid @RequestBody VehicleRequestDto vehicle) {
        return vehicleService.update(id, vehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
