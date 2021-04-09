package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.infra.BalancedParanthesis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;


public final class DatatypesParser {

    /**
     * Used from {@link DatatypesParser#parseList(String)} and {@link DatatypesParser#parseNested(String)}
     */
    private static final Map<Class<? extends Value<?>>, Function<String, Value<?>>> PARSERS_BY_TYPE;

    static {
        PARSERS_BY_TYPE = new LinkedHashMap<>(); // Note: linked hash map in order to force ordering (higher precedence, etc.)

        PARSERS_BY_TYPE.put(IntValue.class, s -> parseInt(s, false));

        PARSERS_BY_TYPE.put(FloatValue.class, s -> parseFloat(s, false));

        PARSERS_BY_TYPE.put(EmptyValue.class, DatatypesParser::parseEmpty);

        PARSERS_BY_TYPE.put(StringValue.class, s -> parseString(s, false));
    }


    private static final Pattern emptyValueRegex = Pattern.compile("\\s*\\{\\s*\\}\\s*");


    public static FloatValue parseFloat(String s) {
        return parseFloat(s, true);
    }

    public static FloatValue parseFloat(String s, boolean checkOpenCloseParenthesis) {
        validateInput(s);

        boolean openParanthesis = false;
        boolean closeParanthesis = false;

        boolean colonFound = false;

        boolean keyParsed = false;
        boolean valueParsed = false;

        String key = "";
        float value = 0.0F;

        String[] splittedInfo = s.split(" ");

        for (String elem : splittedInfo) {

            if ("{".equals(elem)) {
                openParanthesis = true;
                continue;
            }

            if ("}".equals(elem)) {
                closeParanthesis = true;
                continue;
            }

            if (":".equals(elem)) {
                colonFound = true;
                continue;
            }

            if (!keyParsed && elem.startsWith("\"") && !elem.endsWith("\"")) {
                // Note: case where key has empty char between
                throw new ParsingException("malformed, key contains empty character");

            } else if (elem.startsWith("\"") && elem.endsWith("\"") && !keyParsed) {

                key = elem.replace("\"", "");
                keyParsed = true;

            } else {

                // case such as {"grade"
                if (elem.startsWith("{")) {
                    openParanthesis = true;
                    elem = elem.replace("{", "");

                    if (elem.startsWith("\"") && !elem.endsWith("\"")) {
                        // Note: case where key has empty char between
                        throw new ParsingException("malformed, key contains empty character");
                    }


                    if (elem.startsWith("\"") && elem.endsWith("\"")) {
                        key = elem.replace("\"", "");
                        keyParsed = true;
                        continue;
                    }
                }


                // case such as 7.5}
                if (elem.endsWith("}")) {
                    closeParanthesis = true;
                    elem = elem.replace("}", "");

                    try {
                        value = Float.parseFloat(elem);
                        valueParsed = true;
                    } catch (Exception e) {
                        throw new ParsingException("malformed, value is not a float type");
                    }
                    continue;

                }


                // case of raw value
                try {
                    value = Float.parseFloat(elem);
                    valueParsed = true;
                } catch (Exception e) {
                    throw new ParsingException("malformed, value is not a float type");
                }
            }

        }

        _validations(checkOpenCloseParenthesis, openParanthesis, closeParanthesis, colonFound, keyParsed, valueParsed);

        return new FloatValue(key, value);
    }

    public static EmptyValue parseEmpty(String s) {
        if (s == null) {
            throw new ParsingException("malformed, empty value should be a null or empty string");
        }

        if (!emptyValueRegex.asPredicate().test(s)) {
            throw new ParsingException("not valid empty value");
        }

        return new EmptyValue();
    }

    public static IntValue parseInt(String s) {
        return parseInt(s, true);
    }

