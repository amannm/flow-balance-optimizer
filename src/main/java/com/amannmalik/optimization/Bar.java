package com.amannmalik.optimization;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Amann Malik (amannmalik@gmail.com) on 7/17/2015.
 */
public class Bar {

    private int initialHeight;
    public int currentHeight;
    public int maxHeight;

    public Bar(int initialHeight, int maxHeight) {
        this.initialHeight = initialHeight;
        this.currentHeight = initialHeight;
        this.maxHeight = maxHeight;
    }

    public Bar(Pipe src) {
        this.initialHeight = src.getSinkInitial() + src.getSinkFlow();
        this.currentHeight = this.initialHeight;
        this.maxHeight = src.getSinkTarget();
    }

    public int getAllocation() {
        return this.currentHeight - this.initialHeight;
    }

    public boolean tryFlood(AtomicInteger available, int targetHeight) {
        int targetIncrease = targetHeight - currentHeight;
        int maxIncrease = maxHeight - currentHeight;
        if (targetIncrease < maxIncrease) {
            currentHeight += targetIncrease;
            available.updateAndGet(k -> k - targetIncrease);
            return false;
        } else {
            currentHeight += maxIncrease;
            available.updateAndGet(k -> k - maxIncrease);
            return true;
        }
    }

    public void doFlood(AtomicInteger available, int targetHeight) {
        int targetIncrease = targetHeight - currentHeight;
        currentHeight += targetIncrease;
        available.updateAndGet(k -> k - targetIncrease);
    }
}
