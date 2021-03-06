// GROOVE: GRaphs for Object Oriented VErification
// Copyright 2003--2007 University of Twente

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific
// language governing permissions and limitations under the License.
/*
 * $Id: Rule.java 5764 2015-12-14 22:07:04Z rensink $
 */
package groove.grammar;

import groove.algebra.AlgebraFamily;
import groove.control.Binding;
import groove.control.CtrlPar;
import groove.control.CtrlPar.Var;
import groove.control.CtrlType;
import groove.control.CtrlVar;
import groove.grammar.host.HostEdgeSet;
import groove.grammar.host.HostGraph;
import groove.grammar.host.HostNode;
import groove.grammar.rule.Anchor;
import groove.grammar.rule.DefaultRuleNode;
import groove.grammar.rule.LabelVar;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleElement;
import groove.grammar.rule.RuleGraph;
import groove.grammar.rule.RuleNode;
import groove.grammar.rule.RuleToHostMap;
import groove.grammar.type.TypeGraph;
import groove.grammar.type.TypeGuard;
import groove.graph.GraphProperties;
import groove.graph.GraphProperties.Key;
import groove.match.Matcher;
import groove.match.MatcherFactory;
import groove.match.SearchStrategy;
import groove.match.TreeMatch;
import groove.match.plan.PlanSearchStrategy;
import groove.transform.Proof;
import groove.util.Fixable;
import groove.util.Visitor;
import groove.util.parse.FormatException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Type of a production rule. The rule essentially consists of a left hand
 * side graph, a right hand side graph, a rule morphism and a set of NACs.
 * @author Arend Rensink
 * @version $Revision: 5764 $
 */
public class Rule implements Action, Fixable {
    /**
     * Constructs a rule that is a sub-condition of another rule.
     * @param condition the application condition of this rule
     * @param rhs the right hand side graph of the rule
     * @param coRoot map of creator nodes in the parent rule to creator nodes
     *        of this rule
     */
    public Rule(Condition condition, RuleGraph rhs, RuleGraph coRoot) {
        assert condition.getTypeGraph().getFactory() == rhs.getFactory().getTypeFactory()
            && (coRoot == null || rhs.getFactory() == coRoot.getFactory());
        this.condition = condition;
        this.coRoot = coRoot;
        this.lhs = condition.getPattern();
        this.rhs = rhs;
        assert coRoot == null || rhs().nodeSet().containsAll(coRoot.nodeSet()) : String.format("RHS nodes %s do not contain all co-root values %s",
            rhs().nodeSet(),
            coRoot.nodeSet());
    }

    /** Returns the condition with which this rule is associated. */
    public Condition getCondition() {
        return this.condition;
    }

    /** Returns root graph of the condition with which this rule is associated. */
    public RuleGraph getRoot() {
        return getCondition().getRoot();
    }

    /** Returns the type graph of this rule. */
    public TypeGraph getTypeGraph() {
        return getCondition().getTypeGraph();
    }

    /** Returns the qualified name of this rule (which equals the name of the associated condition). */
    @Override
    public String getFullName() {
        return getCondition().getName();
    }

    /**
     * Returns the last part of the qualified name of this rule
     * (which equals the name of the associated condition).
     */
    @Override
    public String getLastName() {
        return QualName.lastName(getFullName());
    }

    /** Returns the system properties. */
    @Override
    public GrammarProperties getGrammarProperties() {
        return getCondition().getGrammarProperties();
    }

    /**
     * Sets the parent rule of this rule, together with the nesting level and
     * the co-root map.
     * @param parent the parent rule for this rule
     * @param level nesting level of this rule within the condition tree
     */
    public void setParent(Rule parent, int[] level) {
        testFixed(false);
        assert getCoRoot() != null : String.format("Sub-rule at level %s must have a non-trivial co-root map",
            Arrays.toString(level));
        if (parent != null) {
            assert parent.rhs().nodeSet().containsAll(getCoRoot().nodeSet()) : String.format("Rule '%s': Parent nodes %s do not contain all co-roots %s",
                getFullName(),
                parent.rhs().nodeSet(),
                getCoRoot().nodeSet());
        }
        this.parent = parent;
    }

    /**
     * Returns the parent rule of this rule. The parent may be this rule itself.
     */
    public Rule getParent() {
        if (this.parent == null) {
            testFixed(true);
            this.parent = this;
        }
        return this.parent;
    }

    /**
     * Sets the rule properties from a graph property map.
     */
    public void setProperties(GraphProperties properties) {
        testFixed(false);
        this.priority = (Integer) properties.parseProperty(Key.PRIORITY);
        this.transitionLabel = (String) properties.parseProperty(Key.TRANSITION_LABEL);
        this.formatString = (String) properties.parseProperty(Key.FORMAT);
    }

    @Override
    public String getTransitionLabel() {
        String result = this.transitionLabel;
        if (result.isEmpty()) {
            result = getFullName();
        }
        return result;
    }

    @Override
    public String getFormatString() {
        return this.formatString;
    }

    /**
     * Returns the priority of this object. A higher number means higher
     * priority, with {@link #DEFAULT_PRIORITY} the lowest.
     */
    @Override
    public int getPriority() {
        return this.priority;
    }

    /** Sets the dangling-edge check for matches of this rule. */
    public void setCheckDangling(boolean checkDangling) {
        this.checkDangling = checkDangling;
    }

    /** Indicates if this is a top-level rule. */
    public boolean isTop() {
        return getParent() == this;
    }

    /** Returns the top rule of the hierarchy in which this rule is nested. */
    public Action getTop() {
        if (isTop()) {
            return this;
        } else {
            return getParent().getTop();
        }
    }

