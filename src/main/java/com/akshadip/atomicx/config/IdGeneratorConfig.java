package com.akshadip.atomicx.config;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public TimeBasedEpochGenerator uuidV7Generator(){
        return Generators.timeBasedEpochGenerator();
    }
}
