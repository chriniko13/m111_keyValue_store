

# Kv Broker

## Description

It is responsible for accepting queries and
redirecting requests to the Key-Value servers, collecting the results and presenting them to the user.



### How to run

* Execute: `java -jar kv-broker/target/kv-broker-1.0-SNAPSHOT-jar-with-dependencies.jar ~/Desktop/m111_kv_christidis_nikolaos/kv-broker/sampleServerFile.txt 3`
    * Sample output:
      ```text
            nchristidis@nchristidis-gl502vmk ~/Desktop/m111_kv_christidis_nikolaos (main)$ java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081
            >>>server__127.0.0.1### kvServer is up and running at: /127.0.0.1:8081
            server with name: server__127.0.0.1 is up at host: 127.0.0.1:8081
    
 
      ```

### More info in provided parameters
* ```text
        Parameters:
            -s serverFile.txt      --> file which has hostname and ports so that broker can connect
            -i dataToIndex.txt     --> not mandatory / path to file generated from `data-injector` which contains data that will be send from broker to kv-server
            -k 2                   --> replication factor
  ```

* Keep in mind that parameters are passed as positional (not name binding ones), correct usage: `java -jar kv-broker/target/kv-broker-1.0-SNAPSHOT-jar-with-dependencies.jar ~/Desktop/m111_kv_christidis_nikolaos/kv-broker/sampleServerFile.txt 3`

