package io.github.pellse.util.reactive;

import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmissionException;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.RetrySpec;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Sinks.EmitResult.FAIL_CANCELLED;

public class ReactiveBridge {

    private Retry retry;
    private Consumer<Throwable> onError;

    public static ReactiveBridge reactiveBridge() {
        return new ReactiveBridge();
    }

    public ReactiveBridge retry(RetrySpec retry) {
        this.retry = retry.filter(EmissionException.class::isInstance);
        return this;
    }

    public ReactiveBridge retry(RetryBackoffSpec retry) {
        this.retry = retry.filter(EmissionException.class::isInstance);
        return this;
    }

    public ReactiveBridge onError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public <T> Function<Flux<Message<T>>, Mono<Void>> bridge(Sinks.Many<T> sink) {
        return messageFlux -> messageFlux.flatMap(message ->
                just(message)
                        .map(Message::getPayload)
                        .map(sink::tryEmitNext)
                        .flatMap(emitResult -> emitResult.isSuccess() ? just(emitResult) : error(new EmissionException(emitResult)))
                        .doOnNext(emitResult -> requireNonNull(message.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class)).acknowledge())
                        .transform(mono -> retry != null ? mono.retryWhen(retry) : mono)
                        .transform(mono -> onError != null ? mono.doOnError(onError) : mono)
                        .onErrorResume(e -> just(FAIL_CANCELLED)) // What we return here doesn't matter, but it's important to return a value so that the Reactive Kafka Binder doesn't stop listening to the Kafka topic
        ).then(); // Converts our Flux to Mono<Void>, which will complete when the Flux completes. The Reactive Kafka Binder will subscribe to this Mono<Void> so we don't have to subscribe to it manually.
    }
}
