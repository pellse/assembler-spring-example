package io.github.pellse.patientobservation.service.heartRate;

import io.github.pellse.patientobservation.common.HR;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

@Service
public class HeartRateStreamingService {

    private final Sinks.Many<HR> heartRateSink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Function<Flux<HR>, Mono<Void>> receiveHeartRates() {
        return flux -> flux.map(heartRateSink::tryEmitNext).then();
    }

    public Flux<HR> stream() {
        return heartRateSink.asFlux();
    }
}
