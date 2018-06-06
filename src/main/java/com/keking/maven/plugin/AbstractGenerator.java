package com.keking.maven.plugin;

import java.util.List;

import com.keking.maven.plugin.mete.Database;
import com.keking.maven.plugin.mete.Table;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractGenerator implements Generator {
    protected Log logger;
    private String targetPackage;

    public List<CompilationUnit> generateAdditionalClasses(Table table, Database database) {
        return null;
    }

    public List<CompilationUnit> generateAdditionalClasses(Database database) {
        return null;
    }

    public void afterEntityGenerated(TopLevelClass entityClass, Table table) {
    }

    public void afterKeyGenerated(TopLevelClass keyClass) {
    }

    public List<GeneralFileContent> generateAdditionalFiles(Database database) {
        return null;
    }

    public List<GeneralFileContent> generateAdditionalFiles(Table table, Database database) {
        return null;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public String getTargetPackage() {
        return this.targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }
}
