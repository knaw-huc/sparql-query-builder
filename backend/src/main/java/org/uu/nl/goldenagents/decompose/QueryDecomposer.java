package org.uu.nl.goldenagents.decompose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.decompose.linkset.*;
import org.uu.nl.goldenagents.netmodels.fipa.AgentQuery;
import org.uu.nl.goldenagents.sparql.QueryInfo;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.goldenagents.sparql.TripleInfo.NodeType;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.platform.Platform;

/**
 *	A class that decomposes a query and generates AgentQuery
 *	by using different types of information.
 *	@author Golden Agents Group, Utrecht University
 */
public class QueryDecomposer {
	/**
	 * QueryInfo stores the basic information to decompose a query
	 */
	protected final QueryInfo qi;
	private final Set<TripleInfo> triples;
	private final Set<String> binds;
	private final Set<String> filters;
	private final Set<String> variables;

	/**
	 * Default constructor that requires only query information
	 * @param queryInfo basic information to decompose a query
	 */
	public QueryDecomposer(QueryInfo queryInfo) {
		this.qi = queryInfo;
		this.triples = new LinkedHashSet<>();
		this.binds = new LinkedHashSet<>();
		this.filters = new LinkedHashSet<>();
		this.variables = new LinkedHashSet<>();
	}
	
	private void clearWhereClause() {
		this.triples.clear();
		this.binds.clear();
		this.filters.clear();
		this.variables.clear();
	}
	
	protected AgentQuery initAgentQuery(AgentID queryOwner) {
		clearWhereClause();
		Integer targetAqlQueryID = null;
		if (this.qi.getQueryRequest().getAql() != null) {
			targetAqlQueryID = this.qi.getQueryRequest().getAql().hashCode();
		}
		AgentQuery aq = new AgentQuery(queryOwner, targetAqlQueryID);
		//Load the prefixes coming from query info
		HashMap<String, String> prefixes = new HashMap<>(this.qi.getAliasedJenaQuery().getPrefixMapping().getNsPrefixMap());
		aq.setPrefixMap(prefixes);
		return aq;
	}
	
	private void addBinds(AgentQuery aq) {
		this.qi.getBinds().forEach(bind -> {
			bind.getVariables().forEach(var -> {
				// If this bind is relevant, i.e. it contains a variable we are using in the triples
				if(variables.contains(var)) {
					binds.add(bind.toString());
					variables.addAll(bind.getVariables());
				}
			});
		});
		aq.setBinds(binds.stream().toArray(String[]::new));
	}
	
	private void addFilters(AgentQuery aq) {
		this.qi.getFilters().forEach(filter -> {
			// If this filter is relevant, i.e. it contains all variables we are using in the triples or binds
			boolean allVarsPresent = filter.getVariables().stream().allMatch(var -> {
				return variables.contains(var);
			});

			if(allVarsPresent) {
				filters.add(filter.toString());
			}
		});
		aq.setFilters(filters.stream().toArray(String[]::new));
	}
	
	protected void addTriple(TripleInfo t) {
		this.triples.add(t);
	}
	
	protected void removeTriple(TripleInfo t) {
		this.triples.remove(t);
	}
	
	protected void finalize(AgentQuery aq) {
		this.triples.forEach(t -> {
			addVariable(t);
		});
		aq.setTriples(triples.stream().toArray(TripleInfo[]::new));
		addBinds(aq);
		addFilters(aq);
	}
	
	private void addVariable(TripleInfo t) {
		if(t.getSubjectType() == NodeType.VARIABLE) {
			variables.add(t.getSubject());
		}
		if(t.getObjectType() == NodeType.VARIABLE) {
			variables.add(t.getObject());
		}
	}

	/**
	 * The simplest function that decomposes a query.
	 * It finds all the matching properties between in the query
	 * and the properties (capabilities) of the DB Agent that will answer the query.
	 * 
	 * @param queryOwner	AgentID of the DB Agent that will answer the query.
	 * @param capabilities	Capabilities of the DB Agent.  
	 * @return	AgentQuery that is ready to translate
	 */
	public AgentQuery fromCapabilities(AgentID queryOwner, List<String> capabilities) {
		
		AgentQuery aq = initAgentQuery(queryOwner);
		this.qi.getTriples().forEach(t -> {
			if (capabilities.stream().anyMatch(cap -> t.contains(cap))) {
				addTriple(t);
			}
		});
		finalize(aq);
		return aq;
	}

	
	
