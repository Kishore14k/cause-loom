package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.InvestigateRequest;
import com.kishore.causeloom.service.AgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/investigate")
    public Map<String, String> investigate(@RequestBody(required = false) InvestigateRequest request) {
        String message = (request == null || request.message() == null)
                ? "Investigate the current alerts." : request.message();
        return Map.of("explanation", agentService.investigate(message));
    }
}
