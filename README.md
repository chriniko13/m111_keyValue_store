### KV In Memo Database (KV stands for Key Value)

#### Assignee: nick.christidis@yahoo.com / cs1200005@di.uoa.gr



### Description


The kv consists from the following modules:
* [data-injector](data-injector/README.md) which is responsible for producing the data which will be inserted to
  the `kv-server`.
  

  ![](data_injector_depends.png)

    
* [kv-broker](kv-broker/README.md) which is responsible for communicating with the `kv-server` and performing the supported actions
  (get, put, delete, query). A client application, will have as dependency the `kv-broker` to interact with our `kv-server`.

  ![](kv_broker_depends.png)


* [kv-datatypes](kv-datatypes/README.md) here are defined the values which are supported from `kv-server`.
    * Flat values ([FlatValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/FlatValue.java)) such as:
      * [StringValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
      * [FloatValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
      * [EmptyValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
      * [IntValue](kv-datatypes/src/main/java/chriniko/kv/datatypes/StringValue.java)
    * Nested value: [NestedValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/NestedValue.java)
    * List value: [ListValue.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/ListValue.java)
    
  
    For this, at first a custom parser has been created to support the initial specification for the datatypes as explained in the
    assignment documentation, so the following code infra has been created: [DatatypesParser.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/parser/DatatypesParser.java)
    which testing it I have seen that the complexity has risen dramatically (not maintainable code and in the future if we want
    a future request things will be hard).
  
    So for this reason, I pulled ANTLR4 (https://www.antlr.org/) to the game in
    order to make it extensible/more robust for the future (my motivation is to continue working on this KV after the class also)
    
    So I defined with changing a little the grammar/definition for the datatypes, where someone can see it here: [KvDatatypes.g4](kv-datatypes/src/main/antlr4/chriniko/kv/datatypes/KvDatatypes.g4)
    and by executing `mvn org.antlr:antlr4-maven-plugin:antlr4 -pl kv-datatypes/`
    `KvDatatypesLexer`, `KvDatatypesParser` and `KvDatatypesBaseListener` has been generated:
  
    ![](kv-datatypes/antlr4_generated_code.png)
  
    where I have extended the `KvDatatypesBaseListener` with [KvDatatypesAssemblyListener.java](kv-datatypes/src/main/java/chriniko/kv/datatypes/grammar/KvDatatypesAssemblyListener.java) in order to write the parsing logic,
     `f: (String) => Value<?>`
    so now we have an extensible grammar for future use.

   So we can understand that we can support very complex data representations, such as:
      ```text
            { "_myList" : [ { "_fn3" : { "_nf4" : { "_float23" : 2.34 } } } ; { "_strTemp" : "allGood allFine all work" } ; { "_fn32" : { "_nf42" : { "_someOtherStr" : "someOtherStrValue" } } } ] }
      ```

  ![](kv_datatypes_depends.png)



  
* [kv-server](kv-server/README.md) which is responsible for receiving the request from `kv-broker`, parsing it, executing
  the required logic, access if necessary the storage engine and reply-back to `kv-broker` with the correct response.
  Both `kv-server` and `kv-broker` has been written with `java.nio.channels` as infra (event loop - non blocking fashion).

  ![](kv_server_depends.png)


    
* [kv-trie](kv-trie/README.md) contains infra code to support the Trie data structure. The [Trie.java](kv-trie/src/main/java/chriniko/kv/trie/Trie.java)
  is a no thread safe implementation.

  ![](kv_trie_depends.png)





### Run all tests
* For running all tests execute: `mvn clean test`.

* For running tests of a selected module, execute for example: `mvn clean test -pl  kv-datatypes/`





### Build everything
* Just execute: `mvn clean install`



### How to run ?

TODO








### Useful tutorials
* Good info about ByteBuffers ==> `https://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html`

* `https://www.waitingforcode.com/java-i-o/handling-multiple-io-one-thread-nio-selector/read`

* [Antlr4 Personal Notes](kv-datatypes/antlr4_notes)