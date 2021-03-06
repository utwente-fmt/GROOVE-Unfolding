package groove.grammar;

import groove.algebra.AlgebraFamily;
import groove.explore.ExploreType;
import groove.grammar.CheckPolicy.PolicyMap;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.type.TypeLabel;
import groove.util.Groove;
import groove.util.Properties;
import groove.util.ThreeValued;
import groove.util.Version;
import groove.util.parse.FormatError;
import groove.util.parse.FormatErrorSet;
import groove.util.parse.FormatException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Properties class for graph production systems.
 * @author Arend Rensink
 * @version $Revision $
 */
public class GrammarProperties extends Properties {
    /**
     * Default constructor.
     */
    public GrammarProperties() {
        this(false);
    }

    /**
     * Constructor that sets the grammar properties.
     */
    public GrammarProperties(boolean useCurrentGrooveVersion) {
        super(GrammarKey.class);
        if (useCurrentGrooveVersion) {
            this.setCurrentVersionProperties();
            setShowLoopsAsLabels(false);
        } else {
            this.setGrooveVersion(Version.getInitialGrooveVersion());
            this.setGrammarVersion(Version.getInitialGrammarVersion());
        }
    }

    /** Constructs a non-fixed clone of a given properties object. */
    private GrammarProperties(GrammarProperties original) {
        super(GrammarKey.class);
        putAll(original);
    }

    /**
     * Set version properties to the latest version.
     */
    public void setCurrentVersionProperties() {
        this.setGrooveVersion(Version.getCurrentGrooveVersion());
        this.setGrammarVersion(Version.getCurrentGrammarVersion());
    }

    /**
     * @return <code>true</code> if the version numbers are set to the current
     * version of the tool. <code>false</code> otherwise.
     */
    public boolean isCurrentVersionProperties() {
        return this.getGrooveVersion().equals(Version.getCurrentGrooveVersion())
            && this.getGrammarVersion().equals(Version.getCurrentGrammarVersion());
    }

    /**
     * Indicates if the LTS labels should be surrounded by angular brackets.
     * Default value: <code>true</code>.
     */
    public boolean isShowLoopsAsLabels() {
        return (Boolean) parseProperty(GrammarKey.LOOPS_AS_LABELS);
    }

    /**
     * Indicates if the LTS labels should be surrounded by angular brackets.
     * Default value: <code>true</code>.
     */
    public void setShowLoopsAsLabels(boolean show) {
        storeProperty(GrammarKey.LOOPS_AS_LABELS, show);
    }

    /**
     * Indicates if the LTS labels should contain transition parameters. Default
     * value: {@link ThreeValued#FALSE}.
     */
    public ThreeValued isUseParameters() {
        ThreeValued result = (ThreeValued) parseProperty(GrammarKey.TRANSITION_PARAMETERS);
        if (result == null) {
            result = ThreeValued.FALSE;
        }
        return result;
    }

    /** Sets the {@link GrammarKey#TRANSITION_PARAMETERS} property to the given value * */
    public void setUseParameters(ThreeValued useParameters) {
        storeProperty(GrammarKey.TRANSITION_PARAMETERS, useParameters);
    }

    /** Sets the {@link GrammarKey#GROOVE_VERSION} property to the given value */
    public void setGrooveVersion(String version) {
        storeProperty(GrammarKey.GROOVE_VERSION, version);
    }

    /**
     * @return the version of Groove that created the grammar.
     */
    public String getGrooveVersion() {
        return (String) parseProperty(GrammarKey.GROOVE_VERSION);
    }

    /** Sets the {@link GrammarKey#GRAMMAR_VERSION} property to the given value */
    public void setGrammarVersion(String version) {
        storeProperty(GrammarKey.GRAMMAR_VERSION, version);
    }

    /**
     * @return the version of the grammar.
     */
    public String getGrammarVersion() {
        return (String) parseProperty(GrammarKey.GRAMMAR_VERSION);
    }

    /**
     * Returns a list of control labels, according to the
     * {@link GrammarKey#CONTROL_LABELS} property of the rule system.
     * @see GrammarKey#CONTROL_LABELS
     */
    @SuppressWarnings("unchecked")
    public List<String> getControlLabels() {
        return (List<String>) parseProperty(GrammarKey.CONTROL_LABELS);
    }

    /**
     * Sets the control labels property.
     * @see GrammarKey#CONTROL_LABELS
     */
    public void setControlLabels(List<String> controlLabels) {
        storeProperty(GrammarKey.CONTROL_LABELS, controlLabels);
    }

    /**
     * Sets the rule application policy map.
     * @param policy the policy map to be used for rule application.
     * @see GrammarKey#ACTION_POLICY
     */
    public void setRulePolicy(PolicyMap policy) {
        storeProperty(GrammarKey.ACTION_POLICY, policy);
    }

    /**
     * Returns the rule application policy map of the rule system.
     * @see GrammarKey#ACTION_POLICY
     */
    public PolicyMap getRulePolicy() {
        return (PolicyMap) parseProperty(GrammarKey.ACTION_POLICY);
    }

