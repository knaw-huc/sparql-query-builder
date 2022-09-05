package org.uu.nl.goldenagents.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;

/**
 * This class represents a query sent by the user to a broker agent.
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class QueryInfo {
	
	private static final Loggable logger = Platform.getLogger();
	
	private final String originalQuery;
	private final Set<TripleInfo> triples;
	private final Set<BindInfo> binds;
	private final Set<FilterInfo> filters;
	private final Map<String, String> aliases = new LinkedHashMap<>();
	private final Map<String, Set<TripleInfo>> varTripleInfoMapping = new LinkedHashMap<>();
	private final PrefixMapping prefixMap;
	private int aliasCount = 0;
	
	public QueryInfo(String originalQuery, Map<String, String> prefixMap) throws QueryException, BadQueryException {

		this.originalQuery = originalQuery;
		this.prefixMap = new PrefixMappingImpl();
		this.prefixMap.setNsPrefixes(prefixMap);
		this.triples = new LinkedHashSet <>();
		this.binds = new LinkedHashSet <>();
		this.filters = new LinkedHashSet <>();

		parseJenaQuery();
	}
	
	/**
	 *	Produces Query object from string and parses the query.
	 */
	private void parseJenaQuery() throws QueryException, BadQueryException {
		//create jena query
		Query query = QueryFactory.create(this.originalQuery);
		//create a visitor that fills this class
		ElementVisitorGA visitor = new ElementVisitorGA(this);
		//walks through all the elements and visitor visits those elements
		ElementWalker.walk(query.getQueryPattern(), visitor);
	}
	
	/**
	 * 	Creates TripleInfo object from triple path.
	 * 	Adds the created object to the set of triples.
	 * @param tp	TriplePath
	 * @param vars	Variables in the triple
	 */
	public void addTripleInfo(TriplePath tp, List<Var> vars) {
		//Create prologue for prefix mapping
		Prologue pg = new Prologue(this.prefixMap);
		//string of path in short form that uses prefixes and remove parentheses
		String predicate = tp.getPath().toString(pg).replaceAll("[()]", "");
		//string of object in the short form that uses prefixes
		String object = tp.getObject().toString(this.prefixMap);
		//creates triple info
		TripleInfo ti = new TripleInfo(tp.getSubject().toString(), predicate, object);
		//checks if it needs an alias
		checkForAlias(ti);
		//adds to the variable mapping
		addToTripleInfoMapping(ti, vars);
		triples.add(ti);
	}
	
	/**
	 * Checks if the predicate of triple info is simple. If it is not, 
	 * then creates an alias for the paths to represent them in the construct clause.
	 * @param ti TripleInfo
	 */
	public void checkForAlias(TripleInfo ti) {
		if(!ti.getPredicatePath().isSimple()) {
			String alias = SparqlUtils.ALIAS_PREFIX + aliasCount++;
			ti.getPredicatePath().setAlias(alias);
			this.aliases.put(ti.getPredicate(), alias);
		}
	}
	
	/**
	 * Adds a mapping from variable to triple info
	 * @param ti triple info object that will be mapped to variable
	 * @param vars variables that are either object or subject of the given triple info
	 */
	private void addToTripleInfoMapping(TripleInfo ti, List<Var> vars) {
		for(Var v : vars) {
			if(this.varTripleInfoMapping.containsKey(v.toString())) {
				this.varTripleInfoMapping.get(v.toString()).add(ti);
			}
			else {
				Set<TripleInfo> temp = new LinkedHashSet<TripleInfo>();
				temp.add(ti);
				this.varTripleInfoMapping.put(v.toString(), temp);	
			}
		}
	}
	
	public String getOriginalQuery() {
		return this.originalQuery;
	}
	
	/**
	 * The original query but with predicate paths replaced with their corresponding aliases
	 * @return
	 */
	private String getAliasedQuery() {
		String aliased = this.originalQuery;	
		for(Map.Entry<String, String> e : this.aliases.entrySet()) {
			aliased = aliased.replace(e.getKey(), e.getValue());
		}
		return aliased;
	}
	
	public Query getAliasedJenaQuery() {
		return QueryFactory.create(getAliasedQuery());
	}
	
	public Set<TripleInfo> getTriples() {
		return this.triples;
	}

	public Set<BindInfo> getBinds() {
		return this.binds;
	}

	public Set<FilterInfo> getFilters() {
		return this.filters;
	}

	public Map<String, Set<TripleInfo>> getVarTripleInfoMapping() {
		return this.varTripleInfoMapping;
	}
}