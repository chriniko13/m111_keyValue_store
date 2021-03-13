package chriniko.kv.datainjector.core;

import chriniko.kv.datainjector.type.ListValue;
import chriniko.kv.datainjector.type.NestedValue;
import chriniko.kv.datainjector.type.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataGeneratorTest {

    @Test
    void processWorksAsExpected() throws Exception {

        // given
        DataGenerator dataGenerator = new DataGenerator();


        // when
        List<Record> records = dataGenerator.create(20, 1, 2, 60, null);


        // then
        assertEquals(20, records.size());

        Record record = records.get(0);
        assertNotNull(record.getKey());

        Value<?> value = record.getValue();
        assertTrue(value instanceof ListValue);
        ListValue listValue = (ListValue) value;
        assertEquals(2, listValue.getValue().size());

        Value<?> someValue = listValue.getValue().get(0);
        assertFalse(someValue instanceof NestedValue);


        // when (keysPerValue more than entries in sampleKeyFile.txt)
        records = dataGenerator.create(20, 1, 20, 60, null);


        // then
        assertEquals(20, records.size());

        record = records.get(0);
        assertNotNull(record.getKey());

        value = record.getValue();
        assertTrue(value instanceof ListValue);
        listValue = (ListValue) value;
        assertEquals(5, listValue.getValue().size());

        someValue = listValue.getValue().get(0);
        assertFalse(someValue instanceof NestedValue);



        // when (depth/nesting is more than 1)
        records = dataGenerator.create(20, 5, 20, 60, null);



        // then
        assertEquals(20, records.size());

        record = records.get(0);
        assertNotNull(record.getKey());

        value = record.getValue();
        assertTrue(value instanceof ListValue);
        listValue = (ListValue) value;
        assertEquals(5, listValue.getValue().size());

        someValue = listValue.getValue().get(0);
        assertTrue(someValue instanceof NestedValue);
        // TODO check that nesting is 5
    }
}