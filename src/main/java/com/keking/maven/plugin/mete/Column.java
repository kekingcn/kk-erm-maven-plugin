package com.keking.maven.plugin.mete;

import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

public class Column {
    private String dbName;
    private String textName;
    private String description;
    private int length;
    private int scale;
    private boolean mandatory;
    private FullyQualifiedJavaType javaType;
    private boolean lob = false;
    private boolean lazy = false;
    private boolean identity = false;
    private boolean version = false;
    private String hint;
    private String temporal;
    private Domain domain;
    private boolean simpleType = true;
    private String propertyName;
    private String id;

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getScale() {
        return this.scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isLob() {
        return this.lob;
    }

    public void setLob(boolean lob) {
        this.lob = lob;
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

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public FullyQualifiedJavaType getJavaType() {
        return this.javaType;
    }

    public void setJavaType(FullyQualifiedJavaType javaType) {
        this.javaType = javaType;
    }

    public Domain getDomain() {
        return this.domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getTemporal() {
        return this.temporal;
    }

    public void setTemporal(String temporal) {
        this.temporal = temporal;
    }

    public boolean isIdentity() {
        return this.identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public boolean isVersion() {
        return this.version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }

    public boolean isSimpleType() {
        return this.simpleType;
    }

    public void setSimpleType(boolean simpleType) {
        this.simpleType = simpleType;
    }

    public String getHint() {
        return this.hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isLazy() {
        return this.lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
}
