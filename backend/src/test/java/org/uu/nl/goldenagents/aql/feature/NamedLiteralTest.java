package org.uu.nl.goldenagents.aql.feature;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.AQLTreeTest;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;

import java.util.ArrayList;
import java.util.List;

public class NamedLiteralTest extends AQLTreeTest {

    @Override
    public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels) {
        List<AQLTree> distinctTestQueries = new ArrayList<>();

        distinctTestQueries.add(createNamedLiteral("first value", XSDDatatype.XSDstring));
        distinctTestQueries.add(createNamedLiteral("second value", XSDDatatype.XSDstring));
        distinctTestQueries.add(createNamedLiteral("6", XSDDatatype.XSDint));
        distinctTestQueries.add(createNamedLiteral("7", XSDDatatype.XSDint));

        return distinctTestQueries;
    }

    private NamedLiteral createNamedLiteral(String label, RDFDatatype datatype) {
        AQLSuggestions.InstanceSuggestion s = new AQLSuggestions.InstanceSuggestion();
        s.setDataTypeURI(datatype.getURI());
        s.setLabel(label);

        EntityList.Entity e = new EntityList.Entity(s);
        return new NamedLiteral(e);
    }

}
