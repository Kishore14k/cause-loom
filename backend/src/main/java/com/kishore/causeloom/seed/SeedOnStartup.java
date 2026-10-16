package com.kishore.causeloom.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "causeloom.seed.enabled", havingValue = "true")
public class SeedOnStartup implements ApplicationRunner {

    private final GraphSeeder graphSeeder;

    public SeedOnStartup(GraphSeeder graphSeeder) {
        this.graphSeeder = graphSeeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        graphSeeder.seed();
    }
}