    //    @Override
    Set<RuleNode> computeInputNodes() {
        Set<RuleNode> result = null;
        // if this is a top-level rule, the (only) input nodes
        // are the input-only parameter nodes
        if (isTop()) {
            result = new HashSet<RuleNode>();
            for (Var var : getSignature()) {
                if (var.isInOnly()) {
                    result.add(var.getRuleNode());
                }
            }
            //        } else {
            //            result = super.computeInputNodes();
        }
        return result;
    }

    /**
     * Indicates if this rule has sub-rules.
     */
    public boolean hasSubRules() {
        assert isFixed();
        return !getSubRules().isEmpty();
    }

    /**
     * Adds a sub-rule to this rule.
     * The sub-rules are those connected to sub-conditions
     * in the associated condition tree.
     * @param subRule the new sub-rule
     */
    public void addSubRule(Rule subRule) {
        assert !isFixed();
        assert subRule.isFixed();
        getSubRules().add(subRule);
    }

    /**
     * Returns the direct sub-rules of this rule, i.e., the sub-rules that have
     * this rule as their parent.
     */
    public Collection<Rule> getSubRules() {
        if (this.subRules == null) {
            this.subRules = new TreeSet<Rule>();
            for (Condition condition : getCondition().getSubConditions()) {
                for (Condition subCondition : condition.getSubConditions()) {
                    if (subCondition.hasRule()) {
                        this.subRules.add(subCondition.getRule());
                    }
                }
            }
        }
        return this.subRules;
    }

    /**
     * Sets the parameters of this rule. The rule can have numbered and hidden
     * parameters. Numbered parameters are divided into input (LHS) and output
     * (RHS-only) parameters, and are visible on the transition label.
     * @param sig the signature of the rule, i.e., the list of (visible) parameters
     * @param hiddenPars the set of hidden (i.e., unnumbered) parameter nodes
     */
    public void setSignature(List<CtrlPar.Var> sig, Set<RuleNode> hiddenPars) {
        assert !isFixed();
        this.sig = sig;
        this.hiddenPars = hiddenPars;
        List<CtrlPar.Var> derivedSig = new ArrayList<CtrlPar.Var>();
        for (int i = 0; i < sig.size(); i++) {
            // add the LHS parameters to the root graph
            RuleNode parNode = sig.get(i).getRuleNode();
            if (this.lhs.containsNode(parNode)) {
                this.condition.getRoot().addNode(parNode);
            }
            String parName = "arg" + i;
            CtrlType parType = sig.get(i).getType();
            CtrlVar var = new CtrlVar(null, parName, parType);
            CtrlPar.Var par;
            boolean inOnly = sig.get(i).isInOnly();
            boolean outOnly = sig.get(i).isOutOnly();
            if (!inOnly && !outOnly) {
                par = new CtrlPar.Var(var);
            } else {
                par = new CtrlPar.Var(var, inOnly);
            }
            derivedSig.add(par);
        }
        assert derivedSig.equals(sig) : String.format("Declared signature %s differs from derived signature %s",
            sig,
            derivedSig);
    }

    /** Returns the signature of the rule. */
    @Override
    public List<CtrlPar.Var> getSignature() {
        assert isFixed();
        if (this.sig == null) {
            this.sig = Collections.emptyList();
        }
        return this.sig;
    }

    /**
     * Returns, for a given index in the signature, the corresponding
     * anchor or creator source of the actual value.
     */
    public Binding getParBinding(int i) {
        if (this.parBinding == null) {
            this.parBinding = computeParBinding();
        }
        return this.parBinding[i];
    }

    /**
     * Creates the connection between parameter positions and
     * anchor respectively created node indices.
     * @see #getParBinding(int)
     */
    private Binding[] computeParBinding() {
        Binding[] result = new Binding[this.sig.size()];
        List<RuleNode> creatorNodes = Arrays.asList(getCreatorNodes());
        for (int i = 0; i < this.sig.size(); i++) {
            CtrlPar.Var par = this.sig.get(i);
            Binding binding;
            RuleNode ruleNode = par.getRuleNode();
            if (par.isCreator()) {
                // look up the node in the creator nodes
                binding = Binding.creator(creatorNodes.indexOf(ruleNode));
            } else {
                // look up the node in the anchor
                int ix = getAnchor().indexOf(ruleNode);
                assert ix >= 0 : String.format("Node %s not in anchors %s", ruleNode, getAnchor());
                binding = Binding.anchor(ix);
            }
            result[i] = binding;
        }
        return result;
    }

    /**
     * Returns the set of hidden (i.e., unnumbered) parameter nodes of this
     * rule.
     */
    Set<RuleNode> getHiddenPars() {
        return this.hiddenPars;
    }

    /**
     * Tests if this condition is ground and has a match to a given host graph.
     * Convenience method for <code>getMatchIter(host, null).hasNext()</code>
     */
    final public boolean hasMatch(HostGraph host) {
        return this.condition.isGround() && getMatch(host, null) != null;
    }

    @Override
    public Kind getKind() {
        return Kind.RULE;
    }

    /** Sets the action role of this rule. */
    public void setRole(Role role) {
        assert !isFixed();
        assert this.role == null && role != null;
        this.role = role;
    }

    @Override
    public Role getRole() {
        assert !this.role.isProperty() || isPropertyLike();
        return this.role;
    }

    private Role role;

    @Override
    public CheckPolicy getPolicy() {
        CheckPolicy result = getGrammarProperties().getRulePolicy().get(getFullName());
        if (result == null) {
            result = CheckPolicy.ERROR;
        }
        return result;
    }

