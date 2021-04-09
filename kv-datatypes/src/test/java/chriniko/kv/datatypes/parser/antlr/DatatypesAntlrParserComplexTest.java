package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.ListValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.datatypes.parser.DatatypesParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserComplexTest {


    @Test
    void worksAsExpected_2entriesInListCombinations() {

        // START: 2 ENTRIES ALL COMBINATIONS

        // =====================================================
        // when
        Value<?> r = DatatypesAntlrParser.process("{ \"_strTemp\" : \"allGood allFine all work\" ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        ListValue listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                "{ \"_strTemp\" : \"allGood allFine all work\" ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } }",
                r.asString()
        );


        // =====================================================
        // when
        r = DatatypesAntlrParser.process("{ \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } ; \"_strTemp\" : \"allGood allFine all work\" }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                "{ \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } ; \"_strTemp\" : \"allGood allFine all work\" }",
                r.asString()
        );


        // =====================================================
        // when
        r = DatatypesAntlrParser.process("{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                "{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } }",
                r.asString()
        );


        // =====================================================
        // when
        r = DatatypesAntlrParser.process("{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 ; \"_int2\" : 234 } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 ; \"_str\" : \"strValue\" } } }");

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                "{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 ; \"_int2\" : 234 } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 ; \"_str\" : \"strValue\" } } }",
                r.asString()
        );


        // =====================================================
        // when
        r = DatatypesAntlrParser.process("{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 ; \"_int2\" : 234 ; \"_nf2N\" : { \"_int1N\" : 2 ; \"_int2N\" : 234 } } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 ; \"_str\" : \"strValue\" } } }");

        // then
        assertNotNull(r);

        assertEquals(
                "{ \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 ; \"_int2\" : 234 ; \"_nf2N\" : { \"_int1N\" : 2 ; \"_int2N\" : 234 } } } ; \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 ; \"_str\" : \"strValue\" } } }",
                r.asString()
        );



        // =====================================================
        // when
        r = DatatypesAntlrParser.process("{\"_n1\" : {\"_fn3\" : {\"_nf4\" : {\"_strTemp\" : \"allGood\"}} ; \"_n2\" : {\"_int2\" : 2} ; \"_n3\" : {\"_n4\" : {\"_strTemp2\" : \"allGood\"}} ; \"_n5\" : {\"_float2\" : 2.34} ; \"_n71\" : {\"_n72\" : {\"_n31\" : {\"_n4\" : {\"_strTemp3\" : \"allGood\"}} ; \"_f1\" : {\"_f2\" : {\"_f3\" : {\"_f4\" : {\"_fString\" : \"fValue\"}}}}}}}}");


        // then
        assertNotNull(r);

        assertEquals(
                "{\"_n1\" : {\"_fn3\" : {\"_nf4\" : {\"_strTemp\" : \"allGood\" } } ; \"_n2\" : { \"_int2\" : 2 } ; \"_n3\" : { \"_n4\" : { \"_strTemp2\" : \"allGood\" } } ; \"_n5\" : { \"_float2\" : 2.34 } ; \"_n71\" : { \"_n72\" : { \"_n31\" : { \"_n4\" : { \"_strTemp3\" : \"allGood\" } } ; \"_f1\" : { \"_f2\" : { \"_f3\" : { \"_f4\" : { \"_fString\" : \"fValue\" } } } } } } } }",
                r.asString()
        );
    }

}
