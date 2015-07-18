/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author amann.malik
 */
public class FlowBalanceOptimizer<T, U> implements Runnable {

    private Set<Source<T, U>> sources;

    public FlowBalanceOptimizer(Set<Source<T, U>> sources) {
        this.sources = sources;
    }

    @Override
    public void run() {
        //a reference to see the state of flow per pipe
        Map<Pipe<T, U>, Integer> pipeFlow = sources.stream()
                .flatMap(s -> s.getOutputs().stream())
                .collect(Collectors.toMap(k -> k, k -> k.getFlow()));

        while (!sources.isEmpty()) {
            sources.forEach(this::balance);

            //this might be inefficient. sources represents the set of sources that need to be processed
            pipeFlow.entrySet().stream()
                    .filter(e -> e.getKey().getFlow() != e.getValue())
                    .peek(e -> e.setValue(e.getKey().getFlow()))
                    .map(x -> x.getKey())
                    .flatMap(v -> v.getSink().getInputs().stream().filter(i -> i != v))
                    .map(p -> ((Pipe<T, U>) p).getSource())
                    .collect(Collectors.toSet());
        }
    }

    private void balance(Source<T, U> s) {
        Set<Pipe<T, U>> outputs = s.getOutputs();
        if (!outputs.isEmpty()) {

            //remove all flow committed to all pipes from this source
            int totalOutput = 0;
            for (Pipe<T, U> o : outputs) {
                int flow = o.getFlow();
                totalOutput += flow;
                o.setFlow(0);
            }

            //create proxy objects for each pipe from this source
            Map<Bar, Pipe<T, U>> bars = outputs.stream().collect(Collectors.toMap(Bar::new, k -> k));

            //compute balanced values
            balanceSet(bars.keySet(), totalOutput);

            //commit balanced flow values
            bars.forEach((k, v) -> {
                v.setFlow(k.getAllocation());
            });

        }

    }

    public void balanceSet(Collection<Bar> bars, int avail) {
        AtomicInteger sourceAvailable = new AtomicInteger(avail);
        List<Bar> range = new ArrayList<>(bars);
        range.sort((a, b) -> Integer.compare(a.currentHeight, b.currentHeight));
        int current = 0;
        while (current < range.size() - 1) {
            Bar currentBar = range.get(current);
            Bar nextBar = range.get(++current);
            int equalizingHeight = nextBar.currentHeight;
            int maxHeight = currentBar.currentHeight + (sourceAvailable.get() / current);
            if (maxHeight < equalizingHeight) {
                current -= floodWindow(range, sourceAvailable, maxHeight, current - 1);
                topOff(range, sourceAvailable);
                return;
            } else {
                current -= floodWindow(range, sourceAvailable, equalizingHeight, current - 1);
            }
        }
        pump(range, sourceAvailable);
    }

    private int floodWindow(List<Bar> range, AtomicInteger available, int targetHeight, int startIndex) {
        Bar newBar = range.remove(startIndex);
        boolean newBarSaturated = tryFlood(available, newBar, targetHeight);
        int removed = 0;
        Bar bar = null;
        while (--startIndex >= 0) {
            bar = range.get(startIndex);
            boolean barSaturated = tryFlood(available, bar, targetHeight);
            if (barSaturated) {
                range.remove(startIndex);
                removed++;
            } else {
                break;
            }
        }
        if (!newBarSaturated) {
            if (bar == null) {
                range.add(0, newBar);
                return removed;
            }
            int insertionIndex = 0;
            if (bar.maxHeight > newBar.maxHeight) {
                insertionIndex = startIndex + 1;
            } else {
                while (--startIndex >= 0) {
                    bar = range.get(startIndex);
                    doFlood(available, bar, targetHeight);
                    if (bar.maxHeight > newBar.maxHeight) {
                        insertionIndex = startIndex + 1;
                        break;
                    }
                }
            }
            range.add(insertionIndex, newBar);
        }
        while (--startIndex >= 0) {
            bar = range.get(startIndex);
            doFlood(available, bar, targetHeight);
        }
        return removed;
    }

    private boolean tryFlood(AtomicInteger available, Bar bar, int targetHeight) {
        int targetIncrease = targetHeight - bar.currentHeight;
        int maxIncrease = bar.maxHeight - bar.currentHeight;
        if (targetIncrease < maxIncrease) {
            bar.currentHeight += targetIncrease;
            available.updateAndGet(k -> k - targetIncrease);
            return false;
        } else {
            bar.currentHeight += maxIncrease;
            available.updateAndGet(k -> k - maxIncrease);
            return true;
        }
    }

    private void doFlood(AtomicInteger available, Bar bar, int targetHeight) {
        int targetIncrease = targetHeight - bar.currentHeight;
        bar.currentHeight += targetIncrease;
        available.updateAndGet(k -> k - targetIncrease);
    }

    private void pump(List<Bar> range, AtomicInteger available) {
        ListIterator<Bar> iter = range.listIterator(range.size() - 1);
        while (iter.hasPrevious()) {
            Bar bar = iter.previous();

            int equalizingIncrease = bar.maxHeight - bar.currentHeight;
            int maxIncrease = available.get() / range.size();

            if (maxIncrease < equalizingIncrease) {
                bar.currentHeight += maxIncrease;
                available.updateAndGet(k -> k - maxIncrease);
                pumpBackward(iter, available, maxIncrease);
                break;
            } else {
                bar.currentHeight += equalizingIncrease;
                available.updateAndGet(k -> k - equalizingIncrease);
                iter.remove();
                pumpBackward(iter, available, equalizingIncrease);
                iter = range.listIterator(range.size() - 1);
            }
        }
        topOff(range, available);
    }

    private void pumpBackward(ListIterator<Bar> iter, AtomicInteger available, int targetIncrease) {
        while (iter.hasPrevious()) {
            Bar bar = iter.previous();
            bar.currentHeight += targetIncrease;
            available.updateAndGet(k -> k - targetIncrease);
        }
    }

    private void topOff(List<Bar> range, AtomicInteger available) {
        //this is very inefficient but I can't think right now
        while (available.get() > 0 && !range.isEmpty()) {
            range.sort((a, b) -> Integer.compare(a.currentHeight, b.currentHeight));
            Bar curb = range.get(0);
            if (curb.currentHeight == curb.maxHeight) {
                range.remove(0);
            } else {
                curb.currentHeight++;
                available.decrementAndGet();
            }
        }
    }

    public class Bar {

        public int initialHeight;
        public int currentHeight;
        public int maxHeight;


        public Bar(Pipe src) {
            this.initialHeight = src.getSinkInitial() + src.getSinkFlow();
            this.currentHeight = this.initialHeight;
            this.maxHeight = src.getSinkTarget();
        }

        public int getAllocation() {
            return this.currentHeight - this.initialHeight;
        }

        @Override
        public String toString() {
            return "Bar{" + currentHeight + "/" + maxHeight + '}';
        }

        public void print(List<Bar> range, String message) {
            System.out.print("*** " + message + "\n");
            for (Bar b : range) {
                System.out.printf("\t%3d/%3d\t", b.currentHeight, b.maxHeight);
                for (int i = 0; i < b.maxHeight; i++) {
                    if (i == b.currentHeight - 1) {
                        System.out.print("|");
                    } else {
                        System.out.print("-");
                    }
                }
                System.out.print("\n");
            }
            System.out.print("***\n");
        }

    }

}
