package chriniko.kv.datatypes.grammar;

import chriniko.kv.datatypes.KvDatatypesBaseListener;
import chriniko.kv.datatypes.KvDatatypesParser;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Experimental log listener to test grammar and understand better ANTLR4 mechanics.
 * It is used from {@link GrammarPoC} for fast testing-prototyping.
 */
public class KvDatatypesLogListener extends KvDatatypesBaseListener {



    // --- parse

    @Override
    public void enterParse(KvDatatypesParser.ParseContext ctx) {
        System.out.println("enterParse: " + ctx.getText());
        System.out.println();
    }

    @Override
    public void exitParse(KvDatatypesParser.ParseContext ctx) {
        System.out.println("exitParse: " + ctx.getText());
        System.out.println();
    }




    // --- entry

    @Override
    public void enterEntry(KvDatatypesParser.EntryContext ctx) {
        if (ctx != null) {
            System.out.println("enterEntry depth: " + ctx.depth());
            System.out.println();
//            if (ctx.entry().nested != null) {
//                System.out.println("enterEntry nested: " + ctx.entry().nested.getText());
//            }
        }
    }

    @Override
    public void exitEntry(KvDatatypesParser.EntryContext ctx) {
        if (ctx != null) {
            System.out.println("exitEntry depth: " + ctx.depth());

//            if (ctx.entry().nested != null) {
//                System.out.println("exitEntry nested: " + ctx.entry().nested.getText());
//            }
        }
    }

    // --- flat entry


    @Override
    public void enterFlatEntry(KvDatatypesParser.FlatEntryContext ctx) {
        System.out.println("enterFlatEntry: " + ctx.getText());
        System.out.println();
    }

    @Override
    public void exitFlatEntry(KvDatatypesParser.FlatEntryContext ctx) {
        System.out.println("exitFlatEntry: " + ctx.getText());
        System.out.println();
    }


    // --- nestedEntry

    @Override
    public void enterNestedEntry(KvDatatypesParser.NestedEntryContext ctx) {
        System.out.println("enterNestedEntry: " + ctx.getText());
        System.out.println();
    }

    @Override
    public void exitNestedEntry(KvDatatypesParser.NestedEntryContext ctx) {
        System.out.println("exitNestedEntry: " + ctx.getText());
        System.out.println();
    }



    // --- listEntry

    @Override
    public void enterListEntry(KvDatatypesParser.ListEntryContext ctx) {
        System.out.println("enterListEntry: " + ctx.getText());
        System.out.println();
    }

    @Override
    public void exitListEntry(KvDatatypesParser.ListEntryContext ctx) {
        System.out.println("exitListEntry: " + ctx.getText());
        System.out.println();
    }


    // --- value

    @Override
    public void enterValue(KvDatatypesParser.ValueContext ctx) {
        System.out.println("enterValue(float): " + ctx.FloatValue());
        System.out.println("enterValue(int): " + ctx.IntValue());
        System.out.println("enterValue(string): " + ctx.StringValue());
        System.out.println("enterValue(empty): " + ctx.EmptyValue());
        System.out.println();
    }

    @Override
    public void exitValue(KvDatatypesParser.ValueContext ctx) {
        System.out.println("exitValue(float): " + ctx.FloatValue());
        System.out.println("exitValue(int): " + ctx.IntValue());
        System.out.println("exitValue(string): " + ctx.StringValue());
        System.out.println("exitValue(empty): " + ctx.EmptyValue());
        System.out.println();
    }



    // --- key

    @Override
    public void enterKey(KvDatatypesParser.KeyContext ctx) {
        System.out.println("enterKey: " + ctx.ID());
        System.out.println();
    }

    @Override
    public void exitKey(KvDatatypesParser.KeyContext ctx) {
        System.out.println("exitKey: " + ctx.ID());
        System.out.println();
    }



    // --- newline
    @Override
    public void enterNewline(KvDatatypesParser.NewlineContext ctx) {
        System.out.println("enterNewLine: " + ctx.NL());
        System.out.println();
    }


    @Override
    public void exitNewline(KvDatatypesParser.NewlineContext ctx) {
        System.out.println("exitNewline: " + ctx.NL());
        System.out.println();
    }



    // --- terminal
    @Override
    public void visitTerminal(TerminalNode node) {
        //System.out.println("NODE: " + node);
    }



    // --- error node
    @Override
    public void visitErrorNode(ErrorNode node) {
        System.out.println("ERROR OCCURRED: " + node);
    }


}
