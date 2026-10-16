package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.CreateIncidentRequest;
import com.kishore.causeloom.models.IncidentResult;
import com.kishore.causeloom.service.IncidentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public IncidentResult create(@RequestBody CreateIncidentRequest request) {
        return incidentService.createIncident(request.rootCause());
    }
}
