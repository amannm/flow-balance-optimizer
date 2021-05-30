package systems.cauldron.algorithms.optimization.optimizer;


import systems.cauldron.algorithms.optimization.network.FlowNetwork;

/**
 * Created by amannmalik on 12/21/16.
 */
public class BalancedMaxFlowOptimizer<T, U> implements Runnable {

    private final FlowNetwork<T, U> network;

    public BalancedMaxFlowOptimizer(FlowNetwork<T, U> network) {
        this.network = network;
    }

    @Override
    public void run() {

        //optimize such that the maximum amount of items are routed to sinks
        MaxFlowOptimizer<T, U> pr = new MaxFlowOptimizer<>(network);
        pr.run();

        //within the constraints of maximum flow, balance the amounts in each sink as much as possible
        FlowBalanceOptimizer<T, U> bal = new FlowBalanceOptimizer<>(network);
        bal.run();

        //execute the transfer of data from source to sink based on the computed flow amounts on each pipe
        network.transferPipeFlow();
    }
}
