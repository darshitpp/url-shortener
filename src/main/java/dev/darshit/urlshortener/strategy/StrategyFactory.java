package dev.darshit.urlshortener.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StrategyFactory {

    @Value("${default.shortening.strategy:words}")
    private String defaultStrategy;

    private final Map<String, ShorteningStrategy> shorteningStrategies;

    public StrategyFactory(List<ShorteningStrategy> strategyList) {
        this.shorteningStrategies = strategyList.stream()
                .collect(Collectors.toMap(strategy -> strategy.getClass()
                                .getDeclaredAnnotation(Service.class).value(),
                        Function.identity()));
    }

    public ShorteningStrategy get(String strategy) {
        if (!shorteningStrategies.containsKey(strategy)) {
            strategy = defaultStrategy;
        }
        return shorteningStrategies.get(strategy);
    }
}
