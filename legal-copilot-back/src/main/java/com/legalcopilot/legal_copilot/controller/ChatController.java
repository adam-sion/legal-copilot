package com.legalcopilot.legal_copilot.controller;

import com.legalcopilot.legal_copilot.service.query.ChatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatQueryService queryService;

    @GetMapping("/")
    public String prompt(@RequestBody String query) {
        return queryService.processQuery(query);
    }

}
