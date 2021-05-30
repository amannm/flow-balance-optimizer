package systems.cauldron.algorithms.optimization;

import java.util.Iterator;
import java.util.Set;

class Pipe<T, U> {

    private final Source<T, U> source;
    private final Sink<T, U> sink;
    private int flow;

    public Pipe(Source<T, U> source, Sink<T, U> sink) {
        this.source = source;
        this.sink = sink;
        this.flow = 0;
        //TODO: this is bad
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
        //TODO: this is weird
        Set<T> sinkContainer = sink.getItems();
        Iterator<T> iter = source.getItems().iterator();
        while (flow-- > 0) {
            sinkContainer.add(iter.next());
            iter.remove();
        }
    }

    public int getAndClearFlow() {
        int currentFlow = flow;
        flow = 0;
        return currentFlow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pipe<?, ?> pipe = (Pipe<?, ?>) o;

        return source.equals(pipe.source) && sink.equals(pipe.sink);

    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + sink.hashCode();
        return result;
    }
}
