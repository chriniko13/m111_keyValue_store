package chriniko.kv.datatypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserEmptyValueTest {


    @Test
    void parseWorksAsExpected() {

        EmptyValue emptyValue = Parser.parseEmpty(null);

        Assertions.assertNotNull(emptyValue);

    }
}