    public static IntValue parseInt(String s, boolean checkOpenCloseParenthesis) {
        validateInput(s);

        boolean openParanthesis = false;
        boolean closeParanthesis = false;

        boolean colonFound = false;

        boolean keyParsed = false;
        boolean valueParsed = false;

        String key = "";
        int value = 0;

        String[] splittedInfo = s.split(" ");

        for (String elem : splittedInfo) {

            if ("{".equals(elem)) {
                openParanthesis = true;
                continue;
            }

            if ("}".equals(elem)) {
                closeParanthesis = true;
                continue;
            }

            if (":".equals(elem)) {
                colonFound = true;
                continue;
            }

            if (!keyParsed && elem.startsWith("\"") && !elem.endsWith("\"")) {
                // Note: case where key has empty char between
                throw new ParsingException("malformed, key contains empty character");

            } else if (elem.startsWith("\"") && elem.endsWith("\"") && !keyParsed) {
                key = elem.replace("\"", "");
                keyParsed = true;

            } else {

                // case such as {"grade"
                if (elem.startsWith("{")) {
                    openParanthesis = true;
                    elem = elem.replace("{", "");

                    if (elem.startsWith("\"") && !elem.endsWith("\"")) {
                        // Note: case where key has empty char between
                        throw new ParsingException("malformed, key contains empty character");
                    }


                    if (elem.startsWith("\"") && elem.endsWith("\"")) {
                        key = elem.replace("\"", "");
                        keyParsed = true;
                        continue;
                    }
                }


                // case such as 7}
                if (elem.endsWith("}")) {
                    closeParanthesis = true;
                    elem = elem.replace("}", "");

                    try {
                        value = Integer.parseInt(elem);
                        valueParsed = true;
                    } catch (Exception e) {
                        throw new ParsingException("malformed, value is not an int type");
                    }
                    continue;

                }


                // case of raw value
                try {
                    value = Integer.parseInt(elem);
                    valueParsed = true;
                } catch (Exception e) {
                    throw new ParsingException("malformed, value is not an int type");
                }
            }

        }

        _validations(checkOpenCloseParenthesis, openParanthesis, closeParanthesis, colonFound, keyParsed, valueParsed);

        return new IntValue(key, value);
    }

    public static ListValue parseList(String s) {
        validateInput(s);

        final String[] splittedEntries = s.split(ListValue.SEPARATOR);

        if (splittedEntries.length == 0) {
            throw new IllegalArgumentException("not valid ListValue");
        }

        // Note: more clever validation if is a list value
        for (int i = 0; i < splittedEntries.length; i++) {

            String entry = splittedEntries[i];

            if (i == 0) { // Note: first entry always drop starting {

                if (entry.startsWith("{")) {
                    entry = entry.substring(1);
                }

            } else if (i == splittedEntries.length - 1) { // Note: last entry always drop ending }

                if (entry.endsWith("}")) {
                    entry = entry.substring(0, entry.length() - 1);
                }

            }

            boolean balanced = BalancedParanthesis.process(entry);
            if (!balanced) {
                throw new ParsingException("not valid list value to be parsed - unbalanced parenthesis");
            }
        }

        final ArrayList<Value<?>> valuesProcessed = new ArrayList<>(splittedEntries.length);

        for (String splittedEntry : splittedEntries) {

            System.out.println("parseList ==> will parse now: " + splittedEntry);

            boolean parsed = false;

            // Note 1: parseList ==> will parse now: "n1" : {"n2" : {"n3" : {"s" : "v"}}}}
            // Note 2: parseList ==> will parse now: {"n1" : {"str1" : "4"
            if (splittedEntry.contains(": {") && splittedEntry.endsWith("}")) { // Note: most probably nested value case.

                if (!splittedEntry.startsWith("{")) {
                    splittedEntry = "{" + splittedEntry;
                }

                final Value<?> result = DatatypesParser.parseNested(splittedEntry);
                parsed = true;
                valuesProcessed.add(result);

            } else { // Note: all other cases/values except nested value

                // try until parsing succeeds
                for (Map.Entry<Class<? extends Value<?>>, Function<String, Value<?>>> entry : PARSERS_BY_TYPE.entrySet()) {

                    final Class<? extends Value<?>> key = entry.getKey();
                    final Function<String, Value<?>> value = entry.getValue();

                    try {
                        System.out.println("will try to parse with parser for: " + key.getSimpleName());
                        final Value<?> result = value.apply(splittedEntry);
                        parsed = true;
                        valuesProcessed.add(result);
                        break;
                    } catch (ParsingException e) {
                        //System.err.println(e.getMessage());
                        //e.printStackTrace(System.err);
                    }
                }
            }

            // if not any parsing succeeds, then fail
            if (!parsed) {
                throw new ParsingException("entry inside ListValue was not a known one");
            }
        }

        return (ListValue) ListValue.of("_list", valuesProcessed.toArray(new Value[0]));
    }

