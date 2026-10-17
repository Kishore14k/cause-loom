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

        int hard = microserviceRepository.countCriticalDependents(serviceName);

        int level = service.tier() == 1 ? 1 : 0;
        if (hard >= 5 || (service.tier() == 1 && hard >= 2)) {
            level = 2;
        } else if (hard >= 3) {
            level = Math.max(level, 1);
        }
        if (rank <= 3 && hard > 0) {                               // PageRank only boosts
            level = Math.min(level + 1, 2);
        }
        String levelName = List.of("medium", "high", "critical").get(level);

        return new SeverityAssessment(levelName, rank, ranks.size(), score,
                affectedCount, hard, service.tier());
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
