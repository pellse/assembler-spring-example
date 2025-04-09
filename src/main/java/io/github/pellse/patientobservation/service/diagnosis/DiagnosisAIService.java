package io.github.pellse.patientobservation.service.diagnosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pellse.patientobservation.common.Diagnosis;
import io.github.pellse.patientobservation.common.Vitals;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

import static io.github.pellse.util.CheckedFunction.unchecked;
import static java.util.function.Function.identity;
import static reactor.core.publisher.Mono.just;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
public class DiagnosisAIService {

    private static final String promptTemplate = """
            You are given an array of JSON objects, each representing a patient's vitals.
            
             Each object contains:
             - `patient`: patient information
             - `heartRate`: a heart rate measurement
             - `bloodPressures`: an array of blood pressure readings
            
             Your task:
             - Analyze the `heartRate` and `bloodPressures` fields.
             - Generate a **diagnosis** for each patient based on these vitals.
             - Keep **all fields and values exactly as they are** unless specified.
             - In your output, include only:
               - `patientId` (same as input)
               - `diagnosis` (based on the patient's vitals)
            
             ⚠️ **Do not modify any part of the input. Especially, do not change or regenerate the `id` fields of any object.**
            
             Here is the input JSON array:
            
             {vitals}
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public DiagnosisAIService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Flux<Diagnosis> getDiagnosesFromLLM(List<Vitals> vitalsList) {

        return just(vitalsList)
                .map(unchecked(objectMapper::writeValueAsString))
                .mapNotNull(this::getDiagnosesFromLLM)
                .flatMapIterable(identity())
                .subscribeOn(boundedElastic());
    }

    private List<Diagnosis> getDiagnosesFromLLM(String vitalsJson) {

        return chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor())
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplate)
                        .param("vitals", vitalsJson))
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
