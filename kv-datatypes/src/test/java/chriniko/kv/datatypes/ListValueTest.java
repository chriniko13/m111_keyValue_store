package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        NestedValue nestedValue = new NestedValue("n1", new NestedValue("n2", new NestedValue("n3", new StringValue("s", "v"))));

        // when
        ListValue listValue = (ListValue) ListValue.of(stringValue, stringValue2, intValue, floatValue, emptyValue);
        listValue.add(stringValue3);
        listValue.add(nestedValue);

        // then
        //System.out.println(listValue.asString());
        assertEquals("[ { \"name\" : \"nikolaos christidis\" } " +
                "; { \"profession\" : \"student\" } " +
                "; { \"age\" : 28 } " +
                "; { \"grade\" : 8.5 } " +
                "; {} " +
                "; { \"creationTime\" : \"2020/10/5\" } " +
                "; { \"n1\" : { \"n2\" : { \"n3\" : { \"s\" : \"v\" } } } } ]", listValue.asString());

        assertEquals("{ \"name\" : \"nikolaos christidis\" } " +
                "; { \"profession\" : \"student\" } " +
                "; { \"age\" : 28 } " +
                "; { \"grade\" : 8.5 } " +
                "; {} " +
                "; { \"creationTime\" : \"2020/10/5\" } " +
                "; { \"n1\" : { \"n2\" : { \"n3\" : { \"s\" : \"v\" } } } }", listValue.asStringUnwrapped());

        assertEquals(7, listValue.getValue().size());


        // when
        String s = ListValue.of(
                ListValue.of(new StringValue("_s1", "v1")),
                new StringValue("_s2", "v2")
        ).asString();

        // then
        assertEquals("[ [ { \"_s1\" : \"v1\" } ] ; { \"_s2\" : \"v2\" } ]", s);
    }

}