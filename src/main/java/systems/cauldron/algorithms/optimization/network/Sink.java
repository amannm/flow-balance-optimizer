package systems.cauldron.algorithms.optimization.network;

import systems.cauldron.algorithms.optimization.filler.Fillable;

import java.util.HashSet;
import java.util.Set;

public class Sink<T, U> {

    private final U id;
    private final Set<T> items;
    private final int target;
    private final int initial;
    private final Set<Pipe<T, U>> inputs;

    public Sink(Fillable<U> fillable) {
        this.id = fillable.getItem();
        this.items = new HashSet<>();
        this.initial = fillable.getCurrentCount();
        this.target = fillable.getMaximumCount();
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

    Set<T> getItems() {
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
}
