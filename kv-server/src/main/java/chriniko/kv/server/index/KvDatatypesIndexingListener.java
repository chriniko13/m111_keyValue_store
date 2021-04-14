package chriniko.kv.server.index;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.error.ParsingInfraException;
import chriniko.kv.datatypes.error.UncheckedParsingException;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.javatuples.Pair;

import java.util.*;


/**
 * The purpose of this listener is to index the key-values of the provided input string (walk it).
 *
 * The result of this parsing-process is used from query operation of the kv-server, in order to
 * have constant time during querying based on key.
 *
 * This listener is used from {@link KvRecordIndexer}
 */
public class KvDatatypesIndexingListener extends KvDatatypesBaseListener {

    private static final String KEYPATH_JOINER = "~>";

    @Getter
    private boolean errorOccurred = false;

    @Getter
    private Value<?> result;

    @Getter
    private boolean parseFinished = false;

    @Getter
    private boolean parsingStarted = false;

    // ---

    private final LinkedList<String> processedKeysList = new LinkedList<>();
    private final HashSet<String> processedKeysSet = new LinkedHashSet<>();

    private String currentKey;

    private final ArrayDeque<Value<?>> processedValuesStack = new ArrayDeque<>();


    private ListValue currentListValue;
    private ArrayDeque<Value<?>> processedValuesForCurrentListStack;

    private final ArrayDeque<
            Pair<ListValue,
                    ArrayDeque<Value<?>>>
            > listValuesStack = new ArrayDeque<>();


    private final ArrayDeque<String> nestedEntriesKeysStack = new ArrayDeque<>();


    @Getter
    private final LinkedHashMap<String, Value<?>> indexedValues = new LinkedHashMap<>();

    // ---


    // ========= parse =========

    @Override
    public void enterParse(KvDatatypesParser.ParseContext ctx) {
        parsingStarted = true;

        if (ctx.ANY() != null) {
            errorOccurred = true;
        }
    }

    @Override
    public void exitParse(KvDatatypesParser.ParseContext ctx) {
        parseFinished = true;

        if (errorOccurred) {
            return;
        }

        // make sure processing finished.
        if (!listValuesStack.isEmpty()) {
            throw new ParsingInfraException("!listValuesStack.isEmpty()");
        }
        if (!nestedEntriesKeysStack.isEmpty()) {
            throw new ParsingInfraException("!nestedEntriesKeysStack.isEmpty()");
        }


        // make sure no duplicate keys found (otherwise input is invalid)
        if (processedKeysList.size() != processedKeysSet.size()) {
            Collections.sort(processedKeysList);

            ArrayList<String> temp = new ArrayList<>(processedKeysSet);
            Collections.sort(temp);

            System.out.println("list: " + processedKeysList);
            System.out.println("set: " + temp);

            ParsingException parsingException
                    = new ParsingException("(processedKeysList.size() != processedKeysSet.size()) duplicated keys found - check your input!");
            throw new UncheckedParsingException(parsingException);
        }



        // just pop the only one value in stack which is the result
        if (processedValuesStack.size() != 1) {
            throw new ParsingInfraException("processedValuesStack.size() != 1");
        }
        result = processedValuesStack.pop();

    }


    // ========= entry =========

    @Override
    public void enterEntry(KvDatatypesParser.EntryContext ctx) {
    }

    @Override
    public void exitEntry(KvDatatypesParser.EntryContext ctx) {
    }


    // ========= flatEntry =========

    @Override
    public void enterFlatEntry(KvDatatypesParser.FlatEntryContext ctx) {
    }

    @Override
    public void exitFlatEntry(KvDatatypesParser.FlatEntryContext ctx) {
    }

    // ========= nestedEntry =========

    @Override
    public void enterNestedEntry(KvDatatypesParser.NestedEntryContext ctx) {
        System.out.println("enterNestedEntry");

        if (ctx != null) {
            if (ctx.key() != null) {

                String key = ctx.key().getText().replace("\"", "");
                nestedEntriesKeysStack.push(key);
                currentKey = null;
            }
        }
    }

    @Override
    public void exitNestedEntry(KvDatatypesParser.NestedEntryContext ctx) {
        System.out.println("exitNestedEntry");
        if (ctx != null) {

            // when exiting from nested entry...extract create values and create nesting hierarchy
            if (!nestedEntriesKeysStack.isEmpty()) {

                // construct key path...
                final String keyPath = constructKeyPath();
                if (keyPath.isEmpty()) {
                    throw new ParsingInfraException("parser not in correct state");
                }


                // time to construct the value and save it
                final String key = nestedEntriesKeysStack.pop();
                final Value<?> v;
                if (currentListValue != null) { // if the nested entry occurred inside a list value then...

                    v = processedValuesForCurrentListStack.pop();
                    NestedValue nestedValue = new NestedValue(key, v);
                    processedValuesForCurrentListStack.push(nestedValue);


                    // index it
                    indexedValues.put(keyPath, nestedValue);

                } else {

                    v = processedValuesStack.pop();
                    NestedValue nestedValue = new NestedValue(key, v);
                    processedValuesStack.push(nestedValue);


                    // index it
                    indexedValues.put(keyPath, nestedValue);

                }

                // index also value
                indexedValues.put(keyPath + KEYPATH_JOINER + v.getKey(), v);

            } else {
                throw new ParsingInfraException("parser in incorrect state!");
            }
        }
    }


