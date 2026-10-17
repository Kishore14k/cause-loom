package com.kishore.causeloom.agent;

import com.kishore.causeloom.exception.ServiceNotFoundException;
import com.kishore.causeloom.service.AlertService;
import com.kishore.causeloom.service.IncidentService;
import com.kishore.causeloom.service.MicroserviceService;
import com.kishore.causeloom.service.PersonService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class AgentTools {

    private final AlertService alertService;
    private final MicroserviceService microserviceService;
    private final PersonService personService;
    private final IncidentService incidentService;

    public AgentTools(AlertService alertService, MicroserviceService microserviceService,
                      PersonService personService, IncidentService incidentService) {
        this.alertService = alertService;
        this.microserviceService = microserviceService;
        this.personService = personService;
        this.incidentService = incidentService;
    }

    @Tool(description = "List all currently open alerts with the service each one fired on.")
    public Object getActiveAlerts() {
        return safe(alertService::getActiveAlerts);
    }

    @Tool(description = "Find the root cause: failing services that have no failing dependency.")
    public Object findRootCause() {
        return safe(microserviceService::findRootCauses);
    }

    @Tool(description = "Get the blast radius of a service: every service that depends on it, directly or indirectly, plus the hidden victims that are affected but not failing yet.")
    public Object getBlastRadius(@ToolParam(description = "Exact service name, e.g. Database") String serviceName) {
        return safe(() -> microserviceService.getBlastRadius(serviceName));
    }

    @Tool(description = "Get who should be paged for a service, including escalation if the on-call person is on leave.")
    public Object getOnCall(@ToolParam(description = "Exact service name") String serviceName) {
        return safe(() -> personService.getResponder(serviceName));
    }

    @Tool(description = "Get the graph-computed severity (PageRank rank and blast radius size) for a service.")
    public Object getSeverity(@ToolParam(description = "Exact service name") String serviceName) {
        return safe(() -> microserviceService.assessSeverity(serviceName));
    }

    @Tool(description = "Create an incident for a confirmed root cause. Refuses duplicates and non-root-cause services.")
    public Object createIncident(@ToolParam(description = "Exact name of the root cause service") String rootCause) {
        return safe(() -> incidentService.createIncident(rootCause));
    }

    private Object safe(Supplier<Object> action) {
        try {
            return action.get();
        } catch (ServiceNotFoundException e) {
            return Map.of("error", e.getMessage());
        } catch (RuntimeException e) {
            return Map.of("error", "Tool failed: " + e.getMessage());
        }
    }
}