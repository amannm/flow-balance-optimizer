package systems.cauldron.algorithms.optimization;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import systems.cauldron.algorithms.optimization.filler.Fillable;
import systems.cauldron.algorithms.optimization.filler.Filler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by amannmalik on 12/21/16.
 */
public class FillerTest {

    private static final Logger LOG = LogManager.getLogger(FillerTest.class.getSimpleName());

    @Test
    public void basicTest() {

        List<Ration> rations = new ArrayList<>();
        IntStream.range(0, 4).forEach(i -> rations.add(new Ration(UUID.randomUUID(), Ration.Type.BEEF_STEW)));
        IntStream.range(0, 3).forEach(i -> rations.add(new Ration(UUID.randomUUID(), Ration.Type.CHEESE_PIZZA)));
        IntStream.range(0, 3).forEach(i -> rations.add(new Ration(UUID.randomUUID(), Ration.Type.SPAGHETTI)));

        List<Fillable<Person>> people = List.of(
                new FillablePerson(new Person("Frank", Person.DietaryRestriction.NONE), 4, 10),
                new FillablePerson(new Person("Pete", Person.DietaryRestriction.VEGAN), 2, 6),
                new FillablePerson(new Person("Susan", Person.DietaryRestriction.VEGETARIAN), 6, 8),
                new FillablePerson(new Person("Cheryl", Person.DietaryRestriction.NONE), 0, 15)
        );

        Map<Person, Set<Ration>> result = Filler.assign(new HashSet<>(rations), FillerTest::isAllowed, people);

        people.forEach(fillablePerson -> {
            Person person = fillablePerson.getItem();
            Set<Ration> assignedRations = result.get(person);
            LOG.info("{} has restriction {}, started with {} rations, has capacity {}", person.getName(), person.getDietaryRestriction(), fillablePerson.getCurrentCount(), fillablePerson.getMaximumCount());
            TreeMap<Ration.Type, Long> assignedRationsByType = assignedRations.stream()
                    .collect(Collectors.groupingBy(Ration::getType, TreeMap::new, Collectors.counting()));
            assignedRationsByType.forEach((type, amount) -> {
                LOG.info("\twas assigned {} {} rations", amount, type);
            });
            int addedCount = assignedRations.size();
            LOG.info("\t\tnow has {} rations", fillablePerson.getCurrentCount() + addedCount);
        });

        result.forEach((person, assignedRations) -> assertTrue(assignedRations.stream().allMatch(ration -> isAllowed(ration, person))));

        List<Integer> totals = people.stream().map(fillablePerson -> {
            Person person = fillablePerson.getItem();
            Set<Ration> assignedRations = result.get(person);
            return fillablePerson.getCurrentCount() + assignedRations.size();
        }).collect(Collectors.toList());
        assertFalse(totals.isEmpty());

        int max = totals.stream().mapToInt(Integer::intValue).max().getAsInt();
        int min = totals.stream().mapToInt(Integer::intValue).min().getAsInt();
        assertTrue(max - min <= 1);
    }

    public static boolean isAllowed(Ration r, Person p) {
        switch (r.getType()) {
            case BEEF_STEW:
                switch (p.getDietaryRestriction()) {
                    case VEGAN:
                    case VEGETARIAN:
                        return false;
                    case NONE:
                        return true;
                    default:
                        throw new AssertionError();
                }
            case CHEESE_PIZZA:
                switch (p.getDietaryRestriction()) {
                    case VEGAN:
                        return false;
                    case VEGETARIAN:
                    case NONE:
                        return true;
                    default:
                        throw new AssertionError();
                }
            case SPAGHETTI:
                switch (p.getDietaryRestriction()) {
                    case VEGAN:
                    case VEGETARIAN:
                    case NONE:
                        return true;
                    default:
                        throw new AssertionError();
                }
            default:
                throw new AssertionError();
        }
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Getter
    public static class Ration {

        public enum Type {
            BEEF_STEW,
            CHEESE_PIZZA,
            SPAGHETTI
        }

        private final UUID id;

        @EqualsAndHashCode.Exclude
        private final Type type;

    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Getter
    public static class Person {

        public enum DietaryRestriction {
            VEGAN,
            VEGETARIAN,
            NONE
        }

        private final String name;

        @EqualsAndHashCode.Exclude
        private final DietaryRestriction dietaryRestriction;

    }

    @RequiredArgsConstructor
    public static class FillablePerson implements Fillable<Person> {

        private final Person person;
        private final int currentRationCount;
        private final int maxRationCount;

        @Override
        public Person getItem() {
            return person;
        }

        @Override
        public int getCurrentCount() {
            return currentRationCount;
        }

        @Override
        public int getMaximumCount() {
            return maxRationCount;
        }
    }
}
