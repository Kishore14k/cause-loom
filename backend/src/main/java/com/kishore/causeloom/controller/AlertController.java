package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.Alert;
import com.kishore.causeloom.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}