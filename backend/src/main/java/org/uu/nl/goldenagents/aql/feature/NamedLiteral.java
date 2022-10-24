package org.uu.nl.goldenagents.aql.feature;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.VariableController;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;

import java.util.HashMap;
import java.util.Objects;

public class NamedLiteral extends Feature {

    private final EntityList.Entity literalEntity;

    public NamedLiteral(EntityList.Entity literalEntity) {
        this.literalEntity = literalEntity;
    }

    private NamedLiteral(EntityList.Entity literalEntity, ID focusName, ID parent) {
        super(focusName, parent);
        this.literalEntity = literalEntity;
    }

    @Override
    public String getAQLLabel() {
        return literalEntity.getLabel();
    }

    @Override
    public Op toARQ(Var var, VariableController controller) {
        checkIfFocus(var, controller);
        controller.addFilterOnVariable(var, this.literalEntity.getAsNode());
        return null;
    }

    @Override
    public AQLTree copy(ID parent, HashMap<ID, AQLTree> foci) {
        NamedLiteral copy = new NamedLiteral(this.literalEntity, this.getFocusName(), parent);
        foci.put(copy.getFocusName(), copy);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedLiteral that = (NamedLiteral) o;
        return Objects.equals(literalEntity.getLabel(), that.literalEntity.getLabel()) &&
                literalEntity.getRdfDataType().equals(that.literalEntity.getRdfDataType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), literalEntity.getLabel(), literalEntity.getRdfDataType());
    }
}
