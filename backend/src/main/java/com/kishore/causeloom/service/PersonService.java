package com.kishore.causeloom.service;

import com.kishore.causeloom.models.OnCallAssignment;
import com.kishore.causeloom.models.OnCallResult;
import com.kishore.causeloom.models.Person;
import com.kishore.causeloom.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public OnCallResult getResponder(String serviceName) {
        Optional<OnCallAssignment> assignment = personRepository.findOnCall(serviceName);
        if (assignment.isEmpty()) {
            return new OnCallResult(serviceName, null, null, null, false,
                    "No on-call person found for " + serviceName);
        }

        OnCallAssignment onCall = assignment.get();
        Person primary = onCall.person();

        if (!primary.onLeave()) {
            return new OnCallResult(serviceName, onCall.team(), primary, primary, false,
                    primary.name() + " is on call for " + onCall.team() + " and available");
        }

        Optional<Person> backup = personRepository.findNearestAvailable(primary.name());
        if (backup.isEmpty()) {
            return new OnCallResult(serviceName, onCall.team(), primary, null, false,
                    primary.name() + " is on leave and no available person was found up the chain");
        }

        Person responder = backup.get();
        return new OnCallResult(serviceName, onCall.team(), primary, responder, true,
                primary.name() + " is on leave, escalated to " + responder.name()
                        + " (" + responder.role() + ")");
    }
}