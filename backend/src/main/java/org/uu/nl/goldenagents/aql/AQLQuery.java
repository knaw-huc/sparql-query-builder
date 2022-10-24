package org.uu.nl.goldenagents.aql;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.springframework.lang.Nullable;
import org.uu.nl.goldenagents.aql.complex.*;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.aql.feature.hasResource;
import org.uu.nl.goldenagents.aql.misc.Exclusion;
import org.uu.nl.goldenagents.netmodels.angular.AQLResource;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLJsonBuilder;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLJsonObject;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;
import java.util.logging.Level;

/**
 * An AQL query object consists of a formal AQL query tree representing the active query, and a focus on one of the
 * nodes of this query. The AQLQuery object encapsulates both
 */
public class AQLQuery implements FIPASendableObject {

    /** The AQL Tree representing this query **/
    private AQLTree queryTree;

    /** The subtree of @ref{this.queryTree} that has the current focus. Focus cannot be null **/
    private AQLTree.ID focus;

    /** Available foci */
    private HashMap<AQLTree.ID, AQLTree> foci;

    /** Suggestions for classes, properties and instances for this query **/
    private AQLSuggestions suggestions = null;

    /** The namespace prefix mapping to use for translating an AQL query to SPARQL **/
    private Map<String, String> prefixMap;

    /**
     * Construct a new AQL query
     */
    public AQLQuery(Map<String, String> prefixMap) {
        foci = new HashMap<>();
        this.prefixMap = prefixMap;
        this.queryTree = new MostGeneralQuery();
        this.focus = this.queryTree.getFocusName();
        foci.put(this.focus, this.queryTree);
    }

    public static void constructSampleAQLQuery(AQLQuery query) {
        SerializableResourceImpl author = new SerializableResourceImpl("https://goldenagents.com/ontology#Author");
        SerializableResourceImpl book = new SerializableResourceImpl("https://goldenagents.com/ontology#Book");

        query.intersection(new TypeSpecification(author));
        AQLTree.ID startFocus = query.getFocus().getFocusName();

        AQLResource authorOf = new AQLResource();
        authorOf.uri = "https://goldenagents.com/ontology#authorOf";
        authorOf.label = "authorOf";
        query.cross(authorOf, false);

        query.intersection(new TypeSpecification(book));

        AQLResource hasPublished = new AQLResource();
        hasPublished.uri = "https://goldenagents.com/ontology#hasPublished";
        hasPublished.label = "hasPublished";
        query.cross(hasPublished, false);

        query.setFocus(startFocus);
        AQLResource hasName = new AQLResource();
        hasName.uri = "https://goldenagents.com/ontology#hasName";
        hasName.label = "hasName";
        query.cross(hasName, false);
    }

    /**
     * Get the AQL tree representing the current query
     * @return AQL tree representing current query
     */
    public AQLTree getQueryTree() {
        return this.queryTree;
    }

    /**
     * Get the current focus
     * @return  AQL tree representing node that has current focus
     */
    public AQLTree getFocus() {
        return this.getNode(this.getFocusName());
    }

    public AQLTree.ID getFocusName() {
        return this.focus;
    }

    @Nullable public AQLTree getNode(AQLTree.ID focusName) {
        if(this.foci.containsKey(focusName)) return this.foci.get(focusName);
        return null;
    }

    /**
     * Shift the focus to a new node
     * @param newFocus  The node ID of node, contained in this query, that should get the active focus
     * @return      True iff focus could be shifted to this node, false if the node does not exist in the query tree
     */
    boolean setFocus(AQLTree.ID newFocus) {
        if(this.foci.containsKey(newFocus)) {
            this.focus = newFocus;
        }

        return this.focus == newFocus;
    }

    /**
     * Transform the current query at the current focus, by appending a new AQL tree
     * @param tree  AQL tree to add to the focus with this transformation
     * @return  True iff successful
     */
    boolean transformAtFocus(AQLTree tree) {
        return false; // TODO
    }

    /**
     * Convert this query to a formal SPARQL algebra object, using Jena ARQ implementation
     * @return  Jena ARQ operation
     */
    public SPARQLTranslation getSparqlAlgebra() {
        return new SPARQLTranslation(this);
    }

    public AQLJsonObject getJson(UUID conversationID) {
        return new AQLJsonBuilder(this, conversationID).build();
    }

    /**
     * Convert this query to an AQL string representation
     * @return  String representation of AQL query
     */
    public String getAqlString() {
        return this.queryTree.toAQLString();
    }

    /**
     * Convert this query to its Natural Language representation
     * @return  Natural Language representation of this query
     */
    public String toNLQuery() {
        return this.queryTree.toNLQuery();
    }

