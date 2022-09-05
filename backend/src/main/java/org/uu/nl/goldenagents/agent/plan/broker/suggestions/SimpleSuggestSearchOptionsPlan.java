package org.uu.nl.goldenagents.agent.plan.broker.suggestions;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This plan creates suggestions based on all available concepts found using the FindOntologyConceptsPlan.
 *
 * It is a naive way of providing query suggestions, as it only takes the ontology into account, but not the
 * current query or available data
 */
public class SimpleSuggestSearchOptionsPlan extends ASuggestSearchOptionsPlan {

    public SimpleSuggestSearchOptionsPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    protected void beforePlanStart() {
        BrokerSearchSuggestionsContext context = this.planInterface.getContext(BrokerSearchSuggestionsContext.class);
        context.addSubscription(this.message, this.header);
    }

    /**
     * Generate a list of classes the current query can be intersected with at the current focus
     */
    @Override
    protected List<AQLSuggestions.TypeSuggestion> generateClassList() {
        Set<OntClass> upperClasses = findUpperClasses(this.brokerContext.getOntologyClasses().iterator());
        return upperClasses.stream()
                .map(ontClass -> new AQLSuggestions.TypeSuggestion(ontClass, this.brokerContext.getOntologyClasses()))
                .collect(Collectors.toList());
    }

    /**
     * Generate a list of properties the current query can be intersected with at the current focus
     */
    @Override
    protected List<AQLSuggestions.TypeSuggestion> generatePropertyList() {
        Set<OntProperty> upperProperties = findUpperProperties(this.brokerContext.getOntologyProperties().iterator());
        final List<AQLSuggestions.TypeSuggestion> suggestions = new ArrayList<>();
        Iterator<OntProperty> it = upperProperties.iterator();
        while(it.hasNext()) {
            OntProperty ontProperty = it.next();
            suggestions.add(new AQLSuggestions.TypeSuggestion(ontProperty, true, this.brokerContext.getOntologyProperties()));
            suggestions.add(new AQLSuggestions.TypeSuggestion(ontProperty, false, this.brokerContext.getOntologyProperties()));
        }
        return suggestions;
    }

    /**
     * Generate a list of instances the current query can be intersected with at the current focus
     */
    @Override
    protected List<AQLSuggestions.InstanceSuggestion> generateInstanceList() {
        return new ArrayList<>();
    }


}
