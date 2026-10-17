package com.kishore.causeloom.service;

import com.kishore.causeloom.agent.AgentTools;
import com.kishore.causeloom.models.AgentResponse;
import com.kishore.causeloom.models.Finding;
import com.kishore.causeloom.models.Microservice;
import com.kishore.causeloom.models.ToolStep;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AgentService {

    private static final String SYSTEM_PROMPT = """
            You are an incident response agent for a microservice platform.
            Use the tools to investigate. Do not guess: every fact must come from a tool result.
            Typical flow: get active alerts, then for each root cause, get its blast radius,
            who to page and severity and create an incident for the root cause.
            Rules:
            - Only describe actions you actually performed with tools.
            - Never claim you paged or contacted anyone. You can only report who should be paged.
            - If a tool returns an error or says something already exists, say so plainly.
            - Alerts on a service do not make it the root cause. Use the root cause tool.
            - Mention services that are affected but not alerting yet.
            Finish with a short explanation: root cause, affected services, who to page,
            severity, and the incident id.
            - Mention any alerting service you ruled out as a root cause (for example a warning-only
            or degraded service) and say why.
            - When you rule a service out, give only the reason found in tool data (its status and the
              alert severity). Never reason about dependency chains unless a tool returned them.
            - If there are several root causes, treat each one separately.
            """;

    private final ChatClient chatClient;
    private final MicroserviceService microserviceService;
    private final PersonService personService;
    private final IncidentService incidentService;

    public AgentService(ChatClient.Builder builder, AgentTools tools,
                        MicroserviceService microserviceService,
                        PersonService personService,
                        IncidentService incidentService) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(tools)
                .build();
        this.microserviceService = microserviceService;
        this.personService = personService;
        this.incidentService = incidentService;
    }

    public AgentResponse investigate(String message) {
        List<ToolStep> steps = new CopyOnWriteArrayList<>();

        String explanation = chatClient.prompt()
                .user(message)
                .toolContext(Map.of(AgentTools.TRACE_KEY, steps))
                .call()
                .content();

        List<Finding> findings = new ArrayList<>();
        for (Microservice cause : microserviceService.findRootCauses()) {
            String root = cause.name();
            findings.add(new Finding(
                    root,
                    microserviceService.assessSeverity(root),
                    personService.getResponder(root),
                    incidentService.findOpenIncident(root).orElse(null),
                    microserviceService.getImpactGraph(root)));
        }
        return new AgentResponse(explanation, findings, List.copyOf(steps));
    }
}