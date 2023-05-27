package io.github.pellse.example.bodymeasurement;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;

@Service
public class BodyMeasureService {

    private final Map<Integer, BodyMeasure> bodyMeasureMap = Map.of(
            1, new BodyMeasure(100, 1, 170, 65, now().minusWeeks(2)),
            2, new BodyMeasure(101, 2, 165, 62, now().minusWeeks(3)),
            3, new BodyMeasure(102, 3, 175, 76, now().minusWeeks(4)));

    public Flux<BodyMeasure> retrieveBodyMeasure(List<Integer> patientIds) {

        return Flux.fromIterable(patientIds)
                .map(bodyMeasureMap::get);
    }
}
