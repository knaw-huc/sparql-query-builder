package org.uu.nl.goldenagents.util.agentconfiguration;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import org.uu.nl.goldenagents.util.StartupArgumentsParser;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * RDF resources, such as ontologies, data sources, or linksets, can be present locally or remotely.
 * This configuration class captures the configuration given in TOML for any of these.
 */
public class RdfSourceConfig {

    private static final int DEFAULT_PAGE_SIZE = 10000;

    public static final String CONF_IS_LOCAL_KEY = "local";
    public static final String CONF_URI_KEY = "uri";
    public static final String CONF_METHOD_KEY = "method";
    public static final String CONF_GRAPH_KEY = "default_graph";
    public static final String CONF_PAGE_SIZE_KEY = "default_page_size";
    public static final String CONF_PREFIX_LIST_KEY = "prefixes";
    public static final String CONF_PREFIX_KEY = "prefix";
    public static final String CONF_PREFIX_IRI_KEY = "iri";
    public static final String CONF_TIMEOUT_KEY = "timeout";

    private final String location;
    private final String defaultGraph;
    private final boolean isLocal;
    private int defaultPageSize = DEFAULT_PAGE_SIZE;
    private long timeout = 10000L;
    private final Map<String, String> prefixes = new HashMap<>();

    private boolean isLoaded = false;

    public RdfSourceConfig(TomlTable rdfSourceConfig) throws IllegalArgumentException {
        this.isLocal = rdfSourceConfig.getBoolean(CONF_IS_LOCAL_KEY);
        this.defaultGraph = rdfSourceConfig.getString(CONF_GRAPH_KEY);

        if(rdfSourceConfig.contains(CONF_PAGE_SIZE_KEY)) {
            this.defaultPageSize = rdfSourceConfig.getLong(CONF_PAGE_SIZE_KEY).intValue();
        }
        if (rdfSourceConfig.contains(CONF_TIMEOUT_KEY)) {
            this.timeout = rdfSourceConfig.getLong(CONF_TIMEOUT_KEY);
        }

        parsePrefixes(rdfSourceConfig);
        this.location = parseLocation(rdfSourceConfig);
    }

    private void parsePrefixes(TomlTable rdfSourceConfig) {
        TomlArray prefixList = rdfSourceConfig.getArrayOrEmpty(CONF_PREFIX_LIST_KEY);
        for(int i = 0; i < prefixList.size(); i++) {
            TomlTable prefixEntry = prefixList.getTable(i);
            String prefix = prefixEntry.getString(CONF_PREFIX_KEY);
            String iri = prefixEntry.getString(CONF_PREFIX_IRI_KEY);
            this.prefixes.put(prefix, iri);
        }
    }

    private String parseLocation(TomlTable rdfSourceConfig) throws IllegalArgumentException {
        String location;
        final String rawLocation = rdfSourceConfig.getString(CONF_URI_KEY);
        if(isLocal) {
            final String resolvedPath = StartupArgumentsParser.resolveRelativePath(rawLocation);

            if ((new File(resolvedPath).exists())) {
                location = resolvedPath;
            } else {
                Platform.getLogger().log(RdfSourceConfig.class, String.format(
                        "%s does not exist locally. Not adding as linkset to broker",
                        rawLocation
                ));

                throw new IllegalArgumentException(String.format("Local file %s cannot be resolved", rawLocation));
            }
        } else {
            location = rawLocation;
        }
        return location;
    }

    /**
     * Get the location of the RDF source, either a URI for a remote source, or a path to a local file
     * @return  Location of RDF source
     */
    public String getLocation() {
        return location;
    }

    public String getDefaultGraph() {
        return defaultGraph;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(isLocal ? "Local" : "Remote")
                .append(" data source at ")
                .append(location);
        if(this.defaultGraph != null) {
            builder.append("Graph ").append(this.defaultGraph);
        }
        builder.append(this.isLoaded ? " (loaded)" : " (not loaded)");
        return builder.toString();
    }
}
