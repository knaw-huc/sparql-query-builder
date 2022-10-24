package org.uu.nl.goldenagents.aql;

import org.junit.jupiter.api.Test;
import org.uu.nl.goldenagents.aql.complex.Intersection;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AQLQueryTest {

    @Test
    void copy() {
        AQLQuery query = new AQLQuery(new HashMap<>());
        AQLQuery.constructSampleAQLQuery(query);
        assertEquals(query, query.copy());
        allNodesInMap(query.getQueryTree(), query.getFoci());
        allFociInTree(query.getQueryTree(), query.getFoci());
    }

    void allNodesInMap(AQLTree tree, HashMap<AQLTree.ID, AQLTree> foci) {
        assertTrue(foci.containsKey(tree.getFocusName()), String.format(
                "Tree of type %s with name %s is missing from foci map",
                tree.getClass().getName(),
                tree.getFocusName()
        ));
        assertEquals(foci.get(tree.getFocusName()), tree, String.format(
                "Tree of type %s with focusName %s refers to tree of type %s that is not the same",
                tree.getClass().getName(),
                tree.getFocusName(),
                foci.get(tree.getFocusName()).getClass().getName()
        ));
        for(AQLTree subquery : tree.getSubqueries()) {
            allNodesInMap(subquery, foci);
        }
    }

    void allFociInTree(AQLTree tree, HashMap<AQLTree.ID, AQLTree> foci) {
        for(AQLTree.ID queryID : foci.keySet()) {
            assertTrue(treeContainsFocus(tree, queryID), String.format(
                    "Query Tree is missing node %s of type %s",
                    queryID,
                    foci.get(queryID).getClass().getName()
            ));
        }
    }

    boolean treeContainsFocus(AQLTree tree, AQLTree.ID focusID) {
        if (tree.getFocusName().equals(focusID)) return true;
        for(AQLTree subQuery : tree.getSubqueries()) {
            if (treeContainsFocus(subQuery, focusID)) return true;
        }
        return false;
    }

    @Test
    void testEquals() {
        AQLQuery q1 = new AQLQuery(new HashMap<>());
        AQLQuery q2 = new AQLQuery(new HashMap<>());
        AQLQuery.constructSampleAQLQuery(q1);
        AQLQuery.constructSampleAQLQuery(q2);

        assertEquals(q1, q1);

        AQLQuery q3 = q1.copy();
        q1.intersection(new MostGeneralQuery());

        assertNotEquals(q1, q3);

        AQLQuery q4 = q1.copy();
        AQLTree.ID currentFocus = q4.getFocusName();
        q4.setFocus(q4.getQueryTree().getFocusName());
        assertNotEquals(q1, q4);
        q4.setFocus(currentFocus);
        assertEquals(q1, q4);
    }

    @Test
    void testHashCode() {
        Intersection i1 = new Intersection(new MostGeneralQuery(), new MostGeneralQuery());
        assertEquals(i1.hashCode(), i1.copy(null, new HashMap<>()).hashCode());

        Intersection i2 = new Intersection(
                new MostGeneralQuery(),
                new TypeSpecification(new SerializableResourceImpl("https://www.ontology.org/ontology#Person1"))
        );

        assertEquals(i2.hashCode(), i2.copy(null, new HashMap<>()).hashCode());
        assertNotEquals(i1.hashCode(), i2.hashCode());

        TypeSpecification t1 = new TypeSpecification(new SerializableResourceImpl("https://www.ontology.org/ontology#Person1"));
        TypeSpecification t2 = new TypeSpecification(new SerializableResourceImpl("https://www.ontology.org/ontology#Person2"));


    }
}