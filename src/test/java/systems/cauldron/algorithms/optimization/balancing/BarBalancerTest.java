package systems.cauldron.algorithms.optimization.balancing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Amann Malik (amannmalik@gmail.com) on 7/17/2015.
 */
public class BarBalancerTest {

    private static final Logger LOG = LogManager.getLogger(BarBalancerTest.class.getSimpleName());

    @Test
    public void balance_random_bars_nothing() {
        int available = 0;
        List<Bar> randomBars = createRandomBars(20);
        List<Bar> barsSnapshot = snapshotBars(randomBars);
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(barsSnapshot, "Before balancing with none available");
        printBar(randomBars, "After balancing with none available");
        int added = getDiffSum(barsSnapshot, randomBars);
        assertEquals(0, added);
        List<Bar> changedBarsNotAtCapacity = getChangedBarsNotAtCapacity(barsSnapshot, randomBars);
        assertTrue(changedBarsNotAtCapacity.isEmpty());
    }

    @Test
    public void balance_random_bars_deficit() {
        int available = 200;
        List<Bar> randomBars = createRandomBars(20);
        List<Bar> barsSnapshot = snapshotBars(randomBars);
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(barsSnapshot, "Before balancing with a deficit available");
        printBar(randomBars, "After balancing with a deficit available");
        int added = getDiffSum(barsSnapshot, randomBars);
        assertEquals(added, available);
        List<Bar> changedBarsNotAtCapacity = getChangedBarsNotAtCapacity(barsSnapshot, randomBars);
        int expectedValue = changedBarsNotAtCapacity.stream().mapToInt(bar -> bar.currentHeight).min().orElseThrow();
        assertTrue(changedBarsNotAtCapacity.stream().allMatch(bar -> bar.currentHeight == expectedValue || bar.currentHeight == expectedValue + 1));
    }

    @Test
    public void balance_random_bars_surplus() {
        int available = 10000;
        List<Bar> randomBars = createRandomBars(20);
        List<Bar> barsSnapshot = snapshotBars(randomBars);
        BarBalancer balancer = new BarBalancer(randomBars, available);
        balancer.run();
        printBar(barsSnapshot, "Before balancing with a surplus available");
        printBar(randomBars, "After balancing with a surplus available");
        int added = getDiffSum(barsSnapshot, randomBars);
        assertTrue(added < available);
        List<Bar> changedBarsNotAtCapacity = getChangedBarsNotAtCapacity(barsSnapshot, randomBars);
        assertTrue(changedBarsNotAtCapacity.isEmpty());
    }

    public static List<Bar> createRandomBars(int size) {
        List<Bar> result = new ArrayList<>();
        while (size-- > 0) {
            Random random = new Random();
            int initialHeight = random.nextInt(50);
            int maxHeight = initialHeight + random.nextInt(50);
            result.add(new Bar(initialHeight, maxHeight));
        }
        result.sort(Comparator.comparingInt(a -> a.maxHeight));
        return result;
    }

    public static List<Bar> snapshotBars(List<Bar> bars) {
        return bars.stream().map(bar -> new Bar(bar.currentHeight, bar.maxHeight)).collect(Collectors.toList());
    }

    public List<Bar> getChangedBarsNotAtCapacity(List<Bar> previous, List<Bar> next) {
        assert previous.size() == next.size();
        List<Bar> results = new ArrayList<>();
        for (int i = 0; i < previous.size(); i++) {
            Bar previousBar = previous.get(i);
            Bar nextBar = next.get(i);
            if (previousBar.currentHeight != nextBar.currentHeight) {
                if (nextBar.currentHeight != nextBar.maxHeight) {
                    results.add(nextBar);
                }
            }
        }
        return results;
    }

    public static int getDiffSum(List<Bar> previous, List<Bar> next) {
        int previousSum = previous.stream().mapToInt(bar -> bar.currentHeight).sum();
        int nextSum = next.stream().mapToInt(bar -> bar.currentHeight).sum();
        return nextSum - previousSum;
    }

    public static void printBar(List<Bar> range, String message) {
        LOG.info("*** " + message);
        for (Bar b : range) {
            StringBuilder sb = new StringBuilder(String.format("\t%3d/%3d\t", b.currentHeight, b.maxHeight));
            for (int i = 0; i < b.maxHeight; i++) {
                if (i == b.currentHeight - 1) {
                    sb.append('|');
                } else {
                    sb.append('-');
                }
            }
            LOG.info(sb);
        }
        LOG.info("***");
    }
}
