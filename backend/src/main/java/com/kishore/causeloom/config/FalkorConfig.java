package com.kishore.causeloom.config;

import com.falkordb.Driver;
import com.falkordb.FalkorDB;
import com.falkordb.Graph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FalkorConfig {

    @Bean(destroyMethod = "close")
    public Driver falkorDriver(FalkorProperties properties){
        return FalkorDB.driver(properties.host(),properties.port());
    }
    
    @Bean
    public Graph incidentGraph(Driver driver, FalkorProperties properties){
        return driver.graph(properties.graph());
    }
}
