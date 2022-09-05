package org.uu.nl.goldenagents.netmodels.angular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpertiseModel {
    private String label;
    private int count;
    private boolean isClass;
    private List<ExpertiseModel> combinations;

    public ExpertiseModel(String label, int count, List<ExpertiseModel> combinations) {
        this.label = label;
        this.count = count;
        this.combinations = combinations;
    }

    public ExpertiseModel(String label, int count, ExpertiseModel... combinations) {
        this.label = label;
        this.count = count;
        this.combinations = Arrays.asList(combinations);
    }

    public ExpertiseModel(String label, int count, boolean isClass) {
        this.label = label;
        this.count = count;
        this.isClass = isClass;
    }

    public void addCombination(ExpertiseModel combination) {
        if(this.combinations == null) {
            this.combinations = new ArrayList<>();
        }
        this.combinations.add(combination);
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    public List<ExpertiseModel> getCombinations() {
        return combinations;
    }

	public boolean isClass() {
		return isClass;
	}

}
