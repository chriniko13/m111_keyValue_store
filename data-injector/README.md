
# Data Injector

## Description


The goal of the program is to randomly generate as much data as was specified by the `-n` parameter, up
to the given length ( -l ) of strings, up to (-m) keys per nesting level and up to the given nesting level
( -d ). Do not worry if the data do not make sense (e.g. age contains an address). This task is meant for
you to create datasets that you can use to develop the remaining of the project. 
You can use random key names and types for the keyFile.txt if you like and you can create your own keyFile.txt. 

For only the top-level keys you can either generate random strings or you can generate keys of the form key1,
key2, key3, etc. for easier debugging.



### How to run tests
* `mvn clean test`

### How to build and run jar
* `mvn clean install` (on the root module: `m111_kv_nikolaos_christidis`)
    * Sample output:
    ```text
      nchristidis@nchristidis-gl502vmk ~/Desktop/m111_kv_christidis_nikolaos (main)$ mvn clean install
      [INFO] Scanning for projects...
      [INFO] ------------------------------------------------------------------------
      [INFO] Reactor Build Order:
      [INFO]
      [INFO] m111_kv_christidis_nikolaos                                        [pom]
      [INFO] data-injector                                                      [jar]
      [INFO] kv-broker                                                          [jar]
      [INFO] kv-server                                                          [jar]
      [INFO] kv-storage                                                         [jar]
      [INFO]
      [INFO] --------------< org.example:m111_kv_christidis_nikolaos >---------------
      [INFO] Building m111_kv_christidis_nikolaos 1.0-SNAPSHOT                  [1/5]
      [INFO] --------------------------------[ pom ]---------------------------------
      [INFO]
  
      .....

      [INFO] Reactor Summary for m111_kv_christidis_nikolaos 1.0-SNAPSHOT:
      [INFO]
      [INFO] m111_kv_christidis_nikolaos ........................ SUCCESS [  0.262 s]
      [INFO] data-injector ...................................... SUCCESS [  4.830 s]
      [INFO] kv-broker .......................................... SUCCESS [  0.067 s]
      [INFO] kv-server .......................................... SUCCESS [  0.058 s]
      [INFO] kv-storage ......................................... SUCCESS [  0.028 s]
      [INFO] ------------------------------------------------------------------------
      [INFO] BUILD SUCCESS
      [INFO] ------------------------------------------------------------------------
      [INFO] Total time:  5.348 s
      [INFO] Finished at: 2021-03-14T11:56:24+02:00
      [INFO] ------------------------------------------------------------------------
    ```


