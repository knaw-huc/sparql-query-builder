package org.uu.nl.goldenagents.agent.plan.broker;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.uu.nl.goldenagents.agent.context.BrokerContext;
import org.uu.nl.goldenagents.agent.context.BrokerPrefixNamespaceContext;
import org.uu.nl.goldenagents.agent.plan.LoadRDFSourcePlan;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadConceptsGoal;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.netmodels.angular.ExpertiseModel;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plan reads the ontology to find all concepts (classes and properties) that can be used
 * in a query, such that the broker agent is able to parse the query. These base concepts can be used
 * by the QuerySuggestionPlan and are stored in the BrokerContext
 *
 * It is executed every time a new data source agent announces itself to the broker
 */
public class FindOntologyConceptsPlan extends LoadRDFSourcePlan {

	private final LoadConceptsGoal goal;
	private BrokerContext brokerContext;
	private BrokerPrefixNamespaceContext prefixContext;
	private OntModel model;
	public FindOntologyConceptsPlan(LoadConceptsGoal goal) {
		this.goal = goal;
	}

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
		this.brokerContext = planInterface.getAgent().getContext(BrokerContext.class);

		boolean shouldUpdatePrefixes = false;

		this.prefixContext = planInterface.getContext(BrokerPrefixNamespaceContext.class);

		if(!brokerContext.isAllOntologiesLoaded()) {
			if ((this.model = brokerContext.getOntology()) == null) {
				model = ModelFactory.createOntologyModel();
				brokerContext.setOntology(model);
			}
			loadModel(planInterface, model, brokerContext.getOntologyConfigs());
			shouldUpdatePrefixes = findPreferredPrefix();
			shouldUpdatePrefixes |= storePrefixesFromSourceConfiguration();
			SparqlUtils.updatePrefixesInModel(model, prefixContext.getOntologyPrefixes());

			shouldUpdatePrefixes |= addMissingPrefixesToMap();
		} else {
			this.model = brokerContext.getOntology();
		}

		if (goal.getUsedPrefixes() != null) {
			SparqlUtils.updatePrefixesInModel(model, goal.getUsedPrefixes());
			shouldUpdatePrefixes |= addMissingPrefixesToMap();
		}

		if(shouldUpdatePrefixes) {
			planInterface.adoptPlan(new SendPreferredNamespacePrefixesPlan());
		}

		processOntology();

		// TODO somehow re-trigger the SimpleSuggestSearchOptionsPlan? If it had already been triggered? If so, with what query?
		planInterface.adoptPlan(new UpdateSearchSuggestionsPlan());

