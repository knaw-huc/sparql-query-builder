package org.uu.nl.goldenagents.sparql;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.VarUtils;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;
import java.util.logging.Level;

/**	
 * 	An element visitor that is created to extract information
 *	from Sparql Query. 
 *
 * 	@author Golden Agents Group, Utrecht University
 *	@see	org.apache.jena.sparql.syntax.ElementWalker
 *	@see	org.apache.jena.query.Query#getQueryPattern()
 */
public class ElementVisitorGA implements ElementVisitor {

	/**.
	 * Logger of the platform
	 */
	private static final Loggable LOGGER = Platform.getLogger();
	private final LinkedHashSet<TriplePath> triplePaths = new LinkedHashSet<>();
	private final LinkedHashSet<Expr> filters = new LinkedHashSet<>();
	private final LinkedHashMap<Var,Expr> binds = new LinkedHashMap<>();
	private final LinkedHashSet<Element> notProccessedElements = new LinkedHashSet<>();
	private QueryInfo queryInfo;
	
	public ElementVisitorGA() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * If you want to throw an error, use {@code BadQueryException}
	 * @param queryInfo
	 */
	public ElementVisitorGA(QueryInfo queryInfo) throws BadQueryException {
		this.queryInfo = queryInfo;
	}

	@Override
	public void visit(ElementPathBlock el) {
		Iterator<TriplePath> it = el.patternElts();
		while (it.hasNext()) {
			TriplePath tp = it.next();
			List<Var> vars = new ArrayList<>();
			VarUtils.addVarsFromTriplePath(vars, tp);
			triplePaths.add(tp);
			queryInfo.addTripleInfo(tp, vars);
		}
	}

	@Override
	public void visit(ElementFilter el) {
		filters.add(el.getExpr());
		LOGGER.log(this.getClass(), Level.FINEST, "Filter " + el.getExpr().getVarsMentioned());
		FilterInfo fi = new FilterInfo(el.toString());
		queryInfo.getFilters().add(fi);
	}
	
	@Override
	public void visit(ElementBind el) {
		if(!binds.containsKey(el.getVar())) {
			binds.put(el.getVar(), el.getExpr());
		}
		LOGGER.log(this.getClass(), Level.FINEST, "Bind " + el.getVar() + el.getExpr().getVarsMentioned());
		BindInfo bi = new BindInfo(el.toString());
		queryInfo.getBinds().add(bi);
	}
	
	/**
	 *	Not implemented because 
	 *	ElementTriplesBlock is not used in SPARQL 1.1.
	 */
	@Override
	public void visit(ElementTriplesBlock el) {
		LOGGER.log(getClass(), Level.WARNING, "ElementTriplesBlock is not implemented in query parsing.");
		addNotProccessed(el);
	}
	
	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementOptional el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementOptional is not implemented in query parsing.");
		addNotProccessed(el);
	}
	
	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementUnion el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementUnion is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementAssign is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementData el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementData is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementGroup el) {
		// TODO Auto-generated method stub
//		el.getElements().forEach(x -> ElementWalker.walk(x, this));
		LOGGER.log(getClass(), Level.WARNING, String.format(
				"Visiting ElementGroup %s. ElementGroup is not implemented for Golden Agents query parsing",
				el.toString()));
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementDataset is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementNamedGraph el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementNamedGraph is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementExists el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementExists is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementNotExists is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementMinus el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementMinus is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementService el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementService is not implemented in query parsing.");
		addNotProccessed(el);
	}

	/**
	 *	Not implemented
	 */
	@Override
	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub
		LOGGER.log(getClass(), Level.WARNING, "ElementSubQuery is not implemented in query parsing.");
		addNotProccessed(el);
	}
	
	private void addNotProccessed(Element el) {
		this.notProccessedElements.add(el);
	}

	public LinkedHashSet<TriplePath> getTriplePaths() {
		return triplePaths;
	}

	public LinkedHashSet<Expr> getFilters() {
		return filters;
	}

	public LinkedHashMap<Var, Expr> getBinds() {
		return binds;
	}

	public LinkedHashSet<Element> getNotProccessedElements() {
		return notProccessedElements;
	}

}
