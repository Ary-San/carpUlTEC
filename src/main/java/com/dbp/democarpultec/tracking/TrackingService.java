package com.dbp.democarpultec.tracking;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishLocation(TrackingLocationDto dto) {
        messagingTemplate.convertAndSend("/topic/rides/" + dto.getRideId() + "/tracking", dto);
    }
}