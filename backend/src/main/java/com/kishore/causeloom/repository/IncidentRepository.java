package com.kishore.causeloom.repository;

import com.falkordb.Graph;
import com.falkordb.Record;
import com.falkordb.ResultSet;
import com.kishore.causeloom.models.Incident;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class IncidentRepository {

    private final Graph graph;

    public IncidentRepository(Graph graph) {
        this.graph = graph;
    }

    public Optional<Incident> findOpenByRootCause(String serviceName) {
        ResultSet rs = graph.query("""
                MATCH (i:Incident {status:'open'})-[:CAUSED_BY]->(s:Service {name: $name})
                RETURN i.id AS id, i.title AS title, i.severity AS severity,
                       i.status AS status, i.createdAt AS createdAt, s.name AS rootCause
                LIMIT 1
                """, Map.of("name", serviceName));
        for (Record r : rs) {
            return Optional.of(new Incident(r.getString("id"), r.getString("title"),
                    r.getString("severity"), r.getString("status"),
                    r.getString("createdAt"), r.getString("rootCause")));
        }
        return Optional.empty();
    }

    public void create(Incident incident, String summary, List<String> affectedNames, String assignee) {
        graph.query("""
                MATCH (root:Service {name: $root})
                CREATE (i:Incident {id: $id, title: $title, severity: $severity, status: 'open',
                                    createdAt: $createdAt, summary: $summary})
                CREATE (i)-[:CAUSED_BY]->(root)
                """, Map.of("root", incident.rootCause(), "id", incident.id(),
                "title", incident.title(), "severity", incident.severity(),
                "createdAt", incident.createdAt(), "summary", summary));

        graph.query("""
                MATCH (i:Incident {id: $id})
                MATCH (s:Service) WHERE s.name IN $names
                CREATE (i)-[:AFFECTS]->(s)
                """, Map.of("id", incident.id(), "names", affectedNames));

        List<String> alertServices = new ArrayList<>(affectedNames);
        alertServices.add(incident.rootCause());
        graph.query("""
                MATCH (i:Incident {id: $id})
                MATCH (a:Alert {status:'open'})-[:FIRED_ON]->(s:Service) WHERE s.name IN $names
                CREATE (i)-[:INCLUDES]->(a)
                """, Map.of("id", incident.id(), "names", alertServices));

        if (assignee != null) {
            graph.query("""
                    MATCH (i:Incident {id: $id}), (p:Person {name: $person})
                    CREATE (i)-[:ASSIGNED_TO]->(p)
                    """, Map.of("id", incident.id(), "person", assignee));
        }
    }
}
