package io.github.pellse.example.client;

import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Optional.ofNullable;
import static java.util.function.UnaryOperator.identity;

@Component
public class DiscoverableRestClient {

    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    public DiscoverableRestClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        this.lbFunction = lbFunction;
    }

    public <T> Flux<T> retrieveData(String serviceName, String uriPath, Class<T> elementClass) {
        return retrieveData(serviceName, uriPath, identity(), elementClass);
    }

    public <T, U> Flux<T> retrieveData(
            String serviceName,
            String uriPath,
            String queryParamName,
            List<U> queryParamValues,
            Class<T> elementClass) {

        return retrieveData(serviceName, uriPath, queryParamName, queryParamValues, identity(), elementClass);
    }

    public <T, U, K> Flux<T> retrieveData(
            String serviceName,
            String uriPath,
            String queryParamName,
            List<U> queryParamValues,
            Function<U, K> keyMapper,
            Class<T> elementClass) {

        final var values = ofNullable(queryParamValues)
                .map(valueList -> valueList.stream().map(keyMapper).toList())
                .orElseGet(Collections::emptyList);

        return retrieveData(serviceName, uriPath, uriBuilder -> uriBuilder.queryParam(queryParamName, values), elementClass);
    }

    public <T> Flux<T> retrieveData(String serviceName, String uriPath, UnaryOperator<UriBuilder> uriBuilderTransformer, Class<T> elementClass) {

        final var transformedUriBuilder = ofNullable(uriBuilderTransformer)
                .orElseGet(UnaryOperator::identity);

        return WebClient.builder()
                .baseUrl("http://" + serviceName)
                .filter(lbFunction)
                .build()
                .get()
                .uri(uriBuilder -> transformedUriBuilder.apply(uriBuilder.path(uriPath)).build())
                .retrieve()
                .bodyToFlux(elementClass);
    }
}
