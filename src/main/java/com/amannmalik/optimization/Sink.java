package com.amannmalik.optimization;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Sink<T, U> {

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
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sink other = (Sink) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Sink{" + "id=" + id + ", items=" + items.size() + ", target=" + target + ", initial=" + initial + ", inputs=" + inputs.size() + '}';
    }
}