    /** Indicates if this rule serves to test a property of a graph.
     * This is only the case if the rule is unmodifying, has no parameters
     * and has zero priority.
     */
    private boolean isPropertyLike() {
        boolean result = !isModifying() && getPriority() == 0 && getHiddenPars().isEmpty();
        if (result) {
            for (CtrlPar.Var var : getSignature()) {
                if (var.isInOnly()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isProperty() {
        return getRole().isProperty();
    }

    /**
     * Returns a match of this condition into a given host graph, given a
     * matching of the root graph.
     * @param host the graph in which the match is to be found
     * @param contextMap a matching of the root of this condition; may be
     *        <code>null</code> if the condition is ground.
     * @throws IllegalArgumentException if <code>patternMatch</code> is
     *         <code>null</code> and the condition is not ground, or if
     *         <code>patternMatch</code> is not compatible with the pattern
     *         graph
     */
    public Proof getMatch(HostGraph host, RuleToHostMap contextMap) {
        return traverseMatches(host, contextMap, Visitor.<Proof>newFinder(null));
    }

    /**
     * Returns the collection of all matches for a given host graph, given a
     * matching of the root context.
     * @param host the graph in which the match is to be found
     * @param contextMap a matching of the pattern of this condition; may be
     *        <code>null</code> if the condition is ground.
     * @throws IllegalArgumentException if <code>contextMap</code> is
     *         <code>null</code> and the condition is not ground, or if
     *         <code>contextMap</code> is not compatible with the root map
     */
    public Collection<Proof> getAllMatches(HostGraph host, RuleToHostMap contextMap) {
        List<Proof> result = new ArrayList<Proof>();
        traverseMatches(host, contextMap, Visitor.newCollector(result));
        return result;
    }

    /**
     * Traverses the matches of this rule on a given host graph and for
     * a given context map, and calls the visitor's visit method on all
     * of them, until the first time the visitor returns
     * {@code false}.
     * @param host the graph in which the match is to be found
     * @param contextMap a matching of the pattern of this condition; may be
     *        <code>null</code> if the condition is ground.
     * @param visitor the visitor invoked for all the matches
     * @return the result of the visitor after the traversal
     * @throws IllegalArgumentException if <code>patternMatch</code> is
     *         <code>null</code> and the condition is not ground, or if
     *         <code>patternMatch</code> is not compatible with the pattern
     *         graph
     * @see Visitor#visit(Object)
     */
    public <R> R traverseMatches(final HostGraph host, RuleToHostMap contextMap,
        final Visitor<Proof,R> visitor) {
        assert isFixed();
        RuleToHostMap seedMap =
            contextMap == null ? host.getFactory().createRuleToHostMap() : contextMap;
        getMatcher(seedMap).traverse(host, contextMap, new Visitor<TreeMatch,R>() {
            @Override
            protected boolean process(TreeMatch match) {
                assert visitor.isContinue();
                if (isValidPatternMap(host, match.getPatternMap())) {
                    match.traverseProofs(visitor);
                }
                return visitor.isContinue();
            }
        });
        return visitor.getResult();
    }

    /**
     * Lazily creates and returns a matcher for rule events of this rule. The
     * matcher will try to extend anchor maps to full matches. This is in
     * contrast with the normal (condition) matcher, which is based on the
     * images of the root map.
     */
    public Matcher getEventMatcher(boolean simple) {
        if (this.eventMatcher == null) {
            this.eventMatcher = createMatcher(getAnchor(), simple);
        }
        return this.eventMatcher;
    }

    /**
     * Returns the match strategy for the target
     * pattern. First creates the strategy using
     * {@link #createMatcher(Anchor, boolean)} if that
     * has not been done.
     *
     * @param seedMap mapping from the seed elements to a host graph.
     *
     * @see #createMatcher(Anchor, boolean)
     */
    private SearchStrategy getMatcher(RuleToHostMap seedMap) {
        assert isTop();
        Matcher result;
        boolean simple = seedMap.getFactory().isSimple();
        if (getSignature().size() > 0) {
            int sigSize = getSignature().size();
            BitSet initPars = new BitSet(sigSize);
            for (int i = 0; i < sigSize; i++) {
                // set initPars if the seed map contains a value
                // for this parameter
                initPars.set(i, seedMap.nodeMap().containsKey(getSignature().get(i).getRuleNode()));
            }
            result = this.matcherMap.get(initPars);
            if (result == null) {
                Anchor seed = new Anchor(seedMap.nodeMap().keySet());
                this.matcherMap.put(initPars, result = createMatcher(seed, simple));
            }
        } else {
            result = getMatcher(simple);
        }
        return result;
    }

    /**
     * Returns a (precomputed) match strategy for the target
     * pattern, based on the rule seed.
     * @param simple indicates if the host graphs are simple or multi-graphs
     * @see #createMatcher(Anchor, boolean)
     */
    public Matcher getMatcher(boolean simple) {
        Matcher result = simple ? this.simpleMatcher : this.multiMatcher;
        if (result == null) {
            result = createMatcher(getSeed(), simple);
            if (simple) {
                this.simpleMatcher = result;
            } else {
                this.multiMatcher = result;
            }
        }
        return result;
    }

    /**
     * The fixed simple matching strategy for this graph rule. Initially
     * <code>null</code>; set by {@link #getMatcher(boolean)} upon its first
     * invocation.
     */
    private Matcher simpleMatcher;

    /**
     * The fixed multi-graph matching strategy for this graph rule. Initially
     * <code>null</code>; set by {@link #getMatcher(boolean)} upon its first
     * invocation.
     */
    private Matcher multiMatcher;

    /**
     * Callback method to create a match strategy. Typically invoked once, at
     * the first invocation of {@link #getMatcher(boolean)}. This implementation
     * retrieves its value from {@link #getMatcherFactory(boolean)}.
     * @param seed the pre-matched subgraph
     * @param simple indicates if the host graphs are simple or multi-graphs
     */
    private Matcher createMatcher(Anchor seed, boolean simple) {
        testFixed(true);
        return getMatcherFactory(simple).createMatcher(getCondition(), seed);
    }

    /** Returns a matcher factory, tuned to the properties of this rule. */
    private MatcherFactory getMatcherFactory(boolean simple) {
        return MatcherFactory.instance(simple);
    }

    /**
     * Tests whether a given match map satisfies the additional constraints
     * imposed by this rule.
     * @param host the graph to be matched
     * @param matchMap the proposed map from {@link #lhs()} to
     *        <code>host</code>
     * @return <code>true</code> if <code>matchMap</code> satisfies the
     *         constraints imposed by the rule (if any).
     */
    public boolean isValidPatternMap(HostGraph host, RuleToHostMap matchMap) {
        boolean result = true;
        if (this.checkDangling) {
            result = satisfiesDangling(host, matchMap);
        }
        return result;
    }

    /**
     * Tests if a given (proposed) match into a host graph leaves dangling
     * edges.
     */
    private boolean satisfiesDangling(HostGraph host, RuleToHostMap match) {
        boolean result = true;
        for (RuleNode eraserNode : getEraserNodes()) {
            HostNode erasedNode = match.getNode(eraserNode);
            HostEdgeSet danglingEdges = new HostEdgeSet(host.edgeSet(erasedNode));
            for (RuleEdge eraserEdge : lhs().edgeSet(eraserNode)) {
                boolean removed = danglingEdges.remove(match.getEdge(eraserEdge));
                assert removed : String.format("Match %s not present in incident edges %s",
                    match.getEdge(eraserEdge),
                    host.edgeSet(erasedNode));
            }
            if (!danglingEdges.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the left hand side of this Rule.
     * @ensure <tt>result == morphism().source()</tt>
     */
    public RuleGraph lhs() {
        return this.lhs;
    }

    /**
     * Returns the right hand side of this Rule.
     * @ensure <tt>result == morphism().cod()</tt>
     */
    public RuleGraph rhs() {
        return this.rhs;
    }

    /**
     * Adds entries to the colour map for this rule.
     * The changes are eventually pushed to the top rule in the hierarchy.
     */
    public void addColorMap(Map<RuleNode,Color> colorMap) {
        this.colorMap.putAll(colorMap);
    }

    /** Returns a mapping from RHS nodes to colours for those nodes. */
    public Map<RuleNode,Color> getColorMap() {
        return this.colorMap;
    }

    /** Returns the anchor of the rule. */
    public Anchor getAnchor() {
        if (this.anchor == null) {
            this.anchor = anchorFactory.newAnchor(this);
        }
        return this.anchor;
    }

    /** Returns the seed of the rule. */
    public Anchor getSeed() {
        if (this.seed == null) {
            this.seed = new Anchor(getRoot());
        }
        return this.seed;
    }

    @Override
    public String toString() {
        StringBuilder res =
            new StringBuilder(String.format("Rule %s; anchor %s%n", getFullName(), getAnchor()));
        res.append(getCondition().toString("    "));
        return res.toString();
    }

    /**
     * Compares two actions on the basis of their names.
     */
    @Override
    public int compareTo(Action other) {
        return getFullName().compareTo(other.getFullName());
    }

    // ------------------- commands --------------------------

    /**
     * In addition to calling the super method, adds implicit NACs as dictated
     * by {@link GrammarProperties#isCheckCreatorEdges()} and
     * {@link GrammarProperties#isRhsAsNac()}.
     */
    @Override
    public boolean setFixed() throws FormatException {
        boolean result = !isFixed();
        if (result && !this.fixing) {
            this.fixing = true;
            this.fixed = true;
            getCondition().setFixed();
            if (PRINT && isTop()) {
                System.out.println(toString());
            }
            // push the colour map to the top rule
            Rule parent = this.parent;
            while (parent != null && parent != this) {
                parent.getColorMap().putAll(getColorMap());
                parent = parent == parent.parent ? null : parent.parent;
            }
            this.fixing = false;
        }
        return result;
    }

    @Override
    public boolean isFixed() {
        return this.fixed;
    }

    /**
     * Tests if the condition is fixed or not. Throws an exception if the
     * fixedness does not coincide with the given value.
     *
     * @param value the expected fixedness state
     * @throws IllegalStateException if {@link #isFixed()} does not yield
     *         <code>value</code>
     */
    @Override
    public void testFixed(boolean value) throws IllegalStateException {
        if (isFixed() != value) {
            String message;
            if (value) {
                message = "Rule should be fixed";
            } else {
                message = "Rule should not be fixed";
            }
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks this rule for compatibility with a chosen algebra.
     * Called before starting an exploration.
     */
    public void checkCombatible(AlgebraFamily family) throws FormatException {
        if (!family.supportsSymbolic()) {
            getCondition().checkResolution();
        }
    }

    /** Returns an array of nodes isolated in the left hand side. */
    final public RuleNode[] getIsolatedNodes() {
        if (this.isolatedNodes == null) {
            this.isolatedNodes = computeIsolatedNodes();
        }
        return this.isolatedNodes;
    }

    /** Computes the array of nodes isolated in the left hand side. */
    private RuleNode[] computeIsolatedNodes() {
        testFixed(true);
        Set<RuleNode> result = new HashSet<RuleNode>();
        for (RuleNode node : lhs().nodeSet()) {
            if (lhs().edgeSet(node).isEmpty()) {
                result.add(node);
            }
        }
        result.removeAll(this.condition.getRoot().nodeSet());
        return result.toArray(new RuleNode[result.size()]);
    }

    /**
     * Indicates if this rule has mergers.
     */
    final public boolean hasMergers() {
        if (!this.hasMergersSet) {
            this.hasMergers = computeHasMergers();
            this.hasMergersSet = true;
        }
        return this.hasMergers;
    }

    /**
     * Computes if the rule has mergers or not.
     */
    private boolean computeHasMergers() {
        boolean result = !getLhsMergeMap().isEmpty() || !getRhsMergeMap().isEmpty();
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.hasMergers();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates if application of this rule actually changes the host graph. If
     * <code>false</code>, this means the rule is essentially a graph
     * condition.
     */
    public boolean isModifying() {
        if (!this.modifyingSet) {
            this.modifying = computeIsModifying();
            this.modifyingSet = true;
        }
        return this.modifying;
    }

    /**
     * Computes if the rule is modifying or not.
     */
    private boolean computeIsModifying() {
        boolean result =
            getEraserEdges().length > 0 || getEraserNodes().length > 0 || hasMergers()
                || hasNodeCreators() || hasEdgeCreators() || !getColorMap().isEmpty();
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.isModifying();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates if the rule creates any nodes.
     */
    public boolean hasNodeCreators() {
        if (!this.hasNodeCreatorsSet) {
            this.hasNodeCreators = computeHasNodeCreators();
            this.hasNodeCreatorsSet = true;
        }
        return this.hasNodeCreators;
    }

    private boolean computeHasNodeCreators() {
        boolean result = getCreatorNodes().length > 0;
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.hasNodeCreators();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates if the rule creates any edges.
     */
    public boolean hasEdgeCreators() {
        if (!this.hasEdgeCreatorsSet) {
            this.hasEdgeCreators = computeHasEdgeCreators();
            this.hasEdgeCreatorsSet = true;
        }
        return this.hasEdgeCreators;
    }

    private boolean computeHasEdgeCreators() {
        boolean result = getCreatorEdges().length > 0;
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.hasEdgeCreators();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates if the rule creates any nodes.
     */
    public boolean hasNodeErasers() {
        if (!this.hasNodeErasersSet) {
            this.hasNodeErasers = computeHasNodeErasers();
            this.hasNodeErasersSet = true;
        }
        return this.hasNodeErasers;
    }

    private boolean computeHasNodeErasers() {
        boolean result = getEraserNodes().length > 0;
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.hasNodeErasers();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates if the rule creates any edges.
     */
    public boolean hasEdgeErasers() {
        if (!this.hasEdgeErasersSet) {
            this.hasEdgeErasers = computeHasEdgeErasers();
            this.hasEdgeErasersSet = true;
        }
        return this.hasEdgeErasers;
    }

    private boolean computeHasEdgeErasers() {
        boolean result = getEraserEdges().length > 0;
        if (!result) {
            for (Rule subRule : getSubRules()) {
                result = subRule.hasEdgeErasers();
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    /** Returns the eraser (i.e., LHS-only) edges. */
    public final RuleEdge[] getEraserEdges() {
        if (this.eraserEdges == null) {
            this.eraserEdges = computeEraserEdges();
        }
        return this.eraserEdges;
    }

    /**
     * Computes the eraser (i.e., LHS-only) edges.
     * Note: this does not include the incident edges of the eraser nodes.
     */
    private RuleEdge[] computeEraserEdges() {
        testFixed(true);
        Set<RuleEdge> result = new HashSet<RuleEdge>(lhs().edgeSet());
        result.removeAll(rhs().edgeSet());
        // also remove the incident edges of the lhs-only nodes
        for (RuleNode eraserNode : getEraserNodes()) {
            result.removeAll(lhs().edgeSet(eraserNode));
        }
        return result.toArray(new RuleEdge[result.size()]);
    }

    /** Returns the eraser edges that are not themselves anchors. */
    public final RuleEdge[] getEraserNonAnchorEdges() {
        if (this.eraserNonAnchorEdges == null) {
            this.eraserNonAnchorEdges = computeEraserNonAnchorEdges();
        }
        return this.eraserNonAnchorEdges;
    }

    /**
     * Computes the array of creator edges that are not themselves anchors.
     */
    private RuleEdge[] computeEraserNonAnchorEdges() {
        Set<RuleEdge> eraserNonAnchorEdgeSet =
            new HashSet<RuleEdge>(Arrays.asList(getEraserEdges()));
        eraserNonAnchorEdgeSet.removeAll(getAnchor().edgeSet());
        return eraserNonAnchorEdgeSet.toArray(new RuleEdge[eraserNonAnchorEdgeSet.size()]);
    }

    /**
     * Returns the LHS nodes that are not mapped to the RHS.
     */
    public final DefaultRuleNode[] getEraserNodes() {
        if (this.eraserNodes == null) {
            this.eraserNodes = computeEraserNodes();
        }
        return this.eraserNodes;
    }

    /**
     * Computes the eraser (i.e., lhs-only) nodes.
     */
    private DefaultRuleNode[] computeEraserNodes() {
        //testFixed(true);
        Set<RuleNode> result = new HashSet<RuleNode>(lhs().nodeSet());
        result.removeAll(rhs().nodeSet());
        return result.toArray(new DefaultRuleNode[result.size()]);
    }

    /**
     * Returns an array of LHS nodes that are endpoints of eraser edges, creator
     * edges or mergers.
     */
    public final Set<RuleNode> getModifierEnds() {
        if (this.modifierEnds == null) {
            this.modifierEnds = computeModifierEnds();
        }
        return this.modifierEnds;
    }

    /**
     * Computes the array of LHS nodes that are endpoints of eraser edges,
     * creator edges or mergers.
     */
    private Set<RuleNode> computeModifierEnds() {
        Set<RuleNode> result = new HashSet<RuleNode>();
        // add the end nodes of eraser edges
        for (RuleEdge eraserEdge : getEraserEdges()) {
            result.add(eraserEdge.source());
            result.add(eraserEdge.target());
        }
        // add the end nodes of creator and merger edges
        for (RuleEdge rhsEdge : rhs().edgeSet()) {
            if (!lhs().containsEdge(rhsEdge)) {
                RuleNode source = rhsEdge.source();
                if (lhs().containsNode(source)) {
                    result.add(source);
                }
                RuleNode target = rhsEdge.target();
                if (lhs().containsNode(target)) {
                    result.add(target);
                }
            }
        }
        return result;
    }

    /**
     * Returns an array of variables that are used in erasers or creators.
     */
    final Set<LabelVar> getModifierVars() {
        if (this.modifierVars == null) {
            this.modifierVars = computeModifierVars();
        }
        return this.modifierVars;
    }

    /**
     * Computes the set of variables occurring in creators or erasers.
     */
    private Set<LabelVar> computeModifierVars() {
        Set<LabelVar> result = new HashSet<LabelVar>();
        // add the variables of creators
        for (RuleEdge edge : getCreatorGraph().edgeSet()) {
            result.addAll(edge.label().allVarSet());
        }
        for (RuleNode node : getCreatorGraph().nodeSet()) {
            for (TypeGuard guard : node.getTypeGuards()) {
                result.add(guard.getVar());
            }
        }
        // add the variables of erasers
        for (RuleNode eraser : getEraserNodes()) {
            for (TypeGuard guard : eraser.getTypeGuards()) {
                result.add(guard.getVar());
            }
        }
        for (RuleEdge eraser : getEraserEdges()) {
            result.addAll(eraser.label().allVarSet());
        }
        return result;
    }

    /** Returns the mapping from the parent RHS to this rule's RHS. */
    public final RuleGraph getCoRoot() {
        return this.coRoot;
    }

    /**
     * Returns the creator edges between reader nodes.
     */
    final public RuleEdge[] getSimpleCreatorEdges() {
        if (this.simpleCreatorEdges == null) {
            this.simpleCreatorEdges = computeSimpleCreatorEdges();
        }
        return this.simpleCreatorEdges;
    }

    /**
     * Computes the creator edges between reader nodes.
     */
    private RuleEdge[] computeSimpleCreatorEdges() {
        List<RuleEdge> result = new ArrayList<RuleEdge>();
        Set<RuleNode> nonCreatorNodes = getCreatorEnds();
        // iterate over all creator edges
        for (RuleEdge edge : getCreatorEdges()) {
            // determine if this edge is simple
            if (nonCreatorNodes.contains(edge.source()) && nonCreatorNodes.contains(edge.target())) {
                result.add(edge);
            }
        }
        return result.toArray(new RuleEdge[result.size()]);
    }

    /**
     * Returns the creator edges that have at least one creator end.
     */
    public final Set<RuleEdge> getComplexCreatorEdges() {
        if (this.complexCreatorEdges == null) {
            this.complexCreatorEdges = computeComplexCreatorEdges();
        }
        return this.complexCreatorEdges;
    }

    /**
     * Computes the creator edges that have at least one creator end.
     */
    private Set<RuleEdge> computeComplexCreatorEdges() {
        Set<RuleEdge> result = new HashSet<RuleEdge>(Arrays.asList(getCreatorEdges()));
        result.removeAll(Arrays.asList(getSimpleCreatorEdges()));
        return result;
    }

    /**
     * Returns the RHS edges that are not images of an LHS edge.
     */
    final public RuleEdge[] getCreatorEdges() {
        if (this.creatorEdges == null) {
            this.creatorEdges = computeCreatorEdges();
        }
        return this.creatorEdges;
    }

    /**
     * Computes the creator (i.e., RHS-only) edges.
     */
    private RuleEdge[] computeCreatorEdges() {
        Set<RuleEdge> result = new HashSet<RuleEdge>(rhs().edgeSet());
        result.removeAll(lhs().edgeSet());
        Rule parent = getParent();
        if (parent != null && parent != this) {
            result.removeAll(parent.rhs().edgeSet());
        }
        result.removeAll(getLhsMergers());
        result.removeAll(getRhsMergers());
        return result.toArray(new RuleEdge[result.size()]);
    }

    /**
     * Returns the RHS nodes that are not images of an LHS node.
     */
    final public RuleNode[] getCreatorNodes() {
        if (this.creatorNodes == null) {
            this.creatorNodes = computeCreatorNodes();
        }
        return this.creatorNodes;
    }

    /**
     * Computes the creator (i.e., RHS-only) nodes.
     */
    private RuleNode[] computeCreatorNodes() {
        Set<RuleNode> result = new HashSet<RuleNode>(rhs().nodeSet());
        result.removeAll(lhs().nodeSet());
        Rule parent = getParent();
        if (parent != null && parent != this) {
            result.removeAll(parent.rhs().nodeSet());
        }
        return result.toArray(new RuleNode[result.size()]);
    }

    /**
     * Returns the variables that occur in creator edges.
     * @see #getCreatorEdges()
     */
    public final LabelVar[] getCreatorVars() {
        if (this.creatorVars == null) {
            this.creatorVars = computeCreatorVars();
        }
        return this.creatorVars;
    }

    /**
     * Computes the variables occurring in RHS nodes and edges.
     */
    private LabelVar[] computeCreatorVars() {
        Set<LabelVar> creatorVarSet = new HashSet<LabelVar>();
        for (int i = 0; i < getCreatorEdges().length; i++) {
            addCreatorVar(creatorVarSet, getCreatorEdges()[i]);
        }
        for (int i = 0; i < getCreatorNodes().length; i++) {
            addCreatorVar(creatorVarSet, getCreatorNodes()[i]);
        }
        return creatorVarSet.toArray(new LabelVar[creatorVarSet.size()]);
    }

    private void addCreatorVar(Set<LabelVar> creatorVarSet, RuleElement creatorEdge) {
        for (TypeGuard guard : creatorEdge.getTypeGuards()) {
            creatorVarSet.add(guard.getVar());
        }
    }

    /**
     * Returns a sub-graph of the RHS consisting of the creator nodes and the
     * creator edges with their endpoints.
     */
    final RuleGraph getCreatorGraph() {
        if (this.creatorGraph == null) {
            this.creatorGraph = computeCreatorGraph();
        }
        return this.creatorGraph;
    }

    /**
     * Computes a creator graph, consisting of the creator nodes together with
     * the creator edges and their endpoints.
     */
    private RuleGraph computeCreatorGraph() {
        RuleGraph result = rhs().newGraph(getFullName() + "(creators)");
        result.addNodeSet(Arrays.asList(getCreatorNodes()));
        result.addEdgeSetContext(Arrays.asList(getCreatorEdges()));
        return result;
    }

    /**
     * Returns the RHS nodes that are not themselves creator nodes but are
     * the ends of creator edges.
     */
    public final Set<RuleNode> getCreatorEnds() {
        if (this.creatorEnds == null) {
            this.creatorEnds = new HashSet<RuleNode>(getCreatorGraph().nodeSet());
            this.creatorEnds.retainAll(lhs().nodeSet());
        }
        return this.creatorEnds;
    }

    /** Returns the set of merger edges in the RHS of which both ends are in the LHS. */
    public final Set<RuleEdge> getLhsMergers() {
        if (this.lhsMergers == null) {
            initMergers();
        }
        return this.lhsMergers;
    }

    /**
     * Returns the set of merger edges in the RHS of which at least one end
     * is a creator node.
     */
    public final Set<RuleEdge> getRhsMergers() {
        if (this.rhsMergers == null) {
            initMergers();
        }
        return this.rhsMergers;
    }

    /** Returns the set of merger edges in the RHS. */
    private final void initMergers() {
        this.lhsMergers = new HashSet<RuleEdge>();
        this.rhsMergers = new HashSet<RuleEdge>();
        this.lhsMergeMap = new HashMap<RuleNode,RuleNode>();
        this.rhsMergeMap = new HashMap<RuleNode,RuleNode>();
        for (RuleEdge rhsEdge : rhs().edgeSet()) {
            if (rhsEdge.label().isEmpty() && !this.lhs.containsEdge(rhsEdge)) {
                RuleNode source = rhsEdge.source();
                RuleNode target = rhsEdge.target();
                if (lhs().containsNode(source) && lhs().containsNode(target)) {
                    this.lhsMergeMap.put(source, target);
                    this.lhsMergers.add(rhsEdge);
                } else {
                    this.rhsMergeMap.put(source, target);
                    this.rhsMergers.add(rhsEdge);
                }
            }
        }
        closeMergeMap(this.lhsMergeMap);
        closeMergeMap(this.rhsMergeMap);
    }

    /**
     * Returns a map from each LHS node that is merged with
     * another to the LHS node it is merged with.
     */
    public final Map<RuleNode,RuleNode> getLhsMergeMap() {
        if (this.lhsMergeMap == null) {
            initMergers();
        }
        return this.lhsMergeMap;
    }

    /**
     * Returns a map from merged nodes to their merge targets,
     * at least one of which is a creator node.
     */
    public final Map<RuleNode,RuleNode> getRhsMergeMap() {
        if (this.rhsMergeMap == null) {
            initMergers();
        }
        return this.rhsMergeMap;
    }

    private void closeMergeMap(Map<RuleNode,RuleNode> mergeMap) {
        // transitively close the map
        // because we don't expect long chains of mergers,
        // we can sacrifice efficiency for brevity
        for (Map.Entry<RuleNode,RuleNode> mergeEntry : mergeMap.entrySet()) {
            RuleNode oldValue = mergeEntry.getValue();
            RuleNode newValue = oldValue;
            while (mergeMap.containsKey(newValue)) {
                newValue = mergeMap.get(newValue);
            }
            mergeEntry.setValue(newValue);
        }
    }

    /**
     * Indicates if this rule is part of a recipe.
     * @see #isPartial()
     */
    @Override
    public boolean isPartial() {
        return this.partial;
    }

    /**
     * Sets the rule to partial.
     * @see #isPartial()
     */
    public void setPartial() {
        this.partial = true;
    }

    /** Flag indicating that this rule is part of a recipe. */
    private boolean partial;
    /** Application condition of this rule. */
    private final Condition condition;
    /**
     * The parent rule of this rule; may be <code>null</code>, if this is a
     * top-level rule.
     */
    private Rule parent;
    /**
     * The collection of direct sub-rules of this rules. Lazily created by
     * {@link #getSubRules()}.
     */
    private Collection<Rule> subRules;
    /**
     * Indicates if this rule has node mergers.
     */
    private boolean hasMergers;
    /** Flag indicating if the {@link #hasMergers} has been computed. */
    private boolean hasMergersSet;
    /**
     * Indicates if this rule has creator nodes.
     */
    private boolean hasNodeCreators;
    /** Flag indicating if the {@link #hasNodeCreators} has been computed. */
    private boolean hasNodeCreatorsSet;
    /**
     * Indicates if this rule has creator edges.
     */
    private boolean hasEdgeCreators;
    /** Flag indicating if the {@link #hasEdgeCreators} has been computed. */
    private boolean hasEdgeCreatorsSet;
    /**
     * Indicates if this rule has eraser nodes.
     */
    private boolean hasNodeErasers;
    /** Flag indicating if the {@link #hasNodeErasers} has been computed. */
    private boolean hasNodeErasersSet;
    /**
     * Indicates if this rule has eraser edges.
     */
    private boolean hasEdgeErasers;
    /** Flag indicating if the {@link #hasEdgeErasers} has been computed. */
    private boolean hasEdgeErasersSet;
    /**
     * Indicates if this rule makes changes to a graph at all.
     */
    private boolean modifying;
    /**
     * Indicates if the {@link #modifying} variable has been computed
     */
    private boolean modifyingSet;
    /**
     * This production rule's left hand side.
     * @invariant lhs != null
     */
    private RuleGraph lhs;
    /**
     * This production rule's right hand side.
     * @invariant rhs != null
     */
    private RuleGraph rhs;
    /** Mapping from the context of this rule to the RHS. */
    private final RuleGraph coRoot;
    /** Flag indicating whether rule applications should be checked for dangling edges. */
    private boolean checkDangling;
    /** Flag indicating whether the rule has been fixed. */
    private boolean fixed;
    /** Flag indicating whether the rule is currently in the process of fixing. */
    private boolean fixing;
    /**
     * A sub-graph of the production rule's right hand side, consisting only of
     * the fresh nodes and edges.
     */
    private RuleGraph creatorGraph;
    /**
     * A map from the nodes of <tt>rhsOnlyGraph</tt> to <tt>lhs</tt>, which is
     * the restriction of the inverse of <tt>ruleMorph</tt> to
     * <tt>rhsOnlyGraph</tt>.
     */
    private Set<RuleNode> creatorEnds;
    /**
     * The lhs nodes that are not ruleMorph keys
     * @invariant lhsOnlyNodes \subseteq lhs.nodeSet()
     */
    private DefaultRuleNode[] eraserNodes;
    /**
     * The lhs edges that are not ruleMorph keys
     * @invariant lhsOnlyEdges \subseteq lhs.edgeSet()
     */
    private RuleEdge[] eraserEdges;
    /** The rule anchor. */
    private Anchor anchor;
    /** The rule seed. */
    private Anchor seed;
    /**
     * The lhs edges that are not ruleMorph keys and are not anchors
     */
    private RuleEdge[] eraserNonAnchorEdges;
    /**
     * The lhs nodes that are end points of eraser or creator edges or mergers,
     * either in this rule or one of its sub-rules.
     */
    private Set<RuleNode> modifierEnds;
    /**
     * The lhs variables that occur in eraser or creator edges, either in this rule or
     * one of its sub-rules.
     */
    private Set<LabelVar> modifierVars;
    /**
     * The LHS nodes that do not have any incident edges in the LHS.
     */
    private RuleNode[] isolatedNodes;
    /**
     * The rhs nodes that are not ruleMorph images
     * @invariant creatorNodes \subseteq rhs.nodeSet()
     */
    private RuleNode[] creatorNodes;

    /**
     * The rhs edges that are not ruleMorph images
     */
    private RuleEdge[] creatorEdges;
    /**
     * The rhs edges that are not ruleMorph images but with all ends morphism
     * images
     */
    private RuleEdge[] simpleCreatorEdges;
    /**
     * The rhs edges with at least one end not a morphism image
     */
    private Set<RuleEdge> complexCreatorEdges;
    /**
     * Variables occurring in the rhsOnlyEdges
     */
    private LabelVar[] creatorVars;
    /** Set of merger edges whose source and target nodes are both in the LHS. */
    private Set<RuleEdge> lhsMergers;
    /** Set of merger edges whose source and target nodes are not both in the LHS. */
    private Set<RuleEdge> rhsMergers;
    /**
     * A mapping from merge sources to merge targets within the LHS.
     */
    private Map<RuleNode,RuleNode> lhsMergeMap;
    /**
     * A mapping from merge sources to merge targets outside the LHS.
     */
    private Map<RuleNode,RuleNode> rhsMergeMap;

    /** Mapping from rule nodes to explicitly declared colours. */
    private final Map<RuleNode,Color> colorMap = new HashMap<RuleNode,Color>();
    /** The rule priority. */
    private int priority;
    /** The optional transition label. */
    private String transitionLabel;
    /** The optional format string. */
    private String formatString;

    /** The signature of the rule. */
    private List<CtrlPar.Var> sig;
    /**
     * List of indices for the parameters, pointing either to the
     * anchor position or to the position in the created nodes list.
     * The latter are offset by the length of the anchor.
     */
    private Binding[] parBinding;
    /**
     * Set of anonymous (unnumbered) parameters.
     */
    private Set<RuleNode> hiddenPars;

    /**
     * Mapping from sets of initialised parameters to match strategies.
     */
    private final Map<BitSet,Matcher> matcherMap = new HashMap<BitSet,Matcher>();

    /** The matcher for events of this rule. */
    private Matcher eventMatcher;

    /** Returns the current anchor factory for all rules. */
    public static AnchorFactory getAnchorFactory() {
        return anchorFactory;
    }

    /**
     * Sets the anchor factory for all rules. Only affects rules created from
     * this moment on.
     */
    public static void setAnchorFactory(AnchorFactory anchorFactory) {
        Rule.anchorFactory = anchorFactory;
    }

    /**
     * Returns the total time doing matching-related computations. This includes
     * time spent in certificate calculation.
     */
    static public long getMatchingTime() {
        return PlanSearchStrategy.searchFindReporter.getTotalTime();
    }

    /**
     * The factory used for creating rule anchors.
     */
    private static AnchorFactory anchorFactory = DefaultAnchorFactory.getInstance();
    /** Debug flag for the constructor. */
    private static final boolean PRINT = false;
}
