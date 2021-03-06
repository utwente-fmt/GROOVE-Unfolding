/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2011 University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * $Id: Procedure.java 5570 2014-10-10 19:45:57Z rensink $
 */
package groove.control;

import groove.control.CtrlPar.Var;
import groove.control.template.Template;
import groove.control.term.Term;
import groove.grammar.Callable;
import groove.grammar.GrammarProperties;
import groove.grammar.QualName;
import groove.grammar.Recipe;
import groove.util.Fixable;
import groove.util.Groove;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Control-defined callable unit.
 * @author Arend Rensink
 * @version $Revision $
 */
public abstract class Procedure implements Callable, Fixable {
    /**
     * Constructor for subclassing.
     * @param fullName name of the unit
     * @param kind procedure kind
     * @param signature signature of the unit
     * @param controlName control program in which the unit has
     * been declared
     * @param startLine first line in the control program at
     * which the unit declaration starts
     * @param grammarProperties grammar properties for this procedure
     */
    protected Procedure(String fullName, Kind kind, List<Var> signature, String controlName,
        int startLine, GrammarProperties grammarProperties) {
        this.fullName = fullName;
        this.signature = signature;
        this.controlName = controlName;
        this.startLine = startLine;
        this.kind = kind;
        this.grammarProperties = grammarProperties;
    }

    @Override
    public String getLastName() {
        return QualName.lastName(getFullName());
    }

    @Override
    public String getFullName() {
        return this.fullName;
    }

    private final String fullName;

    @Override
    public List<Var> getSignature() {
        return this.signature;
    }

    private final List<Var> signature;

    /** Returns the full name of the control program in which this procedure is declared. */
    public String getControlName() {
        return this.controlName;
    }

    private final String controlName;

    /** Returns the start line of this procedure's declaration within the control program. */
    public int getStartLine() {
        return this.startLine;
    }

    private final int startLine;

    /** Returns the kind of this procedure. */
    @Override
    public final Kind getKind() {
        return this.kind;
    }

    private final Kind kind;

    /** Sets the body of the procedure.
     * Should only be invoked once, before the procedure is fixed.
     * The call fixes the procedure.
     */
    public void setTerm(Term body) {
        assert body != null;
        assert !isFixed();
        // make the body atomic if it is a recipe
        this.term = getKind() == Kind.RECIPE ? body.atom() : body;
        setFixed();
    }

    /** Returns the body of this procedure. */
    public Term getTerm() {
        assert isFixed();
        return this.term;
    }

    private Term term;

    /** Sets the control automaton of this procedure. */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /** Returns the control automaton of this procedure. */
    public Template getTemplate() {
        assert isFixed();
        return this.template;
    }

    private Template template;

    /** Returns the grammar properties for this procedure. */
    public GrammarProperties getGrammarProperties() {
        return this.grammarProperties;
    }

    /** The grammar properties for this procedure. */
    private final GrammarProperties grammarProperties;

    /**
     * Returns a mapping from input variables occurring in the signature of this procedure
     * to their corresponding indices in the signature.
     */
    public Map<CtrlVar,Integer> getInPars() {
        assert isFixed();
        if (this.inParMap == null) {
            initParMaps();
        }
        return this.inParMap;
    }

    /**
     * Returns a mapping from output variables occurring in the signature of this procedure
     * to their corresponding indices in the signature.
     */
    public Map<CtrlVar,Integer> getOutPars() {
        assert isFixed();
        if (this.outParMap == null) {
            initParMaps();
        }
        return this.outParMap;
    }

    private void initParMaps() {
        this.inParMap = new LinkedHashMap<CtrlVar,Integer>();
        this.outParMap = new LinkedHashMap<CtrlVar,Integer>();
        for (int i = 0; i < getSignature().size(); i++) {
            CtrlPar.Var par = getSignature().get(i);
            if (par.isInOnly()) {
                this.inParMap.put(par.getVar(), i);
            } else {
                this.outParMap.put(par.getVar(), i);
            }
        }
    }

    private Map<CtrlVar,Integer> inParMap;
    private Map<CtrlVar,Integer> outParMap;

    @Override
    public boolean setFixed() {
        boolean result = this.fixed;
        this.fixed = true;
        return result;
    }

    @Override
    public boolean isFixed() {
        return this.fixed;
    }

    @Override
    public void testFixed(boolean fixed) {
        if (fixed != isFixed()) {
            throw new IllegalStateException(String.format("The unit is %sfixed", fixed ? ""
                : "not "));
        }
    }

    private boolean fixed;

    @Override
    public String toString() {
        return getKind().getName(true) + " " + getFullName()
            + Groove.toString(getSignature().toArray(), "(", ")", ", ");
    }

    @Override
    public int hashCode() {
        return getFullName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Procedure)) {
            return false;
        }
        Procedure other = (Procedure) obj;
        return getFullName().equals(other.getFullName());
    }

    /**
     * Returns a function or recipe with the given signature.
     * @param fullName name of the unit
     * @param kind procedure kind; should satisfy {@link groove.grammar.Callable.Kind#isProcedure()}
     * @param priority priority of the unit
     * @param signature signature of the unit
     * @param controlName control program in which the unit has
     * been declared
     * @param startLine first line in the control program at
     * which the unit declaration starts
     * @param grammarProperties grammar properties for the new procedure
     */
    public static Procedure newInstance(String fullName, Kind kind, int priority,
        List<Var> signature, String controlName, int startLine, GrammarProperties grammarProperties) {
        assert kind.isProcedure();
        Procedure result;
        switch (kind) {
        case FUNCTION:
            assert priority == 0;
            result = new Function(fullName, signature, controlName, startLine, grammarProperties);
            break;
        case RECIPE:
            result =
                new Recipe(fullName, priority, signature, controlName, startLine, grammarProperties);
            break;
        default:
            assert false;
            result = null;
        }
        return result;
    }
}
