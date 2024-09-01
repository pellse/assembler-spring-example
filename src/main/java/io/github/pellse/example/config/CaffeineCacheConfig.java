package io.github.pellse.example.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.List.of;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    public static String BODY_MEASUREMENT_CACHE_1 = "bodyMeasurementCache1";
    public static String SP02_CACHE = "spO2Cache";
    public static String PATIENT_CACHE = "patientCache";
    public static String BODY_MEASUREMENT_CACHE_2 = "bodyMeasurementCache2";

    @Bean
    public CacheManager cacheManager() {

        var cacheManager = new CaffeineCacheManager();

        cacheManager.setAsyncCacheMode(true);
        cacheManager.setCacheNames(of(
                BODY_MEASUREMENT_CACHE_1,
                SP02_CACHE,
                PATIENT_CACHE,
                BODY_MEASUREMENT_CACHE_2));

        return cacheManager;
    }
}