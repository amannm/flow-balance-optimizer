package com.amannmalik.optimization;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


//TODO: needs to be looked at differently, double checked for non-converging states

class FlowBalanceOptimizer<T, U> implements Runnable {

    private Set<Source<T, U>> sources;

    public FlowBalanceOptimizer(FlowNetwork<T, U> network) {
        this.sources = network.getSources();
    }

    @Override
    public void run() {
        //a reference to see the state of flow per pipe
        Map<Pipe<T, U>, Integer> pipeFlow = sources.stream()
                .flatMap(s -> s.getOutputs().stream())
                .collect(Collectors.toMap(Function.identity(), Pipe::getFlow));

        while (!sources.isEmpty()) {
            sources.forEach(this::balance);

            //TODO: this might be inefficient. sources represents the set of sources that need to be processed
            sources = pipeFlow.entrySet().stream()
                    .filter(e -> e.getKey().getFlow() != e.getValue())
                    .peek(e -> e.setValue(e.getKey().getFlow()))
                    .map(Map.Entry::getKey)
                    .flatMap(v -> v.getSink().getInputs().stream().filter(i -> i != v))
                    .map(Pipe::getSource)
                    .collect(Collectors.toSet());
        }
    }

    private void balance(Source<T, U> s) {
        Set<Pipe<T, U>> outputs = s.getOutputs();
        if (!outputs.isEmpty()) {

            //remove all flow committed to all pipes from this source
            int totalOutput = outputs.stream().mapToInt(Pipe::getAndClearFlow).sum();

            //create proxy objects for each pipe from this source
            Map<Bar, Pipe<T, U>> bars = new HashMap<>();
            for (Pipe<T, U> pipe : outputs) {
                int initialHeight = pipe.getSinkInitial() + pipe.getSinkFlow();
                int maxHeight = pipe.getSinkTarget();
                Bar bar = new Bar(initialHeight, maxHeight);
                bars.put(bar, pipe);
            }

            //compute balanced values
            BarBalancer barBalancer = new BarBalancer(bars.keySet(), totalOutput);
            barBalancer.run();

            //commit balanced flow values
            bars.forEach((k, v) -> {
                v.setFlow(k.getAllocation());
            });

        }

    }


}
