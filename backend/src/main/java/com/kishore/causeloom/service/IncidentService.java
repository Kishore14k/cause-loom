package com.kishore.causeloom.service;

import com.kishore.causeloom.exception.IncidentNotFoundException;
import com.kishore.causeloom.models.*;
import com.kishore.causeloom.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MicroserviceService microserviceService;
    private final PersonService personService;

    public IncidentService(IncidentRepository incidentRepository,
                           MicroserviceService microserviceService,
                           PersonService personService) {
        this.incidentRepository = incidentRepository;
        this.microserviceService = microserviceService;
        this.personService = personService;
    }

    public IncidentResult createIncident(String rootCause) {
        boolean isRootCause = microserviceService.findRootCauses().stream()
                .anyMatch(s -> s.name().equals(rootCause));
        if (!isRootCause) {
            return new IncidentResult(false, null,
                    rootCause + " is not a current root cause, no incident created");
        }

        Optional<Incident> existing = incidentRepository.findOpenByRootCause(rootCause);
        if (existing.isPresent()) {
            return new IncidentResult(false, existing.get(),
                    "Incident " + existing.get().id() + " already exists for " + rootCause);
        }

        BlastRadius blast = microserviceService.getBlastRadius(rootCause);
        OnCallResult responder = personService.getResponder(rootCause);
        List<String> affectedNames = blast.affected().stream().map(AffectedService::name).toList();
        String assignee = responder.responder() != null ? responder.responder().name() : null;

        String id = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        SeverityAssessment assessment = microserviceService.assessSeverity(rootCause);
        String severity = assessment.level();
        int count = affectedNames.size();
        String title = rootCause + " failure affecting " + count + (count == 1 ? " service" : " services");
        Incident incident = new Incident(id, title, severity, "open",
                Instant.now().toString(), rootCause);

        String summary = "Root cause: " + rootCause
                + ". Affected: " + String.join(", ", affectedNames)
                + ". Not yet alerting: " + blast.hiddenVictims().stream()
                .map(AffectedService::name).collect(Collectors.joining(", "))
                + ". " + responder.message() + " Severity " + severity + ": PageRank #" + assessment.rank() + " of "
                + assessment.totalServices() + ", " + assessment.affectedCount() + " services affected.";

        incidentRepository.create(incident, summary, affectedNames, assignee);
        return new IncidentResult(true, incident, "Incident " + id + " created");
    }

    public ResolveResult resolveIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException("Incident '" + incidentId + "' not found"));

        if ("closed".equals(incident.status())) {
            return new ResolveResult(false, incidentId, List.of(),
                    "Incident " + incidentId + " is already closed");
        }

        incidentRepository.close(incidentId, Instant.now().toString());
        List<String> restored = incidentRepository.restoreServices(incidentId);
        return new ResolveResult(true, incidentId, restored,
                "Incident " + incidentId + " closed, " + restored.size() + " services restored to healthy");
    }

    public Optional<Incident> findOpenIncident(String rootCause) {
        return incidentRepository.findOpenByRootCause(rootCause);
    }

    public List<Incident> listIncidents() {
        return incidentRepository.findAll();
    }
}
