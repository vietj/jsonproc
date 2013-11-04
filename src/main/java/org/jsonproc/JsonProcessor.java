package org.jsonproc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.Tree;
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
import java.util.Set;

/** @author Julien Viet */
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
    System.out.println("processing");
    System.out.println("processing");
    System.out.println("processing");
    System.out.println("processing");
    System.out.println("processing");
    for (Element element : roundEnv.getRootElements()) {
      attributeClass(element);
      TreePathScanner<Void, Void> visitor = new TreePathScanner<Void, Void>() {
        private com.sun.tools.javac.util.List statements;
        @Override
        public Void visitBlock(BlockTree node, Void aVoid) {
          List statements = (List)node.getStatements();
          if (statements != null) {
            this.statements = statements;
            try {
              while (!this.statements.isEmpty()) {
                Tree statement = (Tree)this.statements.get(0);
                scan(statement, null);
                this.statements = this.statements.tail;
              }
            }
            finally {
              this.statements = null;
            }
          }
          return null;
        }

        @Override
        public Void visitAssignment(AssignmentTree node, Void p) {
          super.visitAssignment(node, p);
          JCTree.JCExpression expr = (JCTree.JCExpression)node.getExpression();
          JCTree.JCExpression variable = (JCTree.JCExpression)node.getVariable();
          if (variable instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess access = (JCTree.JCFieldAccess)variable;
            String name = access.name.toString();
            Type exprType = expr.type;
            Type selType = access.selected.type;
            if (isAssignable(selType, JsonObject_TYPE)) {
              if (isAssignable(exprType, STRING_TYPE)) {
                access.name = (Name)processingEnv.getElementUtils().getName("addProperty");
                JCTree.JCMethodInvocation mi = maker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    access,
                    List.of(maker.Literal(name), expr)
                );
                statements.head = maker.Exec(mi);
              } else if (isAssignable(exprType, JsonElement_TYPE)) {
                access.name = (Name)processingEnv.getElementUtils().getName("add");
                JCTree.JCMethodInvocation mi = maker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    access,
                    List.of(maker.Literal(name), expr)
                );
                statements.head = maker.Exec(mi);
              }
            } else if (isAssignable(selType, JSONObject_TYPE)) {
              access.name = (Name)processingEnv.getElementUtils().getName("put");
              JCTree.JCMethodInvocation mi = maker.Apply(
                  List.<JCTree.JCExpression>nil(),
                  access,
                  List.of(maker.Literal(name), expr)
              );
              statements.head = maker.Exec(mi);
            }
          }
          return p;
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
