package org.uu.nl.goldenagents.sparql;

import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a complex predicate path used in a SPARQL query.
 * 
 * @author Jurian Baas
 * 
 * Utrecht University
 */
public class PathInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Loggable LOGGER = Platform.getLogger();
	private static final String DELIMITERS = "([\\\\/]|[\\\\|])";
	private static final Pattern pattern = Pattern.compile(DELIMITERS);
	private static final String[] PREFIXES = {"^"};
	private static final String[] SUFFIXES = {"*", "+", "?"};
	private String[] predicates, prefixes, suffixes;

	private String[] separators;
	private String alias;
	
	public PathInfo(String raw) {
		try {
			String[] parts = raw.split(DELIMITERS);
			this.predicates = new String[parts.length];
			this.prefixes = new String[parts.length];
			this.suffixes = new String[parts.length];

			for (int i = 0; i < parts.length; i++) {
				prefixes[i] = getPrefix(parts[i]);
				suffixes[i] = getSuffix(parts[i]);

				if (prefixes[i] != null)
					parts[i] = parts[i].substring(1);

				if (suffixes[i] != null)
					parts[i] = parts[i].substring(0, parts[i].length() - 1);

				predicates[i] = parts[i];
			}

			this.separators = new String[parts.length - 1];
			final Matcher matcher = pattern.matcher(raw);
			int i = 0;
			while(matcher.find()) {
				separators[i] = matcher.group();
				i++;
			}

		} catch (StringIndexOutOfBoundsException e) {
			/*
			 * TODO When the prefixes are not mapped properly,
			 * the function throws an error because of the substring operators in the for loop.
			 * For example, predicate is not in the form of ga:x although it is expected to be.
			 *
			 * This can happen if no short-form can be found, as no prefix is available for the namespace of
			 * a resource used as a predicate
			 */
			LOGGER.log(getClass(), Level.SEVERE, "Failed to parse path for predicate string '" + raw + "'");
			LOGGER.log(getClass(), Level.SEVERE, e);
			this.predicates = new String[] {raw};
			this.prefixes = new String[1];
			this.suffixes = new String[1];
			this.separators = new String[0];
		}
	}
	
	/**
	 * Used in constructor time for parsing prefixes
	 * @param predicate
	 * @return
	 */
	private String getPrefix(String predicate) {
		char firstChar = predicate.charAt(0);
		for(int i = 0; i < PREFIXES.length; i++) {
			char prefix = PREFIXES[i].charAt(0);
			if(prefix == firstChar) {
				return PREFIXES[i];
			}
		}
		return null;
	}
	
	/**
	 * Used in constructor time for parsing suffixes
	 * @param predicate
	 * @return
	 */
	private String getSuffix(String predicate) {
		char lastchar = predicate.charAt(predicate.length() - 1);
		for(int i = 0; i < SUFFIXES.length; i++) {
			char suffix = SUFFIXES[i].charAt(0);
			if(suffix == lastchar) {
				return SUFFIXES[i];
			}
		}
		return null;
	}
	
	/**
	 * Give the number of URI's in this path
	 * @return
	 */
	public int length() {
		return this.predicates.length;
	}
	
	public String getAlias() {
		return this.alias;
	}
	
	public String toConstructHeader() {
		if(isSimple()) {
			return this.predicates[0];
		} else {
			return this.alias;
		}
	}
	
	public String toString() {
		String out = "";
		for(int i = 0; i < length(); i++) {
			String predicate = predicates[i];
			if(prefixes[i] != null) predicate = prefixes[i] + predicate;
			if(suffixes[i] != null) predicate += suffixes[i];
			
			out += predicate;
			if(i < separators.length) out += separators[i];
		}
		
		return out;
	}

	/***
	 * Check whether this path contains the given {@code uri}
	 * @param uri
	 * @return
	 */
	public boolean contains(String uri) {
		/* TODO  this function returns true even one of the predicates are in the path. 
		But the other one may not be a capability of the agent */
		for(String predicate : this.predicates) {
			if(predicate.equals(uri)) return true;
		}
		return false;
	}


	/**
	 * Update part of this path that matches {@code originalURI} with a new URI {@code translatedURI}. 
	 * Does nothing if {@code originalURI} is not present in the path.
	 * @param originalURI
	 * @param translatedURI
	 */
	public void update(String originalURI, String translatedURI) {
		for(int i = 0; i < predicates.length; i++) {
			if(predicates[i].equals(originalURI)) predicates[i] = translatedURI;
		}
	}
	
	/**
	 * Invert a part of this path that matches {@code uriToInvert}. Does nothing if nothing in the path matches the supplied URI.
	 * @param uri The URI to invert
	 */
	public void invert(String uriToInvert) {
		for(int i = 0; i < predicates.length; i++) {
			if(predicates[i].equals(uriToInvert)) {
				prefixes[i] = prefixes[i] == null ? "^" : null;
			}
		}
	}

	public boolean isSimple() {
		return this.length() == 1 && this.prefixes[0] == null && this.suffixes[0] == null;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String[] getPredicates() {
		return predicates;
	}

}