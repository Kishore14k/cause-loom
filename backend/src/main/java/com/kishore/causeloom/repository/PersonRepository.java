package com.kishore.causeloom.repository;

import com.falkordb.Graph;
import com.falkordb.ResultSet;
import com.kishore.causeloom.models.OnCallAssignment;
import com.kishore.causeloom.models.Person;
import org.springframework.stereotype.Repository;
import com.falkordb.Record;

import java.util.Map;
import java.util.Optional;

@Repository
public class PersonRepository {

    private final Graph graph;

    public PersonRepository(Graph graph) {
        this.graph = graph;
    }

    public Optional<OnCallAssignment> findOnCall(String serviceName) {
        ResultSet rs = graph.query("""
                MATCH (s:Service {name: $name})-[:OWNED_BY]->(t:Team)<-[:ON_CALL_FOR]-(p:Person)
                RETURN t.name AS team, p.name AS name, p.role AS role, p.onLeave AS onLeave
                LIMIT 1
                """, Map.of("name", serviceName));
        for (Record r : rs) {
            Person person = new Person(r.getString("name"), r.getString("role"),
                    (Boolean) r.getValue("onLeave"));
            return Optional.of(new OnCallAssignment(r.getString("team"), person));
        }
        return Optional.empty();
    }

    public Optional<Person> findNearestAvailable(String personName) {
        ResultSet rs = graph.query("""
                MATCH path=(p:Person {name: $name})-[:REPORTS_TO*0..5]->(m:Person)
                WHERE m.onLeave = false
                RETURN m.name AS name, m.role AS role, length(path) AS hops
                ORDER BY hops
                LIMIT 1
                """, Map.of("name", personName));
        for (Record r : rs) {
            return Optional.of(new Person(r.getString("name"), r.getString("role"), false));
        }
        return Optional.empty();
    }
}
