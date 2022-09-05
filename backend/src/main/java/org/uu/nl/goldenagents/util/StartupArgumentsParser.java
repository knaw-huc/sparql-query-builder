package org.uu.nl.goldenagents.util;

import ch.rasc.sse.eventbus.SseEventBus;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.uu.nl.goldenagents.Application;
import org.uu.nl.net2apl.core.fipa.FIPAMessenger;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

public class StartupArgumentsParser {

    private static final Loggable LOGGER = Platform.getLogger();
    private static final String VERSION = "0.0.1-alpha"; // TODO add to bump-version.sh or get from application.yml (other branch)

    private Platform platform;

    @Arg(dest="host")
    private String host;

    @Arg(dest="port")
    private int port;

    @Arg(dest="otherHosts")
    private String[] otherHosts;

    @Arg(dest="threads")
    private int threads;

    @Arg(dest="agentConfiguration")
    private File agentConfiguration;

    private final TomlConfigurationParser tomlConfigurationParser;
    private static Path confPath;

    public StartupArgumentsParser(SseEventBus serverEventBus) {
        ArgumentParser parser = this.getParser();
        this.tomlConfigurationParser = new TomlConfigurationParser(parser, serverEventBus);
        try {
            parser.parseArgs(Application.ARGS, this);
            this.platform = createPlatform();
            this.agentConfiguration = this.agentConfiguration.getAbsoluteFile();
            confPath = (
                    this.agentConfiguration.isDirectory() ?
                    this.agentConfiguration :
                    this.agentConfiguration.getParentFile()
            ).toPath();
            this.tomlConfigurationParser.parseAgentConfiguration(this.platform, this.agentConfiguration);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    private Platform createPlatform() {

        ArrayList<String> otherHosts = new ArrayList<>();
        ArrayList<Integer> otherPorts = new ArrayList<>();

        if(this.otherHosts != null) {
            for (String host : this.otherHosts) {
                // TODO this could probably be more robust, but who decided we need to lists in the first place?
                String hostString = "127.0.0.1";
                int portInt = -1;
                if (host.contains(":")) {
                    int splitChar = host.lastIndexOf(":");
                    hostString = host.substring(0, splitChar);
                    String portString = host.substring(splitChar);
                    try {
                        portInt = Integer.parseInt(portString);
                    } catch (Exception e) {
                        LOGGER.log(getClass(), Level.SEVERE, "Could not parse port on host " + host);
                        LOGGER.log(getClass(), Level.SEVERE, e);
                    }
                } else {
                    hostString = host;
                }
                otherHosts.add(hostString);
                otherPorts.add(portInt);
            }
        }

        return Platform.newPlatform(
                this.threads,
                new FIPAMessenger(),
                this.host,
                this.port,
                otherHosts,
                otherPorts
        );
    }

    private ArgumentParser getParser() {
        ArgumentParser parser = ArgumentParsers.newFor("Golden Agents Backend").build()
                .version(VERSION)
                .defaultHelp(true)
                .description("The Multi-Agent system that helps a user answer questions spanning multiple " +
                        "RDF knowledge graphs");

        parser.addArgument("-a", "--address")
                .type(String.class)
                .required(false)
                .setDefault("127.0.0.1")
                .help("Specify the default host address on which agents are published")
                .dest("host");
        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .required(false)
                .setDefault(44444)
                .dest("port")
                .help("Specify the default port on which agents are published on their host name");
        parser.addArgument("-r", "--remote-addresses")
                .nargs("*")
                .type(String.class)
                .required(false)
                .help("Specify the IP-address / host name and port of remote hosts to find Directory Facilitator (DF) agents on")
                .dest("otherHosts");
        parser.addArgument("-t", "--threads")
                .dest("threads")
                .type(Integer.class)
                .required(false)
                .setDefault(4)
                .help("Specify the number of execution threads");


        return parser;
    }

    public static String resolveRelativePath(String path) {
        return confPath.resolve(path).toAbsolutePath().toString();
    }

    public Platform getPlatform() {
        return platform;
    }
}