package systems.cauldron.algorithms.optimization.allocation;

public interface Allocatable<U> {
    U getTarget();

    int getCurrentCount();

    int getMaximumCount();
}
