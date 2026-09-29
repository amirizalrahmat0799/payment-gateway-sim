package com.pgs.payment.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class ClientConfig {

    @Bean
    RestClient merchantRestClient(RestClient.Builder builder, ClientProperties properties) {
        return builder.clone()
                .baseUrl(properties.merchant().baseUrl())
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    RestClient tokenizationRestClient(RestClient.Builder builder, ClientProperties properties) {
        return builder.clone()
                .baseUrl(properties.tokenization().baseUrl())
                .requestFactory(requestFactory())
                .build();
    }

    /** Always set timeouts on service-to-service calls; the defaults wait forever. */
    private static SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2_000);
        factory.setReadTimeout(5_000);
        return factory;
    }
}
