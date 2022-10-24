package org.uu.nl.goldenagents.netmodels.fipa;

import java.util.*;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.agent.context.query.DbTranslationContext;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.goldenagents.util.SparqlUtils;
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
	private HashMap<String, String> prefixMap = new HashMap<>();
	private AgentID queryOwner;
	private final Integer targetAqlQueryID;
	private StringBuilder prefixErrors = new StringBuilder();

	public AgentQuery(AgentID queryOwner, Integer targetAqlQueryID) {
		this.queryOwner = queryOwner;
		this.targetAqlQueryID = targetAqlQueryID;
	}

	/**
	 * Translate this query using the {@code model} argument.
	 * @param translationContext
	 */
	public void translate(DbTranslationContext translationContext) {

		//Load the prefixes coming from the model
		Map<String, String> prefixMap = translationContext.getPrefixContext().getPrefixMap();
		prefixMap.forEach((k, v) -> {
			//Check if the prefix has not used before
			if(!this.prefixMap.containsKey(k)) {
				this.prefixMap.put(k,v);
			}
			//If it is used, check if the ontologies of the prefix are different
			else if (!this.prefixMap.get(k).equals(prefixMap.get(k))) {
				prefixErrors
						.append("Prefix ")
						.append(k)
						.append(" is used for different ontologies in the query (")
						.append(this.prefixMap.get(k))
						.append(") and in the mapping file (")
						.append(prefixMap.get(k)).append(").")
						.append(System.lineSeparator());
				/*
				 * TODO We should find a way to prevent this to happen
				 * One solution might be a kind of hashing for prefixes in the mapping to make them (sort of) unique
				 */
			}
		});

		this.translatedTriples = new TripleInfo[triples.length];

		for (int i = 0; i < this.triples.length; i++) {
			this.triples[i].setSubject(SparqlUtils.validateLocalName(this.triples[i].getSubject(), prefixMap, true));
			this.triples[i].setObject(SparqlUtils.validateLocalName(this.triples[i].getObject(), prefixMap, true));
			String[] predicates = this.triples[i].getPredicatePath().getPredicates();
			for(String predicate : predicates) {
				this.triples[i].getPredicatePath().update(predicate, SparqlUtils.validateLocalName(predicate, prefixMap, true));
			}

			final TripleInfo translation = this.triples[i].createCopy();

			// TODO, officially, multiple local concepts can together comprise a global concept. In this case, we need a union,
			// but the current structure hardly allows for that
			List<DbTranslationContext.Translation> tSubj = translationContext.getGlobalToLocalTranslation(translation.getSubject());
			List<DbTranslationContext.Translation> tPred = translationContext.getGlobalToLocalTranslation(translation.getPredicate());
			List<DbTranslationContext.Translation> tObj = translationContext.getGlobalToLocalTranslation(translation.getObject());

			if (tSubj != null) {
				translation.setSubject(getValidatedLocalName(tSubj, translationContext));
			}

			if(tPred != null) {
				String translatedConcept = getValidatedLocalName(tPred, translationContext);
				translation.getPredicatePath().update(translation.getPredicate(), translatedConcept);
				if (tPred.get(0).isInverse()) {
					translation.getPredicatePath().invert(translatedConcept);
				}
			}

			if(tObj != null) {
				translation.setObject(getValidatedLocalName(tObj, translationContext));
			}

			this.translatedTriples[i] = translation;
		}
		this.translated = true;
	}

	private String getValidatedLocalName(List<DbTranslationContext.Translation> translations, DbTranslationContext translationContext) {
		String uri = translations.get(0).getLocalConcept().getURI();
		String shortForm = translationContext.shortForm(uri);
		return shortForm.contains("/") ? uri : shortForm;
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

	public Integer getTargetAqlQueryID() {
		return targetAqlQueryID;
	}
}