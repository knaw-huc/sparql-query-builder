package org.uu.nl.goldenagents.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.uu.nl.net2apl.core.platform.Platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Apache Jena provides a class called OpAsQuery with a static method called asQuery,
 * that allows us to convert a Jena ARQ Op to a String with SPARQL representation.
 *
 * Unfortunately, this class throws a NotImplementedException when a disjunction is
 * used anywhere in the query, even if that is inside an expression. So this does
 * not just apply to the SPARQL UNION operator.
 *
 * Apache, in their wise graciousness, has marked all methods on this class, including
 * the one throwing the exception, as private, meaning that, while we can override the
 * class, we cannot really do anything with all the other methods.
 *
 * The two options are to copy the entire code, and re-implement the one function,
 * or to use a bit of reflection magic.
 *
 * WARNING: Neither option is ideal, as in either case, an update could entirely break
 * this code.
 *
 * However, to avoid complex and duplicate code, the reflection approach is used here
 */
public class OpAsQueryWithDisjunction extends OpAsQuery.Converter {

    private final Op op;
    Map<Class<?>, Method> methodMap = new HashMap<>();
    Method startSubgroup;
    Method endSubgroup;
    Method convertMethod;

    public OpAsQueryWithDisjunction(Op op) {
        super(op);
        this.op = op;
        Method[] methodList = this.getClass().getSuperclass().getDeclaredMethods();
        for (Method method : methodList) {
            method.setAccessible(true);
            switch (method.getName()) {
                case "visit":
                    methodMap.put(method.getParameterTypes()[0], method);
                    break;
                case "startSubGroup":
                    startSubgroup = method;
                    break;
                case "endSubGroup":
                    endSubgroup = method;
                    break;
                case "convert":
                    convertMethod = method;
                    break;
            }
        }
    }

    public Query convertMaintainingDisjunction()
    {
        try {
            return (Query) convertMethod.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Platform.getLogger().log(
                    getClass(),
                    Level.SEVERE,
                    "Failed to convert query to SPARQL String using reflection-based custom class"
            );
            Platform.getLogger().log(getClass(), e);
            Platform.getLogger().log(getClass(), Level.SEVERE, "Falling back to default, hoping it contains no disjunctions");
            return OpAsQuery.asQuery(this.op);
        }
    }

    @Override
    public void visit(OpDisjunction opDisjunction) {
        try {
            for (Op x : opDisjunction.getElements()) {
                startSubgroup.invoke(this);
                methodMap.get(x.getClass()).invoke(this, x);
                endSubgroup.invoke(this);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            /*
            Uh-oh. That is not good.
            Let's parse only the first element. If we cannot add a union, this
            is the best that we can do, as parsing all remaining elements separately,
            ignoring the fact that this is a union, would also change the semantics of
            the query, and in fact, in a less obvious way.
             */
            Platform.getLogger().log(
                    getClass(),
                    Level.SEVERE,
                    "Failed to parse a UNION in the query. To avoid complete crash " +
                            "only the left-hand side of the remainder of the query will be parsed.\n" +
                            "This means PART OF THE QUERY IS LOST!"
            );
            Op firstChild = opDisjunction.getElements().get(0);
            try {
                methodMap.get(firstChild.getClass()).invoke(this);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                // Ok, so our backup did not work either. That is unfortunate
                ex.printStackTrace();
            }
        }
    }
}

