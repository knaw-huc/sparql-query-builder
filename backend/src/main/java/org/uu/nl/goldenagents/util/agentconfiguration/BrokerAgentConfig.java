package org.uu.nl.goldenagents.util.agentconfiguration;

import org.tomlj.TomlTable;
import org.uu.nl.goldenagents.agent.args.BrokerAgentArguments;
import org.uu.nl.goldenagents.agent.context.BrokerContext;
import org.uu.nl.goldenagents.agent.context.BrokerPrefixNamespaceContext;
import org.uu.nl.goldenagents.agent.plan.broker.FindOntologyConceptsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.LoadLinksetsPlan;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadConceptsGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadLinksetGoal;
import org.uu.nl.net2apl.core.agent.AgentArguments;

import java.util.List;

public class BrokerAgentConfig implements IParseAgentConfiguration {

    private final TomlTable configuration;
    private final List<RdfSourceConfig> linksets;
    private final List<RdfSourceConfig> ontologies;

    public BrokerAgentConfig(TomlTable configuration, List<RdfSourceConfig> ontologies, List<RdfSourceConfig> linksets) {
        this.configuration = configuration;
        this.ontologies = ontologies;
        this.linksets = linksets;
    }

	@Override
    public AgentArguments addConfigurationToArguments(AgentArguments arguments) {
        BrokerContext c = new BrokerContext(this.ontologies, this.linksets);
        BrokerAgentArguments brokerAgentArguments = new BrokerAgentArguments(c);
        brokerAgentArguments.addContext(new BrokerPrefixNamespaceContext());
        /*
         * TODO if reading ontology and linksets from remote sources cannot be handled easily,
         * we may consider implementing them as different plan types
         */
        arguments.addInitialPlan(new FindOntologyConceptsPlan(new LoadConceptsGoal()));
        arguments.addInitialPlan(new LoadLinksetsPlan(new LoadLinksetGoal()));
        arguments.include(brokerAgentArguments);
        return arguments;
    }

    public List<RdfSourceConfig> getLinksets() {
		return this.linksets;
	}

	public List<RdfSourceConfig> getOntologies() {
		return this.ontologies;
	}
}
