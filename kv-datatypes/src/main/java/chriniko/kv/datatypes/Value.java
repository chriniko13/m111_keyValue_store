package chriniko.kv.datatypes;


import lombok.Getter;

/*



    As you can see, the payload (or value) is also a set of key-value pairs and can be nested. For example,
    person2 above, has another set of key values as the values of its subkey “address”. person4 has an
    empty value. We only allow records of the form above, i.e. we either have an empty value or a value
    with key-value pairs.

    For example the following data is incorrect:


    “person5” : “hello” <-- wrong value is not key:value or {}
    “person6” : { “address” : { “there” } } <-- wrong, value inside address is not key:value or {}

    For each value, we can store either an empty Integer (e.g. 12), a Float (e.g. 12.5), a String (e.g.
    “hello”, a set of Key Value pairs (e.g. { “key1” : 5 ; “key2” : “five” }, or an empty set of KV value pairs
    (i.e., {}). Each key is of type String only.

 */

@Getter
public abstract class Value<T> {

    protected final String key;

    public Value(String key) {
        this.key = key;
    }

    public String asString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("{")
                .append("\"").append(key).append("\" : ")
                .append(getValue())
                .append("}");

        return sb.toString();
    }

    public String asStringUnwrapped() {
        final StringBuilder sb = new StringBuilder();

        sb.append("\"").append(key).append("\" : ")
                .append(getValue());

        return sb.toString();
    }

    public abstract T getValue();

}
