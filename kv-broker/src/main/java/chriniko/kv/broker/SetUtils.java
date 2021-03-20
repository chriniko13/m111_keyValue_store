package chriniko.kv.broker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SetUtils {


    public static <T> Set<T> pickN(int n, Set<T> s) {

        if (n < 1 || n > s.size()) {
            throw new IllegalArgumentException();
        }

        final Set<T> selected = new HashSet<>();

        while (selected.size() < n) {

            int randomIdx = ThreadLocalRandom.current().nextInt(s.size());

            Iterator<T> iterator = s.iterator();
            T selection;

            int i = 0;
            while (iterator.hasNext() && selected.size() < n) {
                selection = iterator.next();

                if (i == randomIdx) {
                    selected.add(selection);
                }

            }
        }

        return selected;
    }


    public static <T> T pickOneRandomly(Set<T> s) {

        int randomIdx = ThreadLocalRandom.current().nextInt(s.size());

        Iterator<T> iterator = s.iterator();
        T selection;

        int i = 0;
        while (iterator.hasNext()) {
            selection = iterator.next();

            if (i++ == randomIdx) {
                return selection;
            }

        }

        throw new IllegalStateException();
    }
}
