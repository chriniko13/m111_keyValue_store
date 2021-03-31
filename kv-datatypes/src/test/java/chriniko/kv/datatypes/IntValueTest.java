package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntValueTest {


    @Test
    void constructWorksAsExpected() {

        // given
        IntValue intValue = new IntValue("age", 28);

        // when
        String asString = intValue.asString();

        // then
        assertEquals("{\"age\" : 28}", asString);
        assertEquals("\"age\" : 28", intValue.asStringUnwrapped());
        assertEquals(28, intValue.getValue());

    }
}