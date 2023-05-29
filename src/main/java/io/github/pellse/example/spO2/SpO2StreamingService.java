package io.github.pellse.example.spO2;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.kafka.receiver.ReceiverOffset;

import java.util.function.Function;

import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.just;

@Service
public class SpO2StreamingService {

    private final Sinks.Many<SpO2> spO2Sink = Sinks.many().multicast().onBackpressureBuffer();

    private static <T> Function<Flux<Message<T>>, Mono<Void>> messageHandler(Function<T, EmitResult> handler) {

        return messageFlux -> messageFlux
                .flatMap(msg -> just(msg.getPayload())
                        .map(handler)
                        .filter(EmitResult::isSuccess)
                        .mapNotNull(result -> msg.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class))
                        .doOnNext(ReceiverOffset::acknowledge))
                .then();
    }

    @Bean
    public Function<Flux<Message<SpO2>>, Mono<Void>> receiveSpO2() {
        return messageHandler(spO2Sink::tryEmitNext);
    }

    public Flux<SpO2> spO2Flux() {
        return spO2Sink.asFlux();
    }
}
