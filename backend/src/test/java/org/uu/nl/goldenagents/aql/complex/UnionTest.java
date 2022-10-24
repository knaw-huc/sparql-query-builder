package org.uu.nl.goldenagents.aql.complex;

import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.AQLTreeTest;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.ArrayList;
import java.util.List;

public class UnionTest extends AQLTreeTest {

    @Override
    public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels) {
        List<AQLTree> distinctTestQueries = new ArrayList<>();
        for(String uri : uris) {
            distinctTestQueries.add(createUnion(uri));
        }
        distinctTestQueries.add(new Union(new MostGeneralQuery(), new MostGeneralQuery()));
        return distinctTestQueries;
    }

    private Union createUnion(String uri) {
        AQLTree leftLeaf = new TypeSpecification(new SerializableResourceImpl(uri));
        AQLTree middleLeaf = new MostGeneralQuery();
        AQLTree leftChild = new Intersection(leftLeaf, middleLeaf);
        return new Union(
                leftChild,
                new MostGeneralQuery()
        );
    }
}
