package io.github.pellse.util.reactive;

import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmissionException;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.RetrySpec;

import java.util.function.*;

import static java.util.Objects.requireNonNull;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Sinks.EmitResult.*;
import static reactor.core.publisher.Sinks.EmitResult.FAIL_CANCELLED;

public class ReactiveBridge {

    private Retry retry;
    private Consumer<Throwable> onError;

    public static ReactiveBridge reactiveBridge() {
        return new ReactiveBridge();
    }

    public ReactiveBridge retry(RetrySpec retrySpec) {
        return retry(retrySpec, RetrySpec::filter);
    }

    public ReactiveBridge retry(RetryBackoffSpec retryBackoffSpec) {
        return retry(retryBackoffSpec, RetryBackoffSpec::filter);
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
                        .flatMap(emitResult -> shouldRetry(emitResult) ? error(new EmissionException(emitResult)) : just(emitResult))
                        .doOnNext(emitResult -> requireNonNull(message.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class)).acknowledge())
                        .transform(mono -> retry != null ? mono.retryWhen(retry) : mono)
                        .transform(mono -> onError != null ? mono.doOnError(onError) : mono)
                        .onErrorResume(e -> just(FAIL_CANCELLED)) // What we return here doesn't matter, but it's important to return a value so that the Reactive Kafka Binder doesn't stop listening to the Kafka topic
        ).then(); // Converts our Flux to Mono<Void>, which will complete when the Flux completes. The Reactive Kafka Binder will subscribe to this Mono<Void> so we don't have to subscribe to it manually.
    }

    /**
     * Checks if the {@code emitResult} indicates a failure that should be retried.
     * We consider a failure to be retryable only if it is a serialization failure, meaning 2 or more threads
     * are trying to emit to the same sink at the same time.
     */
    private boolean shouldRetry(EmitResult emitResult) {
        return emitResult == FAIL_NON_SERIALIZED;
    }

    /**
     * The {@code Retry} type does not expose the {@code filter()} method; it is declared instead of being overridden in subclasses.
     * Therefore, we must use the {@code RetrySpec} and {@code RetryBackoffSpec} types to access the {@code filter()} method.
     */
    private <T extends Retry> ReactiveBridge retry(T retrySpec, BiFunction<T, Predicate<Throwable>, T> filterFunction) {
        this.retry = filterFunction.apply(retrySpec, EmissionException.class::isInstance);
        return this;
    }
}
