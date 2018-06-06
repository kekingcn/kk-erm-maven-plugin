package com.keking.maven.plugin.mete;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private File source;
    private List<Table> tables = new ArrayList();
    private List<Relationship> relationships = new ArrayList();
    private List<Domain> domains = new ArrayList();
    private List<String> sequences = new ArrayList();

    public List<Table> getTables() {
        return this.tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<Relationship> getRelationships() {
        return this.relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<Domain> getDomains() {
        return this.domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public List<String> getSequences() {
        return this.sequences;
    }

    public void setSequences(List<String> sequences) {
        this.sequences = sequences;
    }

    public File getSource() {
        return this.source;
    }

    public void setSource(File source) {
        this.source = source;
    }
}
