package com.kishore.causeloom.seed;

import com.falkordb.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "causeloom.seed.enabled", havingValue = "true")
public class GraphSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GraphSeeder.class);

    private record ServiceSeed(String name, int tier, String type, String team) {
    }

    private record DepSeed(String from, String to, boolean critical) {
    }

    private record PersonSeed(String name, String role, boolean onLeave, String onCallFor, String reportsTo) {
    }

    private final Graph graph;

    public GraphSeeder(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void run(ApplicationArguments args) {
        reset();
        seedTeams();
        seedServices();
        seedDependencies();
        seedPeople();
        log.info("Graph seeded");
    }

    private void reset() {
        graph.query("MATCH (n) DETACH DELETE n");
    }

    private void seedTeams() {
        List<String> teamNames = List.of("Platform", "Payments", "Commerce", "Identity", "Frontend");

        // List<Map<String, Object>> rows = new ArrayList<>();
        // for (String t : teamNames) {
        // Map<String, Object> row = new HashMap<>();
        // row.put("name", t);
        // rows.add(row);
        // }
        // graph.query("UNWIND $rows AS r CREATE (:Team {name: r.name})", Map.of("rows",
        // rows));
        for (String teamName : teamNames) {
            graph.query("CREATE (:Team {name: $name})", Map.of("name", teamName));
        }

    }

    private void seedServices() {
        List<ServiceSeed> services = List.of(
                new ServiceSeed("Web", 1, "frontend", "Frontend"),
                new ServiceSeed("Cart", 1, "api", "Commerce"),
                new ServiceSeed("Checkout", 1, "api", "Payments"),
                new ServiceSeed("Payments", 1, "api", "Payments"),
                new ServiceSeed("Inventory", 2, "api", "Commerce"),
                new ServiceSeed("Auth", 1, "api", "Identity"),
                new ServiceSeed("Notifications", 3, "worker", "Frontend"),
                new ServiceSeed("Search", 2, "api", "Commerce"),
                new ServiceSeed("Orders", 1, "api", "Commerce"),
                new ServiceSeed("Database", 1, "database", "Platform"),
                new ServiceSeed("Cache", 2, "cache", "Platform"),
                new ServiceSeed("MessageQueue", 2, "queue", "Platform"));

        // List<Map<String, Object>> rows = new ArrayList<>();
        // for (ServiceSeed s : services) {
        // Map<String, Object> row = new HashMap<>();
        // row.put("name", s.name());
        // row.put("tier", s.tier());
        // row.put("type", s.type());
        // row.put("team", s.team());
        // rows.add(row);
        // }
        // graph.query("""
        // UNWIND $rows AS r
        // MATCH (t:Team {name: r.team})
        // CREATE (s:Service {name: r.name, tier: r.tier, type: r.type, status:
        // 'healthy'})
        // CREATE (s)-[:OWNED_BY]->(t)
        // """, Map.of("rows", rows));
        for (ServiceSeed s : services) {
            graph.query("""
                    MATCH (t:Team {name: $team})
                    CREATE (s:Service {name: $name, tier: $tier, type: $type, status: 'healthy'})
                    CREATE (s)-[:OWNED_BY]->(t)
                    """,
                    Map.of("team", s.team(), "name", s.name(), "tier", s.tier(), "type", s.type()));
        }

    }

    private void seedDependencies() {
        List<DepSeed> deps = List.of(
                new DepSeed("Web", "Cart", true),
                new DepSeed("Web", "Checkout", true),
                new DepSeed("Web", "Search", false),
                new DepSeed("Web", "Auth", false),
                new DepSeed("Cart", "Inventory", true),
                new DepSeed("Cart", "Cache", false),
                new DepSeed("Checkout", "Payments", true),
                new DepSeed("Checkout", "Orders", true),
                new DepSeed("Checkout", "Inventory", true),
                new DepSeed("Payments", "Database", true),
                new DepSeed("Payments", "Auth", true),
                new DepSeed("Inventory", "Database", true),
                new DepSeed("Inventory", "Cache", false),
                new DepSeed("Orders", "Database", true),
                new DepSeed("Orders", "MessageQueue", true),
                new DepSeed("Notifications", "MessageQueue", true),
                new DepSeed("Search", "Cache", false),
                new DepSeed("Auth", "Cache", false));

        // List<Map<String, Object>> rows = new ArrayList<>();
        // for (DepSeed d : deps) {
        // Map<String, Object> row = new HashMap<>();
        // row.put("from", d.from());
        // row.put("to", d.to());
        // row.put("critical", d.critical());
        // rows.add(row);
        // }
        // graph.query("""
        // UNWIND $rows AS r
        // MATCH (a:Service {name: r.from}), (b:Service {name: r.to})
        // CREATE (a)-[:DEPENDS_ON {critical: r.critical}]->(b)
        // """, Map.of("rows", rows));
        for (DepSeed d : deps) {
            graph.query("""
                    MATCH (a:Service {name: $from}), (b:Service {name: $to})
                    CREATE (a)-[:DEPENDS_ON {critical: $critical}]->(b)
                    """,
                    Map.of("from", d.from(), "to", d.to(), "critical", d.critical()));
        }
        
    }

    private void seedPeople() {
        List<PersonSeed> people = List.of(
                new PersonSeed("Nisha", "Director of Engineering", false, null, null),
                new PersonSeed("Meera", "Platform Engineering Manager", false, null, "Nisha"),
                new PersonSeed("Ravi", "SRE", true, "Platform", "Meera"),
                new PersonSeed("Anita", "Payments Engineer", false, "Payments", "Meera"),
                new PersonSeed("Karthik", "Commerce Engineer", false, "Commerce", "Meera"),
                new PersonSeed("Divya", "Identity Engineer", false, "Identity", "Meera"),
                new PersonSeed("Sam", "Frontend Engineer", false, "Frontend", "Meera"));

        for (PersonSeed p : people) {
            graph.query("CREATE (:Person {name: $name, role: $role, onLeave: $onLeave})",
                    Map.of("name", p.name(), "role", p.role(), "onLeave", p.onLeave()));
        }

        for (PersonSeed p : people) {
            if (p.onCallFor() != null) {
                graph.query("MATCH (p:Person {name: $name}), (t:Team {name: $team}) CREATE (p)-[:ON_CALL_FOR]->(t)",
                        Map.of("name", p.name(), "team", p.onCallFor()));
            }
            if (p.reportsTo() != null) {
                graph.query("MATCH (p:Person {name: $name}), (m:Person {name: $boss}) CREATE (p)-[:REPORTS_TO]->(m)",
                        Map.of("name", p.name(), "boss", p.reportsTo()));
            }
        }
    }
}