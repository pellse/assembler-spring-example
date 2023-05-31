package io.github.pellse.example.patientobservation.spo2;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

import static io.github.pellse.example.util.MessageUtils.messageHandler;

@Service
public class SpO2StreamingService {

    private final Sinks.Many<SpO2> spO2Sink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Function<Flux<Message<SpO2>>, Mono<Void>> receiveSpO2() {
        return messageHandler(spO2Sink::tryEmitNext);
    }

    public Flux<SpO2> spO2Flux() {
        return spO2Sink.asFlux();
    }
}
