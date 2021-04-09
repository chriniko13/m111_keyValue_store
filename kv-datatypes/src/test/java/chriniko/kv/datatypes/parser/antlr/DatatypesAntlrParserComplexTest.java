package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.ListValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserComplexTest {


    @Test
    void worksAsExpected() {

        // START: 2 ENTRIES ALL COMBINATIONS

        // =====================================================
        // when
        String input = "{ \"_myList\" : [ { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ] }";
        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        ListValue listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                input,
                r.asString()
        );


        // =====================================================
        // when
        input = "{ \"_myList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ; { \"_strTemp\" : \"allGood allFine all work\" } ] }";
        r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                input,
                r.asString()
        );


        // =====================================================
        // when
        input = "{ \"_myList\" : [ { \"_fn1\" : { \"_nf2\" : { \"_int1\" : 2 } } } ; { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ] }";
        r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(2, listValue.getValue().size());

        assertEquals(
                input,
                r.asString()
        );


        // =====================================================
        // when
        input = "{ \"_n1\" : [ { \"_str1\" : \"4\" } ; { \"_n2\" : { \"_int2\" : 2 } } ; { \"_n3\" : { \"_n4\" : { \"_strTemp\" : \"allGood\" } } } ] }";
        r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        listValue = (ListValue) r;
        assertEquals(3, listValue.getValue().size());

        assertEquals(
                input,
                r.asString()
        );

    }



    @Test
    void worksAsExpected2() {
        // =====================================================TODO edw....
        // when
        String input = "{ \"_n1\" : { \"_someList1\" : [ { \"_str1\" : \"4\" } ; { \"_n2\" : { \"_int2\" : 2 } } ; { \"_n31\" : { \"_n41\" : { \"_strTemp1\" : \"allGood\" } } } ; { \"_n5\" : { \"_float2\" : 2.34 } } ; { \"_n71\" : { \"_n72\" : { \"_someList2\" : [ { \"_n3\" : { \"_n4\" : { \"_strTemp\" : \"allGood\" } } } ; { \"_f1\" : { \"_f2\" : { \"_f3\" : { \"_f4\" : { \"_fString\" : \"fValue\" } } } } } ] } } } ] } }";
        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertEquals(
                input,
                r.asString()
        );



        // =====================================================TODO edw...
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
