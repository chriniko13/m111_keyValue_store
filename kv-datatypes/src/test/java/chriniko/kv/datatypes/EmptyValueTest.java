package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmptyValueTest {

    @Test
    void constructWorksAsExpected() {

        // given
        EmptyValue emptyValue = new EmptyValue();

        // when
        String asString = emptyValue.asString();

        // then
        assertEquals("{}", asString);
        assertEquals("", emptyValue.asStringUnwrapped());

        assertNull(emptyValue.getValue());

    }

}