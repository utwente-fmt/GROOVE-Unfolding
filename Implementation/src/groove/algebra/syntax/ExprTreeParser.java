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
 * $Id$
 */
package groove.algebra.syntax;

import static groove.util.parse.TermTreeParser.TokenClaz.ASSIGN;
import static groove.util.parse.TermTreeParser.TokenClaz.NAME;
import groove.algebra.Operator;
import groove.algebra.Sort;
import groove.algebra.syntax.ExprTree.ExprOp;
import groove.util.parse.FormatError;
import groove.util.parse.FormatErrorSet;
import groove.util.parse.FormatException;
import groove.util.parse.Id;
import groove.util.parse.OpKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Parser specialisation that also parses optional sort prefixes,
 * and returns {@link ExprTree} objects.
 * @author Arend Rensink
 * @version $Revision $
 */
public class ExprTreeParser extends groove.util.parse.TermTreeParser<ExprTree.ExprOp,ExprTree> {
    /** Constructs a parser, with optional flags for assignment and test mode.
     * {@code assign} and {@code test} may not be simultaneously true
     * @param assign if {@code true}, the parser expects a top-level assignment
     * @param test if {@code true}, the parser allows legacy test expressions
     * (having a "=" on the top level), which is converted to  
     */
    private ExprTreeParser(boolean assign, boolean test) {
        super(new ExprTree(getAtom()), getOpList());
        super.setQualIds(true);
        assert !assign || !test;
        this.assign = assign;
        this.test = test;
    }

    /** Indicates if this parser expects a top-level assignment. */
    public boolean isAssign() {
        return this.assign;
    }

    private final boolean assign;

    /** Indicates if this parser recognises the legacy test semantics. */
    public boolean isTest() {
        return this.test;
    }

    private final boolean test;

    @Override
    protected ExprTree parse() {
        ExprTree result = null;
        try {
            if (isTest() && has(TokenClaz.NAME)) {
                result = parseTest();
                if (result == null) {
                    result = super.parse();
                }
            } else if (isAssign()) {
                result = parseAssign();
            } else {
                result = super.parse();
            }
        } catch (FormatException exc) {
            result = createErrorTree(exc);
        }
        return result;
    }

    /**
     * Attempts to parse the input as a legacy test expression,
     * with a top-level operator '=' which is converted to equality ("==")
     * @return the resulting (converted) expression, or {@code null}
     * if the input was not shaped as a legacy test expression
     */
    private ExprTree parseTest() throws FormatException {
        ExprTree result = null;
        Token nameToken = consume(NAME);
        if (nameToken != null) {
            if (consume(ASSIGN) == null) {
                // this was not a legacy test expression after all
                rollBack();
            } else {
                // replace the top-level "=" by "=="
                ExprTree lhs = createTree(getAtomOp());
                lhs.setId(new Id(nameToken.substring()));
                result = createTree(getEquality());
                result.addArg(lhs);
                result.addArg(super.parse());
            }
        }
        setParseString(result, nameToken);
        return result;
    }

    /**
     * Attempts to parse the input as an assignment expression,
     * with a top-level operator '='.
     * If {@link #isAssign()} is {@code true}, this throws an
     * exception if there is no to-level assignment.
     * The assignment is encoded as equality ("==")
     */
    private ExprTree parseAssign() throws FormatException {
        ExprTree result = null;
        Token nameToken = consume(NAME);
        if (nameToken == null) {
            throw expectedToken(NAME, next());
        } else if (consume(ASSIGN) == null) {
            throw expectedToken(ASSIGN, next());
        } else {
            ExprTree lhs = createTree(getAtomOp());
            lhs.setId(new Id(nameToken.substring()));
            result = createTree(ExprTree.ASSIGN);
            result.addArg(lhs);
            result.addArg(super.parse());
        }
        setParseString(result, nameToken);
        return result;
    }

    @Override
    protected ExprTree parseName() throws FormatException {
        ExprTree result;
        Token firstToken = next();
        Sort sort = parseSortPrefix();
        if (sort == null) {
            result = super.parseName();
        } else {
            result = super.parse(OpKind.ATOM);
            result.setSort(sort);
            setParseString(result, firstToken);
        }
        return result;
    }

