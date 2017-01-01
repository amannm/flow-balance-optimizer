package com.amannmalik.optimization;

import java.util.HashSet;
import java.util.Set;

class Sink<T, U> {

    private final U id;
    private final Set<T> items;
    private final int target;
    private final int initial;
    private final Set<Pipe<T, U>> inputs;

    public Sink(U id, int initial, int target) {
        this.id = id;
        this.items = new HashSet<>();
        this.initial = initial;
        this.target = target;
        this.inputs = new HashSet<>();
    }

    public int getInitial() {
        return initial;
    }

    public int getTarget() {
        return target;
    }

    public Set<Pipe<T, U>> getInputs() {
        return inputs;
    }

    public int getFlow() {
        return inputs.stream().mapToInt(Pipe::getFlow).sum();
    }

    public U getId() {
        return id;
    }

    public Set<T> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sink<?, ?> sink = (Sink<?, ?>) o;

        return id.equals(sink.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Sink{" + "id=" + id + ", items=" + items.size() + ", target=" + target + ", initial=" + initial + ", inputs=" + inputs.size() + '}';
    }
}
