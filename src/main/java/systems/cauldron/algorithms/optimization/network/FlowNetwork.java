package systems.cauldron.algorithms.optimization.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

/**
 * Created by amannmalik on 12/21/16.
 * <p>
 */
@RequiredArgsConstructor
@Getter
public class FlowNetwork<T, U> {

    private final Set<Source<T, U>> sources;
    private final Set<Pipe<T, U>> pipes;
    private final Set<Sink<T, U>> sinks;

    public Map<U, Set<T>> getSinkContents() {
        return sinks.stream()
                .collect(toMap(Sink::getId, Sink::getItems));
    }

    public void transferPipeFlow() {
        pipes.forEach(Pipe::transferFlow);
    }
}
