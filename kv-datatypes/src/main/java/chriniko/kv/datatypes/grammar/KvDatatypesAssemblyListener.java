package chriniko.kv.datatypes.grammar;

import chriniko.kv.datatypes.*;
import chriniko.kv.datatypes.error.ParsingException;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.javatuples.Pair;

import java.util.*;

public class KvDatatypesAssemblyListener extends KvDatatypesBaseListener {

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

    // ---


    // --- parse

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
            throw new IllegalStateException("!listValuesStack.isEmpty()");
        }
        if (!nestedEntriesKeysStack.isEmpty()) {
            throw new IllegalStateException("!nestedEntriesKeysStack.isEmpty()");
        }


        // make sure no duplicate keys found (otherwise input is invalid)
        if (processedKeysList.size() != processedKeysSet.size()) {
            Collections.sort(processedKeysList);

            ArrayList<String> temp = new ArrayList<>(processedKeysSet);
            Collections.sort(temp);

            System.out.println("list: " + processedKeysList);
            System.out.println("set: " + temp);
            throw new ParsingException("(processedKeysList.size() != processedKeysSet.size()) duplicated keys found - check your input!");
        }

        // just pop the only one value in stack which is the result
        if (processedValuesStack.size() != 1) {
            throw new IllegalStateException("processedValuesStack.size() != 1");
        }
        result = processedValuesStack.pop();

    }


    // --- entry

    @Override
    public void enterEntry(KvDatatypesParser.EntryContext ctx) {
    }

    @Override
    public void exitEntry(KvDatatypesParser.EntryContext ctx) {
    }


    // --- nestedEntry

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

                String key = nestedEntriesKeysStack.pop();

                if (currentListValue != null) {

                    Value<?> v = processedValuesForCurrentListStack.pop();
                    NestedValue nestedValue = new NestedValue(key, v);
                    processedValuesForCurrentListStack.push(nestedValue);

                } else {

                    Value<?> v = processedValuesStack.pop();
                    NestedValue nestedValue = new NestedValue(key, v);
                    processedValuesStack.push(nestedValue);
                }

            } else {
                throw new IllegalStateException();
            }
        }
    }


    // --- listEntry

    @Override
    public void enterListEntry(KvDatatypesParser.ListEntryContext ctx) {
    }

    @Override
    public void exitListEntry(KvDatatypesParser.ListEntryContext ctx) {
    }


    // start
    @Override
    public void enterListBodyStartNode(KvDatatypesParser.ListBodyStartNodeContext ctx) {
        System.out.println("enterListBodyStartNode");

        // save state if before exiting from a list value we found another one list value
        if (currentListValue != null) {
            listValuesStack.push(Pair.with(currentListValue, processedValuesForCurrentListStack));
        }

        // re-init for start processing the list
        currentListValue = new ListValue(currentKey);
        processedValuesForCurrentListStack = new ArrayDeque<>();
    }

    @Override
    public void exitListBodyStartNode(KvDatatypesParser.ListBodyStartNodeContext ctx) {
        System.out.println("exitListBodyStartNode");

        // time to pop all processed values and collect them to a list value
        final ArrayDeque<Value<?>> tempStack = new ArrayDeque<>(); // just create a temp stack to maintain the order

        while (!processedValuesForCurrentListStack.isEmpty()) {
            Value<?> v = processedValuesForCurrentListStack.pop();
            tempStack.push(v);
        }

        while (!tempStack.isEmpty()) {
            currentListValue.add(tempStack.pop());
        }

        processedValuesStack.push(currentListValue);


        // restore state of list value if exists.
        if (!listValuesStack.isEmpty()) {
            Pair<ListValue, ArrayDeque<Value<?>>> popped = listValuesStack.pop();

            currentListValue = popped.getValue0();
            processedValuesForCurrentListStack = popped.getValue1();

        } else {
            currentListValue = null;
        }
    }

    // mid
    @Override
    public void enterListBodyMidNode(KvDatatypesParser.ListBodyMidNodeContext ctx) {
    }

    @Override
    public void exitListBodyMidNode(KvDatatypesParser.ListBodyMidNodeContext ctx) {
    }

    // end
    @Override
    public void enterListBodyEndNode(KvDatatypesParser.ListBodyEndNodeContext ctx) {
    }

    @Override
    public void exitListBodyEndNode(KvDatatypesParser.ListBodyEndNodeContext ctx) {
    }

    // --- value

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

                if (currentListValue != null) {
                    // if this value belong to the currently processed list then add it to it's stack
                    processedValuesForCurrentListStack.push(v);
                } else {
                    // otherwise just mark it as processed
                    processedValuesStack.push(v);
                }


                currentKey = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }


    // --- key

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


    // --- newline
    @Override
    public void enterNewline(KvDatatypesParser.NewlineContext ctx) {
    }


    @Override
    public void exitNewline(KvDatatypesParser.NewlineContext ctx) {
    }


    // --- terminal
    @Override
    public void visitTerminal(TerminalNode node) {
    }


    // --- error node
    @Override
    public void visitErrorNode(ErrorNode node) {
        errorOccurred = true;
    }


}
