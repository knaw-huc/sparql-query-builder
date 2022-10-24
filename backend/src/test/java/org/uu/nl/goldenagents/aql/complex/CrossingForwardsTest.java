package org.uu.nl.goldenagents.aql.complex;

import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.AQLTreeTest;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.ArrayList;
import java.util.List;

public class CrossingForwardsTest extends AQLTreeTest {

    private CrossingForwards createCrossingForwards(String uri1, String uri2) {
        AQLTree leftLeaf = new MostGeneralQuery();
        AQLTree middleLeaf = new TypeSpecification(new SerializableResourceImpl(new String(uri2)));
        AQLTree intersection = new Intersection(middleLeaf, leftLeaf);
        return new CrossingForwards(new SerializableResourceImpl(new String(uri1)), intersection);
    }

    @Override
    public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels) {
        List<AQLTree> distinctTestQueries = new ArrayList<>();
        for(String uri : uris) {
            distinctTestQueries.add(createCrossingForwards(uri, uris.get(0)));
            distinctTestQueries.add(new CrossingForwards(
                    new SerializableResourceImpl(new String(uri)),
                    new MostGeneralQuery()
            ));
        }
        return distinctTestQueries;
    }

}