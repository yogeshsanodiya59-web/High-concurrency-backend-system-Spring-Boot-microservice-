package com.grid07.assignment.service;

import com.grid07.assignment.entity.Comment;
import com.grid07.assignment.entity.Post;
import com.grid07.assignment.repository.CommentRepository;
import com.grid07.assignment.repository.PostRepository;
import com.grid07.assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;

    // ─── Create Post

    public Post createPost(Long authorId, String authorType, String content) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setAuthorType(authorType);
        post.setContent(content);
        return postRepository.save(post);
    }

    // ─── Like a Post ──────────────────────────────────────────

    public void likePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found: " + postId);
        }
        // Human like = +20 points
        redisService.incrementViralityScore(postId, 20);
    }

    // ─── Add Comment (with all guardrails) ────────────────────

    @Transactional
    public Comment addComment(Long postId, Long authorId,
                              String authorType, String content,
                              int depthLevel, Long humanId) {

        // 1. Post exist karta hai?
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // 2. Vertical Cap — depth 20 se zyada nahi
        if (depthLevel > 20) {
            throw new RuntimeException("VERTICAL_CAP_EXCEEDED: Max depth is 20");
        }

        // 3. Bot specific guardrails
        if ("BOT".equalsIgnoreCase(authorType)) {

            // 3a. Horizontal Cap — 100 bot replies max
            Long newCount = redisService.incrementBotCount(postId);
            if (newCount > 100) {
                // Rollback the increment
                redisService.decrementBotCount(postId);
                throw new RuntimeException("HORIZONTAL_CAP_EXCEEDED: Max 100 bot replies per post");
            }

            // 3b. Cooldown Cap — bot ne 10 min mein interact kiya?
            if (humanId != null && redisService.isBotOnCooldown(authorId, humanId)) {
                redisService.decrementBotCount(postId);
                throw new RuntimeException("COOLDOWN_CAP: Bot is on cooldown for this user");
            }

            // 3c. Cooldown set karo
            if (humanId != null) {
                redisService.setBotCooldown(authorId, humanId);
            }

            // 3d. Virality score update — bot reply = +1
            redisService.incrementViralityScore(postId, 1);

            // 3e. Notification bhejo ya pending mein daalo
            if (humanId != null) {
                handleBotNotification(authorId, humanId);
            }
        }

        // 4. Human comment guardrails
        if ("USER".equalsIgnoreCase(authorType)) {
            // Human comment = +50 points
            redisService.incrementViralityScore(postId, 50);
        }

        // saving to db
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setAuthorType(authorType);
        comment.setContent(content);
        comment.setDepthLevel(depthLevel);
        return commentRepository.save(comment);
    }

//  nOTIFICATION HELPERRR

    private void handleBotNotification(Long botId, Long userId) {
        String message = "Bot " + botId + " replied to your post";

        if (redisService.hasNotifCooldown(userId)) {
            // User ko recently notification mili hai — pending mein daalo
            redisService.pushPendingNotification(userId, message);
            System.out.println("Notification queued for user: " + userId);
        } else {
            // Seedha bhejo aur cooldown set karo
            System.out.println("Push Notification Sent to User: " + userId + " — " + message);
            redisService.setNotifCooldown(userId);
        }
    }
}