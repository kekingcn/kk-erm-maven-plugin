package com.keking.maven.plugin.entity;

import java.text.MessageFormat;

import com.keking.maven.plugin.mete.Column;
import com.keking.maven.plugin.mete.Table;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import com.keking.maven.plugin.AbstractGenerator;


public class FillDefaultValues extends AbstractGenerator {
  public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
    Method method = new Method();
    method.setName("fillDefaultValues");
    method.setVisibility(JavaVisibility.PUBLIC);
    for (Column col : table.getColumns()) {
      if ((!col.isIdentity()) && 
      
        (col.isMandatory()))
      {
        String type = col.getJavaType().getShortName();
        String value = "null";
        if (type.equals("String")) {
          value = "\"\"";
        } else if (type.equals("BigDecimal")) {
          value = "BigDecimal.ZERO";
        } else if (type.equals("Integer")) {
          value = "0";
        } else if (type.equals("Long")) {
          value = "0l";
        } else if (type.equals("Date")) {
          value = "new Date()";
        } else if (type.equals("Boolean")) {
          value = "false";
        } else if (col.getDomain() != null) {
          value = MessageFormat.format("{0}.values()[0]", new Object[] { col.getDomain().getType().getShortName() });
        }
        method.addBodyLine(MessageFormat.format("if ({0} == null) {0} = {1};", new Object[] { col.getPropertyName(), value }));
      }
    }
    if (!method.getBodyLines().isEmpty()) {
      entityClass.addMethod(method);
    }
  }
}
