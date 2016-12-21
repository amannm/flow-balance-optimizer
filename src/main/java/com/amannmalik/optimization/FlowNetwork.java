package com.amannmalik.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.toMap;

/**
 * Created by amannmalik on 12/21/16.
 */
public class FlowNetwork<T, U> {

    private final Set<Source<T, U>> sources = new HashSet<>();
    private final Set<Sink<T, U>> sinks = new HashSet<>();
    private final Set<Pipe<T, U>> pipes = new HashSet<>();


    public void FlowNetwork(Set<T> inputSource, BiPredicate<T, U> routeFilter) {

        //a set of items are available for routing to a set of destinations that may vary in the different amount of items they require
        //a sets of rules that govern which items can be routed to which sinks
        //sinks have a unique string identifier and have some limit to how many items they can be routed

        //sources are discriminated by which set of sinks they can be routed to without violating a rule

        Map<Set<Sink<T, U>>, Source<T, U>> sourceMap = new HashMap<>();
        for (T p : inputSource) {
            Set<Sink<T, U>> destinationSinks = new HashSet<>();
            for (Sink<T, U> vc : sinks) {
                if (routeFilter.test(p, vc.getId())) {
                    destinationSinks.add(vc);
                }
            }

            if (!sourceMap.containsKey(destinationSinks)) {
                Set<T> sourceSet = new HashSet<>(1);
                sourceSet.add(p);
                Source<T, U> newSource = new Source<>(sourceSet);
                sources.add(newSource);
                sourceMap.put(destinationSinks, newSource);
            } else {
                Source<T, U> existingSource = sourceMap.get(destinationSinks);
                existingSource.getItems().add(p);
            }
        }

        //pipes are individual links from each source to every sink that source is capable of routing items to
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


    public Map<U, Set<T>> getSinkContents() {
        //gather the routing results by sink
        return sinks.stream().collect(toMap(Sink::getId, Sink::getItems));
    }

    public void transferPipeFlow() {
        pipes.forEach(Pipe::transferFlow);
    }

    public Set<Source<T, U>> getSources() {
        return sources;
    }

    public Set<Sink<T, U>> getSinks() {
        return sinks;
    }

    public Set<Pipe<T, U>> getPipes() {
        return pipes;
    }
}
