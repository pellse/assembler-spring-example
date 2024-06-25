package io.github.pellse.example.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.pellse.util.ObjectUtils.also;
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
        return also(new CaffeineCacheManager(),
                cacheManager -> cacheManager.setAsyncCacheMode(true),
                cacheManager -> cacheManager.setCacheNames(of(
                        BODY_MEASUREMENT_CACHE_1,
                        SP02_CACHE,
                        PATIENT_CACHE,
                        BODY_MEASUREMENT_CACHE_2)));
    }
}