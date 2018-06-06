package com.keking.maven.plugin.mete;

import java.util.ArrayList;
import java.util.List;

public class Relationship {
    private Table parent;
    private Table child;
    private List<JoinColumn> joinColumns = new ArrayList();

    public boolean isOne2One() {
        for (JoinColumn jc : this.joinColumns) {
            if (!this.child.getPrimaryKeyColumns().contains(jc.getFk())) {
                return false;
            }
        }
        if (this.joinColumns.size() != this.child.getPrimaryKeyColumns().size()) {
            return false;
        }
        return true;
    }

    public Table getParent() {
        return this.parent;
    }

    public void setParent(Table parent) {
        this.parent = parent;
    }

    public Table getChild() {
        return this.child;
    }

    public void setChild(Table child) {
        this.child = child;
    }

    public List<JoinColumn> getJoinColumns() {
        return this.joinColumns;
    }

    public void setJoinColumns(List<JoinColumn> joinColumns) {
        this.joinColumns = joinColumns;
    }
}
