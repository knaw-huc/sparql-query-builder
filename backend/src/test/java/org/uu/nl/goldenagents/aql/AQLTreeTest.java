package org.uu.nl.goldenagents.aql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uu.nl.goldenagents.aql.complex.*;
import org.uu.nl.goldenagents.aql.feature.NamedResourceTest;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.aql.feature.TypeSpecificationTest;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public abstract class AQLTreeTest {

    private static final String ONTOLOGY_URI = "https://www.example.org/ontology#";

    private static final List<String> LABELS = Arrays.asList(
            "Book1", "Book2", "Author1", "Author2"
    );

    private static final List<String> URIS =
            LABELS.stream().map(label -> ONTOLOGY_URI + label).collect(Collectors.toList());

    private static final List<Class<? extends AQLTreeTest>> AQLTreeElements = Arrays.asList(
            CrossingBackwardsTest.class,
            CrossingForwardsTest.class,
            IntersectionTest.class,
            NamedResourceTest.class,
            TypeSpecificationTest.class
    );

    /**
     * Construct a list of queries to perform tests with.
     * All queries should have the same root node type, but be distinct in at least one manner.
     * I.e., for each (a,b) in the result, a !== b should be true
     * @param uris      List of distinct URIs that can be used
     * @param labels    List of distinct labels that can be used (correspond to URIs)
     * @return          List of distinct test queries
     */
    abstract public List<AQLTree> getDistinctExampleQueries(List<String> uris, List<String> labels);

    private List<AQLTreePair> queryPairs;
    private List<AQLTree> otherQueryTypes;

    @BeforeEach
    void setUp() {
        List<AQLTree> firstQueries = getDistinctExampleQueries(URIS, LABELS);
        List<AQLTree> secondQueries = getDistinctExampleQueries(URIS, LABELS);
        queryPairs = new ArrayList<>();
        for(int i = 0; i < firstQueries.size(); i++) {
            queryPairs.add(new AQLTreePair(firstQueries.get(i), secondQueries.get(i)));
        }
        otherQueryTypes = new ArrayList<>();
        for(Class<? extends AQLTreeTest> type : AQLTreeElements) {
            if (!getClass().equals(type)) {
                try {
                    otherQueryTypes.addAll(type.getDeclaredConstructor().newInstance().getDistinctExampleQueries(URIS, LABELS));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    void testEquals() {
        for(int i = 0; i < queryPairs.size(); i++) {
            assertEquals(
                    queryPairs.get(i).q1,
                    queryPairs.get(i).q2,
                    String.format("First and second query in %dth pair are not equal", i)
            );

            for (int j = 0; j < queryPairs.size(); j++) {
                if (i != j) {
                    assertNotEquals(
                            queryPairs.get(i).q1,
                            queryPairs.get(j).q1,
                            String.format("Example query %d and %d are equal", i, j)
                    );
                }
            }

            for(int j = 0; j < otherQueryTypes.size(); j++) {
                assertNotEquals(
                        queryPairs.get(i).q1,
                        otherQueryTypes.get(j),
                        String.format(
                                "Example query %d was equal to example query %d of type %s",
                                i, j, otherQueryTypes.get(j).getClass().getName()
                        )
                );
            }
        }
    }

    @Test
    void testCopy() {
        for(int i = 0; i < queryPairs.size(); i++) {
            assertEquals(
                    queryPairs.get(i).q1,
                    queryPairs.get(i).q1.copy(null, new HashMap<>()),
                    String.format("Copy of query %d is not equal to the original", i)
            );
            assertEquals(
                    queryPairs.get(i).q1.hashCode(),
                    queryPairs.get(i).q1.copy(null, new HashMap<>()).hashCode(),
                    String.format("Hashcode of copy of query %d is not equal to that of the original", i)
            );
            assertEquals(
                    queryPairs.get(i).q1.getFocusName(),
                    queryPairs.get(i).q1.copy(null, new HashMap<>()).getFocusName(),
                    String.format("Focus name of copy of query %d does not match original", i)
            );
        }
    }

    @Test
    void testHashCode() {
        for(int i = 0; i < queryPairs.size(); i++) {
            assertEquals(
                    queryPairs.get(i).q1.hashCode(),
                    queryPairs.get(i).q2.hashCode(),
                    String.format("Hash code of first and second query in %dth pair do not match", i)
            );

            for (int j = 0; j < queryPairs.size(); j++) {
                if (i != j) {
                    assertNotEquals(
                            queryPairs.get(i).q1.hashCode(),
                            queryPairs.get(j).q1.hashCode(),
                            String.format("Hash codes of example query %d and %d are equal", i, j)
                    );
                }
            }

            for(int j = 0; j < otherQueryTypes.size(); j++) {
                assertNotEquals(
                        queryPairs.get(i).q1.hashCode(),
                        otherQueryTypes.get(j).hashCode(),
                        String.format(
                                "Hashcode of example query %d was equal to that of example query %d of type %s",
                                i, j, otherQueryTypes.get(j).getClass().getName()
                        )
                );
            }
        }
    }

    private static class AQLTreePair {
        private final AQLTree q1;
        private final AQLTree q2;

        public AQLTreePair(AQLTree q1, AQLTree q2) {
            this.q1 = q1;
            this.q2 = q2;
        }
    }

}
