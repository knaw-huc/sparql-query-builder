package org.uu.nl.goldenagents.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class RemoteQuery implements AutoCloseable {

    private final TypedInputStream stream;
    private final String url;

    public RemoteQuery(RdfSourceConfig config, String query) {
        this(config.getLocation(), config.getDefaultGraph(), query);
    }

    public RemoteQuery(String endpointLocation, String defaultGraph, String query) {
        this.url = createURL(endpointLocation, defaultGraph, query);
        this.stream =  HttpOp.execHttpGet(url);
    }

    TypedInputStream getStream() {
        return stream;
    }

    private String createURL(final String location, final String defaultGraph, final String query) {

        final NameValuePair graphParam = new NameValuePair() {
            @Override
            public String getName() {
                return "default-graph-uri";
            }

            @Override
            public String getValue() {
                return defaultGraph;
            }
        };

        final NameValuePair queryParam = new NameValuePair() {
            @Override
            public String getName() {
                return "query";
            }

            @Override
            public String getValue() {
                return query;
            }
        };

        return location + "/sparql?" + URLEncodedUtils.format(List.of(graphParam, queryParam), StandardCharsets.UTF_8);
    }

    public abstract ResultSet getResults();

    private void closeStream() {
        if(stream != null) stream.close();
    }

    @Override
    public void close() {
        closeStream();
    }

    public static class RemoteSelectQuery extends RemoteQuery {

        public RemoteSelectQuery(RdfSourceConfig config, String query) {
            super(config, query);
        }

        public RemoteSelectQuery(String endpointLocation, String defaultGraph, String query) {
            super(endpointLocation, defaultGraph, query);
        }

        @Override
        public ResultSet getResults() {
            return ResultSetFactory.load(getStream(), ResultsFormat.FMT_RS_XML);
        }
    }

    public static class RemoteConstructQuery extends RemoteQuery {

        public RemoteConstructQuery(RdfSourceConfig config, String query) {
            super(config, query);
        }

        public RemoteConstructQuery(String endpointLocation, String defaultGraph, String query) {
            super(endpointLocation, defaultGraph, query);
        }

        @Override
        public ResultSet getResults() {
            return ResultSetFactory.load(getStream(), ResultsFormat.FMT_RDF_TTL);
        }
    }
}
