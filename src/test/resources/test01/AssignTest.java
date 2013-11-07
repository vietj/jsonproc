package test01;

import org.json.simple.JSONObject;

import java.util.concurrent.Callable;

/** @author Julien Viet */
public class AssignTest implements Callable<JSONObject> {
  class Holder {
    Object o;
  }
  public JSONObject call() throws Exception {
    JSONObject obj = new JSONObject();
    Holder holder = new Holder();
    holder.o = obj.foo = "bar";
    return obj;
  }
}
