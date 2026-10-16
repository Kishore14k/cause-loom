package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.Alert;
import com.kishore.causeloom.models.AlertRequest;
import com.kishore.causeloom.models.AlertResult;
import com.kishore.causeloom.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/active")
    public List<Alert> activeAlerts() {
        return alertService.getActiveAlerts();
    }

    @PostMapping
    public AlertResult ingest(@RequestBody AlertRequest request) {
        return alertService.ingest(request);
    }
}