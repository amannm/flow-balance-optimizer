package com.amannmalik.optimization;

import java.util.HashSet;
import java.util.Set;

public class Source<T,U> {

    private final Set<T> items;
    private final Set<Pipe<T,U>> outputs;

    public Source(Set<T> container) {
        this.items = container;
        this.outputs = new HashSet<>();
    }

    public int getAvailable() {
        return items.size();
    }

    public Set<Pipe<T,U>> getOutputs() {
        return outputs;
    }
    
    public Set<T> getItems() {
        return items;
    }

}
