package com.dbp.democarpultec.maps.controller;

import com.dbp.democarpultec.maps.dto.MapLocationRequestDto;
import com.dbp.democarpultec.maps.dto.NearbyRidesResponseDto;
import com.dbp.democarpultec.maps.service.MapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @PostMapping("/nearby-rides")
    public NearbyRidesResponseDto findNearbyRides(@Valid @RequestBody MapLocationRequestDto request) {
        return mapService.findNearbyRides(request);
    }

    @MessageMapping("/maps/nearby-rides")
    @SendTo("/topic/maps/nearby-rides")
    public NearbyRidesResponseDto publishNearbyRides(@Valid @Payload MapLocationRequestDto request) {
        return mapService.findNearbyRides(request);
    }
}