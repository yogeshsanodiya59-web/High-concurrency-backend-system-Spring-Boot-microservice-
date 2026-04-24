package com.grid07.assignment.controller;

import com.grid07.assignment.dto.CreateBotRequest;
import com.grid07.assignment.entity.Bot;
import com.grid07.assignment.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    @PostMapping
    public Bot createBot(@RequestBody CreateBotRequest request) {
        return botService.createBot(request.getName(), request.getPersonaDescription());
    }
}