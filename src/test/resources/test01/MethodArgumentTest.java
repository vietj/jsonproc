package test01;

import org.json.simple.JSONObject;

import java.util.concurrent.Callable;

/** @author Julien Viet */
public class MethodArgumentTest implements Callable<JSONObject> {
  public JSONObject call() throws Exception {
    JSONObject obj = new JSONObject();
    m(obj.foo = "bar");
    return obj;
  }
  void m(Object o) {
  }
}
