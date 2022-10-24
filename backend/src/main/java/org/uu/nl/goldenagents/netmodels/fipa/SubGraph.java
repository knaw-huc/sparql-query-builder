package org.uu.nl.goldenagents.netmodels.fipa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.platform.Platform;


public class SubGraph implements FIPASendableObject {

	private static final long serialVersionUID = 1L;
	private final byte[] data;
	private final long size;
	private final Integer targetAqlQueryID;
	private final Status status;
	private final String errorReason;

	private SubGraph(Model model, Status status, Integer targetAqlQueryID, String errorReason) throws IOException {
		this.size = model.size();
		this.targetAqlQueryID = targetAqlQueryID;
		this.status = status;
		this.errorReason = errorReason;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			model.write(stream, "N-TRIPLE");
			this.data = stream.toByteArray();
		}
	}

	public SubGraph(Model model, Status status, Integer targetAqlQueryID) throws IOException {
		this(model, status, targetAqlQueryID, null);
	}

	public SubGraph(Integer targetAqlQueryID) throws IOException {
		this(ModelFactory.createDefaultModel(), Status.FINISHED, targetAqlQueryID, null);
	}

	public SubGraph(Integer targetAqlQueryID, String errorReason) throws IOException {
		this(ModelFactory.createDefaultModel(), Status.ERROR, targetAqlQueryID, errorReason);
	}
	
	public Model getModel() throws IOException {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(data)){
			final Model model = ModelFactory.createDefaultModel();
			model.read(stream, null, "N-TRIPLE"); // TODO: what to do with the base parameter?
			return model;
		}
	}
	
	@Override
	public String toString() {
		return "DB-Agent query reply subgraph, size: " + this.size;
	}

	public Integer getTargetAqlQueryID() {
		return targetAqlQueryID;
	}

	public Status getStatus() {
		return status;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public static SubGraph fromACLMessage(ACLMessage messageWithSubGraph) {
		try {
			GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) messageWithSubGraph.getContentObject();
			FIPASendableObject content = contentWrapper.getContent();
			return (SubGraph) content;
		} catch (UnreadableException e) {
			Platform.getLogger().log(SubGraph.class, e);
			return null;
		}
	}

	public enum Status {
			INTERMEDIATE,
			FINISHED,
			ERROR
	}
}
