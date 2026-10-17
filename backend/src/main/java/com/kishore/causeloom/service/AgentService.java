package com.kishore.causeloom.service;

import com.kishore.causeloom.agent.AgentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final String SYSTEM_PROMPT = """
            You are an incident response agent for a microservice platform.
            Use the tools to investigate. Do not guess: every fact must come from a tool result.
            Typical flow: get active alerts, find the root cause, get its blast radius,
            get who to page, get severity, then create an incident for the root cause.
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
            """;

    private final ChatClient chatClient;

    public AgentService(ChatClient.Builder builder, AgentTools tools) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(tools)
                .build();
    }

    public String investigate(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}