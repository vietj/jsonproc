package test01;

import org.json.simple.JSONObject;

import java.util.concurrent.Callable;

/** @author Julien Viet */
public class VariableDeclTest implements Callable<JSONObject> {
  public JSONObject call() throws Exception {
    JSONObject obj = new JSONObject();
    Object o = obj.foo = "bar";
    return obj;
  }
}
