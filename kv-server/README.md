
# Kv Server

## Description

Is responsible for storing the actual data and will be handling the queries coming from the kv-broker.



### How to run

* Execute the jar with your selected input parameters, eg: `java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081`
    * Sample output:
      ```text
            nchristidis@nchristidis-gl502vmk ~/Desktop/m111_kv_christidis_nikolaos (main)$ java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081
            >>>server__127.0.0.1### kvServer is up and running at: /127.0.0.1:8081
            server with name: server__127.0.0.1 is up at host: 127.0.0.1:8081
    
 
      ```

### More info in provided parameters
* ```text
        Parameters passed:
             -a ip_address    ---> ip address
             -p port          ---> port
  ```

* Keep in mind that parameters are passed as positional (not name binding ones), correct usage: `java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081`
