package com.kishore.causeloom.service;

import com.kishore.causeloom.models.AffectedService;
import com.kishore.causeloom.models.BlastRadius;
import com.kishore.causeloom.models.Microservice;
import com.kishore.causeloom.repository.MicroserviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MicroserviceService {

    private final MicroserviceRepository microserviceRepository;

    public MicroserviceService(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    public List<Microservice> findRootCauses() {
        return microserviceRepository.findRootCauses();
    }

    public BlastRadius getBlastRadius(String serviceName) {
        List<AffectedService> affected = microserviceRepository.findDependents(serviceName);
        List<AffectedService> hidden = affected.stream()
                .filter(a -> !"failing".equals(a.status()))
                .toList();
        return new BlastRadius(serviceName, affected, hidden);
    }
}
