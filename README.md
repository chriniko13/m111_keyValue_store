### KV Simple Replication - In Memo Database (KV stands for Key Value)

#### Assignee: nick.christidis@yahoo.com / cs1200005@di.uoa.gr



### Description


The `kv-infra` consists from the following modules:
* [data-injector](data-injector/README.md) which is responsible for producing the data which will be inserted to
  the `kv-server`.
  

  ![](data_injector_depends.png)

<hr/>
    
* [kv-broker](kv-broker/README.md) is responsible for communicating with the `kv-server` and performing the supported actions
  (get, put, delete, query). A client application, will have as dependency the `kv-broker` to interact with our `kv-server`.

  Both `kv-server` and `kv-broker` has been written with socket-programming(`java.nio.channels`) as infra (event loop - non blocking fashion).

  `kv-broker` has been developed as a library artifact ([KvBrokerApi.java](kv-broker/src/main/java/chriniko/kv/broker/KvBrokerApi.java)) 
  rather than a command line program which will receive input from the user as described in the assignment documentation.

  So, a third party program (client) if wants to use the `kv-server` should have `kv-broker` as a dependency, run one or more `kv-server`
  and start the `kv-broker` (inside client program because it has been used as a dependency) pointing to the running `kv-server(s)`.
  

  Moreover, it provides 4 consistency levels for executing the supported operations, these are the following:
    * ONE
    * ALL
    * QUORUM ((sum_of_replication_factors / 2) + 1)
    * REPLICATION FACTOR
  More can be checked at: [ConsistencyLevel.java](kv-broker/src/main/java/chriniko/kv/broker/ConsistencyLevel.java)


  ![](broker_architecture.svg)


  ![](kv_broker_depends.png)

  TODO mention a good test to see the usage of it....

<hr/>

* [kv-datatypes](kv-datatypes/README.md) the definition of supported values which `kv-server` can understand during deserialization of received string.
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
  * ```text
        { "_myList" : [ { "_fn3" : { "_nf4" : { "_float23" : 2.34 } } } ; { "_strTemp" : "allGood allFine all work" } ; { "_fn32" : { "_nf42" : { "_someOtherStr" : "someOtherStrValue" } } } ] }
    ```

  * ```text
        { "_studentDetails" : [ { "_username" : "chriniko" } ; { "_email" : "chriniko" } ; { "_address" : [ { "_street" : "Panepistimioupoli 123, Kesariani" } ; { "_postCode" : "16121" } ; { "_city" : "Athens" } ; { "_country" : "Greece" } ] } ; { "_name" : [ { "_firstname" : "Nikolaos" } ; { "_surname" : "Christidis" } ] } ] }
    ```

  * ```text
          { "_n1" : { "_someList" : [ { "_fn3" : { "_nf4" : { "_strTemp" : "allGood" } } } ; { "_n2" : { "_int2" : 2 } } ; { "_p3" : { "_n3List" : [ { "_gn3" : { "_gn4" : { "_gstrTemp" : "allGood" } } } ; { "_gf1" : { "_gf2" : { "_gf3" : { "_gf4" : { "_gfString" : "gfValue" } } } } } ] } } ; { "_n5" : { "_float2" : 2.34 } } ; { "_n71" : { "_n72" : { "_listGh" : [ { "_n3" : { "_n4" : { "_strTemp2" : "allGood" } } } ; { "_f1" : { "_f2" : { "_f3" : { "_f4" : { "_fString" : "fValue" } } } } } ] } } } ] } }
    ```

  To get a better idea of the supported types, you can check: [DatatypesAntlrParserComplexTest.java](kv-datatypes/src/test/java/chriniko/kv/datatypes/parser/antlr/DatatypesAntlrParserComplexTest.java)

  ![](kv_datatypes_depends.png)


<hr/>
  
* [kv-server](kv-server/README.md) is responsible for receiving the request from `kv-broker`, parsing it, executing
  the required logic, access if necessary the storage engine and reply-back to `kv-broker` with the correct response.
  Both `kv-server` and `kv-broker` has been written with socket-programming(`java.nio.channels`) as infra (event loop - non blocking fashion).

  ![](kv_server_depends.png)

  * Indexing process:
        For supporting query operation, I have implemented `KvRecord#indexContents()` method --> [KvRecord#indexContents.java](kv-server/src/main/java/chriniko/kv/server/infra/KvRecord.java)
        by using ANTLR4 defined grammar in `kv-datatypes`.
        So, I have created a listener [KvDatatypesIndexingListener.java](kv-server/src/main/java/chriniko/kv/server/index/KvDatatypesIndexingListener.java) which process the input string
        and returns a `LinkedHashMap<String, Value<?>>` which for each entry the key contains the key path and for the value the indexed record.
        <br/>
        <br/>
        The indexing process takes place during insert of the record (pre-calculation) which also can happen in an asynchronous manner for leveraging the hardware.
        ![](asyncIndexing.png)
        <br/>
        <br/>
    * Example:
      
      Input string: `"_chriniko1711" : { "_studentDetails" : [ { "_username" : "chriniko" } ; { "_email" : "chriniko" } ; { "_address" : [ { "_street" : "Panepistimioupoli 123, Kesariani" } ; { "_postCode" : "16121" } ; { "_city" : "Athens" } ; { "_country" : "Greece" } ] } ; { "_name" : [ { "_firstname" : "Nikolaos" } ; { "_surname" : "Christidis" } ] } ] }`
      
      Output after indexing:
      ![](indexing.png)
      <br/>
      <br/>
      So we can understand that the query operation has O(n) time complexity (where n is the length of the key) in order to find the record (eg: from above string `_chriniko1711`), and then is O(1)
      in order to access-find the value of the record based on provided key path(query key) which is indexed (eg: from above string `_studentDetails~>_username`)
      <br/>
      `value <- query('_chriniko1711', '_studentDetails~>_username');`
      <br/>
      <br/>
      In order to satisfy the exercise, the indexing of record does not only get supported from the above explained map, but also with an inner trie per record level.
      (Check to see how inner trie per record level is constructed: [KvRecord#indexContents.java](kv-server/src/main/java/chriniko/kv/server/infra/KvRecord.java))
      <br/>
      <br/>
      [KvStorageEngine#query.java](kv-server/src/main/java/chriniko/kv/server/infra/KvStorageEngine.java) takes as parameter if during query search to use trie or map.
      <br/>
      <br/>
      ![](trieOptionToQuery.png)
      <br/>
      <br/>
      Of course, by using the inner trie in order to query-search the provided key path of the target record the time complexity is O(n) where n is the length of the keypath.



<hr/>
    
* [kv-trie](kv-trie/README.md) contains infra code to support the Trie data structure. The [Trie.java](kv-trie/src/main/java/chriniko/kv/trie/Trie.java)
  is a no thread safe implementation.

  ![](kv_trie_depends.png)


<hr/>

* [kv-protocol](kv-protocol/README.md) contains common code which is used for the communication between `kv-server` and `kv-broker`.

  ![](kv_protocol_depends.png)


<hr/>

### Run all tests
* For running all tests execute: `mvn clean test`.

* For running tests of a selected module, execute for example: `mvn clean test -pl  kv-datatypes/`





### Build everything
* Just execute: `mvn clean install`




### How to run ?

TODO







<hr/>

### Useful tutorials
* Good info about ByteBuffers ==> `https://www.javacodegeeks.com/2012/12/the-java-bytebuffer-a-crash-course.html`

* `https://www.waitingforcode.com/java-i-o/handling-multiple-io-one-thread-nio-selector/read`

* [Antlr4 Personal Notes](kv-datatypes/antlr4_notes)