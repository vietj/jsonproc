package org.jsonproc;

import com.sun.tools.javac.api.JavacTool;
import junit.framework.AssertionFailedError;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/** @author Julien Viet */
public class ProcTest {

  private static final javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
  private static final Locale locale = Locale.getDefault();
  private static final Charset charset = Charset.forName("UTF-8");

  private Error failure(Throwable cause) {
    AssertionFailedError afe = new AssertionFailedError();
    afe.initCause(cause);
    return afe;
  }

  private Error failure(String message) {
    return new AssertionFailedError(message);
  }

  //
  private ClassLoader assertCompile(String... sources) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager manager = javac.getStandardFileManager(diagnostics, locale, charset);
    List<File> files = new ArrayList<File>();
    for (String source : sources) {
      URL url = ProcTest.class.getResource(source);
      Assert.assertNotNull(url);
      try {
        files.add(new File(url.toURI()));
      }
      catch (URISyntaxException e) {
        throw failure(e);
      }
    }
    Iterable<? extends JavaFileObject> fileObjects = manager.getJavaFileObjects(files.toArray(new File[files.size()]));
    StringWriter out = new StringWriter();
    JavaCompiler.CompilationTask task = javac.getTask(
        out,
        manager,
        diagnostics,
        Collections.<String>emptyList(),
        Collections.<String>emptyList(),
        fileObjects);
    task.setLocale(locale);
    task.setProcessors(Collections.<Processor>singletonList(new JsonProcessor()));
    if (task.call()) {
      ClassLoader loader = new URLClassLoader(new URL[]{ProcTest.class.getResource("/")}, Thread.currentThread().getContextClassLoader());
      return loader;
    } else {
      StringWriter message = new StringWriter();
      PrintWriter writer = new PrintWriter(message);
      writer.append("Compilation of ").append(Arrays.toString(sources)).println(" failed:");
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())  {
        writer.append(diagnostic.getMessage(locale));
      }
      writer.println("console:");
      writer.append(out.getBuffer());
      throw failure(message.toString());
    }
  }

  @Test
  public void testAssignVariable() throws Exception {
    ClassLoader loader = assertCompile("/test01/Test.java");
    Class<?> clazz = loader.loadClass("test01.Test");
    Callable<JSONObject> callable = (Callable<JSONObject>)clazz.newInstance();
    JSONObject obj = callable.call();
    Assert.assertEquals(Collections.singleton("foo"), obj.keySet());
    Assert.assertEquals("bar", obj.get("foo"));
    // obj.foo = "bar";
    // obj.juu = new JSONObject();
    // obj.bar = new JSONObject();

  }

  @Test
  public void testAnonymousInnerSubclass() throws Exception {
    ClassLoader loader = assertCompile("/test02/Test.java");
    Class<?> clazz = loader.loadClass("test02.Test");
    Callable<JSONObject> callable = (Callable<JSONObject>)clazz.newInstance();
    JSONObject obj = callable.call();
    Assert.assertEquals(Collections.singleton("foo"), obj.keySet());
    Assert.assertEquals("bar", obj.get("foo"));
    // obj.foo = "bar";
    // obj.juu = new JSONObject();
    // obj.bar = new JSONObject();

  }
}
