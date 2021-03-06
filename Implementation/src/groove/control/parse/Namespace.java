/*
 * GROOVE: GRaphs for Object Oriented VErification Copyright 2003--2007
 * University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id: Namespace.java 5517 2014-08-19 22:01:43Z rensink $
 */
package groove.control.parse;

import groove.control.Procedure;
import groove.control.term.Term;
import groove.grammar.Action;
import groove.grammar.Callable;
import groove.grammar.GrammarProperties;
import groove.grammar.QualName;
import groove.grammar.Rule;
import groove.util.antlr.ParseInfo;
import groove.util.parse.FormatError;
import groove.util.parse.FormatErrorSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Namespace for building a control automaton.
 * The namespace holds the function, transaction and rule names.
 * @author Arend Rensink
 * @version $Revision $
 */
public class Namespace implements ParseInfo {
    /** Constructs a new name space, on the basis of a given algebra family.
     */
    public Namespace(GrammarProperties grammarProperties) {
        this.grammarProperties = grammarProperties;
    }

    /** Returns the algebra family of this name space. */
    public GrammarProperties getGrammarProperties() {
        return this.grammarProperties;
    }

    private final GrammarProperties grammarProperties;

    /**
     * Adds a function or recipe to the set of declared procedures.
     * @return {@code true} if the name is new.
     */
    public boolean addProcedure(Procedure proc) {
        String fullName = proc.getFullName();
        boolean result = !hasCallable(fullName);
        if (result) {
            this.callableMap.put(fullName, proc);
            this.controlNameMap.put(fullName, getControlName());
        }
        return result;
    }

    /** Returns the control program name in which a procedure with a given name has been declared. */
    public String getControlName(String procName) {
        return this.controlNameMap.get(procName);
    }

    /** Mapping from declared procedures to the declaring control program. */
    private final Map<String,String> controlNameMap = new HashMap<String,String>();

    /**
     * Adds a rule to the name space.
     */
    public boolean addRule(Rule rule) {
        String ruleName = rule.getFullName();
        boolean result = !hasCallable(ruleName);
        if (result) {
            this.callableMap.put(ruleName, rule);
        }
        return result;
    }

    /** Checks if a callable unit with a given name has been declared. */
    public boolean hasCallable(String name) {
        return this.callableMap.containsKey(name);
    }

    /** Returns the callable unit with a given name. */
    public Callable getCallable(String name) {
        return this.callableMap.get(name);
    }

    /** Returns the collection of all callable units known in this name space. */
    public Collection<Callable> getCallables() {
        return this.callableMap.values();
    }

    /** Tries to add a dependency from a caller to a callee.
     */
    public void addCall(String caller, String callee) {
        if (caller != null) {
            Set<String> parentCallers = getFromMap(this.callerMap, caller);
            Set<String> callees = getFromMap(this.calleeMap, caller);
            if (callees.add(callee)) {
                Set<String> childCallees = getFromMap(this.calleeMap, callee);
                for (String parentCaller : parentCallers) {
                    getFromMap(this.calleeMap, parentCaller).addAll(childCallees);
                }
                for (String childCallee : childCallees) {
                    getFromMap(this.callerMap, childCallee).addAll(parentCallers);
                }
            }
        }
        this.usedNames.add(callee);
    }

    private Set<String> getFromMap(Map<String,Set<String>> map, String key) {
        Set<String> result = map.get(key);
        if (result == null) {
            map.put(key, result = new HashSet<String>());
            result.add(key);
        }
        return result;
    }

    /** Mapping from declared callable unit names to the units. */
    private final Map<String,Callable> callableMap = new TreeMap<String,Callable>();

    /** Mapping from function names to other functions being invoked from it. */
    private final Map<String,Set<String>> calleeMap = new HashMap<String,Set<String>>();
    /** Mapping from function names to other functions invoking it. */
    private final Map<String,Set<String>> callerMap = new HashMap<String,Set<String>>();

    /** Sets the full name of the control program currently being explored. */
    public void setControlName(String controlName) {
        this.controlName = controlName;
        this.parentName = QualName.parent(controlName);
        if (!this.importedMap.containsKey(controlName)) {
            this.importedMap.put(controlName, new HashSet<String>());
            this.importMap.put(controlName, new HashMap<String,String>());
        }
    }

    /** Returns the full name of the control program being parsed. */
    public String getControlName() {
        return this.controlName;
    }

