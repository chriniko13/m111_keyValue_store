package chriniko.kv.datatypes.parser;

import chriniko.kv.datatypes.KvDatatypesLexer;
import chriniko.kv.datatypes.KvDatatypesParser;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.grammar.KvDatatypesAssemblyListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class DatatypesAntlrParser {

    public static Value<?> process(String input) {

        KvDatatypesLexer serverLogLexer = new KvDatatypesLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(serverLogLexer);

        KvDatatypesParser kvDatatypesParser = new KvDatatypesParser(tokens);

        ParseTreeWalker walker = new ParseTreeWalker();

        KvDatatypesAssemblyListener kvDatatypesAssemblyListener = new KvDatatypesAssemblyListener();
        walker.walk(kvDatatypesAssemblyListener, kvDatatypesParser.parse());


        if (kvDatatypesAssemblyListener.isErrorOccurred()) {
            throw new ParsingException("not valid input provided");
        }

        return kvDatatypesAssemblyListener.getResult();
    }
}