    /** Attempts to parse the input as a sort prefix.
     * Rolls back if this does not succeed.
     * @return the parsed sort, or {@code null} if the input does not start with a sort prefix
     */
    private Sort parseSortPrefix() throws ScanException {
        Sort result = null;
        if (has(TokenClaz.SORT)) {
            Token sortToken = consume(TokenClaz.SORT);
            if (consume(TokenClaz.SORT_SEP) == null) {
                // this wasn't meant to be a sort prefix after all
                rollBack();
            } else {
                result = sortToken.type(TokenClaz.SORT).sort();
            }
        }
        return result;
    }

    /** Returns the collection of operators to be recognised by the parser. */
    static public List<ExprOp> getOpList() {
        if (opList == null) {
            opList = createOpList();
        }
        return opList;
    }

    static private List<ExprOp> opList;

    /** Returns the mapping from operator symbols to arity-indexed lists of operators. */
    static private List<ExprOp> createOpList() {
        List<ExprOp> result = new ArrayList<ExprOp>();
        // register all operators
        Map<String,Map<OpKind,ExprOp>> opMapMap = new TreeMap<String,Map<OpKind,ExprOp>>();
        for (Operator sortOp : Operator.getOps()) {
            // register the operator by name, as a call operator
            registerOp(opMapMap, result, sortOp, OpKind.CALL, sortOp.getName());
            // register the operator by symbol, if it is not only usable as call operator
            OpKind opKind = sortOp.getKind();
            if (opKind == OpKind.CALL) {
                continue;
            }
            String opSymbol = sortOp.getSymbol();
            if (opSymbol == null) {
                continue;
            }
            registerOp(opMapMap, result, sortOp, opKind, opSymbol);
        }
        result.add(new ExprOp(OpKind.ATOM, null));
        return result;
    }

    static private void registerOp(Map<String,Map<OpKind,ExprOp>> mapMap, List<ExprOp> opList,
        Operator sortOp, OpKind opKind, String symbol) {
        Map<OpKind,ExprOp> opMap = mapMap.get(symbol);
        if (opMap == null) {
            mapMap.put(symbol, opMap = new EnumMap<OpKind,ExprTree.ExprOp>(OpKind.class));
        }
        ExprOp result = opMap.get(opKind);
        if (result == null) {
            opMap.put(opKind, result = new ExprOp(opKind, symbol, sortOp.getArity()));
            opList.add(result);
        }
        result.add(sortOp);
    }

    /** Parses a given input as assignment.
     * @throws FormatException if there is a parse error.
     */
    static public ExprTree parseAssign(String input) throws FormatException {
        return parse(ASSIGN_PARSER, input);
    }

    /** Parses a given input as an expression, with or without legacy test syntax. 
     * @throws FormatException if there is a parse error.
     */
    static public ExprTree parseExpr(String input, boolean test) throws FormatException {
        return parse(test ? TEST_PARSER : EXPR_PARSER, input);
    }

    /** Parses a given input with a given parser. 
     * @throws FormatException if there is a parse error.
     */
    static private ExprTree parse(ExprTreeParser parser, String input) throws FormatException {
        ExprTree result = parser.parse(input);
        FormatErrorSet errors = new FormatErrorSet();
        for (FormatError error : result.getErrors()) {
            errors.add("Can't parse %s: %s", input, error);
        }
        errors.throwException();
        return result;
    }

    /** Expression parser. */
    static public final ExprTreeParser EXPR_PARSER = new ExprTreeParser(false, false);
    /** Assignment statement parser. */
    static public final ExprTreeParser ASSIGN_PARSER = new ExprTreeParser(true, false);
    /** Expression parser allowing legacy test syntax (top-level "="). */
    static public final ExprTreeParser TEST_PARSER = new ExprTreeParser(false, true);

    /** Retrieves the atom operator from the list of predefined operators. */
    private static ExprOp getAtom() {
        ExprOp result = null;
        for (ExprOp op : getOpList()) {
            if (op.getKind() == OpKind.ATOM) {
                result = op;
                break;
            }
        }
        assert result != null;
        return result;
    }

    /** Retrieves the equality operator from the list of predefined operators. */
    private static ExprOp getEquality() {
        ExprOp result = null;
        for (ExprOp op : getOpList()) {
            if (op.getKind() == OpKind.EQUAL && op.getSymbol().equals(EQUALS_SYMBOL)
                && op.getArity() == 2) {
                result = op;
                break;
            }
        }
        assert result != null;
        return result;
    }

    /**
     * Symbol for the equality operator.
     */
    private static final String EQUALS_SYMBOL = "==";
}