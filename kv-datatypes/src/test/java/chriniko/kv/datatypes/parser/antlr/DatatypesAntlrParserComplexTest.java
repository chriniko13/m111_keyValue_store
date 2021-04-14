package chriniko.kv.datatypes.parser.antlr;

import chriniko.kv.datatypes.ListValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatatypesAntlrParserComplexTest {


    @Test
    void worksAsExpected() throws ParsingException {

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
        input = "{ \"_myList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_float23\" : 2.34 } } } ; { \"_strTemp\" : \"allGood allFine all work\" } ; { \"_fn32\" : { \"_nf42\" : { \"_someOtherStr\" : \"someOtherStrValue\" } } } ] }";
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


        // =====================================================
        // when
        input = "{ \"_fn3\" : { \"_nf4\" : { \"_float23\" : [ { \"_float1\" : 1.23 } ; { \"_str21\" : \"twenty_one\" } ] } } }";
        r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertEquals(
                input,
                r.asString()
        );



    }


    @Test
    void worksAsExpected2() throws ParsingException {

        // =====================================================
        // when
        String input = "{ \"_studentDetails\" : [ { \"_username\" : \"chriniko\" } ; { \"_email\" : \"chriniko\" } ; { \"_address\" : [ { \"_street\" : \"Panepistimioupoli 123, Kesariani\" } ; { \"_postCode\" : \"16121\" } ; { \"_city\" : \"Athens\" } ; { \"_country\" : \"Greece\" } ] } ; { \"_name\" : [ { \"_firstname\" : \"Nikolaos\" } ; { \"_surname\" : \"Christidis\" } ] } ] }";

        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertTrue(r instanceof ListValue);

        ListValue listValue = (ListValue) r;
        assertEquals(4, listValue.getValue().size());

        assertEquals(
                input,
                r.asString()
        );

    }


    @Test
    void worksAsExpected3() throws Exception {
        // =====================================================
        // when
        String input = "{ \"_studentDetails\" : [ { \"_username\" : \"chriniko\" } ; { \"_email\" : \"chriniko\" } ; { \"_lessonsNeeded\" : { \"_semester3\" : [ { \"_les3_01\" : \"compilers\" } ; { \"_semester2\" : { \"_semester1\" : { \"_notPassed\" : [ { \"_les1_01\" : \"algos\" } ; { \"_les1_02\" : \"os\" } ] } } } ] } } ; { \"_address\" : [ { \"_street\" : \"Panepistimioupoli 123, Kesariani\" } ; { \"_postCode\" : \"16121\" } ; { \"_city\" : \"Athens\" } ; { \"_country\" : \"Greece\" } ] } ; { \"_name\" : [ { \"_firstname\" : \"Nikolaos\" } ; { \"_surname\" : \"Christidis\" } ] } ] }";

        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);


        assertEquals(
                input,
                r.asString()
        );
    }


    @Test
    void worksAsExpected4() throws ParsingException {

        // =====================================================
        // when
        String input = "{ \"_n1\" : { \"_someList1\" : [ { \"_str1\" : \"4\" } ; { \"_n2\" : { \"_int2\" : 2 } } ; { \"_n31\" : { \"_n41\" : { \"_strTemp1\" : \"allGood\" } } } ; { \"_n5\" : { \"_float2\" : 2.34 } } ; { \"_n71\" : { \"_n72\" : { \"_someList2\" : [ { \"_n3\" : { \"_n4\" : { \"_strTemp\" : \"allGood\" } } } ; { \"_f1\" : { \"_f2\" : { \"_f3\" : { \"_f4\" : { \"_fString\" : \"fValue\" } } } } } ] } } } ] } }";
        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertEquals(
                input,
                r.asString()
        );


        // =====================================================
        // when
        input = "{ \"_contents\" : [ { \"_level\" : 15 } ; { \"_street\" : \"Apt. 946 4742 Heaney Field, New Marinville, SC 79754\" } ; { \"_name\" : \"jacinda.wilkinson\" } ; { \"_age\" : 40 } ; { \"_height\" : 1.2673556 } ] }";
        r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertEquals(
                input,
                r.asString()
        );

    }

    @Test
    void worksAsExpected5() throws ParsingException {



        // =====================================================
        // when
        String input = "{ \"_n1\" : { \"_someList\" : [ { \"_fn3\" : { \"_nf4\" : { \"_strTemp\" : \"allGood\" } } } ; { \"_n2\" : { \"_int2\" : 2 } } ; { \"_p3\" : { \"_n3List\" : [ { \"_gn3\" : { \"_gn4\" : { \"_gstrTemp\" : \"allGood\" } } } ; { \"_gf1\" : { \"_gf2\" : { \"_gf3\" : { \"_gf4\" : { \"_gfString\" : \"gfValue\" } } } } } ] } } ; { \"_n5\" : { \"_float2\" : 2.34 } } ; { \"_n71\" : { \"_n72\" : { \"_listGh\" : [ { \"_n3\" : { \"_n4\" : { \"_strTemp2\" : \"allGood\" } } } ; { \"_f1\" : { \"_f2\" : { \"_f3\" : { \"_f4\" : { \"_fString\" : \"fValue\" } } } } } ] } } } ] } }";
        Value<?> r = DatatypesAntlrParser.process(input);

        // then
        assertNotNull(r);

        assertEquals(
                input,
                r.asString()
        );
    }

}