    // ========= listEntry =========

    @Override
    public void enterListEntry(KvDatatypesParser.ListEntryContext ctx) {
        System.out.println("enterListBodyStartNode");

        // save state if before exiting from a list value we found another one list value
        if (currentListValue != null) {
            listValuesStack.push(Pair.with(currentListValue, processedValuesForCurrentListStack));
        }


        // re-init for start processing the list
        final String key = ctx.key().getText();
        final String cleanedKey = key.replace("\"", "");

        currentListValue = new ListValue(cleanedKey);
        processedValuesForCurrentListStack = new ArrayDeque<>();
    }

    @Override
    public void exitListEntry(KvDatatypesParser.ListEntryContext ctx) {
        System.out.println("exitListBodyStartNode");
        if (currentListValue == null) {
            throw new ParsingInfraException("parser in incorrect state!");
        }



        // time to pop all processed values and collect them to a list value
        final ArrayDeque<Value<?>> tempStack = new ArrayDeque<>(); // just create a temp stack to maintain the order
        while (!processedValuesForCurrentListStack.isEmpty()) {
            Value<?> v = processedValuesForCurrentListStack.pop();
            tempStack.push(v);
        }
        while (!tempStack.isEmpty()) {
            currentListValue.add(tempStack.pop());
        }

        // construct key path...
        final String keyPath = constructKeyPath();

        // index it
        if (keyPath.isEmpty()) {
            indexedValues.put(currentListValue.getKey(), currentListValue);
        } else {
            indexedValues.put(keyPath, currentListValue);
        }


        // restore state of list value if exists and save in the correct structure the processed value.
        if (!listValuesStack.isEmpty()) {
            // if this list occurred inside another list just add it there...

            final ListValue temp = currentListValue;

            final Pair<ListValue, ArrayDeque<Value<?>>> popped = listValuesStack.pop();
            currentListValue = popped.getValue0();
            processedValuesForCurrentListStack = popped.getValue1();

            processedValuesForCurrentListStack.push(temp);

        } else {
            // if this list did not occur inside another list save it on the processed stack...
            processedValuesStack.push(currentListValue);
            currentListValue = null;
        }
    }


    // ========= value =========

    @Override
    public void enterValue(KvDatatypesParser.ValueContext ctx) {
    }

    @Override
    public void exitValue(KvDatatypesParser.ValueContext ctx) {
        System.out.println("exitValue");

        if (ctx != null) {

            // construct value
            Value<?> v = null;
            if (ctx.FloatValue() != null) {
                v = new FloatValue(currentKey, Float.parseFloat(ctx.getText()));
            }

            if (ctx.IntValue() != null) {
                v = new IntValue(currentKey, Integer.parseInt(ctx.getText()));
            }

            if (ctx.StringValue() != null) {
                String value = ctx.getText();
                value = value.substring(1, value.length() - 1); // drop "
                v = new StringValue(currentKey, value);
            }

            if (ctx.EmptyValue() != null) {
                v = new EmptyValue(currentKey);
            }

            // save value
            if (v != null) {

                // construct key path...
                final String keyPath = constructKeyPath();

                // index it...
                if (keyPath.isEmpty()) {
                    indexedValues.put(v.getKey(), v);
                } else {
                    indexedValues.put(keyPath, v);
                }

                if (currentListValue != null) {
                    // if this value belong to the currently processed list then add it to it's stack
                    processedValuesForCurrentListStack.push(v);
                } else {
                    // otherwise just mark it as processed
                    processedValuesStack.push(v);
                }


                currentKey = null;
            } else {
                throw new ParsingInfraException("exitValue: v == null");
            }
        }
    }


    // ========= key =========

    @Override
    public void enterKey(KvDatatypesParser.KeyContext ctx) {
    }

    @Override
    public void exitKey(KvDatatypesParser.KeyContext ctx) {
        if (ctx != null) {

            currentKey = ctx.getText();
            currentKey = currentKey.replace("\"", "");

            // tracked processed keys to identify if any duplicates exist
            processedKeysList.add(currentKey);
            processedKeysSet.add(currentKey);

        }
    }


    // ========= newline =========
    @Override
    public void enterNewline(KvDatatypesParser.NewlineContext ctx) {
    }


    @Override
    public void exitNewline(KvDatatypesParser.NewlineContext ctx) {
    }


    // ========= terminal =========
    @Override
    public void visitTerminal(TerminalNode node) {
    }


    // ========= error node =========
    @Override
    public void visitErrorNode(ErrorNode node) {
        errorOccurred = true;
    }


    // --- infra ---

    private String constructKeyPath() {
        final String[] objects = nestedEntriesKeysStack.toArray(new String[0]);
        final StringBuilder sb = new StringBuilder();
        for (int i = nestedEntriesKeysStack.size() - 1; i >= 0; i--) {
            String s = objects[i];
            sb.append(s);
            if (i != 0) {
                sb.append(KEYPATH_JOINER);
            }
        }
        return sb.toString();
    }

    // quick test
    public static void main(String[] args) {


        ArrayDeque<String> stack = new ArrayDeque<>();

        stack.push("a");
        stack.push("b");
        stack.push("c");

        System.out.println(String.join(",", stack));

        String[] objects = stack.toArray(new String[0]);
        for (int i=stack.size() - 1; i >= 0; i--) {
            System.out.println(objects[i]);
        }

    }

}
