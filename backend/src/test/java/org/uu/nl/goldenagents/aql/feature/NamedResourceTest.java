package org.uu.nl.goldenagents.aql.feature;

import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.AQLTreeTest;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.ArrayList;
import java.util.List;

public class NamedResourceTest extends AQLTreeTest {

    @Override
    public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels) {
        List<AQLTree> distinctTestQueries = new ArrayList<>();
        for(String uri : uris) {
            distinctTestQueries.add(new NamedResource(new SerializableResourceImpl(new String(uri))));
            distinctTestQueries.add(new NamedResource(new SerializableResourceImpl(new String(uri), "ThisLabelShouldNotExist")));
        }
        return distinctTestQueries;
    }
}