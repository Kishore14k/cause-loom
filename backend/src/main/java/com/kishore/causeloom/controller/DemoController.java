package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.AlertResult;
import com.kishore.causeloom.service.DemoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "causeloom.demo.enabled", havingValue = "true")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        demoService.reset();
        return Map.of("message", "Graph reset to a healthy state");
    }

    @PostMapping("/simulate-outage")
    public List<AlertResult> simulateOutage() {
        return demoService.simulateOutage();
    }
}