A processor for Javac that provides similar JSON notation for Java with json-simple lib.

# Example 1

    JSONObject obj = new JSONObject();
    obj.foo = "abc"; 

equivalent to

    obj.put("foo", "abc");


# Example 2

    JSONObject ojb = new JSONObject() {{
       foo = "abc";
    }};

equivalent to

    JSONObject obj = new JSONObject() {{
      put("foo", "abc");
    }};

# Status

Experimental, put work, get is not implemented, many case not implemented, but it works :-)

