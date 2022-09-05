package org.uu.nl.goldenagents.util.agentconfiguration;

import org.tomlj.TomlTable;
import org.uu.nl.goldenagents.agent.args.UserAgentArguments;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.net2apl.core.agent.AgentArguments;

public class UserAgentConfig implements IParseAgentConfiguration {

    private TomlTable configuration;

    public UserAgentConfig(TomlTable configuration) {
        this.configuration = configuration;
    }

    @Override
    public AgentArguments addConfigurationToArguments(AgentArguments arguments) {
        UserAgentArguments userAgentArguments = new UserAgentArguments();
        userAgentArguments.addContext(new PrefixNSListenerContext());
        arguments.include(userAgentArguments);
        return arguments;
    }
}
