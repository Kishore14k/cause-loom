package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.CreateIncidentRequest;
import com.kishore.causeloom.models.IncidentResult;
import com.kishore.causeloom.models.ResolveResult;
import com.kishore.causeloom.service.IncidentService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/resolve")
    public ResolveResult resolve(@PathVariable String id) {
        return incidentService.resolveIncident(id);
    }
}
