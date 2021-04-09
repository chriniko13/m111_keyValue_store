package chriniko.kv.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NestedValueTest {


    @Test
    void constructWorksAsExpected() {


        // when
        NestedValue nestedValue = new NestedValue("address",
                new NestedValue("street",
                        new IntValue("number", 27)));

        String asString = nestedValue.asString();

        // then
        assertEquals("{ \"address\" : { \"street\" : { \"number\" : 27 } } }", asString);
        assertTrue(nestedValue.getValue() instanceof NestedValue);


        // when
        nestedValue = new NestedValue("address",
                new NestedValue("street",
                        new NestedValue("details",
                                new NestedValue("more-details",
                                        new IntValue("number", 27)))));

        asString = nestedValue.asString();

        // then
        assertEquals("{ \"address\" : { \"street\" : { \"details\" : { \"more-details\" : { \"number\" : 27 } } } } }", asString);
        assertTrue(nestedValue.getValue() instanceof NestedValue);
        assertEquals(3, nestedValue.maxDepth());


        // when - { “name” : “Mary” ; “address” : { “street” : “Panepistimiou” ; “number” : 12 } }
        //        {"name" : "Mary" ; "address" : {"street" : "Panepistimiou" ; "number" : 12}}
        Value<?> r = ListValue.of("_idMary",
                new StringValue("name", "Mary"),

                ListValue.of("address",
                        new StringValue("street", "Panepistimiou"),
                        new IntValue("number", 12))
        );


        // then
        assertEquals("{ \"_idMary\" : [ { \"name\" : \"Mary\" } ; { \"address\" : [ { \"street\" : \"Panepistimiou\" } ; { \"number\" : 12 } ] } ] }", r.asString());
        assertEquals("{ \"name\" : \"Mary\" } ; { \"address\" : [ { \"street\" : \"Panepistimiou\" } ; { \"number\" : 12 } ] }", r.asStringUnwrapped());

    }


    @Test
    void testAsStringWorksAsExpected() {

        // given
        NestedValue nestedValue = new NestedValue("fn3",
                new NestedValue("nf4",
                        new StringValue("strTemp", "allGood")
                )
        );

        // when-then
        assertEquals("{ \"fn3\" : { \"nf4\" : { \"strTemp\" : \"allGood\" } } }", nestedValue.asString());
    }




    @Test
    void combineWorksAsExpected() {

        // when
        NestedValue result = NestedValue.combine("nested",
                new NestedValue("n1", new IntValue("int1", 1)),
                new NestedValue("n2", new IntValue("int2", 2)),
                new NestedValue("n3", new IntValue("int3", 3)),
                new NestedValue("n4", new StringValue("str1", "4")),
                new NestedValue("n5", new StringValue("str2", "5")),
                new NestedValue("n6", new StringValue("str3", "6"))
        );


        // then
        assertEquals(6, result.maxDepth());

        assertEquals("{ \"nested\" : { \"n1\" : { \"n2\" : { \"n3\" : { \"n4\" : { \"n5\" : { \"n6\" : { \"str3\" : \"6\" } } } } } } } }", result.asString());

    }


    @Test
    void constructWorksAsExpected2() {

        // when
        NestedValue result =
                new NestedValue("n1",
                        ListValue.of("list",
                                new StringValue("str1", "4"),

                                new NestedValue("n2",
                                        new IntValue("int2", 2)
                                ),

                                new NestedValue("n3",
                                        new NestedValue("n4",
                                                new StringValue("strTemp", "allGood")
                                        )
                                )
                        )
                );

        // then
        assertEquals(2, result.maxDepth());

        assertEquals("{ \"n1\" : { \"list\" : [ { \"str1\" : \"4\" } ; { \"n2\" : { \"int2\" : 2 } } ; { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ] } }", result.asString());

    }

    @Test
    void constructWorksAsExpected3() {

        // when
        NestedValue result =
                new NestedValue("n1",
                        ListValue.of("list",
                                new StringValue("str1", "4"),

                                new NestedValue("n2",
                                        new IntValue("int2", 2)
                                ),

                                new NestedValue("n3",
                                        new NestedValue("n4",
                                                new StringValue("strTemp", "allGood")
                                        )
                                ),

                                new NestedValue("n5",
                                        new FloatValue("float2", 2.34F)
                                ),

                                new NestedValue("n71",
                                        new NestedValue("n72",
                                                ListValue.of("grades", new FloatValue("float3", 3.34F), new FloatValue("float4", 4.34F))
                                        )
                                )
                        )
                );

        // then
        assertEquals(2, result.maxDepth());

        assertEquals(
                "{ \"n1\" : { \"list\" : [ { \"str1\" : \"4\" } ; { \"n2\" : { \"int2\" : 2 } } ; { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ; { \"n5\" : { \"float2\" : 2.34 } } ; { \"n71\" : { \"n72\" : { \"grades\" : [ { \"float3\" : 3.34 } ; { \"float4\" : 4.34 } ] } } } ] } }",
                result.asString()
        );

    }

    @Test
    void constructWorksAsExpected4() {

        // when
        NestedValue result =
                new NestedValue("n1",
                        ListValue.of("someList",
                                new StringValue("str1", "4"),

                                new NestedValue("n2",
                                        new IntValue("int2", 2)
                                ),

                                new NestedValue("n3",
                                        new NestedValue("n4",
                                                new StringValue("strTemp", "allGood")
                                        )
                                ),

                                new NestedValue("n5",
                                        new FloatValue("float2", 2.34F)
                                ),

                                new NestedValue("n71",
                                        new NestedValue("n72",
                                                ListValue.of("emptyList")
                                        )
                                )
                        )
                );

        // then
        assertEquals(2, result.maxDepth());

        assertEquals(
                "{ \"n1\" : { \"someList\" : [ { \"str1\" : \"4\" } ; { \"n2\" : { \"int2\" : 2 } } ; { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ; { \"n5\" : { \"float2\" : 2.34 } } ; { \"n71\" : { \"n72\" : { \"emptyList\" : [  ] } } } ] } }",
                result.asString()
        );

    }


    @Test
    void constructWorksAsExpected5() {

        // when
        NestedValue result =
                new NestedValue("n1",
                        ListValue.of("someList1",
                                new StringValue("str1", "4"),

                                new NestedValue("n2",
                                        new IntValue("int2", 2)
                                ),

                                new NestedValue("n3",
                                        new NestedValue("n4",
                                                new StringValue("strTemp", "allGood")
                                        )
                                ),

                                new NestedValue("n5",
                                        new FloatValue("float2", 2.34F)
                                ),

                                new NestedValue("n71",
                                        new NestedValue("n72",
                                                ListValue.of("someList2",
                                                        new NestedValue("n3",
                                                                new NestedValue("n4",
                                                                        new StringValue("strTemp", "allGood")
                                                                )
                                                        ),

                                                        new NestedValue("f1",
                                                                new NestedValue("f2",
                                                                        new NestedValue("f3",
                                                                                new NestedValue("f4",
                                                                                        new StringValue("fString", "fValue")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );

        // then
        assertEquals(4, result.maxDepth());

        assertEquals("{ \"n1\" : { \"someList1\" : [ { \"str1\" : \"4\" } ; { \"n2\" : { \"int2\" : 2 } } ; { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ; { \"n5\" : { \"float2\" : 2.34 } } ; { \"n71\" : { \"n72\" : { \"someList2\" : [ { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ; { \"f1\" : { \"f2\" : { \"f3\" : { \"f4\" : { \"fString\" : \"fValue\" } } } } } ] } } } ] } }", result.asString());

    }

    @Test
    void constructWorksAsExpected6() {

        // when
        NestedValue result =
                new NestedValue("n1",
                        ListValue.of("someList",
                                new NestedValue("fn3",
                                        new NestedValue("nf4",
                                                new StringValue("strTemp", "allGood")
                                        )
                                ),

                                new NestedValue("n2",
                                        new IntValue("int2", 2)
                                ),

                                new NestedValue("p3",
                                        ListValue.of("n3List",
                                                new NestedValue("gn3",
                                                        new NestedValue("gn4",
                                                                new StringValue("gstrTemp", "allGood")
                                                        )
                                                ),

                                                new NestedValue("gf1",
                                                        new NestedValue("gf2",
                                                                new NestedValue("gf3",
                                                                        new NestedValue("gf4",
                                                                                new StringValue("gfString", "gfValue")
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                ),

                                new NestedValue("n5",
                                        new FloatValue("float2", 2.34F)
                                ),

                                new NestedValue("n71",
                                        new NestedValue("n72",
                                                ListValue.of("listGh",
                                                        new NestedValue("n3",
                                                                new NestedValue("n4",
                                                                        new StringValue("strTemp", "allGood")
                                                                )
                                                        ),

                                                        new NestedValue("f1",
                                                                new NestedValue("f2",
                                                                        new NestedValue("f3",
                                                                                new NestedValue("f4",
                                                                                        new StringValue("fString", "fValue")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );

        // then
        assertEquals(4, result.maxDepth());

        assertEquals("{ \"n1\" : { \"someList\" : [ { \"fn3\" : { \"nf4\" : { \"strTemp\" : \"allGood\" } } } ; { \"n2\" : { \"int2\" : 2 } } ; { \"p3\" : { \"n3List\" : [ { \"gn3\" : { \"gn4\" : { \"gstrTemp\" : \"allGood\" } } } ; { \"gf1\" : { \"gf2\" : { \"gf3\" : { \"gf4\" : { \"gfString\" : \"gfValue\" } } } } } ] } } ; { \"n5\" : { \"float2\" : 2.34 } } ; { \"n71\" : { \"n72\" : { \"listGh\" : [ { \"n3\" : { \"n4\" : { \"strTemp\" : \"allGood\" } } } ; { \"f1\" : { \"f2\" : { \"f3\" : { \"f4\" : { \"fString\" : \"fValue\" } } } } } ] } } } ] } }", result.asString());

    }


}