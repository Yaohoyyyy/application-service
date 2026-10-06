package com.application.config;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class HazelcastSqlMappingInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HazelcastSqlMappingInitializer.class);

    private static final String CREATE_VENDORS_MAPPING = """
            CREATE OR REPLACE MAPPING vendors (
              __key    VARCHAR,
              id       BIGINT,
              name     VARCHAR,
              active   BOOLEAN
            ) TYPE IMap
            OPTIONS (
              'keyFormat'  = 'varchar',
              'valueFormat'= 'json-flat'
            )
            """;

    private final HazelcastInstance hazelcastInstance;

    public HazelcastSqlMappingInitializer(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            hazelcastInstance.getSql().execute(CREATE_VENDORS_MAPPING);
            log.info("Hazelcast SQL mapping 'vendors' created");
        } catch (Exception e) {
            log.warn("Failed to create Hazelcast SQL mapping 'vendors': {}", e.getMessage());
        }
    }
}