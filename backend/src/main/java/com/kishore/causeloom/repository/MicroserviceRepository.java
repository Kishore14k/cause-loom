package com.kishore.causeloom.repository;

import com.falkordb.Graph;
import com.falkordb.Record;
import com.falkordb.ResultSet;
import com.kishore.causeloom.models.AffectedService;
import com.kishore.causeloom.models.Microservice;
import com.kishore.causeloom.models.ServiceRank;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MicroserviceRepository {

    private final Graph graph;

    public MicroserviceRepository(Graph graph) {
        this.graph = graph;
    }

    public List<Microservice> findRootCauses() {
        ResultSet rs = graph.query("""
                MATCH (s:Service {status:'failing'})
                OPTIONAL MATCH (s)-[:DEPENDS_ON]->(d:Service {status:'failing'})
                WITH s, d WHERE d IS NULL
                RETURN s.name AS name, s.tier AS tier, s.type AS type, s.status AS status
                """);
        List<Microservice> result = new ArrayList<>();
        for (Record r : rs) {
            result.add(new Microservice(r.getString("name"),
                    ((Number) r.getValue("tier")).intValue(),
                    r.getString("type"), r.getString("status")));
        }
        return result;
    }

    public List<AffectedService> findDependents(String serviceName) {
        ResultSet rs = graph.query("""
            MATCH p=(root:Service {name: $name})<-[:DEPENDS_ON*1..5]-(v:Service)
            RETURN v.name AS name, v.status AS status, min(length(p)) AS hops
            ORDER BY hops
            """, Map.of("name", serviceName));
        List<AffectedService> result = new ArrayList<>();
        for (Record r : rs) {
            result.add(new AffectedService(r.getString("name"), r.getString("status"),
                    ((Number) r.getValue("hops")).intValue()));
        }
        return result;
    }

    public List<ServiceRank> findPageRank() {
        ResultSet rs = graph.query("""
            CALL algo.pageRank('Service','DEPENDS_ON') YIELD node, score
            RETURN node.name AS name, score
            ORDER BY score DESC
            """);
        List<ServiceRank> result = new ArrayList<>();
        for (Record r : rs) {
            result.add(new ServiceRank(r.getString("name"),
                    ((Number) r.getValue("score")).doubleValue()));
        }
        return result;
    }

    public boolean existsByName(String name) {
        ResultSet rs = graph.query("MATCH (s:Service {name: $name}) RETURN count(s) AS c",
                Map.of("name", name));
        for (Record r : rs) {
            return ((Number) r.getValue("c")).intValue() > 0;
        }
        return false;
    }

    public Optional<String> findStatus(String name) {
        ResultSet rs = graph.query("MATCH (s:Service {name: $name}) RETURN s.status AS status",
                Map.of("name", name));
        for (Record r : rs) {
            return Optional.of(r.getString("status"));
        }
        return Optional.empty();
    }

    public void updateStatus(String name, String status) {
        graph.query("MATCH (s:Service {name: $name}) SET s.status = $status",
                Map.of("name", name, "status", status));
    }

    public Optional<Microservice> findByName(String name) {
        ResultSet rs = graph.query("""
            MATCH (s:Service {name: $name})
            RETURN s.name AS name, s.tier AS tier, s.type AS type, s.status AS status
            """, Map.of("name", name));
        for (Record r : rs) {
            return Optional.of(new Microservice(r.getString("name"),
                    ((Number) r.getValue("tier")).intValue(),
                    r.getString("type"), r.getString("status")));
        }
        return Optional.empty();
    }
}
