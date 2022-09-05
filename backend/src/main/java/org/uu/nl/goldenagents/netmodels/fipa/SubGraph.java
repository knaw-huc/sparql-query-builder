package org.uu.nl.goldenagents.netmodels.fipa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;


public class SubGraph implements FIPASendableObject {

	private static final long serialVersionUID = 1L;
	private final byte[] data;
	private final long size;
	
	public SubGraph(Model model) throws IOException {
		this.size = model.size();
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			model.write(stream, "N-TRIPLE");
			this.data = stream.toByteArray();
		}
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
}