    void intersection(AQLTree feature) {
        if(feature.nSubtrees() != 0) {
            throw new IllegalArgumentException("Do not intersect with complex trees! Features only");
        }

        // Add the new feature as a focus point to existing foci
        this.foci.put(feature.getFocusName(), feature);

        if(this.intersectFocusWith(feature)) {
            // Set the focus to that of the new feature
            this.focus = feature.getFocusName();
        }
    }

    /**
     * Private method to create an intersection of the current focus and a new (as of yet unused) sub tree, and
     * replace the current focus with that created intersection.
     *
     * Before calling this method, all foci used in the passed AQLTree {@code newNode} need to be added to the current
     * set of foci first!
     *
     * @param newNode   Sub tree to intersect current focus with
     * @return          True if the current focus exists and thus can be replaced. False if subtree was not intersected
     *                      with current focus
     */
    private boolean intersectFocusWith(AQLTree newNode) {
        // Find parent of current focus, for replacement
        AQLTree parent = this.foci.get(this.getFocus().getParentID());

        // Create a new intersection with the current focus on the left, and the new feature on the right
        Intersection intersection = new Intersection(this.getFocus(), newNode);

        // Add the new intersection as a focus point to existing foci
        this.foci.put(intersection.getFocusName(), intersection);

        boolean success = false;
        // Try to replace the child of the parent with the new intersection
        if(parent != null) {
            parent.replaceChild(this.focus, intersection);
            success = true;
        } else if (this.focus.equals(this.queryTree.getFocusName())) {
            // Node to replace is root
            this.queryTree = intersection;
            success = true;
        } else {
            Platform.getLogger().log(getClass(), Level.SEVERE, "No candidate node to put new intersection in");
        }

        // Log the new query as AQL
        Platform.getLogger().log(getClass(), this.getAqlString() == null ? "No AQL string provided" : this.getAqlString());

        return success;
    }

    /**
     * Cross a property with the current query and focus, either forward or backward
     * @param aqlResource       Resource representing property to cross
     * @param crossForward      True iff crossing should be forward (e.g. p of q1); false otherwise (e.g. p : q1)
     */
    void cross(AQLResource aqlResource, boolean crossForward) {
        MostGeneralQuery newGeneralQuery = new MostGeneralQuery();
        SerializableResourceImpl resource = new SerializableResourceImpl(aqlResource.uri);
        CrossingOperator crossingOperator = crossForward ?
                new CrossingForwards(resource, newGeneralQuery, aqlResource.label) :
                new CrossingBackwards(resource, newGeneralQuery, aqlResource.label);

        // Add new query elements as focus points to existing foci
        this.foci.put(crossingOperator.getFocusName(), crossingOperator);
        this.foci.put(newGeneralQuery.getFocusName(), newGeneralQuery);

        if(this.intersectFocusWith(crossingOperator)) {
            this.focus = newGeneralQuery.getFocusName();
        }
    }

    void negativeLookup() {
        // TODO add all to tree
        AQLTree newFocus = new MostGeneralQuery();
        Exclusion newExclusion = new Exclusion(newFocus);
        Intersection newNode = new Intersection(this.getFocus(), newExclusion);

        if(this.replaceFocusWith(newNode))
            this.focus = newFocus.getFocusName();
    }

    void union() {
        // Find parent of current focus, for replacement
        AQLTree parent = this.foci.get(this.getFocus().getParentID());

        // Create a new union with the current focus on the left and the new focus (most general query) on the right
        AQLTree newFocus = new MostGeneralQuery();
        Union union = new Union(this.getFocus(), newFocus);

        // Add the new intersection as a focus point to existing foci
        this.foci.put(union.getFocusName(), union);

        if (parent != null) {
            parent.replaceChild(this.focus, union);
        } else if (this.focus.equals(this.queryTree.getFocusName())) {
            this.queryTree = union;
        } else {
            Platform.getLogger().log(getClass(), this.getAqlString() == null ? "No AQL string provided" : this.getAqlString());
        }
    }

    void name() {

    }

    void reference() {

    }

    void delete() {
        AQLTree parent = getNode(getFocus().getParentID());
        if (parent == null) {
            this.foci.clear();
            this.queryTree = new MostGeneralQuery();
            this.foci.put(this.queryTree.getFocusName(), this.queryTree);
            setFocus(this.queryTree.getFocusName());
        } else {
            MostGeneralQuery replacement = new MostGeneralQuery();
            this.foci.put(replacement.getFocusName(), replacement);
            parent.replaceChild(this.focus, replacement);
            removeFocusIds(getFocus());
            setFocus(replacement.getFocusName());
            if (parent instanceof BinaryAQLInfixOperator) {
                BinaryAQLInfixOperator tParent = (BinaryAQLInfixOperator) parent;
                if (tParent.getLeftChild() instanceof MostGeneralQuery && tParent.getRightChild() instanceof MostGeneralQuery) {
                    Log.info(getClass().getName(), "Removing parent as well");
                    setFocus(parent.getFocusName());
                    delete();
                }
            }
        }
    }

