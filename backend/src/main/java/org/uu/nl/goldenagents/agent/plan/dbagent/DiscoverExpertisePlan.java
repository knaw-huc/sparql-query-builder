package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.vocabulary.RDF;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.DirectSsePublisher;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.goldenagents.agent.plan.broker.AddDbExpertisePlan;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.sparql.MappingPropertyType;
import org.uu.nl.goldenagents.sparql.OntologicalConceptInfo;
import org.uu.nl.goldenagents.sparql.PreparedQueryExecution;
import org.uu.nl.goldenagents.util.DatabaseConfig;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;
import java.util.logging.Level;

/**
 * Plan class that discovers expertise of DB Agent.
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class DiscoverExpertisePlan extends RunOncePlan {
	
	protected static Var VAR_SUBJECT = Var.alloc(SparqlUtils.SUBJECT_VAR_NAME);
	private static Var VAR_COUNT = Var.alloc(SparqlUtils.COUNT_VAR_NAME);

	private AgentID agentID;

    /**
     * Execute the business logic of the plan. Make sure that when you implement this
     * method that the method will return. Otherwise it will hold up other agents that
     * are executed in the same thread. Also, if the plan should only be executed once,
     * then make sure that somewhere in the method it calls the setFinished(true) method.
     *
     * @param planInterface
     * @throws PlanExecutionError If you throw this error than it will be automatically adopted as an internal trigger.
     */
    @Override
    public void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
        this.agentID = planInterface.getAgentID();
        DBAgentContext context = planInterface.getContext(DBAgentContext.class);
        DbAgentExpertise expertise = context.getExpertise();
        if(expertise == null) {
            // Only re-discover expertise if it isn't yet available
            expertise = extractExpertise(context, planInterface.getContext(PrefixNSListenerContext.class));
        }
        context.setExpertise(expertise);
        notifyReady(planInterface);
        setFinished(true);
    }

    /**
     * Extracts expertise information of a data source 
     * @param context Context of DB Agent to get mapping and add extracted info
     * @return expertise of the DB Agent
     */
    private DbAgentExpertise extractExpertise(DBAgentContext context, PrefixNSListenerContext prefixContext) {

        StmtIterator it = context.getOntologyModel().listStatements();
        Map<String, OntologicalConceptInfo> concepts = new HashMap<>();
        Set<String> namespaces = prefixContext.getNamespaces();
        while(it.hasNext()) {
            Statement s = it.nextStatement();
            Node subject = s.getSubject().asNode();
            Property predicate = s.getPredicate();
            Node object = s.getObject().asNode();

            if(subject.isURI() && namespaces.contains(subject.getNameSpace())) {
                //object is the mapping in this case
                String gaConcept = context.getOntologyModel().shortForm(subject.getURI());
                updateConceptMap(concepts, gaConcept, predicate, object);
            }
            else if(object.isURI() && namespaces.contains(object.getNameSpace())) {
                //subject is the mapping in this case
                String gaConcept = context.getOntologyModel().shortForm(object.getURI());
                updateConceptMap(concepts, gaConcept, predicate, subject);
            }
        }
        return addStatsToConceptInfo(context, new ArrayList<>(concepts.values()));
    }

    /**
     * This is an alternative to read mapping of a data source.
     * 
     *	TODO: Further investigation of ontology reading 
     *	It requires further investigation to extract mapping statements 
     *	in a more efficient and semantic way because they are created with
     *	known predicates and actions for those are already known. Therefore,
     *	this method should be expanded in a way that it uses the semantics of mappings
     * 
     * @param context
     */
    private void readAsOntology(DBAgentContext context) {
    	
    	Model m = context.getOntologyModel();
    	//OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, context.getOntologyModel());
    	for(MappingPropertyType mp : MappingPropertyType.values()) {
    		RDFNode n = null;
    		StmtIterator it = m.listStatements(new SimpleSelector(null, mp.getProperty(), n));
        	while(it.hasNext()) {
        	    Statement statement = it.nextStatement();
        		// TODO
        	}
        	it.close();
    	}
    }

    /**
     * Updates the given mapping with the information of given mapping
     * @param concepts map of concepts to their extracted info
     * @param gaConcept	ontological concept
     * @param predicate	predicate of the mapping
     * @param mapping	mapped node
     */
    private void updateConceptMap(
            Map<String, OntologicalConceptInfo> concepts,
            String gaConcept,
            Property predicate,
            Node mapping
    ) {
        //If the concept has not created yet, create it

        MappingPropertyType mappingPropertyType = MappingPropertyType.fromNode(predicate.asNode());

        if (mappingPropertyType == null) {
            // This mapping is not known, so do not add this concept to the concept map, as it cannot be used
            Platform.getLogger().log(MappingPropertyType.class, Level.WARNING, String.format(
                    "Failed to match mapping property from %s used between GA concept %s and subject or object %s. Skipping",
                    predicate.getURI(),
                    gaConcept,
                    mapping.getURI()
            ));
            return;
        }

        if(!concepts.containsKey(gaConcept)) {
            boolean isClass = false;
            //If the concept is a class then the mapping predicate is either equivalent class or subclass
            if(predicate.equals(MappingPropertyType.OWL_EQ_CLASS.getProperty()) ||
                    predicate.equals(MappingPropertyType.OWL_SUBCLASS.getProperty())) {
                isClass = true;
            }
            concepts.put(gaConcept, new OntologicalConceptInfo(gaConcept, isClass));
        }
        concepts.get(gaConcept).addMapping(mappingPropertyType, mapping);
    }
    
    /**
     * Updates concept info with statistical information, which are
     * the number of entities having a concept (class or predicate) and 
     * the number of entities having a combination of the concepts.
     * @param 	context	DB Agent context
     * @param 	conceptList	List of ontological concepts
     * @return	expertise of the DB Agent
     */
    protected DbAgentExpertise addStatsToConceptInfo(DBAgentContext context, List<OntologicalConceptInfo> conceptList) {
    	ArrayList<SelectBuilder> builders = new ArrayList<>();
    	int count = 0;
    	//	Sends count queries to learn number of entities having concepts
    	for(OntologicalConceptInfo conceptInfo : conceptList) {
    		Map<MappingPropertyType, ArrayList<Node>> mappings = conceptInfo.getOntologicalMappings();
    		SelectBuilder sb = buildCountQuery(mappings, count);
    		builders.add(sb);
    		if(context.getConfig().getMethod() == DatabaseConfig.QUERY_METHOD.SPARQL) {
                try (PreparedQueryExecution ex = new PreparedQueryExecution(
                        sb.buildString(), context.getConfig())) {
                    final ResultSet result = ex.queryExecution.execSelect();
                    conceptInfo.setCount(result.nextSolution().get(VAR_COUNT.toString()).asLiteral().getInt());
                } catch (ResultSetException e) {
                	logQueryException(e, sb.buildString());
                }
            } else {
                try(DBAgentContext.DbQuery dbQuery = context.getDbQuery(sb.build())) {
                    final ResultSet rSet = dbQuery.queryExecution.execSelect();
                    conceptInfo.setCount(rSet.nextSolution().get(VAR_COUNT.toString()).asLiteral().getInt());
                } catch (Exception e) {
                	logQueryException(e, sb.buildString());
                }
            }
    		count++;
    	}
    	// Sends count queries to learn number of entities having combinations of concepts 
    	for (int i = 0; i < builders.size(); i++) {
    		for(int j = 0; j <= i; j++) {
    			SelectBuilder sb = builders.get(i).clone();
    			sb.getWhereHandler().addAll(builders.get(j).getWhereHandler());
                if(context.getConfig().getMethod() == DatabaseConfig.QUERY_METHOD.SPARQL) {
                    try (PreparedQueryExecution ex = new PreparedQueryExecution(
                            sb.buildString(), context.getConfig())) {
                        final ResultSet results = ex.queryExecution.execSelect();
                        int result = (results.nextSolution().get(VAR_COUNT.toString()).asLiteral().getInt());
                        conceptList.get(i).addStarCombination(conceptList.get(j).getLabel(), result);
                        conceptList.get(j).addStarCombination(conceptList.get(i).getLabel(), result);
                    } catch (ResultSetException e) {
                    	logQueryException(e, sb.buildString());
                    }
                } else {
                    try(DBAgentContext.DbQuery dbQuery = context.getDbQuery(sb.build())) {
                        ResultSet results = dbQuery.queryExecution.execSelect();
                        int result = (results.nextSolution().get(VAR_COUNT.toString()).asLiteral().getInt());
                        conceptList.get(i).addStarCombination(conceptList.get(j).getLabel(), result);
                        conceptList.get(j).addStarCombination(conceptList.get(i).getLabel(), result);
                    } catch (Exception e) {
                    	logQueryException(e, sb.buildString());
                    }
                }
    		}
		}
    	return new DbAgentExpertise(conceptList);
    }
    
    /**
     * Builds a count query in the form {@code SelectBuilder} with the given mappings
     * @param	mappings mapping of the concept
     * @param	count	count for variable name to keep them unique
     * @return	SelectBuilder with the constraints based on mappings
     */
    protected SelectBuilder buildCountQuery(Map<MappingPropertyType, ArrayList<Node>> mappings, int count) {
    	Expr subjExpr = new ExprVar(VAR_SUBJECT);
    	SelectBuilder sb = new SelectBuilder();
    	Aggregator agg = new AggCountVarDistinct(subjExpr);
    	ExprAggregator expression = new ExprAggregator(VAR_SUBJECT, agg);
    	sb.addVar(expression, VAR_COUNT);
    	addWhereClause(mappings, sb, count);
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

    /**
     * Notifies SSE publisher when the expertise information is ready
     * @param planInterface	plan interface of the agent
     */
    private void notifyReady(PlanToAgentInterface planInterface) {
        MinimalFunctionalityContext context = planInterface.getAgent().getContext(MinimalFunctionalityContext.class);
        if(context.fullFunctionalityReady() && !context.hasNotifiedFullState()) {
            DirectSsePublisher publisher = planInterface.getAgent().getContext(DirectSsePublisher.class);
            if(publisher != null) {
                publisher.publishStateReady();
                context.setHasNotifiedFullState(true);
            } else {
                Platform.getLogger().log(AddDbExpertisePlan.class, Level.SEVERE,
                        "No DirectSsePublisher found on DBAgent. Can't send READY status update");
            }
        }
    }
    
    protected void logQueryException(Exception e, String query) {
    	Platform.getLogger().log(getClass(), Level.SEVERE, 
    			String.format("Agent %s failed to execute query: %s\n%s",
                this.agentID, e.getMessage(), query));
    }
}