    /**
     * Sets the deadlock check policy.
     * @param policy the policy to be used for deadlock checking.
     * @see GrammarKey#DEAD_POLICY
     */
    public void setDeadPolicy(CheckPolicy policy) {
        storeProperty(GrammarKey.DEAD_POLICY, policy);
    }

    /**
     * Returns the deadlock check policy of the rule system.
     * @see GrammarKey#DEAD_POLICY
     */
    public CheckPolicy getDeadPolicy() {
        return (CheckPolicy) parseProperty(GrammarKey.DEAD_POLICY);
    }

    /**
     * Sets the typecheck policy.
     * @param policy the policy to be used for type checking.
     * @see GrammarKey#TYPE_POLICY
     */
    public void setTypePolicy(CheckPolicy policy) {
        storeProperty(GrammarKey.TYPE_POLICY, policy);
    }

    /**
     * Returns the type check policy of the rule system.
     * @see GrammarKey#TYPE_POLICY
     */
    public CheckPolicy getTypePolicy() {
        return (CheckPolicy) parseProperty(GrammarKey.TYPE_POLICY);
    }

    /**
     * Returns a list of common labels, according to the
     * {@link GrammarKey#COMMON_LABELS} property of the rule system.
     * @see GrammarKey#COMMON_LABELS
     */
    @SuppressWarnings("unchecked")
    public List<String> getCommonLabels() {
        return (List<String>) parseProperty(GrammarKey.COMMON_LABELS);
    }

    /**
     * Sets the common labels property.
     * @see GrammarKey#COMMON_LABELS
     */
    public void setCommonLabels(List<String> commonLabels) {
        storeProperty(GrammarKey.COMMON_LABELS,
            Groove.toString(commonLabels.toArray(), "", "", " "));
    }

    /**
     * Sets the injectivity property to a certain value.
     * @param injective if <code>true</code>, non-injective matches are
     *        disallowed
     */
    public void setInjective(boolean injective) {
        storeProperty(GrammarKey.INJECTIVE, injective);
    }

    /**
     * Returns the value of the injectivity property.
     * @return if <code>true</code>, non-injective matches are disallowed
     */
    public boolean isInjective() {
        return (Boolean) parseProperty(GrammarKey.INJECTIVE);
    }

    /**
     * Sets the dangling edge check to a certain value.
     * @param dangling if <code>true</code>, matches with dangling edges are
     *        disallowed
     */
    public void setCheckDangling(boolean dangling) {
        storeProperty(GrammarKey.DANGLING, dangling);
    }

    /**
     * Returns the value of the dangling edge property.
     * @return if <code>true</code>, matches with dangling edges are disallowed.
     */
    public boolean isCheckDangling() {
        return (Boolean) parseProperty(GrammarKey.DANGLING);
    }

    /**
     * Sets the exploration strategy to a certain value.
     * @param exploreType the new exploration strategy
     */
    public void setExploreType(ExploreType exploreType) {
        storeProperty(GrammarKey.EXPLORATION, exploreType);
    }

    /**
     * Returns the exploration strategy, or <code>null</code> if there
     * is no strategy set.
     */
    public ExploreType getExploreType() {
        return (ExploreType) parseProperty(GrammarKey.EXPLORATION);
    }

    /**
     * Sets the active names property of a given resource kind.
     * @param kind the resource kind to set the names for
     * @param names the (non-{@code null}, but possible empty) list of names of the active resources
     */
    public void setActiveNames(ResourceKind kind, Collection<String> names) {
        assert kind != ResourceKind.RULE;
        storeProperty(resourceKeyMap.get(kind), names);
    }

