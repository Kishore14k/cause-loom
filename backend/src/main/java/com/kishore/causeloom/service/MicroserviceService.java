package com.kishore.causeloom.service;

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
        List<AffectedService> affected = microserviceRepository.findDependents(serviceName);
        List<AffectedService> hidden = affected.stream()
                .filter(a -> !"failing".equals(a.status()))
                .toList();
        return new BlastRadius(serviceName, affected, hidden);
    }

    public SeverityAssessment assessSeverity(String serviceName) {
        List<ServiceRank> ranks = microserviceRepository.findPageRank();
        int affectedCount = getBlastRadius(serviceName).affected().size();

        int rank = 0;
        double score = 0;
        for (int i = 0; i < ranks.size(); i++) {
            if (ranks.get(i).name().equals(serviceName)) {
                rank = i + 1;
                score = ranks.get(i).score();
                break;
            }
        }

        String level;
        if ((rank >= 1 && rank <= 3) || affectedCount >= 5) {
            level = "critical";
        } else if ((rank >= 1 && rank <= 6) || affectedCount >= 3) {
            level = "high";
        } else {
            level = "medium";
        }
        return new SeverityAssessment(level, rank, ranks.size(), score, affectedCount);
    }
}