* So now execute the jar with your selected input parameters, eg: `java -jar data-injector/target/data-injector-1.0-SNAPSHOT-jar-with-dependencies.jar 10 1 10 50`
  * Sample output:
  ```text
    nchristidis@nchristidis-gl502vmk ~/Desktop/m111_kv_christidis_nikolaos (main)$ java -jar data-injector/target/data-injector-1.0-SNAPSHOT-jar-with-dependencies.jar 10 1 10 50
    data generator finished successfully, check generated file at: /tmp/data-injector-dir13457210708802394815/dataToIndexff975b1a_13a1_4fe6_a1fc_bf8c1cf029d3.txt

  ```
  
  * ![](kv_di_1.png)
  
  * Contents of the generated file:
    ```text
    
        "78875434-f4dd-4442-baf4-0936c4d19167" : { "_contents" : [ { "_level" : 15 } ; { "_street" : "Apt. 946 4742 Heaney Field, New Marinville, SC 79754" } ; { "_name" : "jacinda.wilkinson" } ; { "_age" : 40 } ; { "_height" : 1.2673556 } ] }
        "bfdcc720-b5fe-490c-8467-85203711ddb5" : { "_contents" : [ { "_level" : 6 } ; { "_street" : "0468 Shanda Harbors, South Earleneview, WI 26933-1884" } ; { "_name" : "dallas.marvin" } ; { "_age" : 98 } ; { "_height" : 1.5102642 } ] }
        "90f08389-a999-4db5-924e-071daac50777" : { "_contents" : [ { "_level" : 17 } ; { "_street" : "Apt. 281 868 Arlen Pine, South Nedberg, DE 41187" } ; { "_name" : "freddie.bailey" } ; { "_age" : 36 } ; { "_height" : 1.4245868 } ] }
        "89c450e2-27bb-4ddd-af45-1ffd7239716b" : { "_contents" : [ { "_level" : 7 } ; { "_street" : "7937 Melaine Trail, Senaview, LA 81478" } ; { "_name" : "jimmie.shields" } ; { "_age" : 60 } ; { "_height" : 1.3048131 } ] }
        "8abb0041-e8a9-4e47-aa99-e290f1557de5" : { "_contents" : [ { "_level" : 9 } ; { "_street" : "813 Leena Forks, New Jackie, FL 52054" } ; { "_name" : "christopher.murphy" } ; { "_age" : 20 } ; { "_height" : 1.8269958 } ] }
        "ed6baa92-6f63-4ac6-9135-99062f6c7de0" : { "_contents" : [ { "_level" : 2 } ; { "_street" : "4924 Retta Trail, West Bernieceport, OK 20781-8246" } ; { "_name" : "bettie.lueilwitz" } ; { "_age" : 73 } ; { "_height" : 1.1231893 } ] }
        "695ac963-4b2f-4dd0-83f7-7ed9166e4962" : { "_contents" : [ { "_level" : 11 } ; { "_street" : "715 Douglas Plaza, Lonnietown, ID 01354" } ; { "_name" : "my.ullrich" } ; { "_age" : 82 } ; { "_height" : 1.1463675 } ] }
        "5d72fc6e-3c69-493e-8c47-61e3675ae5bf" : { "_contents" : [ { "_level" : 8 } ; { "_street" : "977 Elmo Glen, Masonberg, CO 69352-9967" } ; { "_name" : "newton.fisher" } ; { "_age" : 85 } ; { "_height" : 1.9146931 } ] }
        "57c35279-7f73-4716-b80d-a184ebc83d87" : { "_contents" : [ { "_level" : 12 } ; { "_street" : "003 Faviola Passage, Daughertyland, AZ 95143" } ; { "_name" : "wilford.huels" } ; { "_age" : 40 } ; { "_height" : 1.2333218 } ] }
        "63f773b0-c19d-48d8-b548-036dd7ad76dc" : { "_contents" : [ { "_level" : 9 } ; { "_street" : "Suite 623 8776 Herzog Turnpike, Kautzermouth, LA 39682-6208" } ; { "_name" : "arletta.mraz" } ; { "_age" : 81 } ; { "_height" : 1.3793638 } ] }

    ```
    
### More info in provided parameters
* ```text
        Parameters passed:
            -n  indicates the number of lines (i.e. separate data) that we would like to generate (e.g. 1000)

            -d  is the maximum level of nesting (i.e. how many times in a line a value can have a set of { key : values } ).
                Zero means no nesting, i.e. there is only one set of key-values per line (in the value of the
                high level key)

            -m  is the maximum number of keys inside each value.

            -l  is the maximum length of a string value whenever you need to generate a string. For example 4
            means that we can generate Strings of up to length 4 (e.g. “ab”, “abcd”, “a”). We should not generate
            empty strings (i.e. “” is not correct). Strings can be only letters (upper and lowercase) and numbers. No
            symbols.


            -k  keyFile.txt is a file containing a space-separated list of key names and their data types that we
                can potentially use for creating data. For example:

                name string
                age int
                height float
                street string
                level int
  ```
  
* Keep in mind that parameters are passed as positional (not name binding ones), correct usage: `java -jar data-injector/target/data-injector-1.0-SNAPSHOT-jar-with-dependencies.jar 10 1 10 50`

* If no -k (keyFile.txt) provided it will use the default one inside classpath resource: [sampleKeyFile.txt](src/main/resources/sampleKeyFile.txt)