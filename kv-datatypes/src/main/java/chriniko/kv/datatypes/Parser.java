package chriniko.kv.datatypes;

import java.util.*;
import java.util.function.Function;


public final class Parser {

    private static final Map<Class<? extends Value<?>>, Function<String, Value<?>>> PARSERS_BY_TYPE;

    static {
        PARSERS_BY_TYPE = new LinkedHashMap<>(); // Note: linked hash map in order to force ordering (higher precedence, etc.)

        PARSERS_BY_TYPE.put(IntValue.class, s -> parseInt(s, false));

        PARSERS_BY_TYPE.put(FloatValue.class, s -> parseFloat(s, false));

        PARSERS_BY_TYPE.put(EmptyValue.class, Parser::parseEmpty);

        PARSERS_BY_TYPE.put(StringValue.class, s -> parseString(s, false));

        // TODO nested value parser....
    }

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
        if (s != null && !"{}".equals(s)) {
            throw new ParsingException("malformed, empty value should be a null or empty string");
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

        final ArrayList<Value<?>> valuesProcessed = new ArrayList<>(splittedEntries.length);

        for (String splittedEntry : splittedEntries) {

            System.out.println("parseList ==> will parse now: " + splittedEntry);

            boolean parsed = false;

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

            // if not any parsing succeeds, then fail
            if (!parsed) {
                throw new ParsingException("entry inside ListValue was not a known one");
            }
        }

        return (ListValue) ListValue.of(valuesProcessed.toArray(new Value[0]));
    }

    /*
        Note: parse things like:

        {"nested" : {"n1" : {"n2" : {"n3" : {"n4" : {"n5" : {"n6" : {"str3" : "6"}}}}}}}}
     */
    public static NestedValue parseNested(String s) {

        int openParanthesisCounter = 0;

        boolean colonFound = false;
        boolean keyParsed = false;

        String key = "";

        String[] splittedInfo = s.split(" ");

        final ArrayDeque<String> keysParsed = new ArrayDeque<>();
        final ArrayDeque<Value<?>> valuesProcessed = new ArrayDeque<>();


        boolean foundEndOfNesting = false;

        for (String elem : splittedInfo) {

            System.out.println("currently processed elem: " + elem);

            if (elem.startsWith("{")) {
                System.out.println("open parenthesis ---- " + elem);
                openParanthesisCounter++;
            }

            if (":".equals(elem)) {
                colonFound = true;
                continue;
            }

            // case such as {"grade"
            if (elem.startsWith("{") && !keyParsed) {

                elem = elem.replace("{", "");

                if (elem.startsWith("\"") && !elem.endsWith("\"")) {
                    // Note: case where key has empty char between
                    throw new ParsingException("malformed, key contains empty character");
                }

                if (elem.startsWith("\"") && elem.endsWith("\"")) {
                    keysParsed.push(elem);
                    keyParsed = true;
                }

            } else if (elem.startsWith("{") && keyParsed) {

                // Note: if we are here it means that we are in nested case,
                //       if value starts with { then we need to save state and proceed...otherwise we need to stop
                //       and pop the state to calculate the answer


                if (!colonFound) {
                    throw new ParsingException("malformed, not valid nested type");
                }
                colonFound = false;

                key = elem.substring(1); // Note: skip/throw the character:  '{'

                keysParsed.push(key);

            } else {
                // Note: if we are here it means, that we have processed an elem which does not seem like
                //       a continuation of nesting, so we need to identify how to proceed

                System.out.println("HERE BEFORE BREAK: " + elem);

                final StringBuilder sb = new StringBuilder();
                int closeParenthesisToAdd = openParanthesisCounter - 1; // Note: minus one for keeping the parenthesis for the elem.
                while (closeParenthesisToAdd-- > 0) {
                    sb.append("}");
                }

                String endPattern = sb.toString();
                System.out.println("END PATTERN: " + endPattern);

                if (elem.endsWith(endPattern)) {

                    foundEndOfNesting = true;

                    // Note: time to extract the 'clean' elem and calculate what type is.
                    String cleanElem = elem.substring(0, elem.length() - endPattern.length());
                    System.out.println("cleanElem: " + cleanElem);

                    String reconstructed = "{" + keysParsed.peek() + " : " + cleanElem;
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

                } else {
                    throw new ParsingException("malformed, not valid nested type (elem.endsWith(endPattern)--->false)");
                }
            }
        }


        // Note: time to construct the answer
        if (foundEndOfNesting) {

            Value<?> lastValueConstructed = null;

            while (!keysParsed.isEmpty()) {

                final String poppedKey = keysParsed.pop().replace("\"", "");
                System.out.println("POPPED KEY: " + poppedKey);

                if (!valuesProcessed.isEmpty()) {

                    final Value<?> poppedValue = valuesProcessed.pop();

                    if (lastValueConstructed != null) {
                        throw new IllegalStateException("this case should never happen (lastValueConstructed != null)");
                    } else {
                        lastValueConstructed = poppedValue;
                    }

                } else {

                    if (lastValueConstructed == null) {
                        throw new IllegalStateException("this case should never happen (lastValueConstructed == null)");
                    } else {
                        lastValueConstructed = new NestedValue(poppedKey, lastValueConstructed);
                    }

                }
            }

            return (NestedValue) lastValueConstructed;

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
