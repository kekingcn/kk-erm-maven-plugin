package com.keking.maven.plugin.mete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

public class Table {
    private String dbName;
    private String textName;
    private String description;
    private List<Column> columns = new ArrayList();
    private List<Column> primaryKeyColumns = new ArrayList();
    private List<List<Column>> indexes = new ArrayList();
    private List<List<Column>> uniques = new ArrayList();
    private FullyQualifiedJavaType javaClass;
    private FullyQualifiedJavaType javaKeyClass;
    private HashMap<Object, Object> additionalProperties = new HashMap();

    public List<Column> getColumns() {
        return this.columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTextName() {
        return this.textName;
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }

    public List<Column> getPrimaryKeyColumns() {
        return this.primaryKeyColumns;
    }

    public void setPrimaryKeyColumns(List<Column> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
    }

    public FullyQualifiedJavaType getJavaClass() {
        return this.javaClass;
    }

    public void setJavaClass(FullyQualifiedJavaType javaClass) {
        this.javaClass = javaClass;
    }

    public FullyQualifiedJavaType getJavaKeyClass() {
        return this.javaKeyClass;
    }

    public void setJavaKeyClass(FullyQualifiedJavaType javaKeyClass) {
        this.javaKeyClass = javaKeyClass;
    }

    public List<List<Column>> getIndexes() {
        return this.indexes;
    }

    public void setIndexes(List<List<Column>> indexes) {
        this.indexes = indexes;
    }

    public HashMap<Object, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(HashMap<Object, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<List<Column>> getUniques() {
        return this.uniques;
    }

    public void setUniques(List<List<Column>> uniques) {
        this.uniques = uniques;
    }
}
