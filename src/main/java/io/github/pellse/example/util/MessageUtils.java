package io.github.pellse.example.util;

import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.ReceiverOffset;

import java.util.function.Function;

import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.just;

public interface MessageUtils {

    static <T> Function<Flux<Message<T>>, Mono<Void>> messageHandler(Function<T, Sinks.EmitResult> handler) {

        return messageFlux -> messageFlux
                .flatMap(msg -> just(msg.getPayload())
                        .map(handler)
                        .filter(Sinks.EmitResult::isSuccess)
                        .mapNotNull(result -> msg.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class))
                        .doOnNext(ReceiverOffset::acknowledge))
                .then();
    }
}
