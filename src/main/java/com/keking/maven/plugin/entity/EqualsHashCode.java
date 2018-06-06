package com.keking.maven.plugin.entity;

import com.google.common.base.Objects;

import java.text.MessageFormat;

import com.keking.maven.plugin.AbstractGenerator;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

public class EqualsHashCode extends AbstractGenerator {
    public void afterKeyGenerated(TopLevelClass keyClass) {
        keyClass.addImportedType(new FullyQualifiedJavaType(Objects.class.getCanonicalName()));

        Method equals = new Method();
        equals.setName("equals");
        equals.setVisibility(JavaVisibility.PUBLIC);
        equals.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        equals.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "obj"));
        equals.addAnnotation("@Override");
        equals.addBodyLine("if (obj == null) { return false; }");
        equals.addBodyLine("if (obj == this) { return true; }");
        equals.addBodyLine("if (obj.getClass() != getClass()) {return false;}");
        equals.addBodyLine(MessageFormat.format("final {0} rhs = ({0}) obj;", new Object[]{keyClass.getType().getShortName()}));
        equals.addBodyLine("return");
        boolean first = true;
        for (Field field : keyClass.getFields()) {
            equals.addBodyLine(MessageFormat.format("\t{0} Objects.equal(this.{1}, rhs.{1})", new Object[]{!first ? "&&" : "", field.getName()}));

            first = false;
        }
        equals.addBodyLine(";");
        keyClass.addMethod(equals);

        Method hashCode = new Method();
        keyClass.addMethod(hashCode);
        hashCode.setName("hashCode");
        hashCode.setVisibility(JavaVisibility.PUBLIC);
        hashCode.setReturnType(FullyQualifiedJavaType.getIntInstance());
        hashCode.addAnnotation("@Override");
        hashCode.addBodyLine("return Objects.hashCode(");

        first = true;
        for (Field field : keyClass.getFields()) {
            hashCode.addBodyLine(MessageFormat.format("\t{0}this.{1}", new Object[]{!first ? "," : "", field.getName()}));

            first = false;
        }
        hashCode.addBodyLine("\t);");
    }
}