    /** Returns the parent name space of the control program being parsed. */
    public String getParentName() {
        return this.parentName;
    }

    /** Full name of the program file being parsed. */
    private String controlName;
    /** Parent name of {@link #controlName}. */
    private String parentName;

    /** Adds an import to the map of the current control program. */
    public void addImport(String fullName) {
        this.importedMap.get(this.controlName).add(fullName);
        this.importMap.get(this.controlName).put(QualName.lastName(fullName), fullName);
    }

    /** Tests if a given qualified name is imported. */
    public boolean hasImport(String fullName) {
        return this.importedMap.get(this.controlName).contains(fullName);
    }

    /** Returns a mapping from last names to full names for all imported names. */
    public Map<String,String> getImportMap() {
        return this.importMap.get(this.controlName);
    }

    private final Map<String,Set<String>> importedMap = new HashMap<String,Set<String>>();
    private final Map<String,Map<String,String>> importMap =
        new HashMap<String,Map<String,String>>();

    /**
     * Returns the set of all top-level actions (rules and recipes).
     * Rules and recipes directly or indirectly invoked from (other) procedures are
     * excluded from this set.
     */
    public Set<Action> getActions() {
        Set<Action> result = this.actions;
        if (result == null) {
            Set<String> calledNames = new HashSet<String>();
            for (Callable callable : this.callableMap.values()) {
                if (callable instanceof Action) {
                    if (callable.getKind().isProcedure()) {
                        String name = callable.getFullName();
                        Set<String> newCalledNames = new HashSet<String>();
                        Set<String> calleeMapValue = this.calleeMap.get(name);
                        if (calleeMapValue != null) {
                            newCalledNames.addAll(calleeMapValue);
                        }
                        newCalledNames.remove(name);
                        calledNames.addAll(newCalledNames);
                    }
                }
            }
            result = this.actions = new TreeSet<Action>();
            for (Callable unit : this.callableMap.values()) {
                if (!(unit instanceof Action)) {
                    continue;
                }
                Action action = (Action) unit;
                if (action.isProperty() || !calledNames.contains(action.getFullName())) {
                    result.add(action);
                }
            }
        }
        return result;
    }

    /** Set of top-level rule and recipe names. */
    private Set<Action> actions;

    /**
     * Returns the set of all property actions in the grammar.
     */
    public Set<Action> getProperties() {
        Set<Action> result = this.properties;
        if (result == null) {
            result = this.properties = new TreeSet<Action>();
            for (Action action : getActions()) {
                if (action.isProperty()) {
                    result.add(action);
                }
            }
        }
        return result;
    }

    /** Set of property actions. */
    private Set<Action> properties;

    /**
     * Returns the set of all top-level transformer actions (rules and recipes).
     * Rules and recipes directly or indirectly invoked from (other) procedures are
     * excluded from this set.
     */
    public Set<Action> getTransformers() {
        Set<Action> result = this.transformers;
        if (result == null) {
            result = this.transformers = new TreeSet<Action>(getActions());
            result.removeAll(getProperties());
        }
        return result;
    }

    /** Set of transformer actions. */
    private Set<Action> transformers;

    /** Returns the set of all used names,
     * i.e., all rules for which {@link #addCall(String, String)}
     * has been invoked.
     */
    public Set<String> getUsedNames() {
        return this.usedNames;
    }

    /** Set of callable names appearing explicitly in the control program. */
    private final Set<String> usedNames = new HashSet<String>();

    @Override
    public String toString() {
        return String.format("Namespace for %s, defining %s", getControlName(),
            this.callableMap.keySet());
    }

    /** Adds an error to the errors contained in this name space. */
    public void addError(String message, Object... args) {
        this.errors.add(message, args);
    }

    /** Adds an error to the errors contained in this name space. */
    public void addError(FormatError error) {
        this.errors.add(error);
    }

    /** Returns the errors collected in this name space. */
    public FormatErrorSet getErrors() {
        return this.errors;
    }

    /** Indicates if any errors were collected. */
    public boolean hasError() {
        return !getErrors().isEmpty();
    }

    private final FormatErrorSet errors = new FormatErrorSet();

    /** Returns a prototype term, to be shared by all programs compiled
     * against this name space.
     */
    public Term getPrototype() {
        if (this.prototype == null) {
            this.prototype = Term.prototype();
        }
        return this.prototype;
    }

    private Term prototype;
}