	/**
	 * Decomposes a given query and creates a list of sub-queries specific to the DB agents.
	 * This is a basic (naive) function that decomposes a query by using 
	 * only (abstract) capabilities of DB Agents.
	 * @param capables map of DB agent capabilities
	 * @return list of sub-queries
	 */
	public List<AgentQuery> decompositionFromCapabilities(Map<AgentID, List<String>> capables) {
		List<AgentQuery> queries = new ArrayList<>();
		capables.forEach((agentID, capabilities) -> {
			AgentQuery agentQuery = fromCapabilities(agentID, capabilities);
			if (!agentQuery.isEmpty()) { 
				queries.add(agentQuery);
			}
		});
		return queries;
	}
	
	/**
	 * Decomposes a given query and creates a list of sub-queries specific to the DB agents.
	 * This is a purely data-oriented function that decomposes a query by using (enriched) linkset file.
	 * @param	aIDs list of agentIDs
	 * @param	detailedLinkset linkset in a detailed format
	 * @return	list of sub-queries
	 */
	public List<AgentQuery> decompositionFromLinkset(List<AgentID> aIDs, LinksetEntry[] detailedLinkset) {
		List<AgentQuery> queries = new ArrayList<>();
		for(AgentID agentID : aIDs) {
            AgentQuery agentQuery = fromLinkset(agentID, detailedLinkset);
            if (!agentQuery.isEmpty()) { 
				queries.add(agentQuery);
			}
        }
		return queries;
	}

	/**
	 * This function is created to use an enriched linkset file that
	 * stores detailed information of type and predicates for each URI and DB pairs.
	 * In this version, created source queries includes list of URIs (as values of SPARQL query) 
	 * and thus only retrieves triple statements for the given URIs.
	 * @param queryOwner	AgentID of the DB Agent that will answer the query.
	 * @param detailedLinkset enriched linkset in which each entry provides information about URIs
	 * @return agent query
	 */
	private AgentQuery fromLinkset(AgentID queryOwner, LinksetEntry[] detailedLinkset) {
		/* TODO This is a promising idea for small linksets between large datasets 
		 * because the query retrieves only the information of given URIs.
		 * The example enriched linkset is just a prototype and therefore needs to be improved.
		 */
		AgentQuery aq = initAgentQuery(queryOwner);
		HashMap<String, String> varType = new HashMap<>(); 
		HashMap<String, HashSet<String>> values = new HashMap<>();
		TripleInfo rdftrip = null;
		qi.getTriples().forEach(t -> {
			addTriple(t);
			if((t.getSubjectType() == NodeType.VARIABLE) && (t.hasPredicateType())) {
				varType.put(t.getSubject().trim(), t.getObject().trim());
				values.put(t.getSubject().trim(), new HashSet<String>());
			}
		});
		
		//TODO what if more than one rdf:type?
		for(TripleInfo t : triples) {
			if(t.getPredicate().equals("rdf:type")) {
				rdftrip = t;
			}
		}
		varType.forEach((var, type) -> {
			ArrayList<String> predicates = qi.getVarTripleInfoMapping().get(var)
					.stream().map(TripleInfo::getPredicate)
					.collect(Collectors.toCollection(ArrayList::new));
			predicates.remove("rdf:type");
			for (LinksetEntry le : detailedLinkset) {
				if(le.getAllProperties(type) != null && le.getAllProperties(type).containsAll(predicates)) {
					SourceInfo si = le.getSource(queryOwner);
					values.get(var).add(si.getUri());
					for(TripleInfo ti : triples) {
						if(!si.getMatch(type).hasProperty(ti.getPredicate())) {
							removeTriple(ti);
						}
					}
				}
				else {
					Platform.getLogger().log(getClass(), Level.INFO, String.format(
							"%s does not contain %s", le.getAllProperties(type), predicates));
				}
			}
		});
		addTriple(rdftrip);
		aq.setValues(values);
		finalize(aq);
		return aq;
	}
}
