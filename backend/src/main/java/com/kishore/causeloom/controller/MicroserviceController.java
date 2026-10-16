package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.BlastRadius;
import com.kishore.causeloom.models.Microservice;
import com.kishore.causeloom.service.MicroserviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class MicroserviceController {

    private final MicroserviceService microserviceService;

    public MicroserviceController(MicroserviceService microserviceService) {
        this.microserviceService = microserviceService;
    }

    @GetMapping("/root-cause")
    public List<Microservice> rootCause() {
        return microserviceService.findRootCauses();
    }

    @GetMapping("/{name}/blast-radius")
    public BlastRadius blastRadius(@PathVariable String name) {
        return microserviceService.getBlastRadius(name);
    }
}
