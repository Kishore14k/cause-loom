package com.kishore.causeloom.controller;

import com.kishore.causeloom.models.OnCallResult;
import com.kishore.causeloom.service.PersonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/responder/{serviceName}")
    public OnCallResult responder(@PathVariable String serviceName) {
        return personService.getResponder(serviceName);
    }
}