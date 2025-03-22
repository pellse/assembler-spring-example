package io.github.pellse.patientobservation.service.spo2;

import io.github.pellse.patientobservation.common.SpO2;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

@Service
public class SpO2StreamingService {

    private final Sinks.Many<SpO2> spO2Sink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Function<Flux<SpO2>, Mono<Void>> receiveSpO2() {
        return flux -> flux.map(spO2Sink::tryEmitNext).then();
    }

    public Flux<SpO2> spO2Flux() {
        return spO2Sink.asFlux();
    }
}
