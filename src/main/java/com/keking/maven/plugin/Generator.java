package com.keking.maven.plugin;

import java.util.List;

import com.keking.maven.plugin.mete.Database;
import com.keking.maven.plugin.mete.Table;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.maven.plugin.logging.Log;

public abstract interface Generator {
    public abstract List<CompilationUnit> generateAdditionalClasses(Table paramTable, Database paramDatabase);

    public abstract List<CompilationUnit> generateAdditionalClasses(Database paramDatabase);

    public abstract List<GeneralFileContent> generateAdditionalFiles(Database paramDatabase);

    public abstract List<GeneralFileContent> generateAdditionalFiles(Table paramTable, Database paramDatabase);

    public abstract void afterEntityGenerated(TopLevelClass paramTopLevelClass, Table paramTable);

    public abstract void afterKeyGenerated(TopLevelClass paramTopLevelClass);

    public abstract void setLogger(Log paramLog);

    public abstract void setTargetPackage(String paramString);
}
