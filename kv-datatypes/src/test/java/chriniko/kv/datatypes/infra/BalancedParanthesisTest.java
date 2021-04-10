package chriniko.kv.datatypes.infra;

import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesParser;
import chriniko.kv.datatypes.NestedValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BalancedParanthesisTest {

    @Test
    void process() {


        // when
        boolean process = BalancedParanthesis.process("\"n1\" : {\"n2\" : {\"n3\" : {\"s\" : \"v\"}}}");


        // then
        assertTrue(process);


        // when
        process = BalancedParanthesis.process("\"n1\" : {\"n2\" : {\"n3\" : {\"s\" : \"v\"}}}}");


        // then
        assertFalse(process);


        // when
        process = BalancedParanthesis.process("{\"n1\" : {\"n2\" : {\"n3\" : {\"s\" : \"v\"}}}}");


        // then
        assertTrue(process);

        // when
        process = BalancedParanthesis.process("{{{fafa}{aefeaf}af}{}}");


        // then
        assertTrue(process);
    }


    @Test
    void process2() throws ParsingException {


        /*
            input splitted by " ; "


            0 = "{"n1" : {"fn3" : {"nf4" : {"strTemp" : "allGood"}}"
            1 = ""n2" : {"int2" : 2}"
            2 = ""n3" : {"n4" : {"strTemp" : "allGood"}}"
            3 = ""n5" : {"float2" : 2.34}"
            4 = ""n71" : {"n72" : {"n3" : {"n4" : {"strTemp" : "allGood"}}"
            5 = ""f1" : {"f2" : {"f3" : {"f4" : {"fString" : "fValue"}}}}}}}}"
         */


        // 0
        assertFalse(BalancedParanthesis.process("{ \"n1\" : { \"fn3\" : { \"nf4\" : { \"strTemp\" : \"allGood\" } }")); // before elimination
        assertTrue(BalancedParanthesis.process("{ \"nf4\" : { \"strTemp\" : \"allGood\" } }")); // after elimination
        NestedValue nestedValue = DatatypesParser.parseNested("{ \"nf4\" : { \"strTemp\" : \"allGood\" } }");
        assertNotNull(nestedValue);




        // 1, 2, 3
        assertTrue(BalancedParanthesis.process("\"n2\" : { \"int2\" : 2 }"));
        nestedValue = DatatypesParser.parseNested("{ \"n2\" : { \"int2\" : 2 } }");
        assertNotNull(nestedValue);

        assertTrue(BalancedParanthesis.process("\"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } }"));
        nestedValue = DatatypesParser.parseNested("{ \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } }");
        assertNotNull(nestedValue);

        assertTrue(BalancedParanthesis.process("\"n5\" : { \"float2\" : 2.34 }"));
        nestedValue = DatatypesParser.parseNested("{ \"n5\" : { \"float2\" : 2.34 } }");
        assertNotNull(nestedValue);




        // 4, 5
        // we can understand that the below 2 will break due to nested + listed nature
        assertFalse(BalancedParanthesis.process("\"n71\" : { \"n72\" : { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } }"));
        assertFalse(BalancedParanthesis.process("\"f1\" : { \"f2\" : { \"f3\" : { \"f4\" : { \"fString\" : \"fValue\" } } } } } } } }"));



        // but if we combine/concatenate them and eliminate the ending parenthesis based on the start parenthesis depth of nesting
        assertFalse(BalancedParanthesis.process("\"n71\" : { \"n72\" : { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } ; \"f1\" : { \"f2\" : { \"f3\" : { \"f4\" : { \"fString\" : \"fValue\" } } } } } } } }")); // before elimination
        assertTrue(BalancedParanthesis.process("\"n71\" : { \"n72\" : { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } ; \"f1\" : { \"f2\" : { \"f3\" : { \"f4\" : { \"fString\" : \"fValue\" } } } } } }")); // after elimination

    }


    public static void main(String[] args) {
        test();
    }

    private static void test() {

        final String[] inputLines = {
                "\"n2\" : {\"int2\" : 2}",
                "\"n3\" : {\"n4\" : {\"strTemp\" : \"allGood\"}}",
                "\"n5\" : {\"float2\" : 2.34}",
                "\"n5\" : {\"float2\" : 2.34}",
                "\"int2\" : 2"
        };

        for (String inputLine : inputLines) {

            String[] temp = inputLine.split(" ");

            boolean keyParsed = false;

            for (String s : temp) {

                if (s.startsWith("\"") && s.endsWith("\"") && !keyParsed) {

                    keyParsed = true;


                }



            }


        }


    }

}