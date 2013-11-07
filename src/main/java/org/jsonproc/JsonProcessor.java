package org.jsonproc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import org.json.simple.JSONObject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.Set;

/**
 * todo:
 * JSONObject.put has type Object and not the type of the value inserted
 *
 * @author Julien Viet
 */
@SupportedAnnotationTypes("*")
public class JsonProcessor extends AbstractProcessor {

  /** . */
  private Trees trees;

  /** . */
  private Attr attr;

  /** . */
  private TreeMaker maker;

  /** . */
  private TypeMirror JsonObject_TYPE;

  /** . */
  private TypeMirror JsonElement_TYPE;

  /** . */
  private TypeMirror JSONObject_TYPE;

  /** . */
  private TypeMirror STRING_TYPE;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    //
    Context context = ((JavacProcessingEnvironment)processingEnv).getContext();

    //
    this.trees = Trees.instance(processingEnv);
    this.attr = Attr.instance(context);
    this.maker = TreeMaker.instance(context);
    this.JsonObject_TYPE = processingEnv.getElementUtils().getTypeElement(JsonObject.class.getName()).asType();
    this.JsonElement_TYPE = processingEnv.getElementUtils().getTypeElement(JsonElement.class.getName()).asType();
    this.JSONObject_TYPE = processingEnv.getElementUtils().getTypeElement(JSONObject.class.getName()).asType();
    this.STRING_TYPE = processingEnv.getElementUtils().getTypeElement(String.class.getName()).asType();
  }

  private boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    return processingEnv.getTypeUtils().isAssignable(t1, t2);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getRootElements()) {
      attributeClass(element);
      TreePathScanner<Void, Void> visitor = new TreePathScanner<Void, Void>() {
        private final LinkedList<Boolean> inJSONClass = new LinkedList<Boolean>();
        @Override
        public Void visitClass(ClassTree node, Void p) {
          JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl)node;
          inJSONClass.addLast(isAssignable(classDecl.type, JSONObject_TYPE));
          try {
            return super.visitClass(node, p);
          }
          finally {
            inJSONClass.removeLast();
          }
        }
        @Override
        public Void visitExpressionStatement(ExpressionStatementTree node, Void p) {
          p = super.visitExpressionStatement(node, p);
          JCTree.JCExpressionStatement exec = (JCTree.JCExpressionStatement)node;
          exec.expr = foo(exec.expr);
          return p;
        }
        @Override
        public Void visitReturn(ReturnTree node, Void p) {
          p = super.visitReturn(node, p);
          JCTree.JCReturn ret = (JCTree.JCReturn)node;
          ret.expr = foo(ret.expr);
          return p;
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
          p = super.visitVariable(node, p);
          JCTree.JCVariableDecl variable = (JCTree.JCVariableDecl)node;
          variable.init = foo(variable.init);
          return p;
        }
        @Override
        public Void visitAssignment(AssignmentTree node, Void p) {
          p = super.visitAssignment(node, p);
          JCTree.JCAssign assign = (JCTree.JCAssign)node;
          assign.rhs = foo(assign.rhs);
          return p;
        }
        private JCTree.JCExpression foo(JCTree.JCExpression expr) {
          if (expr instanceof AssignmentTree) {
            AssignmentTree assignment = (AssignmentTree)expr;
            JCTree.JCExpression assignmentExpr = (JCTree.JCExpression)assignment.getExpression();
            JCTree.JCExpression assignmentVar = (JCTree.JCExpression)assignment.getVariable();
            if (assignmentVar instanceof JCTree.JCFieldAccess) {
              JCTree.JCFieldAccess access = (JCTree.JCFieldAccess)assignmentVar;
              String name = access.name.toString();
              Type type = assignmentExpr.type;
              Type selType = access.selected.type;
              if (isAssignable(selType, JsonObject_TYPE)) {
                if (isAssignable(type, STRING_TYPE)) {
                  access.name = (Name)processingEnv.getElementUtils().getName("addProperty");
                  return maker.Apply(
                      List.<JCTree.JCExpression>nil(),
                      access,
                      List.of(maker.Literal(name), assignmentExpr));
                } else if (isAssignable(type, JsonElement_TYPE)) {
                  access.name = (Name)processingEnv.getElementUtils().getName("add");
                  return maker.Apply(
                      List.<JCTree.JCExpression>nil(),
                      access,
                      List.of(maker.Literal(name), assignmentExpr));
                }
              } else if (isAssignable(selType, JSONObject_TYPE)) {
                access.name = (Name)processingEnv.getElementUtils().getName("put");
                return maker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    access,
                    List.of(maker.Literal(name), assignmentExpr));
              }
            } else if (inJSONClass.peekLast() && assignmentVar instanceof JCTree.JCIdent) {
              return maker.Apply(
                  List.<JCTree.JCExpression>nil(),
                  maker.Ident((Name)processingEnv.getElementUtils().getName("put")),
                  List.of(maker.Literal(((JCTree.JCIdent)assignmentVar).name.toString()), assignmentExpr));
            }
          }
          return expr;
        }
      };
      TreePath path = trees.getPath(element);
      visitor.scan(path, null);
    }
    return false;
  }

  public void attributeClass(Element classElement) {
    assert classElement.getKind() == ElementKind.CLASS;
    JCTree.JCClassDecl ct = (JCTree.JCClassDecl) trees.getTree(classElement);
    if (ct.sym != null) {
      if ((ct.sym.flags_field & Flags.UNATTRIBUTED) != 0) {
        attr.attribClass(ct.pos(), ct.sym);
      }
    }
  }
}
