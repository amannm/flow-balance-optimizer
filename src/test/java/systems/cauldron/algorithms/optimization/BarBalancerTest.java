package systems.cauldron.algorithms.optimization;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Amann Malik (amannmalik@gmail.com) on 7/17/2015.
 */
public class BarBalancerTest {

    @Test
    public void balance_random_bars_nothing() {
        int available = 0;
        List<Bar> randomBars = createRandomBars(20);
        printBar(randomBars, "Before Nothing");
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(randomBars, "After Nothing");
    }

    @Test
    public void balance_random_bars_deficit() {
        int available = 100;
        List<Bar> randomBars = createRandomBars(20);
        printBar(randomBars, "Before Deficit");
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(randomBars, "After Deficit");
    }

    @Test
    public void balance_random_bars_surplus() {
        int available = 10000;
        List<Bar> randomBars = createRandomBars(20);
        printBar(randomBars, "Before Surplus");
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(randomBars, "After Surplus");
    }


    public static List<Bar> createRandomBars(int size) {
        List<Bar> result = new ArrayList<>();
        while (size-- > 0) {
            Random random = new Random();
            int initialHeight = random.nextInt(50);
            int maxHeight = initialHeight + random.nextInt(50);
            result.add(new Bar(initialHeight, maxHeight));
        }
        return result;
    }

    public static void printBar(List<Bar> range, String message) {
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
