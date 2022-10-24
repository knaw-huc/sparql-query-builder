package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.vocabulary.RDF;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.sparql.MappingPropertyType;
import org.uu.nl.goldenagents.sparql.OntologicalConceptInfo;
import org.uu.nl.goldenagents.sparql.PreparedQueryExecution;
import org.uu.nl.goldenagents.util.CollectionUtils;
import org.uu.nl.goldenagents.util.DatabaseConfig;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Plan class that discovers entities for each capability of DB Agent.
 * If the entities are not necessary (i.e., broker does not need expertise graph),
 * DB agents can discover their own expertise with {@link DiscoverExpertisePlan}.
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class DiscoverEntitiesPlan extends DiscoverExpertisePlan {

	@Override
	protected DbAgentExpertise addStatsToConceptInfo(List<OntologicalConceptInfo> conceptList) {
		int count = 0;
		//	Sends queries to collect entities
		for(OntologicalConceptInfo conceptInfo : conceptList) {
			Map<MappingPropertyType, ArrayList<Node>> mappings = conceptInfo.getOntologicalMappings();
			SelectBuilder sb = buildSelectQuery(mappings, count);
			HashSet<String> entities = new HashSet<>();
			conceptInfo.setEntities(entities);
			if(context.getConfig().getMethod() == DatabaseConfig.QUERY_METHOD.SPARQL) {
				String query = sb.buildString();
				boolean isRetrieved = true;
				int offset = 0;
				int limit = context.getConfig().getDefaultPageSize();
				while(isRetrieved) {
					isRetrieved = false;
					String queryExtension = " LIMIT " + limit + " OFFSET " + offset;
					try (PreparedQueryExecution ex = new PreparedQueryExecution(
							query + queryExtension, context.getConfig())) {
						ResultSet result = null;
						try {
							result = ex.queryExecution.execSelect();
						} catch (QueryExceptionHTTP e) {
							Platform.getLogger().log(getClass(), Level.SEVERE, String.format(
									"Broker encountered an error in evaluating query:\nQuery %s\n Graph %s\nReason %s\n Server response %s",
									query + queryExtension,
									context.getConfig(),
									e.getMessage(),
									e.getResponseMessage()
							));
							if (e.getMessage().contains("java.net.SocketTimeoutException") && context.getConfig().getTimeout() < 120000) {
								context.getConfig().setTimeout(context.getConfig().getTimeout() * 2);
								Platform.getLogger().log(getClass(), Level.INFO, String.format(
										"Increased timeout to %d and trying again",
										context.getConfig().getTimeout()
								));
							} else {
								throw e;
							}
						}
						if (result != null) {
							while (result.hasNext()) {
								isRetrieved = true;
								QuerySolution qs = result.next();
								if (qs != null) {
									try {
										entities.add(qs.get(VAR_SUBJECT.toString()).toString());
									} catch (NullPointerException e) {
										Platform.getLogger().log(getClass(), Level.SEVERE, String.format(
												"Failed to add entities from %s with subject %s",
												qs,
												VAR_SUBJECT
										));
										Platform.getLogger().log(getClass(), Level.SEVERE, e);
									}
								}
							}
							offset += limit;
						}
					} catch (ResultSetException e) {
						logQueryException(e, sb.buildString());
					}
				}
				conceptInfo.setCount(entities.size());
			} else {
				try(DBAgentContext.DbQuery dbQuery = context.getDbQuery(sb.build())) {
					final ResultSet rSet = dbQuery.queryExecution.execSelect();
					rSet.forEachRemaining(qs -> entities.add(qs.get(VAR_SUBJECT.toString()).toString()));
					conceptInfo.setCount(entities.size());
				} catch (Exception e) {
					logQueryException(e, sb.buildString());
				}
			}
			count++;
		}
		// Intersect entities to learn the number of entities having combinations of concepts 
		for (int i = 0; i < conceptList.size(); i++) {
			Set<String> set1 = conceptList.get(i).getEntities();
			for(int j = 0; j < i; j++) {
				Set<String> set2 = conceptList.get(j).getEntities();
				Set<String> intersection = CollectionUtils.intersect(set1, set2);
				conceptList.get(i).addStarCombination(conceptList.get(j).getLabel(), intersection.size());
				conceptList.get(j).addStarCombination(conceptList.get(i).getLabel(), intersection.size());
			}
		}
		return new DbAgentExpertise(conceptList, null);
	}

	/**
	 * Builds a select query in the from of {@code SelectBuilder} with the given mappings
	 * @param	mappings mapping of the concept
	 * @param	count	count for variable name to keep them unique
	 * @return	SelectBuilder with the constraints based on mappings
	 */
	private SelectBuilder buildSelectQuery(Map<MappingPropertyType, ArrayList<Node>> mappings, int count) {
		Expr subjExpr = new ExprVar(VAR_SUBJECT);
		SelectBuilder sb = new SelectBuilder();
		sb.addVar(subjExpr);
		addWhereClause(mappings, sb, count);
		sb.setDistinct(true);
		return sb;
	}

	protected void addWhereClause(Map<MappingPropertyType, ArrayList<Node>> mappings, SelectBuilder sb, int count) {
		for (MappingPropertyType relation : mappings.keySet()) {
			ArrayList<Node> nodeList = mappings.get(relation);
			for (int i = 0; i < nodeList.size(); i++) {
				/*
				 * This is an anonymous variable that we need in the object position
				 * If we do not make them unique then a problem arises because variables
				 * in two different statements become the same.
				 * Var.ANON is useless because it produces the same variable everytime
				 */
				Var var_obj = Var.alloc(count + SparqlUtils.OBJECT_VAR_NAME + i);
				Node node = nodeList.get(i);
				WhereBuilder wb = new WhereBuilder();
				//owl:equivalent class and owl:subclass
				if ((relation == MappingPropertyType.OWL_SUBCLASS)
						|| (relation == MappingPropertyType.OWL_EQ_CLASS)) {
					wb.addWhere(VAR_SUBJECT, RDF.type, node);
				}
				//owl:equivalent property and rdfs:subPropertyOf
				else if ((relation == MappingPropertyType.OWL_EQ_PROPERTY)
						|| (relation == MappingPropertyType.RDFS_SUBPROPERTY)) {
					wb.addWhere(VAR_SUBJECT, node, var_obj);
				}
				//owl:inverse
				else if (relation == MappingPropertyType.OWL_INVERSE) {
					wb.addWhere(var_obj, node, VAR_SUBJECT);
				}
				sb.addUnion(wb);
			}
		}
	}

}
