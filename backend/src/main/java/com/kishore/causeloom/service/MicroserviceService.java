package com.kishore.causeloom.service;

import com.kishore.causeloom.exception.ServiceNotFoundException;
import com.kishore.causeloom.models.*;
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
        requireService(serviceName);
        List<AffectedService> affected = microserviceRepository.findDependents(serviceName);
        List<AffectedService> hidden = affected.stream()
                .filter(a -> !"failing".equals(a.status()))
                .toList();
        return new BlastRadius(serviceName, affected, hidden);
    }

    public SeverityAssessment assessSeverity(String serviceName) {
        Microservice service = microserviceRepository.findByName(serviceName)
                .orElseThrow(() -> new ServiceNotFoundException("Service '" + serviceName + "' not found"));

        List<ServiceRank> ranks = microserviceRepository.findPageRank();
        int affectedCount = getBlastRadius(serviceName).affected().size();

        double score = ranks.stream()
                .filter(r -> r.name().equals(serviceName))
                .mapToDouble(ServiceRank::score)
                .findFirst().orElse(0);
        int rank = 1 + (int) ranks.stream().filter(r -> r.score() > score).count();

        String level;
        if (rank <= 3 || affectedCount >= 5) {
            level = "critical";
        } else if (rank <= 6 || affectedCount >= 3 || service.tier() == 1) {
            level = "high";
        } else {
            level = "medium";
        }
        return new SeverityAssessment(level, rank, ranks.size(), score, affectedCount, service.tier());
    }

    private void requireService(String name) {
        if (!microserviceRepository.existsByName(name)) {
            throw new ServiceNotFoundException("Service '" + name + "' not found");
        }
    }

    public String getStatus(String name) {
        return microserviceRepository.findStatus(name)
                .orElseThrow(() -> new ServiceNotFoundException("Service '" + name + "' not found"));
    }

    public void markStatus(String name, String status) {
        microserviceRepository.updateStatus(name, status);
    }
}
