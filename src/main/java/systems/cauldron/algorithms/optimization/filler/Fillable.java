package systems.cauldron.algorithms.optimization.filler;

public interface Fillable<U> {
    U getItem();

    int getCurrentCount();

    int getMaximumCount();
}
