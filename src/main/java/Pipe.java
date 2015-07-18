/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author amann.malik
 */
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

    public Source getSource() {
        return source;
    }

    public Sink getSink() {
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
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.source);
        hash = 83 * hash + Objects.hashCode(this.sink);
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
        final Pipe other = (Pipe) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.sink, other.sink)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pipe{" + "source=" + source.getAvailable() + ", sink=" + sink.getId() + ", flow=" + flow + '}';
    }
}
