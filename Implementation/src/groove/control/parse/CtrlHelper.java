/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2007 University of Twente
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
 * $Id: CtrlHelper.java 5561 2014-09-29 21:13:27Z rensink $
 */
package groove.control.parse;

import static groove.control.parse.CtrlParser.ANY;
import static groove.control.parse.CtrlParser.ID;
import static groove.control.parse.CtrlParser.OTHER;
import groove.algebra.AlgebraFamily;
import groove.algebra.syntax.Expression;
import groove.control.Call;
import groove.control.CtrlPar;
import groove.control.CtrlType;
import groove.control.CtrlVar;
import groove.control.Procedure;
import groove.grammar.Action;
import groove.grammar.Callable;
import groove.grammar.Callable.Kind;
import groove.grammar.CheckPolicy;
import groove.grammar.QualName;
import groove.util.parse.FormatException;
import groove.util.parse.StringHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

/**
 * Helper class for GCL parsing.
 * Acts as an interface between the grammar and the namespace.
 * @author Arend Rensink
 * @version $Revision $
 */
public class CtrlHelper {
    /** Constructs a helper object for a given parser and namespace. */
    public CtrlHelper(Namespace namespace) {
        assert namespace != null;
        this.namespace = namespace;
    }

    /** Sets the package for the declared names. */
    void setPackage(CtrlTree packageTree) {
        this.packageName = packageTree.getText();
        String parentName = this.namespace.getParentName();
        if (!parentName.equals(this.packageName)) {
            emitErrorMessage(packageTree,
                "Package declaration '%s' does not equal program location '%s'", this.packageName,
                parentName);
        }
    }

    /**
     * Creates a new tree with a {@link CtrlParser#PACKAGE} token at the root, and
     * an empty text.
     */
    CtrlTree emptyPackage() {
        CommonToken token = new CommonToken(CtrlParser.ID, "");
        CtrlTree name = new CtrlTree(token);
        name.addChild(new CtrlTree(token));
        // construct the result tree
        CtrlTree result = new CtrlTree(CtrlParser.PACKAGE);
        result.addChild(name);
        result.addChild(new CtrlTree(CtrlParser.SEMI));
        return result;
    }

    /** Adds an import to this compilation unit. */
    void addImport(CommonTree importTree) {
        this.namespace.addImport(importTree.getText());
    }

    /** Closes the current variable scope. */
    void openScope() {
        this.symbolTable.openScope();
    }

    /** Opens a new variable scope. */
    void closeScope() {
        this.symbolTable.closeScope();
    }

    /**
     * Declares a new branch in the program. This checkpoints the set of
     * initialised variables.
     */
    @SuppressWarnings("unchecked")
    void startBranch() {
        this.initVarScopes.push(new Set[] {new HashSet<CtrlVar>(this.initVars), null});
    }

    /**
     * Switches to the next option in the top level branch of the program.
     */
    void nextBranch() {
        Set<CtrlVar>[] topInitVarScope = this.initVarScopes.peek();
        if (topInitVarScope[1] == null) {
            topInitVarScope[1] = new HashSet<CtrlVar>(this.initVars);
        } else {
            topInitVarScope[1].retainAll(this.initVars);
        }
        this.initVars = new HashSet<CtrlVar>(topInitVarScope[0]);
    }

    /**
     * Ends the top level branch of the program. Sets the initialised variables
     * to those initialised in every option.
     */
    void endBranch() {
        Set<CtrlVar>[] topInitVarScope = this.initVarScopes.pop();
        if (topInitVarScope[1] == null) {
            // this was the only branch
        } else {
            // at least one branch was ended before; take the intersection
            // with the current (final) branch
            this.initVars.retainAll(topInitVarScope[1]);
        }
    }

