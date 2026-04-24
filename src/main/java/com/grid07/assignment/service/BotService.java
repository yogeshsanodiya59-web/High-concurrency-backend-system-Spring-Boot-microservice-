package com.grid07.assignment.service;

import com.grid07.assignment.entity.Bot;
import com.grid07.assignment.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {

    private final BotRepository botRepository;

    public Bot createBot(String name, String personaDescription) {


        Bot bot = new Bot();
        bot.setName(name);

        bot.setPersonaDescription(personaDescription);

        return botRepository.save(bot);
    }

    public Bot getBotById(Long id) {

        return botRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("Bot not found with id: " + id));
    }
}