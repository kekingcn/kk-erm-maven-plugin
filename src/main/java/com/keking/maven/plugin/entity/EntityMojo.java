package com.keking.maven.plugin.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.keking.maven.plugin.ERMImporter;
import com.keking.maven.plugin.GeneralFileContent;
import com.keking.maven.plugin.Generator;
import com.keking.maven.plugin.GeneratorUtils;
import com.keking.maven.plugin.mete.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.ibatis.ibator.api.dom.java.TopLevelEnumeration;
import org.apache.ibatis.ibator.internal.util.JavaBeansUtil;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;
/**
 * Created by kl on 2016/12/20.
 * Content :生成实体的mojo
 */
@Mojo(name = "entity", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true)
public class EntityMojo extends AbstractMojo {
    @org.apache.maven.plugins.annotations.Parameter(required = true)
    public String[] designs;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project.build.directory}/kl-entities")
    public String outputDirectory;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = ".*")
    public String tablePattern;

    @org.apache.maven.plugins.annotations.Parameter(required = true)
    public String targetModule;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "0")
    public Integer maxSymbolLength;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "false")
    public boolean useEnhancedSequenceGenerator;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "false")
    public boolean tableNameConstant;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Component
    public BuildContext buildContext;
    private List<Generator> generators = new ArrayList();

    public EntityMojo() {
        this.generators.add(new EqualsHashCode());
        this.generators.add(new FillDefaultValues());
        this.generators.add(new ToString());
        this.generators.add(new PropertyNameField());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String targetBasePackage = StringUtils.left(this.targetModule, StringUtils.lastIndexOf(this.targetModule, "."));

        if (this.buildContext.isIncremental()) {
            getLog().warn("增量编译");
        }
        try {

            this.project.addCompileSourceRoot(this.outputDirectory);

            List<Database> databases = new ArrayList();
            ERMImporter ermImporter = new ERMImporter(getLog());
            for (String design : this.designs) {
                File target = new File(this.project.getBasedir(), design);
                getLog().info("处理源文件：" + target.getCanonicalPath());
                Database db = ermImporter.doImport(target, this.tablePattern);
                databases.add(db);
            }
            for (Generator generator : this.generators) {
                generator.setLogger(getLog());
                generator.setTargetPackage(targetBasePackage);
            }
            List<CompilationUnit> allUnits = new ArrayList();
            Resource r = new Resource();
            r.setDirectory(this.outputDirectory);
            for (Database db : databases) {
                List<CompilationUnit> units = Lists.newArrayList();
                List<GeneralFileContent> files = Lists.newArrayList();

                List<CompilationUnit> entities = generateEntity(db, targetBasePackage);
                units.addAll(entities);
                for (Generator gen : this.generators) {
                    List<CompilationUnit> result = gen.generateAdditionalClasses(db);
                    if (result != null) {
                        units.addAll(result);
                    }
                    List<GeneralFileContent> f = gen.generateAdditionalFiles(db);
                    if (f != null) {
                        files.addAll(f);
                    }
                }
                Table table;
                for (Iterator i$ = db.getTables().iterator(); i$.hasNext(); ) {
                    table = (Table) i$.next();
                    for (Generator gen : this.generators) {
                        List<CompilationUnit> result = gen.generateAdditionalClasses(table, db);
                        if (result != null) {
                            units.addAll(result);
                        }
                        List<GeneralFileContent> f = gen.generateAdditionalFiles(table, db);
                        if (f != null) {
                            files.addAll(f);
                        }
                    }
                }

                for (CompilationUnit unit : units) {
                    GeneratedJavaFile gjf = new GeneratedJavaFile(unit, this.outputDirectory);
                    String filename = MessageFormat.format("{0}/{1}", new Object[]{StringUtils.replace(gjf.getTargetPackage(), ".", "/"), gjf.getFileName()});

                    files.add(new GeneralFileContent(filename, gjf.getFormattedContent()));
                }
                Set<String> filenames = Sets.newHashSet();
                for (GeneralFileContent file : files) {
                    File targetFile = new File(FilenameUtils.concat(this.outputDirectory, file.getFilename()));
                    FileUtils.forceMkdir(targetFile.getParentFile());
                    OutputStream os = this.buildContext.newFileOutputStream(targetFile);
                    os.write(file.getContent().getBytes(file.getEncoding()));
                    os.close();

                    r.addInclude(file.getFilename());

                    filenames.add(file.getFilename());
                }
                String dbFilename = db.getSource().getName();
                Object value = this.buildContext.getValue(dbFilename);
                if (value != null) {
                    Set<String> olds = (Set) value;
                    olds.removeAll(filenames);
                    for (String old : olds) {
                        getLog().info("删除" + old);
                        File fileOld = new File(new File(this.outputDirectory), old);
                        FileUtils.deleteQuietly(fileOld);
                        this.buildContext.refresh(fileOld);
                    }
                }
                this.buildContext.setValue(db.getSource().getName(), filenames);

                allUnits.addAll(units);
            }
            this.project.addResource(r);
        } catch (Exception e) {
            throw new MojoFailureException("生成过程出错", e);
        }
    }

    private TopLevelClass generateKeyClass(Table table) {
        TopLevelClass keyClass = new TopLevelClass(new FullyQualifiedJavaType(table.getJavaClass().getFullyQualifiedName() + "Key"));
        keyClass.setVisibility(JavaVisibility.PUBLIC);

        Method cm = new Method();
        cm.setConstructor(true);
        cm.setVisibility(JavaVisibility.PUBLIC);
        cm.setName(keyClass.getType().getShortName());
        cm.addBodyLine("");
        keyClass.addMethod(cm);

        cm = new Method();
        cm.setConstructor(true);
        cm.setVisibility(JavaVisibility.PUBLIC);
        cm.setName(keyClass.getType().getShortName());
        for (Column pc : table.getPrimaryKeyColumns()) {
            cm.addParameter(new org.apache.ibatis.ibator.api.dom.java.Parameter(pc.getJavaType(), pc.getPropertyName()));
            cm.addBodyLine(MessageFormat.format("this.{0} = {0};", new Object[]{pc.getPropertyName()}));
        }
        keyClass.addMethod(cm);
        for (Column pc : table.getPrimaryKeyColumns()) {
            GeneratorUtils.generateProperty(keyClass, pc.getJavaType(), pc.getPropertyName(), GeneratorUtils.generatePropertyJavadoc(pc), false);
        }
        keyClass.addSuperInterface(GeneratorUtils.forType(keyClass, Serializable.class.getCanonicalName()));
        keyClass.addAnnotation("@SuppressWarnings(\"serial\")");

        return keyClass;
    }

    private List<CompilationUnit> generateEntity(Database db, String basePackage) {
        List<CompilationUnit> generatedFiles = new ArrayList();

        Map<Table, TopLevelClass> generatingMap = new HashMap();
        for (Domain domain : db.getDomains()) {
            if (domain.getType() == null) {
                TopLevelEnumeration clazz = new TopLevelEnumeration(new FullyQualifiedJavaType(basePackage + ".enums." + GeneratorUtils.dbName2ClassName(domain.getCode()) + "Def"));
                domain.setType(clazz.getType());
                clazz.setVisibility(JavaVisibility.PUBLIC);
                for (Map.Entry<String, String> entry : domain.getValueMap().entrySet()) {
                    String value = (String) entry.getKey();
                    if (!CharUtils.isAsciiAlpha(value.charAt(0))) {
                        getLog().warn(MessageFormat.format("常量值不要以字母大头，跳过[{0}]-[{1}]", new Object[]{domain.getCode(), value}));
                    } else {
                        clazz.addEnumConstant(MessageFormat.format("/** {0} */\t{1}", new Object[]{entry.getValue(), entry.getKey()}));
                    }
                }
                generatedFiles.add(clazz);
            }
        }
        for (Table table : db.getTables()) {
            table.setJavaClass(new FullyQualifiedJavaType(basePackage + ".model." + JavaBeansUtil.getCamelCaseString(table.getDbName(), true)));
            for (Column col : table.getColumns()) {
                col.setPropertyName(JavaBeansUtil.getCamelCaseString(col.getDbName(), false));
            }
            TopLevelClass entityClass = new TopLevelClass(table.getJavaClass());
            entityClass.setVisibility(JavaVisibility.PUBLIC);
            if (StringUtils.isNotBlank(table.getTextName())) {
                entityClass.addJavaDocLine("/**");
                entityClass.addJavaDocLine(" * " + table.getTextName());
                entityClass.addJavaDocLine(" * @author http://kailing.pub");
                entityClass.addJavaDocLine(" */");
            }
            if (this.tableNameConstant) {
                Field tableNameField = new Field();
                tableNameField.setVisibility(JavaVisibility.PUBLIC);
                tableNameField.setStatic(true);
                tableNameField.setFinal(true);
                tableNameField.setType(FullyQualifiedJavaType.getStringInstance());
                tableNameField.setName("TABLE_NAME");
                tableNameField.setInitializationString('"' + table.getDbName() + '"');
                entityClass.addField(tableNameField);
            }
            entityClass.addSuperInterface(GeneratorUtils.forType(entityClass, Serializable.class.getCanonicalName()));
            entityClass.addAnnotation("@SuppressWarnings(\"serial\")");

            GeneratorUtils.forType(entityClass, "javax.persistence.Entity");
            entityClass.addAnnotation("@Entity");
            GeneratorUtils.forType(entityClass, "javax.persistence.Table");
            String tableName = table.getDbName();
            if ((this.maxSymbolLength.intValue() > 0) && (tableName.length() > this.maxSymbolLength.intValue())) {
                getLog().warn(String.format("表名[%s]超过设定的最大长度[%d]，将被截取", new Object[]{table.getDbName(), this.maxSymbolLength}));
                tableName = StringUtils.left(tableName, this.maxSymbolLength.intValue());
            }
            entityClass.addAnnotation("@Table(name=\"" + tableName + "\")");
            for (Column col : table.getColumns()) {
                if (col.getDomain() != null) {
                    col.setJavaType(col.getDomain().getType());
                }
            }
            List<Column> pks = table.getPrimaryKeyColumns();
            if (pks.size() > 1) {
                TopLevelClass keyClass = generateKeyClass(table);
                entityClass.addImportedType(keyClass.getType());
                entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.IdClass"));
                entityClass.addAnnotation(MessageFormat.format("@IdClass({0}.class)", new Object[]{keyClass.getType().getShortName()}));
                table.setJavaKeyClass(keyClass.getType());
                for (Generator generator : this.generators) {
                    generator.afterKeyGenerated(keyClass);
                }
                generatedFiles.add(keyClass);
            } else if (pks.size() == 1) {
                table.setJavaKeyClass(((Column) table.getPrimaryKeyColumns().get(0)).getJavaType());
            } else {
                throw new IllegalArgumentException("主键数量不对" + table.getDbName());
            }
            for (Column col : table.getColumns()) {
                Preconditions.checkArgument(col.getJavaType() != null, "属性[%s]没有分配类型，数据库[%s.%s]", new Object[]{col.getPropertyName(), table.getDbName(), col.getDbName()});
                if (GeneratorUtils.isJavaKeyword(col.getPropertyName())) {
                    getLog().warn(String.format("[%s.%s]是java关键字，跳过", new Object[]{table.getDbName(), col.getDbName()}));
                } else {
                    Field f = GeneratorUtils.generateProperty(entityClass, col.getJavaType(), col.getPropertyName(), GeneratorUtils.generatePropertyJavadoc(col), false);
                    if (table.getPrimaryKeyColumns().contains(col)) {
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
                        f.addAnnotation("@Id");
                        if (col.isIdentity()) {
                            entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
                            entityClass.addImportedType(new FullyQualifiedJavaType("org.hibernate.annotations.GenericGenerator"));
                            String genName = "GEN_" + table.getDbName();
                            if ((this.maxSymbolLength.intValue() > 0) && (genName.length() > this.maxSymbolLength.intValue())) {
                                getLog().warn(String.format("Sequence名[%s]超过设定的最大长度[%d]，将被截取", new Object[]{genName, this.maxSymbolLength}));
                                genName = StringUtils.left(genName, this.maxSymbolLength.intValue());
                            }
                            f.addAnnotation("@GeneratedValue(generator=\"" + genName + "\")");
                            if (!this.useEnhancedSequenceGenerator) {
                                f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"native\")");
                            } else {
                                entityClass.addImportedType(new FullyQualifiedJavaType("org.hibernate.annotations.Parameter"));
                                f.addAnnotation("@GenericGenerator(name=\"" + genName + "\", strategy=\"org.hibernate.id.enhanced.SequenceStyleGenerator\",\n" + "parameters = {@Parameter( name = \"prefer_sequence_per_entity\", value = \"true\")})");
                            }
                        }
                    }
                    if (col.getDomain() != null) {
                        f.addAnnotation("@Enumerated(EnumType.STRING)");
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Enumerated"));
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.EnumType"));
                    }
                    if (col.getJavaType().getShortName().equals("Date")) {
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Temporal"));
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.TemporalType"));
                        f.addAnnotation("@Temporal(value=TemporalType." + col.getTemporal() + ")");
                    }
                    if (col.isLob()) {
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Lob"));
                        f.addAnnotation("@Lob");
                    }
                    if (col.isLazy()) {
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Basic"));
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.FetchType"));
                        f.addAnnotation("@Basic(fetch=FetchType.LAZY)");
                    }
                    entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Column"));

                    String annoColumn = "@Column(";
                    annoColumn = annoColumn + "name=\"" + col.getDbName() + "\"";
                    annoColumn = annoColumn + ", nullable=" + (!col.isMandatory());
                    if (col.getJavaType().getShortName().equals("BigDecimal")) {
                        annoColumn = annoColumn + ", precision=" + col.getLength();
                        annoColumn = annoColumn + ", scale=" + col.getScale();
                    }
                    if ((col.getDomain() != null) || (col.getJavaType().equals(FullyQualifiedJavaType.getStringInstance()))) {
                        annoColumn = annoColumn + ", length=" + col.getLength();
                    }
                    annoColumn = annoColumn + ")";
                    f.addAnnotation(annoColumn);
                    if (col.isVersion()) {
                        entityClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Version"));
                        f.addAnnotation("@Version");
                    }
                }
            }
            for (Generator generator : this.generators) {
                generator.afterEntityGenerated(entityClass, table);
            }
            generatedFiles.add(entityClass);
            generatingMap.put(table, entityClass);
        }
        for (Relationship rel : db.getRelationships()) {
            TopLevelClass parentClass = (TopLevelClass) generatingMap.get(rel.getParent());
            TopLevelClass childClass = (TopLevelClass) generatingMap.get(rel.getChild());

            Field f = GeneratorUtils.generateProperty(childClass, parentClass.getType(), WordUtils.uncapitalize(parentClass.getType().getShortName()), null, false);
            if (rel.isOne2One()) {
                childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToOne"));
                f.addAnnotation("@OneToOne");
            } else {
                childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.ManyToOne"));
                f.addAnnotation("@ManyToOne");
            }
            childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.JoinColumn"));
            if (rel.getJoinColumns().size() > 1) {
                childClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.JoinColumns"));
                f.addAnnotation("@JoinColumns({");

                Iterator<JoinColumn> iter = rel.getJoinColumns().iterator();
                while (iter.hasNext()) {
                    JoinColumn jc = (JoinColumn) iter.next();
                    f.addAnnotation(MessageFormat.format("\t@JoinColumn(name=\"{0}\", referencedColumnName = \"{1}\", updatable=false, insertable=false){2}", new Object[]{jc.getFk().getDbName(), jc.getPk().getDbName(), iter.hasNext() ? "," : ""}));
                }
                f.addAnnotation("})");
            } else {
                JoinColumn jc = (JoinColumn) rel.getJoinColumns().get(0);
                f.addAnnotation(MessageFormat.format("@JoinColumn(name=\"{0}\", referencedColumnName = \"{1}\", updatable=false, insertable=false)", new Object[]{jc.getFk().getDbName(), jc.getPk().getDbName()}));
            }
            if (rel.isOne2One()) {
                parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToOne"));
                f = GeneratorUtils.generateProperty(parentClass, childClass.getType(), WordUtils.uncapitalize(childClass.getType().getShortName()), null, false);
                f.addAnnotation(MessageFormat.format("@OneToOne(mappedBy = \"{0}\", optional = true, cascade = CascadeType.ALL)", new Object[]{WordUtils.uncapitalize(parentClass.getType().getShortName())}));
            } else {
                parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.OneToMany"));
                FullyQualifiedJavaType fqjtSet = GeneratorUtils.forType(parentClass, "java.util.Set");
                fqjtSet.addTypeArgument(childClass.getType());
                f = GeneratorUtils.generateProperty(parentClass, fqjtSet, WordUtils.uncapitalize(childClass.getType().getShortName() + "s"), null, false);

                parentClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.CascadeType"));
                f.addAnnotation(MessageFormat.format("@OneToMany(mappedBy = \"{0}\", cascade = CascadeType.ALL)", new Object[]{WordUtils.uncapitalize(parentClass.getType().getShortName())}));
            }
        }
        return generatedFiles;
    }
}
