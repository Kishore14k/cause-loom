package com.kishore.causeloom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "falkordb")
public record FalkorProperties(String host, int port, String graph) {}
