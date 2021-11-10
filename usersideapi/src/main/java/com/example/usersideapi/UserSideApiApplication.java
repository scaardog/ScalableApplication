package com.example.usersideapi;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class UserSideApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserSideApiApplication.class, args);
    }

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance() {
        ClientConfig config = new ClientConfig();
        config.setClusterName("bucketCache");
        return HazelcastClient.newHazelcastClient(config);
    }
}
