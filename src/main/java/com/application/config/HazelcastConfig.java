package com.application.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    private final String address;
    private final String clusterName;

    public HazelcastConfig(@Value("${app.hazelcast.address:localhost:5701}") String address,
                           @Value("${app.hazelcast.cluster-name:dev}") String clusterName) {
        this.address = address;
        this.clusterName = clusterName;
    }

    /** Клиент к Hazelcast-кластеру (docker-контейнер hazelcast:5.7.0). */
    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName(clusterName);
        clientConfig.getNetworkConfig().addAddress(address);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}