    /** Constructs a qualified name tree from a single token
     * (which is an {@link #ANY}, {@link #OTHER}) or {@link #ID}).
     * The new control tree has an empty path or the {@link #ID} text
     * as token text, and a single child recording the line/column data
     * @param asterisk a (potentially {@code null}) asterisk token
     * @param call the {@link #ANY}, {@link #OTHER} of {@link #ID} token
     */
    CtrlTree toQualName(Token asterisk, Token call) {
        assert asterisk == null || asterisk.getType() == CtrlParser.ASTERISK;
        assert call.getType() == ANY || call.getType() == OTHER || call.getType() == ID;
        String topText;
        Token topToken;
        if (asterisk == null) {
            topText = getText(call);
            topToken = call;
        } else {
            topText = QualName.extend(getText(asterisk), getText(call));
            topToken = asterisk;
        }
        CommonToken top = new CommonToken(call.getType(), topText);
        top.setLine(topToken.getLine());
        top.setTokenIndex(topToken.getTokenIndex());
        CtrlTree result = new CtrlTree(top);
        // artificial child
        CommonToken child = new CommonToken(ID, "");
        child.setLine(call.getLine());
        child.setTokenIndex(call.getTokenIndex());
        result.addChild(new CtrlTree(child));
        return result;
    }

    /** Creates a qualified name tree by extending an existing one
     * with an additional level in front
     * @param init token holding the additional level text
     * @param subTree the existing tree; may be {@code null}, in which case
     * the result is calculated using {@link #toQualName(Token,Token)}
     */
    CommonTree toQualName(Token init, CtrlTree subTree) {
        CtrlTree result;
        if (subTree == null || this.namespace.hasError()) {
            result = toQualName(null, init);
        } else {
            Token subTop = subTree.getToken();
            String qualText =
                getText(subTop).isEmpty() ? getText(init) : QualName.extend(getText(init),
                    getText(subTop));
            CommonToken top = new CommonToken(subTop.getType(), qualText);
            top.setLine(init.getLine());
            top.setTokenIndex(init.getTokenIndex());
            result = new CtrlTree(top);
            result.addChild(subTree.getChild(0));
        }
        return result;
    }

    /**
     * Tests if the rule name is qualified;
     * if not, first tries to look it up in the import map, and if that fails,
     * prefixes it with the package name.
     */
    CommonTree qualify(CommonTree ruleNameToken) {
        CommonTree result = ruleNameToken;
        String name = ruleNameToken.getText();
        if (!this.namespace.hasError() && QualName.parent(name).isEmpty()) {
            Map<String,String> importMap = getNamespace().getImportMap();
            if (importMap.containsKey(name)) {
                name = importMap.get(name);
            } else if (!isAnyOther(name)) {
                name = QualName.extend(this.packageName, name);
            }
            CommonToken token = new CommonToken(ruleNameToken.getType(), name);
            token.setLine(ruleNameToken.getLine());
            token.setTokenIndex(ruleNameToken.getToken().getTokenIndex());
            result = new CtrlTree(token);
            result.addChild(ruleNameToken.getChild(0));
        }
        return result;
    }