		// Done!
		this.goal.setAchieved(true);
	}

	/**
	 * Store all prefixes specified in the configuration of this RDF source in the broker NS prefix map
	 * @return 	True iff the prefix map has been updated, indicating subscribing agents should be updated with the
	 * new NS map
	 */
	private boolean storePrefixesFromSourceConfiguration() {
		boolean shouldUpdatePrefixes = false;
		for(RdfSourceConfig conf : brokerContext.getOntologyConfigs()) {
			shouldUpdatePrefixes |= prefixContext.addAllPrefixes(conf.getPrefixes());
		}
		return shouldUpdatePrefixes;
	}

	/**
	 * The model may contain prefixes not officially specified as core prefixes. To ensure data source agents use
	 * the same prefixes for the same IRI's, add those prefixes and namespaces to the NS prefix map
	 * @return 	True iff the broker NS prefix map has changed
	 */
	private boolean addMissingPrefixesToMap() {
		Map<String, String> prefixMap = model.getNsPrefixMap();
		boolean shouldUpdate = false;
		for(String prefix : prefixMap.keySet()) {
			if (!prefixContext.getOntologyPrefixes().containsKey(prefix)) {
				shouldUpdate |= prefixContext.setPrefix(prefix, prefixMap.get(prefix));
			}
		}
		return shouldUpdate;
	}

	/**
	 * If the ontology specifies the preferred prefix using the PURL vocab, try to extract it
	 */
	private boolean findPreferredPrefix() {
		Query preferredNsQuery = SparqlUtils.createPreferredPrefixQuery("prefix", "ns");
		ResultSet result = QueryExecutionFactory.create(preferredNsQuery, model).execSelect();
		boolean shouldUpdatePrefixes = false;
		while(result.hasNext()) {
			QuerySolution solution = result.next();
			String prefix = solution.get("?prefix").asLiteral().getString();
			String namespace = ensureNamespaceComplete(solution.get("?ns").asResource().getURI());;
			shouldUpdatePrefixes |= prefixContext.setPrefix(prefix, namespace);
		}
		return shouldUpdatePrefixes;
	}

	/**
	 * A namespace should end with a hashtag ('#') or forward slashs ('/') to work to abbreviate IRI's in the model.
	 * However, when specifying such a namespace while not testing, this trailing character is easy to miss.
	 * This method tries to complete the namespace with the trailing character if missing, by finding a resource
	 * that starts with the provided namespace, and getting the official namespace for that IRI.
	 *
	 * @param namespace The namespace to complete
	 * @return	Namespace with trailing character added if missing
	 */
	private String ensureNamespaceComplete(String namespace) {
		if(SparqlUtils.NAMESPACE_ENDS_WITH.contains(namespace.charAt(namespace.length() - 1))) {
			return namespace;
		}
		String resolvedNamespace = returnFirstNamespaceStartingWith(model.listClasses(), namespace);
		if(resolvedNamespace == null) {
			resolvedNamespace = returnFirstNamespaceStartingWith(model.listAllOntProperties(), namespace);
		}

		// If its still null, we did the best we could
		return resolvedNamespace == null ? namespace : resolvedNamespace;
	}

	/**
	 * Select the first resource in an iterator from which the IRI starts with the passed namespace string
	 * @param iterator	Resource iterator
	 * @param namespace	Namespace to match on
	 * @return Official namespace of resource corresponding to the passed namespace
	 */
	private String returnFirstNamespaceStartingWith(ExtendedIterator<? extends OntResource> iterator, String namespace) {
		while(iterator.hasNext()) {
			OntResource next = iterator.next();
			if (next.getNameSpace().startsWith(namespace)) {
				return next.getNameSpace();
			}
		}
		return null;
	}

	private void processOntology() {
		Map<String, String> prefixes = model.getNsPrefixMap();

		// Prepare aggregators for classes and properties used in ontology
		Set<OntClass> availableClasses = new HashSet<>();
		Set<OntProperty> availableProperties = new HashSet<>();

		// Find classes and properties that actually have a mapping to some database
		for(DbAgentExpertise expertise : brokerContext.getDbAgentExpertises().values()) {
			ExpertiseModel[] netmodel = expertise.toNetModel();
			for(ExpertiseModel expertiseModel : netmodel) {
				if(expertiseModel.isClass()) {
					OntClass clazz = model.getOntClass(expertiseModel.getLabel());
					if (clazz == null) {
						String uri = SparqlUtils.validateLocalName(expertiseModel.getLabel(), prefixes, false, true);
						clazz = model.getOntClass(uri);
						if (clazz == null) {
							clazz = model.createClass(uri);
						}
					}
					availableClasses.add(clazz);
				} else {
					OntProperty property = model.getOntProperty(expertiseModel.getLabel());
					if (property == null) {
						String uri = SparqlUtils.validateLocalName(expertiseModel.getLabel(), prefixes, false, true);
						property = model.getOntProperty(uri);
						if (property == null) {
							property = model.createOntProperty(uri);
						}
					}
					availableProperties.add(property);
				}
			}
		}
		brokerContext.setClassesAndProperties(availableClasses, availableProperties);
	}

	/**
	 * Find the ontology resources from our model, that are also mapped to at least one data source agent
	 * @param iterator			Ontology resource iterator from model
	 * @param mappedLabels		List of labels of ontology resource type that are mapped to at least one data source agent
	 * @param <T>				Ontology resource type
	 * @return					Set of ontology resources
	 */
	private <T extends OntResource> Set<T> extractAvailableOntologyResources(ExtendedIterator<T> iterator, Set<String> mappedLabels) {
		Set<T> availableClasses = new HashSet<>();
		while(iterator.hasNext()) {
			T nextOntResource = iterator.next();
			if (!nextOntResource.isAnon()) {
				String shortForm = model.shortForm(nextOntResource.getURI());
				if (mappedLabels.contains(shortForm)) {
					availableClasses.add(nextOntResource);
				}
			}
		}
		return availableClasses;
	}
}
