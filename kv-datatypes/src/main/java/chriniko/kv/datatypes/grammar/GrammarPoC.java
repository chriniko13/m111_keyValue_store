package chriniko.kv.datatypes.grammar;

import chriniko.kv.datatypes.KvDatatypesLexer;
import chriniko.kv.datatypes.KvDatatypesParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.InputStream;
import java.util.Objects;

public class GrammarPoC {

    public static void main(String[] args) throws Exception {

        InputStream inputStream = GrammarPoC.class.getResourceAsStream("/grammar_example.txt");
        Objects.requireNonNull(inputStream, "inputStream is null");


        KvDatatypesLexer serverLogLexer = new KvDatatypesLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokens = new CommonTokenStream( serverLogLexer );

        KvDatatypesParser kvDatatypesParser = new KvDatatypesParser(tokens);

        ParseTreeWalker walker = new ParseTreeWalker();

        KvDatatypesListener kvDatatypesListener = new KvDatatypesListener();
        walker.walk(kvDatatypesListener, kvDatatypesParser.entry());


    }
}
