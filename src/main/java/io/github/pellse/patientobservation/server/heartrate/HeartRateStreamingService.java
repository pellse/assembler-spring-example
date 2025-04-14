package io.github.pellse.patientobservation.server.heartrate;

import io.github.pellse.patientobservation.common.HR;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

import static io.github.pellse.util.reactive.ReactiveBridge.reactiveBridge;
import static java.time.Duration.ofMillis;
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED;
import static reactor.util.retry.Retry.fixedDelay;

@Service
public class HeartRateStreamingService {

    private static final Logger logger = getLogger(HeartRateStreamingService.class);

    private final Sinks.Many<HR> heartRateSink = Sinks.many().multicast().onBackpressureBuffer();

    private final Function<Flux<Message<HR>>, Mono<Void>> bridge = reactiveBridge()
            .retry(fixedDelay(10, ofMillis(1)))
            .retryCondition(emitResult -> emitResult == FAIL_NON_SERIALIZED)
            .onError(this::logError)
            .bridge(heartRateSink);

    @Bean
    public Function<Flux<Message<HR>>, Mono<Void>> receiveHeartRates() {
        return bridge;
    }

    public Flux<HR> stream() {
        return heartRateSink.asFlux();
    }

    private void logError(Throwable throwable) {
        logger.error("Error while processing heart rate: ", throwable);
    }
}