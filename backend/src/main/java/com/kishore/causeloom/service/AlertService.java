package com.kishore.causeloom.service;

import com.kishore.causeloom.models.Alert;
import com.kishore.causeloom.models.AlertRequest;
import com.kishore.causeloom.models.AlertResult;
import com.kishore.causeloom.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final MicroserviceService microserviceService;

    public AlertService(AlertRepository alertRepository, MicroserviceService microserviceService) {
        this.alertRepository = alertRepository;
        this.microserviceService = microserviceService;
    }

    public List<Alert> getActiveAlerts() {
        return alertRepository.findOpenAlerts();
    }

    private static final Set<String> SEVERITIES = Set.of("critical", "high", "warning");

    public AlertResult ingest(AlertRequest request) {
        String severity = request.severity() == null ? "" : request.severity().toLowerCase();
        if (!SEVERITIES.contains(severity)) {
            throw new IllegalArgumentException("Severity must be one of: critical, high, warning");
        }

        String currentStatus = microserviceService.getStatus(request.service()); // 404 if unknown

        if (alertRepository.existsOpenForService(request.service())) {
            return new AlertResult(false, null,
                    "An open alert already exists for " + request.service());
        }

        String id = "A-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Alert alert = new Alert(id, request.service(), severity, request.message());
        alertRepository.create(alert, Instant.now().toString());

        String newStatus = "warning".equals(severity) && !"failing".equals(currentStatus)
                ? "degraded" : "failing";
        microserviceService.markStatus(request.service(), newStatus);

        return new AlertResult(true, alert,
                "Alert " + id + " recorded, " + request.service() + " is now " + newStatus);
    }
}