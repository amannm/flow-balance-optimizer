package com.amannmalik.optimization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MaxFlowOptimizer<T, U> implements Runnable {

    private final int numNodes;
    private final int[][] capacity;
    private final int[][] flow;
    private final int sourceIndex;
    private final int sinkIndex;
    private final int[] excess;
    private final int[] height;
    private final int[] seen;

    private final Set<Pipe<T, U>> pipes;
    private final Map<Source<T, U>, Integer> sourceMap;
    private final Map<Sink<T, U>, Integer> sinkMap;

    public MaxFlowOptimizer(Set<Source<T, U>> sources, Set<Pipe<T, U>> pipes, Set<Sink<T, U>> sinks) {

        this.numNodes = sources.size() + sinks.size() + 2;

        this.capacity = new int[numNodes][];
        for (int i = 0; i < numNodes; i++) {
            capacity[i] = new int[numNodes];
        }

        this.flow = new int[numNodes][];
        for (int i = 0; i < numNodes; i++) {
            flow[i] = new int[numNodes];
        }

        AtomicInteger nextId = new AtomicInteger(2);

        sourceMap = new HashMap<>();
        for (Source<T, U> s : sources) {
            int id = nextId.getAndIncrement();
            sourceMap.put(s, id);
            this.capacity[0][id] = s.getAvailable();
        }

        sinkMap = new HashMap<>();
        for (Sink<T, U> s : sinks) {
            int id = nextId.getAndIncrement();
            sinkMap.put(s, id);
            this.capacity[id][1] = Math.max(s.getTarget() - s.getInitial(), 0);
        }

        for (Pipe<T, U> p : pipes) {
            int pipeSourceIndex = sourceMap.get(p.getSource());
            int pipeSinkIndex = sinkMap.get(p.getSink());
            this.capacity[pipeSourceIndex][pipeSinkIndex] = Integer.MAX_VALUE;
        }

        this.pipes = pipes;

        this.sourceIndex = 0;
        this.sinkIndex = 1;
        this.excess = new int[numNodes];
        this.height = new int[numNodes];
        this.seen = new int[numNodes];
    }

    private void push(int u, int v) {
        int send = Math.min(excess[u], capacity[u][v] - flow[u][v]);
        flow[u][v] += send;
        flow[v][u] -= send;
        excess[u] -= send;
        excess[v] += send;
    }

    private void relabel(int u) {
        int minHeight = Integer.MAX_VALUE;
        for (int v = 0; v < numNodes; v++) {
            if (capacity[u][v] - flow[u][v] > 0) {
                minHeight = Math.min(minHeight, height[v]);
                height[u] = minHeight + 1;
            }
        }
    }

    private boolean discharge(int u) {
        int initialHeight = height[u];
        while (excess[u] > 0) {
            if (seen[u] < numNodes) {
                int v = seen[u];
                if (capacity[u][v] - flow[u][v] > 0 && height[u] > height[v]) {
                    push(u, v);
                } else {
                    seen[u] += 1;
                }
            } else {
                relabel(u);
                seen[u] = 0;
            }
        }
        return height[u] > initialHeight;
    }

    @Override
    public void run() {
        int i;
        int z;

        //copy every node index but the supersource and supersink into a work queue
        int[] list = new int[numNodes - 2];
        for (i = 0, z = 0; i < numNodes; i++) {
            if ((i != sourceIndex) && (i != sinkIndex)) {
                list[z] = i;
                z++;
            }
        }

        //initial saturating push
        height[sourceIndex] = numNodes;
        excess[sourceIndex] = Integer.MAX_VALUE;
        for (i = 0; i < numNodes; i++) {
            push(sourceIndex, i);
        }

        //iteration across work queue
        for (int x = 0; x < list.length; x++) {
            int u = list[x];
            if (discharge(u)) {
                //moves node u to the front of the work queue and resets progress
                for (int n = x; n > 0; n--) {
                    list[n] = list[n - 1];
                }
                list[0] = u;
                x = 0;
            }
        }

        //translate
        for (Pipe<T, U> p : pipes) {
            int pipeSourceIndex = sourceMap.get(p.getSource());
            int pipeSinkIndex = sinkMap.get(p.getSink());
            int pipeFlow = flow[pipeSourceIndex][pipeSinkIndex];
            p.setFlow(pipeFlow);
        }
    }
}