    private void removeFocusIds(AQLTree tree) {
        this.foci.remove(tree.getFocusName());
        for(AQLTree child : tree.getSubqueries()) {
            removeFocusIds(child);
        }
    }

    /**
     * Overload method to ensure the right focus is removed
     * @param newFocus Focus to remove
     */
    void delete(AQLTree.ID newFocus) {
        // TODO, this method should call the inverse of methods to add stuff to the query, e.g. inverse of Intersect() if parent is intersection
        this.focus = newFocus;
        delete();
    }


    @Deprecated
    // TODO, this method is deprecated. Look at Intersect(). For deleting, inverse of methods that elaborate the query should be used
    private boolean replaceFocusWith(AQLTree newFocus) {
        if(!this.foci.containsKey(newFocus.getFocusName()))
            this.foci.put(newFocus.getFocusName(), newFocus);

        AQLTree focusParent = this.foci.get(this.getFocus().getParentID());
        if (focusParent != null) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "Putting a new child somewhere. Current parent is " + newFocus.getParentID());
            focusParent.replaceChild(this.focus, newFocus);
            Platform.getLogger().log(getClass(), Level.SEVERE, "After putting child new parent is " + newFocus.getParentID());
            return true;
        } else if (this.queryTree.getFocusName() == this.focus) {
            // Current focus is the root. Replace entire tree
            this.queryTree = newFocus;
            return true;
        } else {
            Platform.getLogger().log(getClass(), "Could not replace current focus for some reason");
            return false;
        }
    }

    public PrefixMapping getPrefixMapping() {
        List<SerializableResourceImpl> resources = getResources(this.queryTree);
        Set<String> uris = new HashSet<>();
        for(SerializableResourceImpl resource : resources) {
            uris.add(resource.getNameSpace());
        }

        PrefixMapping mapping = new PrefixMappingImpl();
        mapping.setNsPrefixes(this.prefixMap);
        PrefixMapping newMapping = new PrefixMappingImpl();

        for (String uri : uris) {
            if (uri != null && mapping.getNsURIPrefix(uri) != null) {
                newMapping.setNsPrefix(mapping.getNsURIPrefix(uri), uri);
            }
        }

        return newMapping;
    }

    private List<SerializableResourceImpl> getResources(AQLTree tree) {
        if(tree.getSubqueries().isEmpty()) {
            if(tree instanceof hasResource) {
                return Collections.singletonList(((hasResource) tree).getResource());
            } else {
                return Collections.emptyList();
            }
        } else {
            ArrayList<SerializableResourceImpl> resources = new ArrayList<>();
            for(AQLTree subquery : tree.getSubqueries()) {
                resources.addAll(getResources(subquery));
            }
            return resources;
        }
    }

    public AQLSuggestions getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(AQLSuggestions suggestions) {
        this.suggestions = suggestions;
    }

    public HashMap<AQLTree.ID, AQLTree> getFoci() {
        return new HashMap<>(foci);
    }

    public AQLQuery copy() {
        AQLQuery copy = new AQLQuery(this.prefixMap); // Copy by reference is fine in this case
        copy.foci = new HashMap<>();
        copy.queryTree = this.queryTree.copy(null, copy.foci);
        copy.suggestions = null;
        copy.focus = this.focus;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AQLQuery aqlQuery = (AQLQuery) o;
        return
                this.queryTree.equals(aqlQuery.queryTree) &&
                this.getFocus().equals(aqlQuery.getFocus()) &&
                nodesAreSameBranch(getFocus(), aqlQuery.getFocus(), aqlQuery);
    }

    private boolean nodesAreSameBranch(AQLTree t1, AQLTree t2, AQLQuery otherQuery) {
        if (!t1.equals(t2)) return false;
        if (t1.getParentID() == null && t2.getParentID() == null) return true;
        if (t1.getParentID() == null || t2.getParentID() == null) return false;
        AQLTree p1 = this.getNode(t1.getParentID());
        AQLTree p2 = otherQuery.getNode(t2.getParentID());
        return nodesAreSameBranch(p1, p2, otherQuery);
    }

    @Override
    public int hashCode() {
        return getFocus().hashCode() * 10 + queryTree.hashCode() * 101;
    }

    @Override
    public String toString() {
        return this.hashCode() + ": " + getAqlString();
    }
}
