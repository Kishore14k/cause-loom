package com.kishore.causeloom.repository;

import com.falkordb.Graph;
import com.falkordb.Record;
import com.falkordb.ResultSet;
import com.kishore.causeloom.models.Alert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AlertRepository {

    private final Graph graph;

    public AlertRepository(Graph graph) {
        this.graph = graph;
    }

    public List<Alert> findOpenAlerts() {
        ResultSet rs = graph.query("""
                MATCH (a:Alert {status:'open'})-[:FIRED_ON]->(s:Service)
                RETURN a.id AS id, s.name AS service, a.severity AS severity, a.message AS message
                ORDER BY a.id
                """);
        List<Alert> alerts = new ArrayList<>();
        for (Record r : rs) {
            alerts.add(new Alert(r.getString("id"), r.getString("service"),
                    r.getString("severity"), r.getString("message")));
        }
        return alerts;
    }

    public boolean existsOpenForService(String serviceName) {
        ResultSet rs = graph.query("""
            MATCH (a:Alert {status:'open'})-[:FIRED_ON]->(s:Service {name: $name})
            RETURN count(a) AS c
            """, Map.of("name", serviceName));
        for (Record r : rs) {
            return ((Number) r.getValue("c")).intValue() > 0;
        }
        return false;
    }

    public void create(Alert alert, String firedAt) {
        graph.query("""
            MATCH (s:Service {name: $service})
            CREATE (a:Alert {id: $id, severity: $severity, message: $message,
                             firedAt: $firedAt, status: 'open'})-[:FIRED_ON]->(s)
            """, Map.of("service", alert.service(), "id", alert.id(),
                "severity", alert.severity(), "message", alert.message(), "firedAt", firedAt));
    }
}