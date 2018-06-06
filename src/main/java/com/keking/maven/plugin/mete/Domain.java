package com.keking.maven.plugin.mete;

import java.util.LinkedHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

public class Domain {
    private String code;
    private String name;
    private String dbType;
    private FullyQualifiedJavaType type;
    private FullyQualifiedJavaType supportClientType;
    private LinkedHashMap<String, String> valueMap;

    public boolean hasValueMap() {
        return (this.valueMap != null) && (!this.valueMap.isEmpty());
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbType() {
        return this.dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public LinkedHashMap<String, String> getValueMap() {
        return this.valueMap;
    }

    public void setValueMap(LinkedHashMap<String, String> valueMap) {
        this.valueMap = valueMap;
    }

    public FullyQualifiedJavaType getType() {
        return this.type;
    }

    public void setType(FullyQualifiedJavaType type) {
        this.type = type;
    }

    public FullyQualifiedJavaType getSupportClientType() {
        return this.supportClientType;
    }

    public void setSupportClientType(FullyQualifiedJavaType supportClientType) {
        this.supportClientType = supportClientType;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Domain rhs = (Domain) obj;
        return new EqualsBuilder().append(this.supportClientType, rhs.supportClientType).append(this.type, rhs.type).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(this.type).append(this.supportClientType).toHashCode();
    }
}
