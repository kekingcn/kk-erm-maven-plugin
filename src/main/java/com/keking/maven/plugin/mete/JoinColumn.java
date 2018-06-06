package com.keking.maven.plugin.mete;

public class JoinColumn {
    private Column pk;
    private Column fk;

    public Column getPk() {
        return this.pk;
    }

    public void setPk(Column pk) {
        this.pk = pk;
    }

    public Column getFk() {
        return this.fk;
    }

    public void setFk(Column fk) {
        this.fk = fk;
    }
}
