package chriniko.kv.datatypes.grammar;

import chriniko.kv.datatypes.KvDatatypesLexer;
import chriniko.kv.datatypes.KvDatatypesParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class GrammarTest {



    @Test
    void grammarWorksAsExpected() throws Exception {

        InputStream inputStream = getClass().getResourceAsStream("/grammar_example.txt");
        Assertions.assertNotNull(inputStream);


        KvDatatypesLexer serverLogLexer = new KvDatatypesLexer(CharStreams.fromStream(inputStream));
        CommonTokenStream tokens = new CommonTokenStream( serverLogLexer );

        KvDatatypesParser kvDatatypesParser = new KvDatatypesParser(tokens);

        ParseTreeWalker walker = new ParseTreeWalker();

        KvDatatypesLogListener kvDatatypesLogListener = new KvDatatypesLogListener();
        walker.walk(kvDatatypesLogListener, kvDatatypesParser.entry());


        /*
                assertThat(logWalker.getEntries().size(), is(2));
                LogEntry error = logWalker.getEntries().get(1);
                assertThat(error.getLevel(), is(LogLevel.ERROR));
                assertThat(error.getMessage(), is("Bad thing happened"));
                assertThat(error.getTimestamp(), is(LocalDateTime.of(2018,5,5,14,20,24)));
         */

    }

}
