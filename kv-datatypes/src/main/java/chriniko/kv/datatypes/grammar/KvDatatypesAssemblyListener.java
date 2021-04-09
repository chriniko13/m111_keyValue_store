package chriniko.kv.datatypes.grammar;

import chriniko.kv.datatypes.*;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayDeque;

public class KvDatatypesAssemblyListener  extends KvDatatypesBaseListener {

    @Getter
    private boolean errorOccurred = false;

    @Getter
    private Value<?> result;

    @Getter
    private boolean parseFinished = false;

    @Getter
    private boolean parsingStarted = false;

    // ---

    private final ArrayDeque<Value<?>> valuesStack = new ArrayDeque<>();


    private String currentKey;
    private ListValue currentListValue;
    private int currentListMidNodesCounter;


    private final ArrayDeque<String> nestedEntriesKeysStack = new ArrayDeque<>();

    // ---



    // --- parse

    @Override
    public void enterParse(KvDatatypesParser.ParseContext ctx) {
        parsingStarted = true;

        if (ctx.ANY() != null) {
            errorOccurred  = true;
        }
    }

    @Override
    public void exitParse(KvDatatypesParser.ParseContext ctx) {
        parseFinished = true;

        if (!valuesStack.isEmpty()) {
            result = valuesStack.pop();
        }
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
        if (ctx != null) {
           if (ctx.key() != null) {
               String key = ctx.key().getText().replace("\"", "");
               nestedEntriesKeysStack.push(key);
           }
        }
    }

    @Override
    public void exitNestedEntry(KvDatatypesParser.NestedEntryContext ctx) {
        if (ctx != null) {

            Value<?> v = valuesStack.pop();

            String key = nestedEntriesKeysStack.pop();

            NestedValue nestedValue = new NestedValue(key, v);

            valuesStack.push(nestedValue);
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
    public void enterListEntryStartNode(KvDatatypesParser.ListEntryStartNodeContext ctx) {
        currentListValue = new ListValue();
        currentListMidNodesCounter = 0;
    }

    @Override
    public void exitListEntryStartNode(KvDatatypesParser.ListEntryStartNodeContext ctx) {
        currentListMidNodesCounter++;

        // time to pop all processed values and collect them to a list value
        int count = 0;
        final ArrayDeque<Value<?>> tempStack = new ArrayDeque<>(); // just create a temp stack to maintain the order
        while (count++ < currentListMidNodesCounter) {
            Value<?> v = valuesStack.pop();
            tempStack.push(v);
        }

        while (!tempStack.isEmpty()) {
            currentListValue.add(tempStack.pop());
        }

        valuesStack.push(currentListValue);
    }

    // mid
    @Override
    public void enterListEntryMidNode(KvDatatypesParser.ListEntryMidNodeContext ctx) {
        currentListMidNodesCounter++;
    }

    @Override
    public void exitListEntryMidNode(KvDatatypesParser.ListEntryMidNodeContext ctx) {
    }

    // end
    @Override
    public void enterListEntryEndNode(KvDatatypesParser.ListEntryEndNodeContext ctx) {
    }

    @Override
    public void exitListEntryEndNode(KvDatatypesParser.ListEntryEndNodeContext ctx) {
        currentListMidNodesCounter++;
    }

    // --- value

    @Override
    public void enterValue(KvDatatypesParser.ValueContext ctx) {
    }

    @Override
    public void exitValue(KvDatatypesParser.ValueContext ctx) {
        if (ctx != null) {

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

            if (v != null) {
                valuesStack.push(v);
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
