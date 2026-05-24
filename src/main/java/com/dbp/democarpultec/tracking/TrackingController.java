package com.dbp.democarpultec.tracking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @MessageMapping("/tracking/location")
    public void track(@Valid @Payload TrackingLocationDto dto) {
        trackingService.publishLocation(dto);
    }
}