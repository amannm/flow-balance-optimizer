package systems.cauldron.algorithms.optimization.allocation;

import systems.cauldron.algorithms.optimization.network.FlowNetwork;
import systems.cauldron.algorithms.optimization.network.Pipe;
import systems.cauldron.algorithms.optimization.network.Sink;
import systems.cauldron.algorithms.optimization.network.Source;
import systems.cauldron.algorithms.optimization.optimizer.BalancedMaxFlowOptimizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class AllocationPlanner {

    public static <T, U> Map<U, Set<T>> getPlan(Set<T> inputSource, BiPredicate<T, U> routeFilter, Collection<Allocatable<U>> inputSinks) {

        Set<Source<T, U>> networkSources = new HashSet<>();
        Set<Pipe<T, U>> networkPipes = new HashSet<>();
        Set<Sink<T, U>> networkSinks = inputSinks.stream()
                .map(Sink<T, U>::new)
                .collect(Collectors.toSet());

        Map<Set<Sink<T, U>>, Source<T, U>> sinksToSourceMap = new HashMap<>();
        for (T inputItem : inputSource) {
            Set<Sink<T, U>> routableSinks = networkSinks.stream()
                    .filter(sink -> routeFilter.test(inputItem, sink.getId()))
                    .collect(Collectors.toSet());
            sinksToSourceMap.computeIfAbsent(routableSinks, sinks -> new Source<>())
                    .getItems()
                    .add(inputItem);
        }

        for (Map.Entry<Set<Sink<T, U>>, Source<T, U>> sinksToSourceRoute : sinksToSourceMap.entrySet()) {
            Set<Sink<T, U>> sinksForThisSource = sinksToSourceRoute.getKey();
            Source<T, U> source = sinksToSourceRoute.getValue();
            for (Sink<T, U> sink : sinksForThisSource) {
                Pipe<T, U> pipe = new Pipe<>(source, sink);
                networkPipes.add(pipe);
            }
            networkSources.add(source);
        }

        FlowNetwork<T, U> network = new FlowNetwork<>(networkSources, networkPipes, networkSinks);

        BalancedMaxFlowOptimizer<T, U> optimizer = new BalancedMaxFlowOptimizer<>(network);
        optimizer.run();

        return network.getSinkContents();
    }
}
