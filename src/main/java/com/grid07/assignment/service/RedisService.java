package com.grid07.assignment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // Virality Score

    public void incrementViralityScore(Long postId, int points) {
        String key = "post:" + postId + ":virality_score";

        redisTemplate.opsForValue().increment(key, points);
    }

    public String getViralityScore(Long postId) {
        String key = "post:" + postId + ":virality_score";
        return redisTemplate.opsForValue().get(key);
    }

    // Bot Counts

    public Long incrementBotCount(Long postId) {

        String key = "post:" + postId + ":bot_count";

        return redisTemplate.opsForValue().increment(key);
    }

    public void decrementBotCount(Long postId) {

        String key = "post:" + postId + ":bot_count";


        redisTemplate.opsForValue().decrement(key);
    }

    // The cool doewn

    public boolean isBotOnCooldown(Long botId, Long humanId) {

        String key = "cooldown:bot_" + botId + ":human_" + humanId;

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setBotCooldown(Long botId, Long humanId) {

        String key = "cooldown:bot_" + botId + ":human_" + humanId;

        redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.MINUTES);
    }



    public boolean hasNotifCooldown(Long userId) {


        String key = "user:" + userId + ":notif_cooldown";

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setNotifCooldown(Long userId) {
        String key = "user:" + userId + ":notif_cooldown";


        redisTemplate.opsForValue().set(key, "1", 15, TimeUnit.MINUTES);
    }

    public void pushPendingNotification(Long userId, String message) {
        String key = "user:" + userId + ":pending_notifs";


        redisTemplate.opsForList().rightPush(key, message);
    }

    public String popPendingNotification(Long userId) {
        String key = "user:" + userId + ":pending_notifs";

        return redisTemplate.opsForList().leftPop(key);
    }

    public Long getPendingNotifCount(Long userId) {

        String key = "user:" + userId + ":pending_notifs";

        return redisTemplate.opsForList().size(key);
    }

    public void clearPendingNotifications(Long userId) {
        String key = "user:" + userId + ":pending_notifs";
        redisTemplate.delete(key);
    }

    public String getPendingNotifKey(Long userId) {
        return "user:" + userId + ":pending_notifs";
    }
}