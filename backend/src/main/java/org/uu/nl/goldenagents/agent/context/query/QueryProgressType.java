package org.uu.nl.goldenagents.agent.context.query;

public enum QueryProgressType {

	QUERY_SENT(1), 
	QUERY_TRANSLATED(2), 
	SUBQUERY_SENT(3), 
	DATA_COLLECTED(4), 
	QUERY_EXECUTED(5), 
	RESULTS_RETURNED(6),
	RESULTS_COLLECTED(7),


	/****************
	 * ERRORS
	 */

	DATABASE_ERROR(-100);


	final int index;
	
	private QueryProgressType(int index){
		this.index = index;
	}
	
	public int toIndex() {
		return this.index;
	}
	
}
