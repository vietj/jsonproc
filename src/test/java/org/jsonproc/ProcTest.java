package org.jsonproc;

import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import org.junit.Test;

/** @author Julien Viet */
public class ProcTest {

  @Test
  public void testProc() throws Exception {

    JSONObject obj = new JSONObject();
    obj.foo = "bar";
    obj.juu = new JSONObject();
    obj.bar = new JSONObject();
    System.out.println("obj = " + obj);
    System.out.println("obj = " + obj);
    System.out.println("obj = " + obj);
    System.out.println("obj = " + obj);

  }

}
