package com.kishore.causeloom.agent;

import com.kishore.causeloom.exception.ServiceNotFoundException;
import com.kishore.causeloom.models.ToolStep;
import com.kishore.causeloom.service.AlertService;
import com.kishore.causeloom.service.IncidentService;
import com.kishore.causeloom.service.MicroserviceService;
import com.kishore.causeloom.service.PersonService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public static final String TRACE_KEY = "trace";

    @Tool(description = "List all currently open alerts with the service each one fired on.")
    public Object getActiveAlerts(ToolContext toolContext) {
        return trace(toolContext, "getActiveAlerts", "", alertService::getActiveAlerts);
    }

    @Tool(description = "Find the root cause: failing services that have no failing dependency.")
    public Object findRootCause(ToolContext toolContext) {
        return trace(toolContext, "findRootCause", "", microserviceService::findRootCauses);
    }

    @Tool(description = "Get the blast radius of a service: every service that depends on it, directly or indirectly, plus the hidden victims that are affected but not failing yet.")
    public Object getBlastRadius(@ToolParam(description = "Exact service name, e.g. Database") String serviceName,
                                 ToolContext toolContext) {
        return trace(toolContext, "getBlastRadius", serviceName,
                () -> microserviceService.getBlastRadius(serviceName));
    }

    @Tool(description = "Get who should be paged for a service, including escalation if the on-call person is on leave.")
    public Object getOnCall(@ToolParam(description = "Exact service name") String serviceName,
                            ToolContext toolContext) {
        return trace(toolContext, "getOnCall", serviceName,
                () -> personService.getResponder(serviceName));
    }

    @Tool(description = "Get the graph-computed severity (tier, hard dependents and PageRank) for a service.")
    public Object getSeverity(@ToolParam(description = "Exact service name") String serviceName,
                              ToolContext toolContext) {
        return trace(toolContext, "getSeverity", serviceName,
                () -> microserviceService.assessSeverity(serviceName));
    }

    @Tool(description = "Create an incident for a confirmed root cause. Refuses duplicates and non-root-cause services.")
    public Object createIncident(@ToolParam(description = "Exact name of the root cause service") String rootCause,
                                 ToolContext toolContext) {
        return trace(toolContext, "createIncident", rootCause,
                () -> incidentService.createIncident(rootCause));
    }

    @SuppressWarnings("unchecked")
    private Object trace(ToolContext toolContext, String tool, String input, Supplier<Object> action) {
        Object result = safe(action);
        List<ToolStep> steps = (List<ToolStep>) toolContext.getContext().get(TRACE_KEY);
        if (steps != null) {
            boolean error = result instanceof Map<?, ?> m && m.containsKey("error");
            steps.add(new ToolStep(steps.size() + 1, tool, input, error));
        }
        return result;
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