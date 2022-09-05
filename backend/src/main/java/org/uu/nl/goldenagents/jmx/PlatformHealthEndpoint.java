package org.uu.nl.goldenagents.jmx;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

@Component
@WebEndpoint(id = "platform")
public class PlatformHealthEndpoint {

	@Autowired
	private Platform platform;

	@Value("${golden-agents-version}")
	String version;

	@ReadOperation
	public PlatformHealth health() {
		final PlatformHealth health = new PlatformHealth();

		if(platform == null) {
			health.setHealthDetail("online", false);
		} else {
			health.setHealthDetail("version", PlatformHealthEndpoint.this.version);
			health.setHealthDetail("online", true);
			health.setHealthDetail("agent.count", platform.getLocalAgentsList().size());
			health.setHealthDetail("description", platform.getDescription());
			health.setHealthDetail("port", platform.getPort());
			health.setHealthDetail("host", platform.getHost());
			health.setHealthDetail("DF.count", platform.getLocalDirectoryFacilitators().size());
		}

		return health;
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class PlatformHealth {
		
		private final Map<String, Object> healthDetails = new HashMap<>();

		@JsonAnyGetter
		public Map<String, Object> getHealthDetails() {
			return this.healthDetails;
		}
		
		public void setHealthDetail(String key, Object value) {
			this.healthDetails.put(key, value);
		}

	}
}
