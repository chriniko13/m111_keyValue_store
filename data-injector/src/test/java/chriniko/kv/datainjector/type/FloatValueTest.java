package chriniko.kv.datainjector.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatValueTest {

    @Test
    void constructWorksAsExpected() {

        // given
        FloatValue floatValue = new FloatValue("grade", 7.5F);

        // when
        String asString = floatValue.asString();

        // then
        assertEquals("{\"grade\" : 7.5}", asString);
        assertEquals("\"grade\" : 7.5", floatValue.asStringUnwrapped());

        assertEquals(7.5F, floatValue.getValue());

    }

}