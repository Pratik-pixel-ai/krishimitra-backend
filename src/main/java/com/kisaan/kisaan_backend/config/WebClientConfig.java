package com.kisaan.kisaan_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Default WebClient buffer limit is 256KB, which is far smaller than a
    // base64-encoded plant photo. Without raising this, any real image sent
    // to Gemini throws a DataBufferLimitException instead of ever reaching
    // the API. 20MB comfortably covers a base64-inflated 10MB upload.
    @Bean
    public WebClient.Builder webClientBuilder() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
        return WebClient.builder().exchangeStrategies(strategies);
    }
}