    /**
     * Returns a list of active resource names of a given kind.
     * @param kind the queried resource kind
     * @return a (non-{@code null}, but possibly empty) set of active names
     */
    public Set<String> getActiveNames(ResourceKind kind) {
        assert kind != ResourceKind.RULE;
        if (kind == ResourceKind.CONFIG || kind == ResourceKind.GROOVY) {
            return Collections.emptySet();
        }
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) parseProperty(resourceKeyMap.get(kind));
        return new TreeSet<String>(names);
    }

    /**
     * Sets the algebra family to a given value.
     */
    public void setAlgebraFamily(AlgebraFamily family) {
        storeProperty(GrammarKey.ALGEBRA, family);
    }

    /**
     * Returns the selected algebra family.
     * @return the selected algebra family, or {@link AlgebraFamily#DEFAULT}
     * if none is selected.
     */
    public AlgebraFamily getAlgebraFamily() {
        AlgebraFamily result = (AlgebraFamily) parseProperty(GrammarKey.ALGEBRA);
        if (result == null) {
            result = AlgebraFamily.DEFAULT;
        }
        return result;
    }

    /**
     * Sets the creator edge check to a certain value.
     * @param check if <code>true</code>, creator edges are treated as negative
     *        application conditions
     */
    public void setCheckCreatorEdges(boolean check) {
        storeProperty(GrammarKey.CREATOR_EDGE, check);
    }

    /**
     * Returns the value of the creator edge check property.
     * @return if <code>true</code>, creator edges are treated as negative
     *         application conditions
     */
    public boolean isCheckCreatorEdges() {
        return (Boolean) parseProperty(GrammarKey.CREATOR_EDGE);
    }

    /**
     * Sets the graph isomorphism check to a certain value.
     * @param check if <code>true</code>, state graphs are compared up to
     *        isomorphism
     */
    public void setCheckIsomorphism(boolean check) {
        storeProperty(GrammarKey.ISOMORPHISM, check);
    }

    /**
     * Returns the value of the graph isomorphism check property.
     * @return if <code>true</code>, state graphs are compared up to isomorphism
     */
    public boolean isCheckIsomorphism() {
        return (Boolean) parseProperty(GrammarKey.ISOMORPHISM);
    }

    /**
     * Returns the value of the RHS-as-NAC property.
     * @return if <code>true</code>, the RHS is treated as a negative
     *         application condition, preventing the same rule instance from
     *         being applied twice in a row
     */
    public boolean isRhsAsNac() {
        return (Boolean) parseProperty(GrammarKey.RHS_AS_NAC);
    }

    /**
     * Sets the RHS-as-NAC property to a certain value.
     * @param value if <code>true</code>, the RHS is treated as a negative
     *        application condition, preventing the same rule instance from
     *        being applied twice in a row
     */
    public void setRhsAsNac(boolean value) {
        storeProperty(GrammarKey.RHS_AS_NAC, value);
    }

    /**
     * Returns a clone of this properties object where all occurrences of a
     * given label are replaced by a new label.
     * @param oldLabel the label to be replaced
     * @param newLabel the new value for {@code oldLabel}
     * @return a clone of these properties, or the properties themselves if
     *         {@code oldLabel} did not occur
     */
    public GrammarProperties relabel(TypeLabel oldLabel, TypeLabel newLabel) {
        GrammarProperties result = clone();
        boolean hasChanged = false;
        String oldText = oldLabel.text();
        // change the control labels
        List<String> controlLabels = getControlLabels();
        List<String> newControlLabels = new ArrayList<String>(controlLabels);
        if (controlLabels != null && controlLabels.contains(oldText)) {
            int index = controlLabels.indexOf(oldText);
            newControlLabels.set(index, newLabel.text());
            result.setControlLabels(newControlLabels);
            hasChanged = true;
        }
        // change the common labels
        List<String> commonLabels = getControlLabels();
        List<String> newCommonLabels = new ArrayList<String>(commonLabels);
        if (commonLabels != null && commonLabels.contains(oldText)) {
            int index = commonLabels.indexOf(oldText);
            newCommonLabels.set(index, newLabel.text());
            result.setCommonLabels(newCommonLabels);
            hasChanged = true;
        }
        return hasChanged ? result : this;
    }

    /**
     * Checks if the stored properties are valid in a given grammar.
     */
    public void check(GrammarModel grammar) throws FormatException {
        FormatErrorSet errors = new FormatErrorSet();
        for (GrammarKey key : GrammarKey.values()) {
            for (FormatError error : key.check(grammar, parseProperty(key))) {
                errors.add("Error in property key '%s': %s", key.getKeyPhrase(), error, key);
            }
        }
        errors.throwException();
    }

    /** Tests if the grammar properties specify any remove policies. */
    public boolean hasRemovePolicies() {
        if (getTypePolicy() == CheckPolicy.REMOVE) {
            return true;
        }
        if (getRulePolicy().containsValue(CheckPolicy.REMOVE)) {
            return true;
        }
        return false;
    }

    /** Returns a non-fixed clone of the properties. */
    @Override
    public GrammarProperties clone() {
        return new GrammarProperties(this);
    }

    /**
     * Returns a default, fixed properties object, with a given value for
     * attribute support.
     */
    static public GrammarProperties getInstance() {
        return instance;
    }

    /**
     * Tests whether {@link #isCheckDangling()} holds for a given properties
     * object. If the properties object is <code>null</code>, the method returns
     * <code>false</code>.
     * @param properties the properties to be tested; may be <code>null</code>
     * @return <true> if <code>properties</code> is not <code>null</code> and
     *         satisfies {@link #isCheckDangling()}
     */
    static public boolean isCheckDangling(GrammarProperties properties) {
        return properties != null && properties.isCheckDangling();
    }

    /** Map storing default property instances. */
    static private GrammarProperties instance = new GrammarProperties();

    /**
     * The default rule properties: not attributed and no control or common
     * labels.
     */
    static public final GrammarProperties DEFAULT_PROPERTIES = getInstance();

    /** Mapping from resource kinds to corresponding property keys. */
    static private final Map<ResourceKind,GrammarKey> resourceKeyMap =
        new EnumMap<ResourceKind,GrammarKey>(ResourceKind.class);
    static {
        resourceKeyMap.put(ResourceKind.TYPE, GrammarKey.TYPE_NAMES);
        resourceKeyMap.put(ResourceKind.CONTROL, GrammarKey.CONTROL_NAMES);
        resourceKeyMap.put(ResourceKind.PROLOG, GrammarKey.PROLOG_NAMES);
        resourceKeyMap.put(ResourceKind.HOST, GrammarKey.START_GRAPH_NAMES);

    }
}
