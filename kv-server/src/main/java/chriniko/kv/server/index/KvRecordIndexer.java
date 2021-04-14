package chriniko.kv.server.index;

import chriniko.kv.datatypes.KvDatatypesLexer;
import chriniko.kv.datatypes.KvDatatypesParser;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.error.UncheckedParsingException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedHashMap;

public class KvRecordIndexer {

    public static LinkedHashMap<String, Value<?>> process(String input) throws ParsingException {

        final KvDatatypesLexer kvDatatypesLexer = new KvDatatypesLexer(CharStreams.fromString(input));

        final CommonTokenStream tokens = new CommonTokenStream(kvDatatypesLexer);
        final KvDatatypesParser kvDatatypesParser = new KvDatatypesParser(tokens);

        final ParseTreeWalker walker = new ParseTreeWalker();
        final KvDatatypesIndexingListener kvDatatypesIndexingListener = new KvDatatypesIndexingListener();


        try {
            walker.walk(kvDatatypesIndexingListener, kvDatatypesParser.parse());
        } catch (UncheckedParsingException e) {
            throw e.getError();
        }

        if (kvDatatypesIndexingListener.isErrorOccurred()) {
            throw new ParsingException("not valid input provided");
        }

        return kvDatatypesIndexingListener.getIndexedValues();
    }

}
