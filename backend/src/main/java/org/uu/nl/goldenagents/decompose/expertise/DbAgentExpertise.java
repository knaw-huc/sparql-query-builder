package org.uu.nl.goldenagents.decompose.expertise;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.netmodels.angular.ExpertiseModel;
import org.uu.nl.goldenagents.sparql.OntologicalConceptInfo;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * FIPA complaint Expertise class for DB Agent
 */
public class DbAgentExpertise implements FIPASendableObject {

	private static final long serialVersionUID = 1L;

	private final ArrayList<OntologicalConceptInfo> conceptInfoList = new ArrayList<OntologicalConceptInfo>();

	public DbAgentExpertise(ArrayList<String> concepts) {
		super();
		for(String concept : concepts) {
			conceptInfoList.add(new OntologicalConceptInfo(concept));
		}
	}

	public DbAgentExpertise(List<OntologicalConceptInfo> infoList) {
		super();
		conceptInfoList.addAll(infoList);
	}

	/**
	 * Finds the combination ratio of two concepts. Combination ratio is calculated as
	 * the total number of records that have both concepts in their triples is divided by 
	 * the total number of records that have base concept in their triples.
	 * If either of the given concepts is not in the list of concepts, the function returns 0.
	 * @param baseConcept the base concept of the combination
	 * @param combinedConcept the pair of the base concept in the combination
	 * @return ratio of combination of two concepts to the count of base concept
	 */
	public float getStarCombinationRatio(String baseConcept, String combinedConcept) {
		
		int base = conceptInfoList.indexOf(new OntologicalConceptInfo(baseConcept));
		if(base == -1) {
			return 0f;
		}
		else {
			return conceptInfoList.get(base).getStarCombinationRatio(combinedConcept);
		}
	}
	/**
	 * Checks whether the concept is in the list of concepts and finds the count of it.
	 * 
	 * @param concept
	 * @return if it finds the concept returns the count of it, otherwise 0
	 */
	public int getCountOfConcept(String concept) {
		int index = conceptInfoList.indexOf(new OntologicalConceptInfo(concept));
		if(index == -1) {
			return 0;
		}
		else {
			return conceptInfoList.get(index).getCount();
		}
	}

	public ArrayList<OntologicalConceptInfo> getExpertInfo() {
		return conceptInfoList;
	}
	
	public List<String> getCapabilities() {
		return conceptInfoList.stream().map(OntologicalConceptInfo::getLabel).collect(Collectors.toList());
	}
	
	public boolean isCapable(String concept) {
		return getCapabilities().contains(concept);
	}
	
	public List<Integer> getCounts() {
		return conceptInfoList.stream().map(OntologicalConceptInfo::getCount).collect(Collectors.toList());
	}

	public String printAsMatrix(String dbName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Expertise Matrix of DB Agent " + dbName + ":").append(System.lineSeparator());
		sb.append(formatAsTable());
		return sb.toString();
	}

	/**
	 * Formats the expertise information as a table. 
	 * The first row of the table is concepts, the second one is counts of concepts, 
	 * and the rest is percentage of combinations of concepts that are at the corresponding
	 * row and column to the concept at row.
	 * @return nicely formatted table view of the expertise as a string
	 */
	public String formatAsTable(){
		
		List<String> labels = getCapabilities();
		List<Integer> counts = getCounts();
		
		int[] maxLengths = new int[this.conceptInfoList.size()];
		int max = labels.get(0).length();
		for (int i = 0; i < labels.size(); i++) {
			int temp = labels.get(i).length();
			maxLengths[i] = temp;
			if(temp > max) {
				max = temp;
			}
		}
		String firstColumnFormat = "%-" + (max + 2) + "s";
		StringBuilder formatBuilder = new StringBuilder();
		for (int maxLength : maxLengths)
		{
			formatBuilder.append("%-").append(maxLength + 2).append("s");
		}
		String format = formatBuilder.toString();

		StringBuilder result = new StringBuilder();
		result.append(String.format(firstColumnFormat, "CONCEPT"));
		result.append(String.format(format, labels.toArray())).append(System.lineSeparator());
		
		result.append(String.format(firstColumnFormat, "IS CLASS?"));
		result.append(String.format(format, getExpertInfo().stream().map(OntologicalConceptInfo::isClass)
				.collect(Collectors.toList()).toArray())).append(System.lineSeparator());

		if(counts != null && !counts.isEmpty()) {
			result.append(String.format(firstColumnFormat, "COUNT"));
			result.append(String.format(format, counts.toArray())).append(System.lineSeparator());
		}

		for (int i = 0; i < labels.size(); i++){
			result.append(String.format(firstColumnFormat, labels.get(i)));
			for (int j = 0; j < labels.size(); j++) {
				result.append(String.format("%-" + 
						(maxLengths[j]+2) + ".2f",conceptInfoList.get(i).getStarCombinationRatio(labels.get(j)) * 100));
			}
			result.append(System.lineSeparator());
		}
		return result.toString();
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Summary of Expertise is ");		
		conceptInfoList.forEach(expertInfo -> {
			sb.append(expertInfo.getLabel()).append(",");
		});
		if(conceptInfoList.size() > 0) {
			sb.trimToSize();
			sb.deleteCharAt(sb.length()-1);
		}	
		return sb.toString();
	}

	public ExpertiseModel[] toNetModel() {
		ExpertiseModel[] netModel = null;
		netModel = new ExpertiseModel[this.conceptInfoList.size()];
		for(int i = 0; i < this.conceptInfoList.size(); i++) {
			ExpertiseModel expertiseModel = 
					new ExpertiseModel(this.conceptInfoList.get(i).getLabel(), 
							this.conceptInfoList.get(i).getCount(), 
							this.conceptInfoList.get(i).isClass());
			for(int j = 0; j < this.conceptInfoList.size(); j++) {
				String label = conceptInfoList.get(j).getLabel();
				expertiseModel.addCombination(new ExpertiseModel(label, 
						conceptInfoList.get(i).getStarCombination(label),
						conceptInfoList.get(j).isClass()));
			}
			netModel[i] = expertiseModel;
		}
		return netModel;
	};

}
