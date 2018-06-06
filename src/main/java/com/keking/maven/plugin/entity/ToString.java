package com.keking.maven.plugin.entity;

import com.google.common.base.MoreObjects;
import java.text.MessageFormat;

import com.keking.maven.plugin.mete.Table;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import com.keking.maven.plugin.AbstractGenerator;

public class ToString extends AbstractGenerator {
  public void afterKeyGenerated(TopLevelClass keyClass)
  {
    createToString(keyClass);
  }
  
  public void afterEntityGenerated(TopLevelClass entityClass, Table table)
  {
    createToString(entityClass);
  }
  
  private void createToString(TopLevelClass clazz)
  {
    clazz.addImportedType(new FullyQualifiedJavaType(MoreObjects.class.getCanonicalName()));
    
    Method m = new Method();
    m.setName("toString");
    m.setVisibility(JavaVisibility.PUBLIC);
    m.setReturnType(FullyQualifiedJavaType.getStringInstance());
    m.addAnnotation("@Override");
    m.addBodyLine("return MoreObjects.toStringHelper(this)");
    for (Field field : clazz.getFields()) {
      if (!field.isStatic()) {
        m.addBodyLine(MessageFormat.format("\t.addValue(this.{0})", new Object[] { field.getName() }));
      }
    }
    m.addBodyLine("\t.toString();");
    clazz.addMethod(m);
  }
}
