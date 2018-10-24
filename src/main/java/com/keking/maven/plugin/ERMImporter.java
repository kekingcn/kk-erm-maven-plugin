package com.keking.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.keking.maven.plugin.mete.Column;
import com.keking.maven.plugin.mete.Database;
import com.keking.maven.plugin.mete.Domain;
import com.keking.maven.plugin.mete.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ERMImporter {
    private Log logger;

    public ERMImporter(Log logger) {
        this.logger = logger;
    }

    private FullyQualifiedJavaType fqjtInteger = new FullyQualifiedJavaType("java.lang.Integer");
    private FullyQualifiedJavaType fqjtBigDecimal = new FullyQualifiedJavaType("java.math.BigDecimal");
    private FullyQualifiedJavaType fqjtDate = new FullyQualifiedJavaType("java.util.Date");
    private FullyQualifiedJavaType fqjtBlob = new FullyQualifiedJavaType("java.sql.Blob");
    private static final Pattern hintPattern = Pattern.compile("\\[\\[.*\\]\\]");
    private static final Pattern javaTypePattern = Pattern.compile("!!!.*!!!");
    private static final String enumPrefix = "///@";

    public Database doImport(File ermSource, String tablePattern) throws DocumentException, IOException {
        Database result = new Database();
        result.setSource(ermSource);

        SAXReader sar = new SAXReader();
        Document docSource = sar.read(ermSource);

        Map<String, Element> words = new HashMap();
        Map<String, Domain> domains = new HashMap();
        for (Object nodeWord1 : docSource.selectNodes("/diagram/dictionary/word")) {
            Element nodeWord = (Element) nodeWord1;
            String id = nodeWord.elementText("id");
            words.put(id, nodeWord);
            //忽略备注
//            Domain domain = parseDomain(nodeWord.elementText("physical_name"), nodeWord.elementText("description"));
//            if (domain != null) {
//                domains.put(id, domain);
//                result.getDomains().add(domain);
//            }
        }
        Map<String, Element> tables = new HashMap();
        for (Object nodeTable1 : docSource.selectNodes("/diagram/contents/table")) {
            Element nodeTable = (Element) nodeTable1;

            tables.put(nodeTable.elementText("id"), nodeTable);
        }
        Map<String, Column> allColumns = new HashMap();
        for (Element nodeTable : tables.values()) {
            Table table = new Table();
            table.setDbName(nodeTable.elementText("physical_name"));
            table.setTextName(nodeTable.elementText("logical_name"));
            this.logger.debug(table.getDbName());

            Set<String> columnNames = new HashSet();
            for (Object nodeColumn1 : nodeTable.selectNodes("columns/*")) {
                Element nodeColumn = (Element) nodeColumn1;

                Column column = new Column();
                this.logger.debug(nodeColumn.getName());

                String word_id = nodeColumn.elementText("word_id");
                if (word_id == null) {
                    Element node = nodeColumn;
                    do {
                        String refId = node.elementText("referenced_column");
                        if (refId == null) {
                            throw new IllegalArgumentException();
                        }
                        node = (Element) docSource.selectSingleNode("//table/columns/*[id='" + refId + "']");
                        word_id = node.elementText("word_id");
                    } while (StringUtils.isEmpty(word_id));
                }
                Element nodeWord = (Element) words.get(word_id);

                String physicalName = nodeColumn.elementText("physical_name");
                if (StringUtils.isBlank(physicalName)) {
                    physicalName = nodeWord.elementText("physical_name");
                }
                physicalName = StringUtils.remove(physicalName, '\n');
                physicalName = StringUtils.remove(physicalName, '\r');
                column.setDbName(physicalName);
                column.setIdentity(Boolean.valueOf(nodeColumn.elementText("auto_increment")).booleanValue());

                String logicalName = nodeColumn.elementText("logical_name");
                if (StringUtils.isBlank(logicalName)) {
                    logicalName = nodeWord.elementText("logical_name");
                }
                column.setTextName(logicalName);
                column.setDescription(nodeWord.elementText("description"));
                column.setId(nodeColumn.elementText("id"));
                column.setMandatory(Boolean.parseBoolean(nodeColumn.elementText("not_null")));

                column.setHint(extractHint(column.getDescription()));

                String type = nodeWord.elementText("type");
                String length = nodeWord.elementText("length");
                String decimal = nodeWord.elementText("decimal");
                String javaType = extractJavaType(column.getDescription());
                Domain domain = extractEnumType(column.getDescription());
                if (javaType != null) {
                    column.setJavaType(new FullyQualifiedJavaType(javaType));
                    column.setLength(Integer.parseInt(length));
                } else if (domain != null) {
                    column.setDomain(domain);
                    column.setLength(Integer.parseInt(length));
                } else if ("char".equals(type)) {
                    column.setJavaType(FullyQualifiedJavaType.getStringInstance());
                    column.setLength(1);
                } else if (("character(n)".equals(type)) || ("varchar(n)".equals(type))) {
                    column.setJavaType(FullyQualifiedJavaType.getStringInstance());
                    column.setLength(Integer.parseInt(length));
                } else if ("decimal".equals(type)) {
                    this.logger.warn(MessageFormat.format("decimal没有指定长度，按1处理[{0}], {1}, {2}", new Object[]{type, column.getDbName(), table.getDbName()}));
                    column.setJavaType(this.fqjtInteger);
                    column.setLength(1);
                } else if (("decimal(p)".equals(type)) || ("numeric(p)".equals(type)) || ("float(p)".equals(type))) {
                    int l = Integer.parseInt(length);
                    if (l <= 9) {
                        column.setJavaType(this.fqjtInteger);
                    } else {
                        column.setJavaType(this.fqjtBigDecimal);
                    }
                    column.setLength(l);
                } else if (("decimal(p,s)".equals(type)) || ("numeric(p,s)".equals(type)) || ("float(m,d)".equals(type)) || ("double(m,d)".equals(type))) {
                    int l = Integer.parseInt(length);
                    int s = Integer.parseInt(decimal);
                    if ((s == 0) && (l <= 9)) {
                        column.setJavaType(this.fqjtInteger);
                    } else {
                        column.setJavaType(this.fqjtBigDecimal);
                    }
                    column.setLength(l);
                    column.setScale(s);
                } else if (("integer".equals(type)) || ("int".equals(type)) || ("tinyint".equals(type)) || ("smallint".equals(type)) || ("mediumint".equals(type))) {
                    column.setJavaType(this.fqjtInteger);
                    column.setLength(9);
                } else if ("bigint".equals(type)) {
                    column.setJavaType(new FullyQualifiedJavaType("java.lang.Long"));
                    column.setLength(18);
                } else if ("date".equals(type)) {
                    column.setJavaType(this.fqjtDate);
                    column.setTemporal("DATE");
                } else if ("time".equals(type)) {
                    column.setJavaType(this.fqjtDate);
                    column.setTemporal("TIME");
                } else if (("timestamp".equals(type)) || ("datetime".equals(type))) {
                    column.setJavaType(this.fqjtDate);
                    column.setTemporal("TIMESTAMP");
                } else if (("clob".equals(type)) || ("tinytext".equals(type)) || ("text".equals(type)) || ("mediumtext".equals(type)) || ("longtext".equals(type))) {
                    column.setJavaType(FullyQualifiedJavaType.getStringInstance());
                    column.setLob(true);
                } else if (type.endsWith("blob")) {
                    column.setJavaType(this.fqjtBlob);
                    column.setLob(true);
                    column.setLazy(true);
                } else if (("boolean".equals(type)) || ("bit".equals(type))) {
                    column.setJavaType(new FullyQualifiedJavaType(Boolean.class.getCanonicalName()));
                } else {
                    this.logger.warn(MessageFormat.format("无法识别的类型[{0}]，跳过, {1}, {2}", new Object[]{type, column.getDbName(), table.getDbName()}));
                    continue;
                }
                if (type.startsWith("numeric")) {
                    this.logger.warn(MessageFormat.format("建议不要使用numeric，用decimal代替[{0}], {1}, {2}", new Object[]{type, column.getDbName(), table.getDbName()}));
                }
                if (type.startsWith("datetime")) {
                    this.logger.warn(MessageFormat.format("建议不要使用datetime，用timestamp代替[{0}], {1}, {2}", new Object[]{type, column.getDbName(), table.getDbName()}));
                }
                if (domain == null && column.getDescription() != null && column.getDescription().startsWith("///")) {
                    this.logger.warn(MessageFormat.format("枚举类型格式不正确，使用其他类型代替[{0}] -> [{1}], {2}, {3}", new Object[]{type, column.getJavaType().getFullyQualifiedName(), column.getDbName(), table.getDbName()}));
                }
                column.setVersion(("JPA_VERSION".equalsIgnoreCase(column.getDbName())) || ("JPA_TIMESTAMP".equalsIgnoreCase(column.getDbName())));
                if ("true".equals(nodeColumn.elementText("unique_key"))) {
                    List<Column> unique = new ArrayList();
                    unique.add(column);
                    table.getUniques().add(unique);
                }
                if (columnNames.contains(column.getDbName())) {
                    this.logger.warn(MessageFormat.format("字段重复，跳过 {0}, {1}", new Object[]{column.getDbName(), table.getDbName()}));
                } else {
                    columnNames.add(column.getDbName());

                    allColumns.put(column.getId(), column);
                    table.getColumns().add(column);
                    if (Boolean.parseBoolean(nodeColumn.elementText("primary_key"))) {
                        table.getPrimaryKeyColumns().add(column);
                    }
                    if (domains.containsKey(word_id)) {
                        column.setDomain((Domain) domains.get(word_id));
                    }
                }
            }
            if (table.getPrimaryKeyColumns().isEmpty()) {
                this.logger.warn(table.getDbName() + " 没有主键，跳过");
            } else {
                for (Object nodeIndex1 : nodeTable.selectNodes("indexes/*")) {
                    Element nodeIndex = (Element) nodeIndex1;

                    List<Column> index = new ArrayList();
                    for (Object nodeColumn1 : nodeIndex.selectNodes("columns/column")) {
                        Element nodeColumn = (Element) nodeColumn1;

                        index.add(allColumns.get(nodeColumn.elementText("id")));
                    }
                    table.getIndexes().add(index);
                }
                for (Object nodeIndex1 : nodeTable.selectNodes("complex_unique_key_list/complex_unique_key")) {
                    Element nodeIndex = (Element) nodeIndex1;
                    List<Column> unique = new ArrayList();
                    for (Object nodeColumn1 : nodeIndex.selectNodes("columns/column")) {
                        Element nodeColumn = (Element) nodeColumn1;

                        unique.add(allColumns.get(nodeColumn.elementText("id")));
                    }
                    table.getUniques().add(unique);
                }
                result.getTables().add(table);
            }
        }
        for (Object nodeName1 : docSource.selectNodes("/diagram/sequence_set/sequence/name")) {
            Element nodeName = (Element) nodeName1;
            result.getSequences().add(nodeName.getText());
        }
        return result;
    }

    private Domain parseDomain(String code, String desc) {
        try {
            Domain domain = null;

            BufferedReader br = new BufferedReader(new StringReader(desc));
            String line = br.readLine();
            boolean started = false;
            while (line != null) {
                if (StringUtils.isNotBlank(line)) {
                    if (started) {
                        if (line.startsWith("@")) {
                            String type = StringUtils.remove(line.trim(), "@");
                            domain.setType(new FullyQualifiedJavaType(type));
                            domain.setCode(domain.getType().getShortName());

                            break;
                        }
                        String[] kv = line.split("\\|");
                        if (kv.length != 2) {
                            throw new IllegalArgumentException("键值语法错误[" + code + "]:" + line);
                        }
                        String key = kv[0];
                        key = StringUtils.replace(key, ".", "_");
                        domain.getValueMap().put(key, kv[1]);
                    } else if ("///".equals(StringUtils.trim(line))) {
                        started = true;
                        domain = new Domain();
                        domain.setCode(code);
                        domain.setValueMap(new LinkedHashMap());
                    }
                }
                line = br.readLine();
            }
            return domain;
        } catch (Exception t) {
            throw new IllegalArgumentException(t);
        }
    }

    public String extractHint(String desc) {
        Matcher m = hintPattern.matcher(desc);
        if (!m.find()) {
            return null;
        }
        return desc.substring(m.start() + 2, m.end() - 2);
    }

    public String extractJavaType(String desc) {
        Matcher m = javaTypePattern.matcher(desc);
        if (!m.find()) {
            return null;
        }
        return desc.substring(m.start() + 3, m.end() - 3);
    }

    /**
     * 判断是否为枚举类型
     * @param description
     * @return
     */
    private Domain extractEnumType(String description) {
        if (description == null) {
            return null;
        }
        description = StringUtils.remove(description, "\n");
        description = StringUtils.remove(description, "\r");
        if (description.startsWith(enumPrefix)) {
            FullyQualifiedJavaType fqjtEnumType = new FullyQualifiedJavaType(description.substring(enumPrefix.length()));
            Domain domain = new Domain();
            domain.setType(fqjtEnumType);
            return domain;
        }
        return null;
    }

}