    /*
        Note:


        parse things like:

            {"nested" : {"n1" : {"n2" : {"n3" : {"n4" : {"n5" : {"n6" : {"str3" : "6"}}}}}}}}


       for the following case, it will fail because you should use the parser created with ANTLR4 Grammar


            {"n1" : {"str1" : "4" ; "n2" : {"int2" : 2} ; "n3" : {"n4" : {"strTemp" : "allGood"}} ; "n5" : {"float2" : 2.34} ; "n71" : {"n72" : {"float3" : 3.34 ; "float4" : 4.34}}}}

     */
    public static NestedValue parseNested(String s) {

        int openParanthesisCounter = 0;

        boolean colonFound = false;
        boolean keyParsed = false;

        String[] splittedInfo = s.split(" ");

        final ArrayDeque<String> keysParsed = new ArrayDeque<>();
        final ArrayDeque<Value<?>> valuesProcessed = new ArrayDeque<>();

        boolean foundEndOfNesting = false;

        boolean listSeparatorOccurred = false;

        boolean finished = false;

        for (int k = 0; k < splittedInfo.length && !finished; k++) {
            String elem = splittedInfo[k];

            System.out.println("currently processed elem: " + elem);

            if (elem.startsWith("{")) {
                System.out.println("open parenthesis ---- " + elem);
                openParanthesisCounter++;

                if (elem.equals("{")) {
                    continue;
                }
            }

            if (":".equals(elem)) {
                colonFound = true;
                continue;
            }

            // Note: hardest case ===> combination of nested && listed values.
            if (listSeparatorOccurred) {
                //listSeparatorOccurred = false;
                throw new ParsingException("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4");
            }

            if (!keyParsed) {

                elem = elem.replace("{", "");

                if (elem.startsWith("\"") && !elem.endsWith("\"")) {
                    // Note: case where key has empty char between
                    throw new ParsingException("malformed, key contains empty character");
                }

                if (elem.startsWith("\"") && elem.endsWith("\"")) {
                    keysParsed.push(elem);
                    keyParsed = true;
                } else {
                    throw new ParsingException("malformed key provided");
                }

            } else if (k < splittedInfo.length - 2
                    && splittedInfo[k + 1].equals(":")
                    && splittedInfo[k + 2].equals("{")) {

                // Note: if we are here it means that we are in nested case,
                //       if value starts with { then we need to save state and proceed...otherwise we need to stop
                //       and pop the state to calculate the answer

                if (!colonFound) {
                    throw new ParsingException("malformed, not valid nested type");
                }
                colonFound = false;
                keysParsed.push(elem);

            } else {
                System.out.println("HERE BEFORE BREAK: " + elem);
                // Note: if we are here it means, that we have processed an elem which does not seem like
                //       a continuation of nesting (exiting of nesting), so we need to identify how to proceed

                if (k + 1 < splittedInfo.length
                        && ";".equals(splittedInfo[k + 1])) {

                    listSeparatorOccurred = true; // Note: if next splitted entry is ; enable list parser approach

                } else {
                    if (!splittedInfo[k + 1].equals(":")) {
                        throw new ParsingException("invalid");
                    }

                    final StringBuilder sb = new StringBuilder();
                    int closeParenthesisToAdd = openParanthesisCounter - 1; // Note: minus one for keeping the parenthesis for the elem.
                    while (closeParenthesisToAdd-- > 0) {
                        sb.append(" }");
                    }

                    String endPattern = sb.toString();
                    String portion = s.substring(s.indexOf(elem));
                    System.out.println("PORTION: " + portion);
                    System.out.println("END PATTERN: " + endPattern);

                    if (portion.endsWith(endPattern)) {

                        foundEndOfNesting = true;

                        // Note: time to extract the 'clean' elem and calculate what type is.
                        String cleanElem = portion.substring(0, portion.length() - endPattern.length());
                        System.out.println("cleanElem: " + cleanElem);

                        String reconstructed = "{ " + cleanElem;
                        System.out.println("reconstructed: " + reconstructed);

                        // Note: try until parsing succeeds
                        boolean parsed = false;

                        for (Map.Entry<Class<? extends Value<?>>, Function<String, Value<?>>> entry : PARSERS_BY_TYPE.entrySet()) {

                            final Class<? extends Value<?>> parserType = entry.getKey();
                            final Function<String, Value<?>> value = entry.getValue();

                            System.out.println("\n--------------");
                            System.out.println("will try to parse with parser for: " + parserType.getSimpleName());

                            try {
                                final Value<?> result = value.apply(reconstructed);
                                parsed = true;
                                valuesProcessed.push(result);
                                break;
                            } catch (ParsingException e) {
                                //System.err.println(e.getMessage());
                                //e.printStackTrace(System.err);
                            }
                        }

                        // if not any parsing succeeds, then fail
                        if (!parsed) {
                            throw new ParsingException("entry inside ListValue was not a known one");
                        }

                        // Note: otherwise if we are here it means that parsing was successful
                        finished = true;

                    } else {
                        if (s.contains(";")) {
                            throw new ParsingException("provided input is a complex case (nested type & listed type), please use parser created from ANTLR4");
                        }
                        throw new ParsingException("malformed, not valid nested type (elem.endsWith(endPattern)--->false)");
                    }
                }

            }
        }


        // Note: time to construct the answer
        if (foundEndOfNesting) {
            NestedValue lastValueConstructed = null;

            while (!keysParsed.isEmpty()) {

                final String poppedKey = keysParsed.pop().replace("\"", "");
                System.out.println("POPPED KEY: " + poppedKey);

                if (!valuesProcessed.isEmpty()) {

                    final Value<?> poppedValue = valuesProcessed.pop();

                    if (lastValueConstructed != null) {
                        throw new IllegalStateException("this case should never happen (lastValueConstructed != null)");
                    } else {
                        lastValueConstructed = new NestedValue(poppedKey, poppedValue);
                    }

                } else {

                    if (lastValueConstructed == null) {
                        throw new IllegalStateException("this case should never happen (lastValueConstructed == null)");
                    } else {
                        lastValueConstructed = new NestedValue(poppedKey, lastValueConstructed);
                    }

                }
            }

            return lastValueConstructed;

        } else {
            throw new ParsingException("malformed, not valid nested type (foundEndOfNesting--->false)");
        }
    }

