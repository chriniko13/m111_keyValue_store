
# Kv Server

## Description

Is responsible for storing the actual data and will be handling the queries coming from the kv-broker.



### How to run

* Execute the jar with your selected input parameters, eg: `java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081 18081`
    * Sample output:
      ```text
            java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081 18081
            >>>server__127.0.0.1:8081### kvServer is up and running at: /127.0.0.1:8081
            server with name: server__127.0.0.1:8081 is up at host: 127.0.0.1:8081
            Apr 16, 2021 10:53:34 PM io.undertow.Undertow start
            INFO: starting server: Undertow - 2.2.7.Final
            Apr 16, 2021 10:53:34 PM org.xnio.Xnio <clinit>
            INFO: XNIO version 3.8.0.Final
            Apr 16, 2021 10:53:34 PM org.xnio.nio.NioXnio <clinit>
            INFO: XNIO NIO Implementation Version 3.8.0.Final
            Apr 16, 2021 10:53:34 PM org.jboss.threads.Version <clinit>
            INFO: JBoss Threads version 3.1.0.Final
            actuator is up at port: 18081

      ```

### More info in provided parameters
* ```text
        Parameters passed:
             -a ip_address    ---> ip address
             -p port          ---> port
             -aP actuatorPort ---> actuator port

  ```

* Keep in mind that parameters are passed as positional (not name binding ones), correct usage: `java -jar kv-server/target/kv-server-1.0-SNAPSHOT-jar-with-dependencies.jar 127.0.0.1 8081 18081`
