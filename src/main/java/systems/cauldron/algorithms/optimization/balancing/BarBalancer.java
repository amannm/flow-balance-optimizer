package systems.cauldron.algorithms.optimization.balancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Amann Malik (amannmalik@gmail.com) on 7/17/2015.
 */
public class BarBalancer implements Runnable {

    private final List<Bar> range;

    //TODO: remember what my thread safe aspirations were exactly...
    private final AtomicInteger available;

    public BarBalancer(Collection<Bar> bars, int avail) {
        range = new ArrayList<>(bars);
        range.sort(Comparator.comparingInt(a -> a.currentHeight));
        available = new AtomicInteger(avail);
    }

    public void run() {
        if (flood()) {
            pump();
        }
    }

    private boolean flood() {
        int current = 0;
        while (current < range.size() - 1) {
            Bar currentBar = range.get(current);
            Bar nextBar = range.get(++current);
            int equalizingHeight = nextBar.currentHeight;
            int maxHeight = currentBar.currentHeight + (available.get() / current);
            if (maxHeight < equalizingHeight) {
                topOff();
                return false;
            } else {
                current -= floodWindow(equalizingHeight, current - 1);
            }
        }
        return true;
    }

    private int floodWindow(int targetHeight, int startIndex) {

        Bar newBar = range.remove(startIndex);
        boolean newBarSaturated = newBar.tryFlood(available, targetHeight);
        int removed = 0;
        Bar bar = null;
        while (--startIndex >= 0) {
            bar = range.get(startIndex);
            boolean barSaturated = bar.tryFlood(available, targetHeight);
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
                    bar.doFlood(available, targetHeight);
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
            bar.doFlood(available, targetHeight);
        }

        return removed;
    }


    private void pump() {
        ListIterator<Bar> iter = range.listIterator(range.size() - 1);
        while (iter.hasPrevious()) {
            Bar bar = iter.previous();

            int equalizingIncrease = bar.maxHeight - bar.currentHeight;
            int maxIncrease = available.get() / range.size();

            if (maxIncrease < equalizingIncrease) {
                bar.currentHeight += maxIncrease;
                available.updateAndGet(k -> k - maxIncrease);
                pumpBackward(iter, maxIncrease);
                break;
            } else {
                bar.currentHeight += equalizingIncrease;
                available.updateAndGet(k -> k - equalizingIncrease);
                iter.remove();
                pumpBackward(iter, equalizingIncrease);
                iter = range.listIterator(range.size() - 1);
            }
        }
        topOff();
    }

    private void pumpBackward(ListIterator<Bar> iter, int targetIncrease) {
        while (iter.hasPrevious()) {
            Bar bar = iter.previous();
            bar.currentHeight += targetIncrease;
            available.updateAndGet(k -> k - targetIncrease);
        }
    }

    private void topOff() {
        while (available.get() > 0 && !range.isEmpty()) {
            range.sort(Comparator.comparingInt(a -> a.currentHeight));
            Bar curb = range.get(0);
            if (curb.currentHeight == curb.maxHeight) {
                range.remove(0);
            } else {
                curb.currentHeight++;
                available.decrementAndGet();
            }
        }
    }
}
