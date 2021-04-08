package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.EmptyValue;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatatypesParserEmptyValueTest {


    @Test
    void parseWorksAsExpected() {

        EmptyValue emptyValue = DatatypesParser.parseEmpty(" {  } ");

        Assertions.assertNotNull(emptyValue);

    }
}
