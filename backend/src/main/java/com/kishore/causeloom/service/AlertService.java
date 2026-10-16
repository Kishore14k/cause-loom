package com.kishore.causeloom.service;

import com.kishore.causeloom.models.Alert;
import com.kishore.causeloom.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<Alert> getActiveAlerts() {
        return alertRepository.findOpenAlerts();
    }
}