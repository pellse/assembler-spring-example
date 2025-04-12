package io.github.pellse.patientobservation.server.heartrate;

import io.github.pellse.patientobservation.common.HR;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.kafka.receiver.ReceiverOffset;

import java.util.function.Function;

import static java.time.Duration.ofMillis;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.publisher.Mono.*;
import static reactor.core.publisher.Sinks.EmitResult.FAIL_CANCELLED;
import static reactor.util.retry.Retry.fixedDelay;

@Service
public class HeartRateStreamingService {

    private static final Logger logger = getLogger(HeartRateStreamingService.class);
    private static final RuntimeException TRY_EMIT_FAILED_EXCEPTION = new RuntimeException();

    private final Sinks.Many<HR> heartRateSink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Function<Flux<Message<HR>>, Mono<Void>> receiveHeartRates() {
        return flux -> flux.flatMap(message ->
                just(heartRateSink.tryEmitNext(message.getPayload()))
                        .filter(EmitResult::isSuccess)
                        .doOnNext(emitResult -> requireNonNull(message.getHeaders().get(ACKNOWLEDGMENT, ReceiverOffset.class)).acknowledge())
                        .switchIfEmpty(error(TRY_EMIT_FAILED_EXCEPTION))
                        .retryWhen(fixedDelay(10, ofMillis(1)))
                        .doOnError(HeartRateStreamingService::logError)
                        .onErrorResume(Exceptions::isRetryExhausted, e -> just(FAIL_CANCELLED))
        ).then();
    }

    public Flux<HR> stream() {
        return heartRateSink.asFlux();
    }

    private static void logError(Throwable throwable) {
        logger.error("Error while processing heart rate: ", throwable);
    }
}
