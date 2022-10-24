package org.uu.nl.goldenagents.aql.complex;

import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.AQLTreeTest;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.ArrayList;
import java.util.List;

public class IntersectionTest extends AQLTreeTest {

    private Intersection createIntersection(String uri) {
        AQLTree leftLeaf = new MostGeneralQuery();
        AQLTree middleLeaf = new TypeSpecification(new SerializableResourceImpl(uri));
        AQLTree leftChild = new Intersection(leftLeaf, middleLeaf);
        return new Intersection(
                leftChild,
                new MostGeneralQuery()
        );
    }

    @Override
    public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels) {
        List<AQLTree> distinctTestQueries = new ArrayList<>();
        for(String uri : uris) {
            distinctTestQueries.add(createIntersection(uri));
        }
        distinctTestQueries.add(new Intersection(new MostGeneralQuery(), new MostGeneralQuery()));
        return distinctTestQueries;
    }

}