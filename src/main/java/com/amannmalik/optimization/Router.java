package com.amannmalik.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.toMap;

public class Router<T, U> {

    private Set<Source<T, U>> sources;
    private Set<Sink<T, U>> sinks;
    private Set<Pipe<T, U>> pipes;

    private BiPredicate<T, U> routeFilter;

    public Router() {
        this.routeFilter = (s, p) -> true;
    }

    public void build(Set<T> inputSource, Set<Sink<T, U>> inputSinks) {

        //a set of items are available for routing to a set of destinations that may vary in the different amount of items they require
        //a sets of rules that govern which items can be routed to which sinks
        //sinks have a unique string identifier and have some limit to how many items they can be routed
        sinks = inputSinks;

        //sources are discriminated by which set of sinks they can be routed to without violating a rule
        sources = new HashSet<>();
        Map<Set<Sink<T, U>>, Source<T, U>> sourceMap = new HashMap<>();
        for (T p : inputSource) {
            Set<Sink<T, U>> destinationSinks = new HashSet<>();
            for (Sink<T, U> vc : inputSinks) {
                if (routeFilter.test(p, vc.getId())) {
                    destinationSinks.add(vc);
                }
            }

            if (!sourceMap.containsKey(destinationSinks)) {
                Set<T> sourceSet = new HashSet<>(1);
                sourceSet.add(p);
                Source<T, U> newSource = new Source<T, U>(sourceSet);
                sources.add(newSource);
                sourceMap.put(destinationSinks, newSource);
            } else {
                Source<T, U> existingSource = sourceMap.get(destinationSinks);
                existingSource.getItems().add(p);
            }
        }

        //pipes are individual links from each source to every sink that source is capable of routing items to
        pipes = new HashSet<>();
        for (Map.Entry<Set<Sink<T, U>>, Source<T, U>> entry : sourceMap.entrySet()) {
            Set<Sink<T, U>> sinksForThisSource = entry.getKey();
            Source<T, U> source = entry.getValue();
            for (Sink<T, U> sink : sinksForThisSource) {
                Pipe<T, U> pipe = new Pipe<>(source, sink);
                pipes.add(pipe);
            }
        }

        //sources are independent entities that contain items and are linked to sinks via pipes
    }

    public void setRouteFilter(BiPredicate<T, U> filter) {
        routeFilter = filter;
    }

    public Map<U, Set<T>> route() {

        //optimize such that the maximum amount of items are routed to sinks
        MaxFlowOptimizer<T, U> pr = new MaxFlowOptimizer<>(sources, pipes, sinks);
        pr.run();

        //within the constraints of maximum flow, balance the amounts in each sink as much as possible
        FlowBalanceOptimizer<T, U> bal = new FlowBalanceOptimizer<>(sources);
        bal.run();

        //execute the transfer of data from source to sink based on the computed flow amounts on each pipe
        pipes.forEach(Pipe::transferFlow);

        //gather the routing results by sink
        return sinks.stream().collect(toMap(Sink::getId, Sink::getItems));
    }
}
