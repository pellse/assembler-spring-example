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
import static java.util.Optional.ofNullable;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Sinks.EmitResult.*;
import static reactor.core.publisher.Sinks.EmitResult.FAIL_CANCELLED;

/**
 * Utility class bridging a reactive Kafka consumer to a reactive sink,
 * managing error handling and retries for message emission. Since auto-commit
 * is unavailable in Reactive Kafka Binder, we have to commit offsets manually,
 * and this class abstracts the logic to handle that process seamlessly.
 */
public class ReactiveBridge {

    private Retry retry;
    private Predicate<EmitResult> retryCondition = emitResult -> emitResult == FAIL_NON_SERIALIZED || emitResult == FAIL_OVERFLOW;
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

    public ReactiveBridge retryCondition(Predicate<EmitResult> retryCondition) {
        this.retryCondition = retryCondition;
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
                        .flatMap(emitResult -> retryCondition.test(emitResult) ? error(new EmissionException(emitResult)) : just(emitResult))
                        .doOnNext(emitResult -> requireNonNull(message.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class)).acknowledge())
                        .transform(mono -> ofNullable(retry).map(mono::retryWhen).orElse(mono))
                        .transform(mono -> ofNullable(onError).map(mono::doOnError).orElse(mono))
                        .onErrorResume(e -> just(FAIL_CANCELLED))   // We return a value so that the binder keeps listening on the Kafka topic
        ).then(); // The Reactive Kafka Binder will subscribe to this Mono<Void> so we don't have to subscribe to it manually.
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
