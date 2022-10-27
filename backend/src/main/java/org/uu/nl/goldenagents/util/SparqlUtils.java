package org.uu.nl.goldenagents.util;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SparqlUtils {
	/** Generic variable name for objects of triples */
	public static final String OBJECT_VAR_NAME = "obj";
	/** Generic variable name for predicates of triples */
	public static final String PREDICATE_VAR_NAME = "pred";
	/** Generic variable name for subjects of triples */
	public static final String SUBJECT_VAR_NAME = "sub";
	/** Generic variable name for counts in queries */
	public static final String COUNT_VAR_NAME = "count";
	/** Generic path name to produce aliases for predicate paths */
	public static final String ALIAS_PREFIX = "ga:path";
	public static final String ALL_TRIPLES_CONSTRUCT_QUERY = "CONSTRUCT{ ?s ?p ?o}WHERE {?s ?p ?o}";
	public static final String ALL_TRIPLES_SELECT_QUERY = "SELECT * WHERE {?s ?p ?o}";
	public static final List<Character> NAMESPACE_ENDS_WITH = Arrays.asList('/', '#');

	public static Query createPreferredPrefixQuery(Var vPrefix, Var vNamespace) {
		return new SelectBuilder()
				.addWhere(vNamespace, RDF.type.asNode(), OWL.Ontology.asNode())
				.addWhere(
						vNamespace,
						NodeFactory.createURI("http://purl.org/vocab/vann/preferredNamespacePrefix"),
						vPrefix
				)
				.build();
	}

	public static Query createPreferredPrefixQuery(String vPrefixStr, String vNamespaceStr) {
		Var vPrefix = Var.alloc("prefix");
		Var vNs = Var.alloc("ns");
		return createPreferredPrefixQuery(vPrefix, vNs);
	}

	/**
	 * Update the model, such that all prefixes known by the broker are set in the model, with the correct IRI
	 * @param model		Model to update
	 * @param prefixMap	NS prefix map with which to update model
	 */
	public static void updatePrefixesInModel(Model model, Map<String, String> prefixMap) {
		Map<String, String> modelNsPrefixMap = model.getNsPrefixMap();

		for(String prefix : prefixMap.keySet()) {
			for(String modelPrefix : modelNsPrefixMap.keySet()) {
				// Automatically generated namespaces are not necessarily exactly equal
				String uri = modelNsPrefixMap.get(modelPrefix);
				if(uri.startsWith(prefixMap.get(prefix)) || prefixMap.get(prefix).startsWith(uri)) {
					model.removeNsPrefix(modelPrefix);
				}
			}
			if(!prefixMap.get(prefix).equals(model.getNsPrefixURI(prefix))) {
				model.removeNsPrefix(prefix);
			}
			if(!model.getNsPrefixMap().containsKey(prefix)) {
				model.setNsPrefix(prefix, prefixMap.get(prefix));
			}
		}
	}

	/**
	 * Same as validateLocalName(localName, prefixMap, false);
	 * @param localName
	 * @param prefixMap
	 * @return
	 */
	public static String validateLocalName(String localName, Map<String, String> prefixMap) {
		return validateLocalName(localName, prefixMap, false, false);
	}

    public static String validateLocalName(String localName, Map<String, String> prefixMap, boolean wrapInAngleBrackets, boolean forceFullUri) {
        if (
                (localName.startsWith("<") && localName.endsWith(">")) ||
				(!localName.contains("/") && !forceFullUri) || !localName.contains(":")
        )
            return localName;

        String[] segments = localName.split(":");
        if (prefixMap.containsKey(segments[0])) {
            StringBuilder name = new StringBuilder();
			if (wrapInAngleBrackets) {
				name.append("<");
			}
			name.append(prefixMap.get(segments[0]));
            for(int i = 1; i < segments.length; i++) {
                name.append(segments[i]);
            }
			if (wrapInAngleBrackets) {
				name.append(">");
			}
            return name.toString();
        } else {
            return localName; // hope for the best
        }
    }
}