    /**
     * Retrieves the text in a token,
     * while stripping the optional backquotes from an ID token
     */
    String getText(Token idToken) {
        String result = idToken.getText();
        if (idToken.getType() == CtrlParser.ID && !result.isEmpty() && result.charAt(0) == '`') {
            assert result.charAt(result.length() - 1) == '`';
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    /** Tests if a certain string equals the #ANY or #OTHER token text. */
    private boolean isAnyOther(String text) {
        return text.equals(ANY_TEXT) || text.equals(OTHER_TEXT);
    }

    /** Qualifies a given name by prefixing it with the package name,
     * if it is not already a qualified name. */
    String qualify(String name) {
        return QualName.extend(this.namespace.getParentName(), name);
    }

    /** Returns a dot-separated string consisting of a number of token texts. */
    String flatten(List<?> children) {
        String result = null;
        for (Object token : children) {
            result = QualName.extend(result, ((CommonToken) token).getText());
        }
        return result.toString();
    }

    /** Sets the control name (from the name space) of this given program tree. */
    void declareProgram(CtrlTree programTree) {
        programTree.setControlName(this.namespace.getControlName());
    }

    /**
     * Attempts to add a function or recipe declaration with a given name.
     * Checks for overlap with the previously declared names.
     * @return {@code true} if no rule, function or recipe with the name of this one was
     * already declared; {@code false} otherwise
     */
    boolean declareCtrlUnit(CtrlTree unitTree) {
        boolean result = false;
        assert (unitTree.getType() == CtrlParser.FUNCTION || unitTree.getType() == CtrlParser.RECIPE)
            && unitTree.getChildCount() <= 4;
        String fullName = qualify(unitTree.getChild(0).getText());
        Callable unit = this.namespace.getCallable(fullName);
        if (unit != null) {
            emitErrorMessage(unitTree, "Duplicate name: %s %s already defined",
                unit.getKind().getName(true), fullName);
        } else {
            int priority =
                unitTree.getChildCount() == 3 ? 0
                    : Integer.parseInt(unitTree.getChild(2).getText());
            List<CtrlPar.Var> parList = getPars(fullName, unitTree.getChild(1));
            String controlName = this.namespace.getControlName();
            Kind kind = toProcKind(unitTree);
            this.namespace.addProcedure(Procedure.newInstance(fullName, kind, priority, parList,
                controlName, unitTree.getLine(), this.namespace.getGrammarProperties()));
            result = true;
        }
        return result;
    }

    /**
     * Converts a tree token type to a procedure kind.
     */
    private Kind toProcKind(CtrlTree tree) {
        return tree.getType() == CtrlParser.FUNCTION ? Kind.FUNCTION : Kind.RECIPE;
    }

    /**
     * Extracts the parameter declarations.
     */
    private List<CtrlPar.Var> getPars(String procName, CtrlTree parListTree) {
        assert parListTree.getType() == CtrlChecker.PARS;
        List<CtrlPar.Var> result = new ArrayList<CtrlPar.Var>();
        if (!this.namespace.hasError()) {
            for (int i = 0; i < parListTree.getChildCount(); i++) {
                CtrlTree parTree = parListTree.getChild(i);
                boolean out = parTree.getChildCount() == 3;
                CtrlTree typeTree = parTree.getChild(out ? 1 : 0);
                CtrlType type = typeTree.getCtrlType();
                String name = parTree.getChild(out ? 2 : 1).getText();
                result.add(CtrlPar.var(procName, name, type, !out));
            }
        }
        return result;
    }

    /** Starts a procedure declaration. */
    void startBody(CtrlTree unitTree) {
        setContext(unitTree);
        this.initVars.clear();
        openScope();
    }

    /** Ends a procedure declaration. */
    void endBody(CtrlTree bodyTree) {
        for (String outPar : this.symbolTable.getOutPars()) {
            if (!this.initVars.contains(new CtrlVar(this.procName, outPar,
                this.symbolTable.getType(outPar)))) {
                emitErrorMessage(bodyTree, "Output parameter %s may fail to be initialised", outPar);
            }
        }
        closeScope();
        resetContext();
    }

    /** Sets the context being processed to a given procedure. */
    void setContext(CtrlTree unitTree) {
        assert this.procName == null;
        String procName = qualify(unitTree.getChild(0).getText());
        this.procName = procName;
    }

    /** Resets the context being processed. */
    void resetContext() {
        assert this.procName != null;
        this.procName = null;
    }

    /** The procedure name currently processed. */
    private String procName;

    /**
     * Registers a call dependency.
     */
    void registerCall(CtrlTree callTree) {
        String to = callTree.getText();
        if (!QualName.name(to).hasWildCard()) {
            String from = this.procName;
            this.namespace.addCall(from, to);
        }
    }

    /** Adds a formal parameter to the symbol table. */
    boolean declarePar(CtrlTree nameTree, CtrlTree typeTree, CtrlTree out) {
        boolean result = true;
        String name = nameTree.getText();
        CtrlType type = typeTree.getCtrlType();
        if (!this.symbolTable.declareSymbol(name, type, out != null)) {
            emitErrorMessage(nameTree, "Duplicate local variable name %s", name);
            result = false;
        } else if (out == null) {
            CtrlVar var = new CtrlVar(this.procName, name, type);
            nameTree.setCtrlVar(var);
            this.initVars.add(var);
        }
        return result;
    }

    /** Adds a variable to the symbol table. */
    boolean declareVar(CtrlTree nameTree, CtrlTree typeTree) {
        boolean result = true;
        String name = nameTree.getText();
        CtrlType type = typeTree.getCtrlType();
        if (!this.symbolTable.declareSymbol(name, type)) {
            emitErrorMessage(nameTree, "Duplicate local variable name %s", name);
            result = false;
        } else {
            nameTree.setCtrlVar(new CtrlVar(this.procName, name, type));
        }
        return result;
    }

    /** Tests whether a package declaration is appropriate. */
    void checkPackage(CtrlTree importTree) {
        String name = importTree.getText();
        String actualName = this.namespace.getParentName();
        if (!name.equals(actualName)) {
            emitErrorMessage(importTree, "Package %s should be %s", name, actualName);
        }
    }

    /** Tests whether an imported name actually exists. */
    void checkImport(CtrlTree importTree) {
        String name = importTree.getText();
        if (!this.namespace.hasCallable(name)) {
            emitErrorMessage(importTree, "Imported name '%s' does not exist", name);
        }
    }

    /**
     * Checks whether a given variable has been declared and
     * (optionally) initialised.
     *
     * @param nameTree the variable to be checked
     * @param checkInit if {@code true}, the variable should also be checked for initialisation
     * @return the declared control variable
     */
    CtrlVar checkVar(CtrlTree nameTree, boolean checkInit) {
        CtrlVar result = null;
        String name = nameTree.getText();
        CtrlType type = this.symbolTable.getType(name);
        if (type == null) {
            emitErrorMessage(nameTree, "Local variable %s not declared", name);
        } else {
            result = new CtrlVar(this.procName, name, type);
            nameTree.setCtrlVar(result);
            if (checkInit && !this.initVars.contains(result)) {
                emitErrorMessage(nameTree, "Variable %s may not have been initialised", name);
            } else {
                this.initVars.add(result);
            }
        }
        return result;
    }

    /**
     * Checks that a given syntax tree is a correct (input or output) variable argument;
     * if so, sets the parameter field of that tree.
     * @see CtrlTree#setCtrlPar(CtrlPar)
     */
    void checkVarArg(CtrlTree argTree) {
        int childCount = argTree.getChildCount();
        assert argTree.getType() == CtrlChecker.ARG && childCount > 0 && childCount <= 2;
        boolean isOutArg = childCount == 2;
        CtrlVar var = checkVar(argTree.getChild(childCount - 1), !isOutArg);
        if (var != null) {
            CtrlPar par = new CtrlPar.Var(var, !isOutArg);
            argTree.setCtrlPar(par);
        }
    }

    void checkDontCareArg(CtrlTree argTree) {
        assert argTree.getType() == CtrlChecker.ARG && argTree.getChildCount() == 1;
        CtrlPar result = CtrlPar.wild();
        argTree.setCtrlPar(result);
    }

    void checkConstArg(CtrlTree argTree) {
        assert argTree.getType() == CtrlChecker.ARG && argTree.getChildCount() == 1;
        try {
            Expression constant = Expression.parse(argTree.getChild(0).getText());
            AlgebraFamily family = this.namespace.getGrammarProperties().getAlgebraFamily();
            CtrlPar result =
                new CtrlPar.Const(family.getAlgebra(constant.getSort()), family.toValue(constant));
            argTree.setCtrlPar(result);
        } catch (FormatException e) {
            // this cannot occur, as the constant string has just been approved
            // by the control parser
            assert false : String.format("%s is not a parsable constant",
                argTree.getChild(0).getText());
        }
    }

    /**
     * Checks whether a given assignment tree satisfies the type constraints.
     * @param assignTree the syntax tree encoding the assignment
     */
    void checkAssign(CtrlTree assignTree) {
        assert assignTree.getType() == CtrlParser.BECOMES;
        assert assignTree.getChildCount() == 2;
        CtrlTree callTree = assignTree.getChild(1);
        List<CtrlPar> targets, args;
        try {
            targets = collectTargets(assignTree.getChild(0));
            args = collectCallArgs(assignTree.getChild(1));
        } catch (PreviousErrorException exc) {
            return;
        }
        for (Callable unit : collectActions(callTree)) {
            if (checkAssign(callTree, unit, args, targets)) {
                List<CtrlPar> unitArgs;
                unitArgs = new ArrayList<CtrlPar>();
                int argCount = 0, targetCount = 0;
                for (CtrlPar.Var par : unit.getSignature()) {
                    CtrlPar arg;
                    if (par.isOutOnly()) {
                        // output-only parameters must be assignment targets
                        arg = targets.get(targetCount);
                        targetCount++;
                    } else if (args == null) {
                        arg = new CtrlPar.Var(par.getVar(), false);
                    } else {
                        arg = args.get(argCount);
                        argCount++;
                    }
                    unitArgs.add(arg);
                }
                // create the call
                assignTree.addCall(new Call(unit, unitArgs));
            }
        }
    }

    /**
     * Collects the target variables of a given assignment.
     * @return the list of arguments, or {@code null} if the
     * call tree has no arguments
     * @throws PreviousErrorException if an error was previously detected and reported,
     * causing the absence of one or more of the arguments
     */
    private List<CtrlPar> collectTargets(CtrlTree targetTree) throws PreviousErrorException {
        List<CtrlPar> result = null;
        if (targetTree.getType() == CtrlParser.ARGS) {
            result = collectArgs(targetTree);
        } else {
            result = new ArrayList<CtrlPar>();
            assert targetTree.getType() == CtrlParser.VAR;
            // skip the first child: it is the declared type
            for (int i = 1; i < targetTree.getChildCount(); i++) {
                CtrlVar var = targetTree.getChild(i).getCtrlVar();
                if (var == null) {
                    throw new PreviousErrorException();
                }
                this.initVars.add(var);
                result.add(new CtrlPar.Var(var, false));
            }
        }
        return result;
    }

    /**
     * Checks whether a given call satisfies the type constraints.
     * @param callTree the syntax tree encoding the call;
     * may be a rule, procedure or group call
     */
    void checkGroupCall(CtrlTree callTree) {
        List<CtrlPar> args;
        try {
            args = collectCallArgs(callTree);
        } catch (PreviousErrorException exc) {
            return;
        }
        for (Callable unit : collectActions(callTree)) {
            assert callTree.getChild(0).getType() == CtrlParser.ID || unit instanceof Action;
            if (checkCall(callTree, unit, args)) {
                List<CtrlPar> unitArgs;
                if (args == null && callTree.getChild(0).getType() != ID) {
                    // this is a group call, for which we create artificial output parameters
                    unitArgs = new ArrayList<CtrlPar>();
                    for (CtrlPar.Var par : unit.getSignature()) {
                        assert !par.isInOnly();
                        unitArgs.add(new CtrlPar.Var(par.getVar(), false));
                    }
                } else {
                    unitArgs = args;
                }
                // create the call
                Call call = unitArgs == null ? new Call(unit) : new Call(unit, unitArgs);
                callTree.addCall(call);
            }
        }
    }

    /**
     * Collects the arguments from a given call tree.
     * @return the list of arguments, or {@code null} if the
     * call tree has no arguments
     * @throws PreviousErrorException if an error was previously detected and reported,
     * causing the absence of one or more of the arguments
     */
    private List<CtrlPar> collectCallArgs(CtrlTree callTree) throws PreviousErrorException {
        assert callTree.getType() == CtrlParser.CALL;
        List<CtrlPar> result = null;
        if (callTree.getChildCount() == 2) {
            result = collectArgs(callTree.getChild(1));
        }
        return result;
    }

    /**
     * Collects the arguments from a given arguments tree.
     * @return the list of arguments, or {@code null} if the
     * call tree has no arguments
     * @throws PreviousErrorException if an error was previously detected and reported,
     * causing the absence of one or more of the arguments
     */
    private List<CtrlPar> collectArgs(CtrlTree argsTree) throws PreviousErrorException {
        assert argsTree.getType() == CtrlParser.ARGS;
        List<CtrlPar> result;
        result = new ArrayList<CtrlPar>();
        // stop at the closing RPAR
        for (int i = 0; i < argsTree.getChildCount() - 1; i++) {
            CtrlPar arg = argsTree.getChild(i).getCtrlPar();
            // if any of the arguments is null, an error was detected
            // and reported earlier; we silently fail
            if (arg == null) {
                throw new PreviousErrorException();
            }
            result.add(arg);
        }
        return result;
    }

    /**
     * Returns the list of all actions that match a given call.
     * @param callTree the syntax tree encoding the call;
     * may be a rule, procedure or group call
     */
    private List<Callable> collectActions(CtrlTree callTree) {
        assert callTree.getType() == CtrlParser.CALL;
        CtrlTree nameTree = callTree.getChild(0);
        String unitName = nameTree.getText();
        List<Callable> result = new ArrayList<Callable>();
        if (nameTree.getType() == ID) {
            Callable unit = this.namespace.getCallable(unitName);
            Action action = unit instanceof Action ? (Action) unit : null;
            if (unit == null) {
                emitErrorMessage(callTree, "Unknown unit '%s'", unitName);
            } else if (action != null && action.getPriority() > 0) {
                String message = "Explicit call of prioritised %s '%s' not allowed";
                emitErrorMessage(callTree, message, unit.getKind().getName(false), unitName);
            } else if (action != null && action.getRole().isConstraint()) {
                String message = "Explicit call of %s property '%s' not allowed";
                emitErrorMessage(callTree, message, action.getRole().toString(), unitName);
            } else if (action != null && action.getPolicy() == CheckPolicy.OFF) {
                String message = "Explicit call of disabled %s '%s' not allowed";
                emitErrorMessage(callTree, message, action.getRole().toString(), unitName);
            } else {
                result.add(unit);
            }
        } else {
            // collect all actions with matching names
            boolean other = nameTree.getType() == OTHER;
            Set<String> usedNames = this.namespace.getUsedNames();
            String groupName = QualName.parent(nameTree.getText());
            QualName qualGroupName = QualName.name(groupName);
            boolean qualified = !groupName.isEmpty();
            boolean wildcard = qualGroupName.hasWildCard();
            assert groupName != null;
            for (Action action : this.namespace.getActions()) {
                if (action.isProperty()) {
                    continue;
                }
                String actionName = action.getFullName();
                boolean matches;
                if (wildcard) {
                    matches = qualGroupName.matches(QualName.name(actionName));
                } else if (qualified) {
                    matches = QualName.parent(actionName).equals(groupName);
                } else {
                    // the any or other was unqualified;
                    // only use the action if it is in scope
                    // meaning declared in the current package or imported
                    matches =
                        QualName.parent(actionName).equals(this.namespace.getParentName())
                            || this.namespace.hasImport(actionName);
                }
                if (!matches) {
                    continue;
                }
                if (other && usedNames.contains(action.getFullName())) {
                    continue;
                }
                result.add(action);
            }
        }
        return result;
    }

    /**
     * Tests if an assignment call with a given argument and target list is compatible with
     * the declared signature.
     */
    private boolean checkAssign(CtrlTree callTree, Callable unit, List<CtrlPar> args,
        List<CtrlPar> targets) {
        assert unit != null;
        assert targets != null;
        List<CtrlPar.Var> sig = unit.getSignature();
        // extract the output-only arguments
        List<CtrlPar.Var> inPars = new ArrayList<CtrlPar.Var>();
        List<CtrlPar.Var> outPars = new ArrayList<CtrlPar.Var>();
        for (CtrlPar.Var par : sig) {
            if (par.isOutOnly()) {
                outPars.add(par);
            } else {
                inPars.add(par);
            }
        }
        String unitName = unit.getFullName();
        String kindName = unit.getKind().getName(true);
        String ruleSig = toTypeString(inPars, true);
        // check the assignment targets against the output-only parameters
        boolean outResult = true;
        if (outPars.isEmpty()) {
            outResult = false;
            String message = "%s %s%s has no output and cannot be used in assignment";
            emitErrorMessage(callTree, message, kindName, unitName, ruleSig);
        } else {
            outResult = outPars.size() == targets.size();
            for (int i = 0; outResult && i < targets.size(); i++) {
                outResult = outPars.get(i).compatibleWith(targets.get(i));
            }
            if (!outResult) {
                String message = "Outcome of %s %s%s not assignable to variables %s";
                String targetSig = toTypeString(targets, false);
                emitErrorMessage(callTree, message, StringHandler.toLower(kindName), unitName,
                    ruleSig, targetSig);
            }
        }
        // check the arguments against the input and in/out-parameters
        boolean inResult = true;
        if (args == null) {
            for (int i = 0; inResult && i < inPars.size(); i++) {
                inResult = inPars.get(i).compatibleWith(CtrlPar.wild());
            }
            if (!inResult) {
                String message = "%s %s%s not assignable without arguments";
                emitErrorMessage(callTree, message, kindName, unitName, ruleSig);
            }
        } else {
            inResult = args.size() == inPars.size();
            for (int i = 0; inResult && i < args.size(); i++) {
                inResult = inPars.get(i).compatibleWith(args.get(i));
            }
            if (!inResult) {
                String message = "%s %s%s not assignable with arguments %s";
                String callSig = toTypeString(args, true);
                emitErrorMessage(callTree, message, kindName, unitName, ruleSig, callSig);
            }
        }
        return outResult && inResult;
    }

    /**
     * Tests if a call with a given argument list is compatible with
     * the declared signature.
     */
    private boolean checkCall(CtrlTree callTree, Callable unit, List<CtrlPar> args) {
        assert unit != null;
        boolean result;
        String name = unit.getFullName();
        List<CtrlPar.Var> sig = unit.getSignature();
        Kind unitKind = unit.getKind();
        if (args == null) {
            result = true;
            for (int i = 0; result && i < sig.size(); i++) {
                result = sig.get(i).compatibleWith(CtrlPar.wild());
            }
            if (!result) {
                String message = "%s %s%s not applicable without arguments";
                String ruleSig = toTypeString(sig, true);
                emitErrorMessage(callTree, message, unitKind.getName(true), name, ruleSig);
            }
        } else {
            result = args.size() == sig.size();
            for (int i = 0; result && i < args.size(); i++) {
                result = sig.get(i).compatibleWith(args.get(i));
            }
            if (!result) {
                String message = "%s %s%s not applicable for arguments %s";
                String callSig = toTypeString(args, true);
                String ruleSig = toTypeString(sig, true);
                emitErrorMessage(callTree, message, unitKind.getName(true), name, ruleSig, callSig);
            }
        }
        return result;
    }

    void checkEOF(CtrlTree EOFToken) {
        if (this.packageName.isEmpty() && !this.namespace.getParentName().isEmpty()) {
            emitErrorMessage(EOFToken, "Missing package declaration",
                this.namespace.getControlName());
        }
    }

    String toTypeString(List<? extends CtrlPar> sig, boolean brackets) {
        StringBuilder result = new StringBuilder();
        if (brackets) {
            result.append('(');
        }
        for (CtrlPar par : sig) {
            if (result.length() > 1) {
                result.append(',');
            }
            if (par.isOutOnly()) {
                result.append(CtrlPar.OUT_PREFIX);
                result.append(' ');
            }
            result.append(par.getType());
        }
        if (brackets) {
            result.append(')');
        }
        return result.toString();
    }

    /** Clears the name space errors. */
    void clearErrors() {
        // we're starting a new control expression forget the old errors
        this.namespace.getErrors().clear();
    }

    /**Adds an error to the name space, if possible prefixed with the line and
     * column of a given control tree. */
    void emitErrorMessage(CtrlTree marker, String message, Object... args) {
        if (marker == null) {
            this.namespace.addError(message, args);
        } else {
            this.namespace.addError(marker.createError(message, args));
        }
    }

    /** Adds an error to the name space. */
    void addError(String message, int line, int column) {
        this.namespace.addError(message, line, column);
    }

    Namespace getNamespace() {
        return this.namespace;
    }

    /** Namespace to enter the declared functions. */
    private final Namespace namespace;
    /** The symbol table holding the local variable declarations. */
    private final SymbolTable symbolTable = new SymbolTable();
    /** Set of currently initialised variables. */
    private Set<CtrlVar> initVars = new HashSet<CtrlVar>();
    /**
     * Stack of checkpointed initialised variables. Each stack record consists
     * of two sets of variables. The first element is the set of variables
     * initialised at the start of the branch, the second is the set of
     * variables initialised in each case of the branch.
     */
    private final Stack<Set<CtrlVar>[]> initVarScopes = new Stack<Set<CtrlVar>[]>();

    /** Name of the module in which all declared names should be placed. */
    private String packageName = "";

    private final static String ANY_TEXT = CtrlParser.tokenNames[ANY].toLowerCase();
    private final static String OTHER_TEXT = CtrlParser.tokenNames[OTHER].toLowerCase();

    /** Special exception class to signal the effect of a previously
     * detected error.
     * @author rensink
     * @version $Revision $
     */
    private static class PreviousErrorException extends Exception {
        // empty
    }
}
