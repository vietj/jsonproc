package test01;

import org.json.simple.JSONObject;

import java.util.concurrent.Callable;

/** @author Julien Viet */
public class ReturnTest implements Callable<JSONObject> {
  public JSONObject call() throws Exception {
    JSONObject obj = new JSONObject();
    Object s = a(obj);
    return obj;
  }

  private Object a(JSONObject obj) {
    return obj.foo = "bar";
  }
}
