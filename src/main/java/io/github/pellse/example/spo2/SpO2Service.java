package io.github.pellse.example.spo2;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static java.util.function.Function.identity;

@Service
public class SpO2Service {

    private final Map<Integer, List<SpO2>> spO2Map = Map.of(
            1, of(new SpO2(201, 1, 97, now().minusSeconds(1)), new SpO2(202, 1, 96, now().minusSeconds(2))),
            2, of(new SpO2(203, 2, 99, now().minusSeconds(1)), new SpO2(204, 2, 98, now().minusSeconds(2))),
            3, of(new SpO2(205, 3, 95, now().minusSeconds(1)), new SpO2(206, 3, 94, now().minusSeconds(2))));

    public Flux<SpO2> retrieveSpO2(List<Integer> patientIds) {

        return Flux.fromIterable(patientIds)
                .map(spO2Map::get)
                .flatMapIterable(identity());
    }
}
