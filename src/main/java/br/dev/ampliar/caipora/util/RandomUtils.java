package br.dev.ampliar.caipora.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Random helpers for the development data loaders.
 */
@Component
public class RandomUtils {

    private final RandomGenerator random = RandomGenerator.getDefault();

    public <T> T pickOne(final List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public <T> List<T> createRandomSublist(final List<T> list, final int n) {
        final List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    public <T extends Enum<?>> T pickRandomEnumValue(final Class<T> enumClass) {
        final T[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    public boolean pickRandomBoolean() {
        return random.nextBoolean();
    }

    /**
     * @return true with the given probability (0..1)
     */
    public boolean chance(final double probability) {
        return random.nextDouble() < probability;
    }

    public int nextInt(final int origin, final int bound) {
        return random.nextInt(origin, bound);
    }

    public LocalDate getRandomDate(final LocalDate start, final LocalDate end) {
        return start.plusDays(random.nextLong(0, end.toEpochDay() - start.toEpochDay() + 1));
    }
}
