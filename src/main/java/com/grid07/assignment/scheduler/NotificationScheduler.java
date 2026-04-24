package com.grid07.assignment.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedRate = 300000)
    public void sweepPendingNotifications() {
        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            List<String> messages = redisTemplate.opsForList().range(key, 0, -1);

            if (messages == null || messages.isEmpty()) {
                redisTemplate.delete(key);
                continue;
            }

            String userId = extractUserId(key);
            int total = messages.size();

            String firstMessage = messages.get(0);
            int others = total - 1;

            if (others > 0) {
                // Extracts "Bot X" from "Bot X replied to your post"
                String botPart = firstMessage.split(" replied")[0];
                System.out.println(
                        "Summarized Push Notification: " + botPart + " and " + others + " others interacted with your posts."
                );
            } else {
                System.out.println(
                        "Summarized Push Notification: " + firstMessage
                );
            }

            redisTemplate.delete(key);
        }
    }

    private String extractUserId(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "unknown";
    }
}