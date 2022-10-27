package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.vocabulary.*;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.DirectSsePublisher;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.context.UIContext;
import org.uu.nl.goldenagents.agent.context.query.DbTranslationContext;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.goldenagents.agent.plan.broker.AddDbExpertisePlan;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.sparql.MappingPropertyType;
import org.uu.nl.goldenagents.sparql.OntologicalConceptInfo;
import org.uu.nl.goldenagents.sparql.PreparedQueryExecution;
import org.uu.nl.goldenagents.util.DatabaseConfig;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    Map<String, String> usedPrefixes;
    protected DBAgentContext context;
    protected PrefixNSListenerContext prefixContext;
    protected DbTranslationContext translationContext;

    // Optimization
    private Integer ignoreConceptsWithLessThanEntities = null;
    private boolean ignoreNonGAConcepts = true;

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
        this.context = planInterface.getContext(DBAgentContext.class);
        DbAgentExpertise expertise = context.getExpertise();
        this.translationContext = planInterface.getContext(DbTranslationContext.class);
        this.prefixContext = planInterface.getContext(PrefixNSListenerContext.class);

        if (expertise == null) { // TODO, force reload?
            expertise = readExpertiseFromFile(planInterface);
        }
        // Still null?
        if(expertise == null) {
            // Only re-discover expertise if it isn't yet available
            usedPrefixes = new HashMap<>();
            translationContext.addMappedConceptsToPrefixListenerMap(planInterface.getAgentID());
            Map<String, OntologicalConceptInfo> concepts = getConcepts();
            expertise = extractExpertise(concepts);
            writeExpertiseToFile(planInterface, expertise);
        } else {
            translationContext.addMappedConceptsToPrefixListenerMap(planInterface.getAgentID());
        }
        context.setExpertise(expertise);
        notifyReady(planInterface);
        setFinished(true);
    }

    private void writeExpertiseToFile(PlanToAgentInterface planToAgentInterface, DbAgentExpertise expertise) {
        File targetFile = getExpertiseTargetFile(planToAgentInterface);
        try {
            if(targetFile.getParentFile().mkdirs()) {
                Platform.getLogger().log(getClass(), "Created required parent directories " + targetFile.getParentFile());
            }
            FileOutputStream fos = new FileOutputStream(targetFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(expertise);
            os.close();
            Platform.getLogger().log(getClass(), "Successfully stored expertise in file " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            Platform.getLogger().log(getClass(), e);
        }
    }

    private DbAgentExpertise readExpertiseFromFile(PlanToAgentInterface planToAgentInterface) {
        File targetFile = getExpertiseTargetFile(planToAgentInterface);
        if (targetFile.exists()) {
            Platform.getLogger().log(getClass(), "Found " + targetFile.getAbsolutePath());
            try {
                FileInputStream fis = new FileInputStream(targetFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object obj = ois.readObject();
                DbAgentExpertise expertise = (DbAgentExpertise) obj;
                Platform.getLogger().log(getClass(), "Succesfully read DB agent expertise");
                return expertise;
            } catch (IOException | ClassNotFoundException e) {
                Platform.getLogger().log(getClass(), e);
            }
        } else {
            Platform.getLogger().log(getClass(), "No stored expertise object yet found. Was looking for " + targetFile.getAbsolutePath());
        }
        return null;
    }

    private File getExpertiseTargetFile(PlanToAgentInterface planToAgentInterface) {
        String nickname = planToAgentInterface.getContext(UIContext.class).getNickname();
        nickname = nickname.replaceAll("[\\\\ /'\"@#$^*()!,?;:`~]", "");
        return Path.of(
                "data",
                "db-expertise",
                nickname,
                "expertise.dat"
        ).toFile();
    }

    private Map<String, OntologicalConceptInfo> getConcepts() {
        Map<String, OntologicalConceptInfo> concepts = new HashMap<>();
        Set<Node> classes = listNodesForQuery("?x a ?concept");
        Set<Node> properties = listNodesForQuery("?x ?concept []");

        for (Node className : classes) {
            addNodeToMap(className, true, concepts);
        }

        for(Node propertyName : properties) {
            addNodeToMap(propertyName, false, concepts);
        }
        return concepts;
    }

    private Set<Node> listNodesForQuery(String bgp) {
        // Group by clause is bullshit, but stops jena from complaining so what do we care
        String q;
        if (ignoreConceptsWithLessThanEntities != null) {
            q = String.format("SELECT DISTINCT ?concept (COUNT(?x) as ?count) WHERE {%s} GROUP BY ?concept", bgp);
        } else {
            // Not doing a count makes the query faster
            q = String.format("SELECT DISTINCT ?concept WHERE {%s} GROUP BY ?concept", bgp);
        }
        Set<Node> nodes = new HashSet<>();
        try (DBAgentContext.DbQuery dbQuery = context.getDbQuery(q)) {
            ResultSet resultSet = dbQuery.queryExecution.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution s = resultSet.next();
                Node item = s.get("concept").asNode();
                if (ignoreConceptsWithLessThanEntities != null) {
                    int amount = s.get("count").asLiteral().getInt();
                    if (amount >= ignoreConceptsWithLessThanEntities || translationContext.translateLocalToGlobal(item.getURI()) != null) {
                        nodes.add(item);
                    }
                } else {
                    nodes.add(item);
                }
            }
        }
        return nodes;
    }

    private void addNodeToMap(
            Node node, boolean isClass, Map<String, OntologicalConceptInfo> concepts) {
        if(!(
                node.getNameSpace().equals(RDFS.getURI()) ||
                node.getNameSpace().equals(RDF.getURI()) ||
                node.getNameSpace().equals(OWL.getURI()) ||
                node.getNameSpace().equals(OWL2.getURI()) ||
                node.getNameSpace().equals(SKOS.getURI()) ||
                node.getNameSpace().equals(SKOSXL.getURI())
        )) {
            List<DbTranslationContext.Translation> translations = translationContext.getLocalToGlobalTranslation(node.getURI());
            if (translations == null && (!ignoreNonGAConcepts || node.getNameSpace().contains("goldenagents"))) {
                String shortForm = translationContext.shortForm(node.getURI());
                if(!addPrefixIfUsed(shortForm)) {
                    shortForm = node.getURI();
                }
                OntologicalConceptInfo info = new OntologicalConceptInfo(shortForm, isClass);
                info.addMapping(isClass ? MappingPropertyType.OWL_EQ_CLASS : MappingPropertyType.OWL_EQ_PROPERTY, node);
                concepts.put(info.getLabel(), info); // TODO, make it harder for the server!!!
            } else if (translations != null) {
                for(DbTranslationContext.Translation translation : translations) {
                    String shortForm = translation.getGlobalConceptShortform();
                    OntologicalConceptInfo info = concepts.get(shortForm);
                    addPrefixIfUsed(shortForm);
                    if (info == null) {
                        info = new OntologicalConceptInfo(translation.getGlobalConceptShortform(), isClass);
                    }
                    info.addMapping(translation.getMappingPropertyType(), node);
                    concepts.put(info.getLabel(), info);
                }
            }
        }
    }

    private boolean addPrefixIfUsed(String shortForm) {
        String prefix = shortForm.substring(0, shortForm.indexOf(":"));
        String ns = prefixContext.getNamespaceForPrefix(prefix);
        if (ns != null && shortForm.startsWith(prefix)) {
            if (usedPrefixes.get(prefix) == null || ns.equals(usedPrefixes.get(prefix))) {
                usedPrefixes.put(prefix, ns);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extracts expertise information of a data source 
     * @return expertise of the DB Agent
     */
    private DbAgentExpertise extractExpertise(Map<String, OntologicalConceptInfo> concepts) {
        return addStatsToConceptInfo(new ArrayList<>(concepts.values())).removeConceptsWithZeroEntities();
    }

    /**
     * Updates concept info with statistical information, which are
     * the number of entities having a concept (class or predicate) and 
     * the number of entities having a combination of the concepts.
     * @param 	conceptList	List of ontological concepts
     * @return	expertise of the DB Agent
     */
    protected DbAgentExpertise addStatsToConceptInfo(List<OntologicalConceptInfo> conceptList) {
        for(int i = 0; i < conceptList.size(); i++) {
            makeConceptListQuery(conceptList.get(i), conceptList.subList(i+1, conceptList.size()));
        }
        return new DbAgentExpertise(conceptList, usedPrefixes);
    }

    private String getTriplePattern(MappingPropertyType relation, Node node, int count) {
        if (relation.isClass()) {
            return String.format("{?p%d a <%s> filter (?x = ?p%d)}", count, node, count);
        }
        //owl:equivalent property and rdfs:subPropertyOf
        else if (relation.isEquivalentProperty()) {
            return String.format("{?x <%s> ?p%d}", node, count);
        }
        //owl:inverse
        else if (relation.isInverseProperty()) {
            return String.format("{?p%d <%s> ?x}", count, node);
        }
        return null;
    }

    private void makeConceptListQuery(OntologicalConceptInfo info, List<OntologicalConceptInfo> conceptList) {
        List<String> unions = new ArrayList<>();
        List<String> projection = new ArrayList<>();
        Map<String, OntologicalConceptInfo> varToInfoMap = new HashMap<>();
        int count = 0;
        for(OntologicalConceptInfo otherInfo : conceptList) {
            // Avoid large queries to prevent time outs
            if (count > 30) {
                boolean success;
                do {
                    success = executeConceptListQuery(info, unions, projection, varToInfoMap, count);
                } while (!success);
                unions = new ArrayList<>();
                projection = new ArrayList<>();
                varToInfoMap = new HashMap<>();
                count = 0;
            }
            for(MappingPropertyType relation : otherInfo.getOntologicalMappings().keySet()) {
                ArrayList<Node> nodeList = otherInfo.getOntologicalMappings().get(relation);
                for (Node node : nodeList) {
                    unions.add(getTriplePattern(relation, node, count));
                    projection.add(String.format("(COUNT (?p%d) as ?c%d)", count, count));
                    varToInfoMap.put(String.format("c%d", count), otherInfo);
                }
                count++;
            }
        }
        boolean success;
        do {
            success = executeConceptListQuery(info, unions, projection, varToInfoMap, count);
        } while(!success);
    }

    private boolean executeConceptListQuery(
            OntologicalConceptInfo info,
            List<String> unions, List<String> projection,
            Map<String, OntologicalConceptInfo> varToInfoMap,
            int count
    ) {
        List<String> mainSelect = buildMainSelect(info, count);
        String query = "SELECT\n\t" +
                "(COUNT (DISTINCT ?x) as ?c)\n\t" +
                String.join("\n\t", projection) +
                "\n{\n\t" +
                String.join(" UNION \n\t", mainSelect);
        if (unions.size() > 0) {
            query += " . \n\t{\n\t\t{} UNION\n\t\n\t\t" +
                    String.join(" UNION \n\t\t", unions) +
                    "\n\t}";
        }
        query += "\n}";
        System.out.println(query);

        long millis = System.currentTimeMillis();

        boolean success = false;

        if(context.getConfig().getMethod() == DatabaseConfig.QUERY_METHOD.SPARQL) {
            try (PreparedQueryExecution ex = new PreparedQueryExecution(
                    query, context.getConfig())) {
                ex.queryExecution.setTimeout(3, TimeUnit.MINUTES);
                final ResultSet results = ex.queryExecution.execSelect();
                processConceptListQuery(results, info, varToInfoMap);
                success = true;
            } catch (QueryExceptionHTTP e) {
                Platform.getLogger().log(getClass(), e);
            } catch (ResultSetException e) {
                logQueryException(e, query);
            }
        } else {
            try(DBAgentContext.DbQuery dbQuery = context.getDbQuery(query)) {
                dbQuery.queryExecution.setTimeout(3, TimeUnit.MINUTES);
                ResultSet results = dbQuery.queryExecution.execSelect();
                processConceptListQuery(results, info, varToInfoMap);
                success = true;
            } catch (Exception e) {
                logQueryException(e, query);
            }
        }
        if (System.currentTimeMillis() - millis > 120000 || !success) { // 2 minutes
            try {
                Thread.sleep(success ? 30000 : 6000); // Give server some rest
            } catch (InterruptedException e) {
                Platform.getLogger().log(getClass(), e);
            }
        }
        return success;
    }

    private void processConceptListQuery(ResultSet results, OntologicalConceptInfo info, Map<String, OntologicalConceptInfo> varToInfoMap) {
        while (results.hasNext()) {
            QuerySolution s = results.next();
            Iterator<String> it = s.varNames();
            while (it.hasNext()) {
                String var = it.next();
                int count = s.get(var).asLiteral().getInt();
                if (var.equals("c")) {
                    info.setCount(count);
                } else {
                    info.addStarCombination(varToInfoMap.get(var).getLabel(), count);
                    varToInfoMap.get(var).addStarCombination(info.getLabel(), count);
                }
            }
        }
    }

    private List<String> buildMainSelect(OntologicalConceptInfo info,  int count) {
        List<String> mainSelect = new ArrayList<>();

        for (MappingPropertyType relation : info.getOntologicalMappings().keySet()) {
            ArrayList<Node> nodeList = info.getOntologicalMappings().get(relation);
            for (Node node : nodeList) {
                String tp = null;
                if (relation.isClass()) {
                    tp = String.format("{?x a <%s>}", node);
                }
                //owl:equivalent property and rdfs:subPropertyOf
                else if (relation.isEquivalentProperty()) {
                    tp = String.format("{?x <%s> ?p%d}", node, count);
                }
                //owl:inverse
                else if (relation.isInverseProperty()) {
                    tp = String.format("{?p%d <%s> ?x}", count, node);
                }

                if (tp != null) mainSelect.add(tp);
            }
            count++;
        }

        return mainSelect;
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
