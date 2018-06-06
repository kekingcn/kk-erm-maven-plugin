package com.keking.maven.plugin;

import com.google.common.collect.ImmutableSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.keking.maven.plugin.mete.Column;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;

public class GeneratorUtils {
    private static Set<String> javaKeywords = ImmutableSet.copyOf(new String[]{"abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while"});

    public static boolean isJavaKeyword(String word) {
        return javaKeywords.contains(word);
    }

    public static String dbName2ClassName(String dbName) {
        String s = dbName;

        boolean allUpperCaseOrNumeric = true;
        for (char c : s.toCharArray()) {
            if ((c != '_') && (!CharUtils.isAsciiNumeric(c)) && (!CharUtils.isAsciiAlphaUpper(c))) {
                allUpperCaseOrNumeric = false;
                break;
            }
        }
        if (allUpperCaseOrNumeric) {
            s = s.toLowerCase();
            s = WordUtils.capitalizeFully(s, new char[]{'_'});
            s = StringUtils.remove(s, "_");
        }
        if (!StringUtils.isAlpha(StringUtils.left(s, 1))) {
            s = "_" + s;
        }
        return s;
    }

    public static String dbName2PropertyName(String dbName) {
        return WordUtils.uncapitalize(dbName2ClassName(dbName));
    }

    public static FullyQualifiedJavaType forType(TopLevelClass topLevelClass, String type) {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(type);
        topLevelClass.addImportedType(fqjt);
        return fqjt;
    }

    public static Field generateProperty(TopLevelClass clazz, FullyQualifiedJavaType fqjt, String property, List<String> javadoc, boolean trimStrings) {
        clazz.addImportedType(fqjt);

        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(fqjt);
        field.setName(property);

        clazz.addField(field);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);
        method.setName(getGetterMethodName(field.getName(), field.getType()));
        StringBuilder sb = new StringBuilder();
        sb.append("return ");
        sb.append(property);
        sb.append(';');
        method.addBodyLine(sb.toString());

        createJavadoc(method, javadoc);

        clazz.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(getSetterMethodName(property));
        method.addParameter(new Parameter(fqjt, property));
        createJavadoc(method, javadoc);
        if ((trimStrings) && (fqjt.equals(FullyQualifiedJavaType.getStringInstance()))) {
            sb.setLength(0);
            sb.append("this.");
            sb.append(property);
            sb.append(" = ");
            sb.append(property);
            sb.append(" == null ? null : ");
            sb.append(property);
            sb.append(".trim();");
            method.addBodyLine(sb.toString());
        } else {
            sb.setLength(0);
            sb.append("this.");
            sb.append(property);
            sb.append(" = ");
            sb.append(property);
            sb.append(';');
            method.addBodyLine(sb.toString());
        }
        clazz.addMethod(method);

        return field;
    }

    private static void createJavadoc(Method method, List<String> javadoc) {
        if (javadoc != null) {
            method.addJavaDocLine("/**");
            for (String line : javadoc) {
                method.addJavaDocLine(" * <p>" + line + "</p>");
            }
            method.addJavaDocLine(" */");
        }
    }

    public static List<String> generatePropertyJavadoc(Column col) {
        try {
            List<String> result = new ArrayList();
            result.add(col.getTextName());

            String desc = col.getDescription();
            if (StringUtils.isNotBlank(desc)) {
                BufferedReader br = new BufferedReader(new StringReader(desc));
                String line = br.readLine();
                while (line != null) {
                    if (line.equals("///")) {
                        break;
                    }
                    line = StringUtils.remove(line, "[[");
                    line = StringUtils.remove(line, "]]");
                    result.add(line);
                    line = br.readLine();
                }
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getGetterMethodName(String property, FullyQualifiedJavaType fullyQualifiedJavaType) {
        String name = StringUtils.capitalize(property);
        if (fullyQualifiedJavaType.equals(FullyQualifiedJavaType.getBooleanPrimitiveInstance())) {
            name = "is" + name;
        } else {
            name = "get" + name;
        }
        return name;
    }

    public static String getSetterMethodName(String property) {
        String name = StringUtils.capitalize(property);
        return "set" + name;
    }

}
