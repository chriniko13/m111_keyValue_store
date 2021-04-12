
# Kv Datatypes

## Description

Contains the data types supported from `kv-server`:

* Flat values ([FlatValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/FlatValue.java)) such as:
  * [StringValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
  * [FloatValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
  * [EmptyValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
  * [IntValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
* Nested value: [NestedValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/NestedValue.java)
* List value: [ListValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/ListValue.java)


* Parser development history:
    At first a custom parser has been created to support the initial specification for the datatypes as explained in the
    assignment documentation, so the following code infra has been created: [DatatypesParser.java](src/main/java/chriniko/kv/datatypes/parser/DatatypesParser.java)
    which testing it I have seen that the complexity has risen dramatically (not maintainable code and in the future if we want
    a future request things will be hard).
    
    So for this reason, I pulled ANTLR4 (https://www.antlr.org/) to the game in
    order to make it extensible/more robust.
    
    So I defined with changing a little the grammar/definition for the datatypes, where someone can see it here: [KvDatatypes.g4](src/main/antlr4/chriniko/kv/datatypes/KvDatatypes.g4)
    and by executing `mvn org.antlr:antlr4-maven-plugin:antlr4 -pl kv-datatypes/`
    `KvDatatypesLexer`, `KvDatatypesParser` and `KvDatatypesBaseListener` has been generated:
      
    ![](../kv-datatypes/antlr4_generated_code.png)
  
    where I have extended the `KvDatatypesBaseListener` with [KvDatatypesAssemblyListener.java](src/main/java/chriniko/kv/datatypes/grammar/KvDatatypesAssemblyListener.java) in order to write the parsing logic,
     `f: (String) => Value<?>`
    so now we have an extensible grammar for future use.
    
    So we can understand that we can support very complex data representations, such as:
    ```text
    { "_myList" : [ { "_fn3" : { "_nf4" : { "_float23" : 2.34 } } } ; { "_strTemp" : "allGood allFine all work" } ; { "_fn32" : { "_nf42" : { "_someOtherStr" : "someOtherStrValue" } } } ] }
    ```
  
## How to generate the code from ANTLR4 (lexer, parser, listener, etc)
* Execute: `mvn org.antlr:antlr4-maven-plugin:antlr4 -pl kv-datatypes/`