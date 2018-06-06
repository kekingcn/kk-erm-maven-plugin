package com.keking.maven.plugin.entity;

import com.keking.maven.plugin.mete.Column;
import com.keking.maven.plugin.mete.Table;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import com.keking.maven.plugin.AbstractGenerator;

public class PropertyNameField extends AbstractGenerator {

    public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
        for (Column col : table.getColumns()) {
            Field f = new Field();
            f.setVisibility(JavaVisibility.PUBLIC);
            f.setStatic(true);
            f.setFinal(true);
            f.setType(FullyQualifiedJavaType.getStringInstance());
            f.setName("_" + col.getPropertyName());
            f.setInitializationString('"' + col.getPropertyName() + '"');

            entityClass.addField(f);
        }
    }
}
