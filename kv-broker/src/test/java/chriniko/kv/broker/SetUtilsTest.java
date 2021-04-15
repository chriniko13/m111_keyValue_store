package chriniko.kv.broker;

import chriniko.kv.broker.infra.SetUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SetUtilsTest {

    @Test
    void pickNWorkAsExpected() {

        // given
        Set<Integer> s = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);


        // when
        Set<Integer> result = SetUtils.pickN(4, s);


        // then
        assertEquals(4, result.size());


        // when
        result = SetUtils.pickN(1, s);


        // then
        assertEquals(1, result.size());

        // when
        result = SetUtils.pickN(10, s);


        // then
        assertEquals(10, result.size());

    }


    @Test
    void pickOneRandomlyWorkAsExpected() {

        // given
        Set<Integer> s = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);


        for (int i=0; i<s.size(); i++) {
            // when
            Integer result = SetUtils.pickOneRandomly(s);


            // then
            assertNotNull(result);
        }

    }
}