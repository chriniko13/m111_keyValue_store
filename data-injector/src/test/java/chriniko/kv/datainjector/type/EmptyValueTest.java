package chriniko.kv.datainjector.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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