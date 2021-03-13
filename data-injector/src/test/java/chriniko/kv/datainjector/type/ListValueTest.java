package chriniko.kv.datainjector.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListValueTest {

    @Test
    void constructWorksAsExpected() {

        // given
        StringValue stringValue = new StringValue("name", "nikolaos christidis");
        StringValue stringValue2 = new StringValue("profession", "student");
        IntValue intValue = new IntValue("age", 28);
        FloatValue floatValue = new FloatValue("grade", 8.5F);
        EmptyValue emptyValue = new EmptyValue();
        StringValue stringValue3 = new StringValue("creationTime", "2020/10/5");

        // when
        ListValue listValue = (ListValue) ListValue.of(stringValue, stringValue2, intValue, floatValue, emptyValue);
        listValue.add(stringValue3);

        // then
        assertEquals("{\"name\" : \"nikolaos christidis\" ; \"profession\" : \"student\" ; \"age\" : 28" +
                " ; \"grade\" : 8.5 ; {} ; \"creationTime\" : \"2020/10/5\"}", listValue.asString());

        assertEquals("\"name\" : \"nikolaos christidis\" ; \"profession\" : \"student\" ; \"age\" : 28 " +
                "; \"grade\" : 8.5 ; {} ; \"creationTime\" : \"2020/10/5\"", listValue.asStringUnwrapped());

        assertEquals(6, listValue.getValue().size());

    }

}