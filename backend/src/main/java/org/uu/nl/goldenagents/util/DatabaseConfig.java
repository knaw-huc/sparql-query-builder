package org.uu.nl.goldenagents.util;

import org.tomlj.TomlTable;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;

public class DatabaseConfig extends RdfSourceConfig {

    private final QUERY_METHOD method;

    public DatabaseConfig(TomlTable rdfSourceConfig) throws IllegalArgumentException {
        super(rdfSourceConfig);

        if(!rdfSourceConfig.contains(CONF_URI_KEY))
            throw new IllegalArgumentException("Missing endpoint URI on database config");

        if(!rdfSourceConfig.contains(CONF_METHOD_KEY))
            throw new IllegalArgumentException("Missing query method on database config");

        this.method = QUERY_METHOD.valueOf(rdfSourceConfig.getString(CONF_METHOD_KEY, () -> ""));
    }

    public QUERY_METHOD getMethod() {
        return method;
    }

    public enum QUERY_METHOD {
        SPARQL(false), // A remote endpoint that can be queried using SPARQL
        TDB2(true), // A local TDB2 directory
        FILE(true); // A local single file, e.g. triple store

        private final boolean local;

        QUERY_METHOD(boolean local) {
            this.local = local;
        }

        public boolean isLocal() {
            return this.local;
        }
    }
}