    public static StringValue parseString(String s) {
        return parseString(s, true);
    }

    public static StringValue parseString(String s, boolean checkOpenCloseParenthesis) {
        validateInput(s);

        for (String notAllowedChar : StringValue.NOT_ALLOWED_CHARS) {
            if (s.contains(notAllowedChar)) {
                throw new IllegalArgumentException("value provided contains a not allowed char " + notAllowedChar);
            }
        }

        boolean openParanthesis = false;
        boolean closeParanthesis = false;

        boolean colonFound = false;

        boolean keyParsed = false;
        boolean valueParsed = false;

        String key = "";
        String value = "";

        String[] splittedInfo = s.split(" ");

        for (String elem : splittedInfo) {

            System.out.println(elem);

            if ("{".equals(elem)) {
                openParanthesis = true;
                continue;
            }

            if (":".equals(elem)) {
                colonFound = true;
                continue;
            }

            if (!keyParsed && elem.startsWith("\"") && !elem.endsWith("\"")) {
                // Note: case where key has empty char between
                throw new ParsingException("malformed, key contains empty character");

            } else if (elem.startsWith("\"") && elem.endsWith("\"") && !keyParsed) {

                key = elem.replace("\"", "");
                keyParsed = true;

            } else {

                // case such as {"grade"
                if (elem.startsWith("{")) {
                    openParanthesis = true;
                    elem = elem.replace("{", "");

                    if (elem.startsWith("\"") && !elem.endsWith("\"")) {
                        // Note: case where key has empty char between
                        throw new ParsingException("malformed, key contains empty character");
                    }


                    if (elem.startsWith("\"") && elem.endsWith("\"")) {
                        key = elem.replace("\"", "");
                        keyParsed = true;
                        continue;
                    }
                }

                // case of raw value
                if (keyParsed) {
                    //value = elem;

                    if (!elem.startsWith("\"") && !elem.endsWith("\"")) {
                        throw new ParsingException("malformed, value is not a string type");
                    }

                    value = s.substring(s.indexOf(elem));
                    valueParsed = true;

                    if (value.endsWith("}")) {
                        closeParanthesis = true;
                        value = value.substring(0, value.length() - 1);
                    }

                    break;

                } else {
                    throw new ParsingException("malformed, value is not a string type");
                }
            }

        }

        _validations(checkOpenCloseParenthesis, openParanthesis, closeParanthesis, colonFound, keyParsed, valueParsed);

        // drop beginning " and ending "
        value = value.trim();
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }

        return new StringValue(key, value);
    }

    // --- infra ---

    private static void _validations(boolean checkOpenCloseParenthesis, boolean openParanthesis, boolean closeParanthesis,
                                     boolean colonFound, boolean keyParsed,
                                     boolean valueParsed) {

        if (checkOpenCloseParenthesis) {
            if (!openParanthesis) {
                throw new ParsingException("malformed, open parenthesis is missing");
            }
            if (!closeParanthesis) {
                throw new ParsingException("malformed, close parenthesis is missing");
            }
        }


        if (!colonFound) {
            throw new ParsingException("malformed, colon(:) is missing");
        }
        if (!keyParsed) {
            throw new ParsingException("malformed, key not parsed/found");
        }
        if (!valueParsed) {
            throw new ParsingException("malformed, value not parsed/found");
        }
    }

    private static void validateInput(String s) {
        if (s == null || s.isEmpty()) {
            throw new ParsingException("malformed, empty string (or null) provided");
        }
    }


}
