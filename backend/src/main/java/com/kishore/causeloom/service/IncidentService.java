package com.kishore.causeloom.service;

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
        String severity = "critical"; // placeholder, the graph computes this in the next step
        String title = rootCause + " failure affecting " + affectedNames.size() + " services";
        Incident incident = new Incident(id, title, severity, "open",
                Instant.now().toString(), rootCause);

        String summary = "Root cause: " + rootCause
                + ". Affected: " + String.join(", ", affectedNames)
                + ". Not yet alerting: " + blast.hiddenVictims().stream()
                .map(AffectedService::name).collect(Collectors.joining(", "))
                + ". " + responder.message();

        incidentRepository.create(incident, summary, affectedNames, assignee);
        return new IncidentResult(true, incident, "Incident " + id + " created");
    }
}
