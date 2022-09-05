package org.uu.nl.goldenagents.netmodels.fipa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * This class is the representation of a sub-query that is sent by the broker to a DB-agent.
 */
public class AgentQuery implements FIPASendableObject {

	private static final String NEWLINE = "\n";
	private static final long serialVersionUID = 1L;

	/**
	 * Null after construction, filled when {@code translate()} method is called.
	 */
	private TripleInfo[] translatedTriples;
	private TripleInfo[] triples;
	private String[] binds;
	private String[] filters;
	private HashMap<String, HashSet<String>> values;
	private boolean translated = false;
	private HashMap<String, String> prefixMap = null;
	private AgentID queryOwner;
	private StringBuilder prefixErrors = new StringBuilder();

	public AgentQuery(AgentID queryOwner) {
		this.queryOwner = queryOwner;
	}

	/**
	 * Translate this query using the {@code model} argument.
	 * @param model
	 */
	public void translate(Model model) {
		
		//Load the prefixes coming from the model
		model.getNsPrefixMap().forEach((k,v) -> {
			//Check if the prefix has not used before
			if(!prefixMap.containsKey(k)) {
				this.prefixMap.put(k,v);
			}
			//If it is used, check if the ontologies of the prefix are different
			else if (!prefixMap.get(k).equals(model.getNsPrefixMap().get(k))) {
				prefixErrors.append("Prefix " + k + " is used for different ontologies in the query (" + 
						this.prefixMap.get(k) + ") and in the mapping file (" + 
						model.getNsPrefixMap().get(k) + ").").append(System.lineSeparator());
				/*
				 * TODO We should find a way to prevent this to happen
				 * One solution might be a kind of hashing for prefixes in the mapping to make them (sort of) unique
				 */
			}
		});
		
		this.translatedTriples = new TripleInfo[triples.length];

		for (int i = 0; i < this.triples.length; i++) {
			final TripleInfo translation = this.triples[i].createCopy();

			StmtIterator it = model.listStatements();
			// TODO maybe this could be refactored to use DbTranslationContext
			while (it.hasNext()) {
				Statement s = it.next();

				// Assume subject is the GoldenAgents concept
				String gaURI = model.shortForm(s.getSubject().getURI());
				String translationPredicate = model.shortForm(s.getPredicate().asResource().getURI());
				String translatedURI = model.shortForm(s.getObject().asResource().getURI());

				// If previous assumption was wrong (i.e. object, which is translated URI starts with GA prefix), switch
				// object and subject around
				if(translatedURI.startsWith("ga:")) {
					String temp = gaURI;
					gaURI = translatedURI;
					translatedURI = temp;
				}

				// If a Golden Agents concept is used in the triple at the subject position,
				// replace it with the translated URI
				if (translation.getSubject().equals(gaURI)) {
					translation.setSubject(translatedURI);
				}

				// This update does nothing if gaURI is not present somewhere in path
				translation.getPredicatePath().update(gaURI, translatedURI);

				// If a GoldenAgents concept is used in the triple at the object position, replace it with the
				// translated URI
				if (translation.getObject().equals(gaURI)) {
					translation.setObject(translatedURI);
				}

				// If the equivalence property used in the mapping is OWL:InverseOf, inverse the property path of the
				// query translation as well
				if (translationPredicate.equals("owl:inverseOf")) {
					translation.getPredicatePath().invert(translatedURI);
				}
			}

			this.translatedTriples[i] = translation;
		}
		this.translated = true;
	}

	public boolean isEmpty() {
		return (this.triples == null) || this.triples.length == 0;
	}

	public boolean hasBinds() {
		return (this.binds != null) && this.binds.length != 0;
	}

	public boolean hasFilters() {
		return (this.filters != null) && this.filters.length != 0;
	}
	
	public boolean hasValues() {
		return (this.values != null) && !this.values.isEmpty();
	}

	/**
	 * Used for giving a short representation for use in the UI
	 */
	@Override
	public String toString() {
		return "Construct query with " + triples.length + " triples, " + binds.length + " binds and " + filters.length + " filters";
	}

	public String createConstruct(AgentID aid) {

		if(!translated) {
			throw new IllegalStateException("Cannot create a construct from an untranslated query, please call translate() method first");
		}

		int idStringLength = aid.toString().length();
		char[] hashtags = new char[idStringLength + 2];
		Arrays.fill(hashtags, '#');

		String header = new String(hashtags) + NEWLINE + 
				"# Golden Agents Framework " + NEWLINE +
				"# Generated construct query " + NEWLINE +
				"# " + aid.toString() + NEWLINE + 
				new String(hashtags);

		String prefixes = prefixMap.entrySet()
				.stream()
				.map(e -> "PREFIX " + e.getKey() + ": <" + e.getValue() + ">" )
				.collect(Collectors.joining(NEWLINE));

		String tripleConstructPattern = Arrays.stream(triples)
				.map(ti -> "\t" + ti.toConstructHeader() + " .")
				.collect(Collectors.joining(NEWLINE));

		String tripleWherePattern = Arrays.stream(translatedTriples)
				.map(ti -> {
					return "\t" + ti.toString() + " .";
				})
				.collect(Collectors.joining(NEWLINE));

		//if(hasConstraints())
		//	tripleWherePattern += NEWLINE + Arrays.stream(getConstraints())
		//		.map(constraint -> "\t" + constraint.toString())
		//		.collect(Collectors.joining(NEWLINE));


		if(hasBinds())
			tripleWherePattern += NEWLINE + Arrays.stream(getBinds())
			.map(bind -> "\t" + bind.toString())
			.collect(Collectors.joining(NEWLINE));

		if(hasFilters())
			tripleWherePattern += NEWLINE +  Arrays.stream(getFilters())
			.map(filter -> "\t" + filter.toString())
			.collect(Collectors.joining(NEWLINE));
		
		if(hasValues()) {
			StringBuilder sb = new StringBuilder();
			sb.append(NEWLINE).append("VALUES ");
			for(String varName : values.keySet()) {
				sb.append(varName).append(" {");
				for(String uri : values.get(varName)) {
					sb.append(uri + " ");
				}
				sb.append("}");
			}
			tripleWherePattern += sb.toString();
		}

		return	header + NEWLINE +
				prefixes + NEWLINE +
				"CONSTRUCT {" + NEWLINE + tripleConstructPattern + NEWLINE +"}" + NEWLINE + 
				"WHERE {" + NEWLINE + tripleWherePattern + NEWLINE + "}";
	}
	
	/**
	 * If there is a prefix conflict error, returns the prefixes and their conflicting mappings. 
	 * Otherwise returns null.
	 * @return the prefixes and their conflicting mappings
	 */
	public String getPrefixError() {
		if(prefixErrors.toString().equals("")) {
			return null;
		}
		return prefixErrors.toString();
	}
	
	public AgentID getQueryOwner() {
		return queryOwner;
	}

	public TripleInfo[] getTriples() {
		return triples;
	}

	public void setTriples(TripleInfo[] triples) {
		this.triples = triples;
	}

	public String[] getBinds() {
		return binds;
	}

	public void setBinds(String[] binds) {
		this.binds = binds;
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	public HashMap<String, HashSet<String>> getValues() {
		return values;
	}

	public void setValues(HashMap<String, HashSet<String>> values) {
		this.values = values;
	}

	public HashMap<String, String> getPrefixMap() {
		return prefixMap;
	}

	public void setPrefixMap(HashMap<String, String> prefixMap) {
		this.prefixMap = prefixMap;
	}

	public TripleInfo[] getTranslatedTriples() {
		return translatedTriples;
	}
}