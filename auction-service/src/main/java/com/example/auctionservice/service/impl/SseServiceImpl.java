package com.example.auctionservice.service.impl;

import com.example.auctionservice.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseServiceImpl implements SseService {
    // Map to store auction ID to client IDs and their emitters
    private final Map<Long, Map<String, SseEmitter>> auctionEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter registerClient(Long auctionId, String clientId) {
        // Ensure the auction ID exists in the map
        auctionEmitters.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>());
        SseEmitter emitter = new SseEmitter(600000L); // 10 min
        emitter.onCompletion(() -> removeClient(auctionId, clientId));
        emitter.onTimeout(() -> removeClient(auctionId, clientId));

        auctionEmitters.get(auctionId).put(clientId, emitter);
        return emitter;
    }

    // Remove a specific client
    private void removeClient(Long auctionId, String clientId) {
        Map<String, SseEmitter> emitters = auctionEmitters.get(auctionId);
        if (emitters != null) {
            emitters.remove(clientId);
            if (emitters.isEmpty()) {
                auctionEmitters.remove(auctionId);
            }
        }
    }

    @Override
    public <T> void notifyClient(Long auctionId,String clientId, T data) {
        SseEmitter emitter = getEmitter(auctionId, clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                removeClient(auctionId, clientId);
            }
        }
    }

    @Override
    public <T> void notifyAllClients(Long auctionId,T data) {
        Map<String, SseEmitter> emitters = auctionEmitters.get(auctionId);
        if (emitters != null) {
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event().data(data));
                } catch (IOException e) {
                    emitters.remove(entry.getKey());
                }
            }
        }
    }

    // Get the emitter for a specific client
    private SseEmitter getEmitter(Long auctionId, String clientId) {
        Map<String, SseEmitter> emitters = auctionEmitters.get(auctionId);
        return emitters != null ? emitters.get(clientId) : null;
    }
}
