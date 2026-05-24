package com.dbp.democarpultec.vehicle.service;

import com.dbp.democarpultec.user.service.UserService;
import com.dbp.democarpultec.vehicle.domain.Vehicle;
import com.dbp.democarpultec.vehicle.dto.VehicleRequestDto;
import com.dbp.democarpultec.vehicle.dto.VehicleResponseDto;
import com.dbp.democarpultec.vehicle.repository.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    public List<VehicleResponseDto> findAll() {
        return vehicleRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public VehicleResponseDto findById(@NonNull Long id) {
        return toResponseDto(findEntityById(id));
    }

    @Transactional
    public VehicleResponseDto create(VehicleRequestDto dto) {
        Vehicle vehicle = new Vehicle();
        updateEntity(vehicle, dto);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return toResponseDto(savedVehicle);
    }

    @Transactional
    public VehicleResponseDto update(@NonNull Long id, VehicleRequestDto dto) {
        Vehicle vehicle = findEntityById(id);
        updateEntity(vehicle, dto);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return toResponseDto(savedVehicle);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found with id " + id);
        }
        vehicleRepository.deleteById(id);
    }

    public Vehicle findEntityById(@NonNull Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id " + id));
    }

    private void updateEntity(Vehicle vehicle, VehicleRequestDto dto) {
        vehicle.setOwner(userService.findVerifiedEntityById(dto.getOwnerId()));
        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setColor(dto.getColor());
        vehicle.setSeats(dto.getSeats());
    }

    private VehicleResponseDto toResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwner().getId())
                .plate(vehicle.getPlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .seats(vehicle.getSeats())
                .build();
    }
}
