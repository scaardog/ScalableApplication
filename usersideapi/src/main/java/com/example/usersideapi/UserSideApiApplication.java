package com.example.usersideapi;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.HttpClient;

import javax.net.ssl.SSLContext;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class UserSideApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSideApiApplication.class, args);
        System.setProperty("javax.net.ssl.trustStore", "src\\main\\resources\\scalableTrustStore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
    }

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance() {
        ClientConfig config = new ClientConfig();
        config.setClusterName("bucketCache");
        return HazelcastClient.newHazelcastClient(config);
    }

    @Bean
    public RestTemplate externalServiceRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                                    @Value("${externalservice.url}") String remoteUrl,
                                                    @Value("${trustStore}") Resource keyStore,
                                                    @Value("${trustStorePassword}") String keyStorePassword)
            throws Exception{

        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(
                        keyStore.getURL(),
                        keyStorePassword.toCharArray()
                ).build();
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory).build();
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return restTemplateBuilder
                .rootUri(remoteUrl)
                .requestFactory(() -> factory)
                .build();
    }
}
