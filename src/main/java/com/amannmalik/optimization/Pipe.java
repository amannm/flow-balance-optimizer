package com.amannmalik.optimization;

import java.util.Iterator;
import java.util.Set;

public class Pipe<T,U> {

    private final Source source;
    private final Sink sink;
    private int flow;

    public Pipe(Source source, Sink sink) {
        this.source = source;
        this.sink = sink;
        this.flow = 0;
        //this is bad
        source.getOutputs().add(this);
        sink.getInputs().add(this);
    }

    public Source<T, U> getSource() {
        return source;
    }

    public Sink<T, U> getSink() {
        return sink;
    }

    public int getSinkFlow() {
        return sink.getFlow();
    }
    public int getSinkInitial() {
        return sink.getInitial();
    }
    public int getSinkTarget() {
        return sink.getTarget();
    }

    public void setFlow(int amount) {
        flow = amount;
    }

    public int getFlow() {
        return flow;
    }

    public void transferFlow() {
        //this is weird
        Set<T> sinkContainer = sink.getItems();
        Iterator<T> iter = source.getItems().iterator();
        while (flow-- > 0) {
            sinkContainer.add(iter.next());
            iter.remove();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pipe<?, ?> pipe = (Pipe<?, ?>) o;

        if (!source.equals(pipe.source)) return false;
        return sink.equals(pipe.sink);

    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + sink.hashCode();
        return result;
    }
}
