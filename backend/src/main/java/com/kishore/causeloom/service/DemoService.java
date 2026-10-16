package com.kishore.causeloom.service;

import com.kishore.causeloom.models.AlertRequest;
import com.kishore.causeloom.models.AlertResult;
import com.kishore.causeloom.seed.GraphSeeder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "causeloom.demo.enabled", havingValue = "true")
public class DemoService {

    private final GraphSeeder graphSeeder;
    private final AlertService alertService;

    public DemoService(GraphSeeder graphSeeder, AlertService alertService) {
        this.graphSeeder = graphSeeder;
        this.alertService = alertService;
    }

    public void reset() {
        graphSeeder.seed();
    }

    public List<AlertResult> simulateOutage() {
        List<AlertRequest> requests = List.of(
                new AlertRequest("Payments", "critical", "Payment API error rate 80%"),
                new AlertRequest("Inventory", "critical", "Stock lookups timing out"),
                new AlertRequest("Checkout", "critical", "Checkout failures above 50%"),
                new AlertRequest("Cart", "high", "Add-to-cart errors rising"),
                new AlertRequest("Database", "critical", "Connection pool exhausted"),
                new AlertRequest("Auth", "warning", "Login latency slightly elevated"));

        List<AlertResult> results = new ArrayList<>();
        for (AlertRequest request : requests) {
            results.add(alertService.ingest(request));
        }
        return results;
    }
}