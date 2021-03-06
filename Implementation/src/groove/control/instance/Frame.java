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
 * $Id: Frame.java 5546 2014-09-02 07:32:40Z rensink $
 */
package groove.control.instance;

import groove.control.Call;
import groove.control.CallStack;
import groove.control.CtrlVar;
import groove.control.Position;
import groove.control.template.Location;
import groove.control.template.Switch;
import groove.control.template.SwitchAttempt;
import groove.control.template.SwitchStack;
import groove.grammar.CheckPolicy;
import groove.grammar.Recipe;
import groove.util.DefaultFixable;
import groove.util.Fixable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Run-time composed control location.
 * @author Arend Rensink
 * @version $Revision $
 */
public class Frame implements Position<Frame,Step>, Fixable {
    /** Constructs an error or absence frame.
     * @param ctrl the control automaton being built
     * @param stack underlying call stack
     * @param pred predecessor in a verdict transition; if {@code null}, this is
     * a prime frame
     * @param policy if {@link CheckPolicy#ERROR}, this is an error frame, otherwise it is an absence frame
     */
    Frame(Automaton ctrl, SwitchStack stack, Frame pred, int transience, CheckPolicy policy) {
        this.aut = ctrl;
        this.nr = ctrl.getFrames().size();
        List<Assignment> pops = new ArrayList<Assignment>();
        this.pred = pred;
        if (pred == null) {
            this.prime = this;
        } else {
            this.prime = pred.getPrime();
            pops.addAll(pred.getPops());
        }
        this.pops = pops;
        // avoid sharing
        this.switchStack = new SwitchStack(stack);
        this.location = Location.getSpecial(policy, transience);
    }

    /** Constructs a new live frame.
     * @param ctrl the control automaton being built
     * @param loc top template location of the frame
     * @param stack underlying call stack
     * @param pred predecessor in a verdict transition; if {@code null}, this is
     * a prime frame
     */
    Frame(Automaton ctrl, Location loc, SwitchStack stack, Frame pred) {
        this.aut = ctrl;
        this.nr = ctrl.getFrames().size();
        List<Assignment> pops = new ArrayList<Assignment>();
        // avoid sharing
        this.pred = pred;
        if (pred == null) {
            this.prime = this;
        } else {
            this.prime = pred.getPrime();
            pops.addAll(pred.getPops());
        }
        stack = new SwitchStack(stack);
        // pop the call stack until we have a non-final location or empty stack
        while (loc.isFinal() && !stack.isEmpty()) {
            Switch done = stack.pop();
            // add pop actions if we are not a prime frame
            if (pred != null) {
                pops.add(Assignment.exit(loc, done));
            }
            loc = done.onFinish();
        }
        this.pops = pops;
        this.switchStack = stack;
        this.location = loc;
    }

    /** Returns the containing control automaton. */
    public Automaton getAut() {
        return this.aut;
    }

    private final Automaton aut;

    /**
     * Returns the number of this frame.
     * After a frame has been added to the automaton,
     * the frame number uniquely identifies the frame.
     */
    public int getNumber() {
        return this.nr;
    }

    private final int nr;

    @Override
    public boolean isStart() {
        return getAut().getStart() == this;
    }

    /** Returns the call stack giving rise to this frame. */
    public SwitchStack getSwitchStack() {
        return this.switchStack;
    }

    private final SwitchStack switchStack;

    /**
     * Returns the top control location instantiated by this frame.
     */
    public Location getLocation() {
        return this.location;
    }

    private final Location location;

    /** Indicates whether this is an absence frame. */
    public boolean isRemoved() {
        return getLocation().isRemoved();
    }

    /** Indicates whether this is an error frame. */
    public boolean isError() {
        return getLocation().isError();
    }

    /**
     * Returns the predecessor frame in the chain between the
     * prime frame and this, or {@code null} if this is a prime frame.
     */
    private Frame getPred() {
        return this.pred;
    }

    /** The predecessor frame, or {@code null} if this is a prime frame. */
    private final Frame pred;

    /**
     * Returns the prime frame of this frame.
     * The prime frame is the initial frame from which this one was
     * reached after a sequence of verdicts.
     */
    public Frame getPrime() {
        return this.prime;
    }

    /** Indicates if this frame is its own prime. */
    public boolean isPrime() {
        return getPrime() == this;
    }

    private final Frame prime;

    /**
     * Returns the set of called actions that have been tried
     * between the prime frame and this one (inclusive).
     */
    public Set<CallStack> getPastAttempts() {
        Set<CallStack> result = this.pastAttempts;
        if (result == null) {
            result = new HashSet<CallStack>();
            if (!isPrime()) {
                result.addAll(getPred().getPastAttempts());
            }
            if (isTrial()) {
                for (Step step : getAttempt()) {
                    result.add(step.getCallStack());
                }
            }
            this.pastAttempts = result;
        }
        return this.pastAttempts;
    }

    private Set<CallStack> pastAttempts;

    /** Returns the set of rule calls that have been tried since the prime frame. */
    public Set<Call> getPastCalls() {
        if (this.pastCalls == null) {
            Set<Call> result = this.pastCalls = new HashSet<Call>();
            for (CallStack attempt : getPastAttempts()) {
                result.add(attempt.peek());
            }
        }
        return this.pastCalls;
    }

    private Set<Call> pastCalls;

    /**
     * Returns the list of frame pop actions corresponding to
     * procedure exits due to verdict transitions between the prime frame and this frame.
     */
    public List<Assignment> getPops() {
        return this.pops;
    }

    private final List<Assignment> pops;

    @Override
    public Type getType() {
        if (this.type == null) {
            this.type = getLocation() == null ? Type.DEAD : getLocation().getType();
        }
        return this.type;
    }

    /** The type of this frame. */
    private Type type;

    @Override
    public boolean isDead() {
        return getType() == Type.DEAD;
    }

    @Override
    public boolean isFinal() {
        return getType() == Type.FINAL;
    }

    @Override
    public boolean isTrial() {
        return getType() == Type.TRIAL;
    }

    @Override
    public StepAttempt getAttempt() {
        assert isFixed();
        if (this.attempt == null) {
            this.attempt = computeAttempt();
        }
        return this.attempt;
    }

    private StepAttempt attempt;

    /** Computes the attempt of this frame. */
    private StepAttempt computeAttempt() {
        SwitchAttempt locAttempt = getLocation().getAttempt();
        // divide the switches of the control location
        // into constraints and "proper" calls
        List<SwitchStack> constraintCalls = new ArrayList<SwitchStack>();
        List<SwitchStack> properCalls = new ArrayList<SwitchStack>();
        for (SwitchStack sw : locAttempt) {
            if (sw.peek().getCall().getRule().getRole().isConstraint()) {
                constraintCalls.add(sw);
            } else {
                properCalls.add(sw);
            }
        }
        StepAttempt result;
        if (properCalls.isEmpty()) {
            // we only have a constraint attempt
            Frame onVerdict = newFrame(locAttempt.onSuccess());
            assert onVerdict.getLocation() == locAttempt.onFailure();
            result = new StepAttempt(onVerdict);
            for (SwitchStack sw : constraintCalls) {
                result.add(createStep(sw));
            }
        } else if (constraintCalls.isEmpty()) {
            // we only have a proper attempt
            Frame onSuccess = newFrame(locAttempt.onSuccess());
            Frame onFailure = newFrame(locAttempt.onFailure());
            result = new StepAttempt(onSuccess, onFailure);
            for (SwitchStack sw : properCalls) {
                result.add(createStep(sw));
            }
        } else {
            // the initial attempt tests for constraints only;
            // the verdict leads to an intermediate frame
            Frame inter = newFrame(getLocation());
            result = new StepAttempt(inter);
            for (SwitchStack sw : constraintCalls) {
                result.add(createStep(sw));
            }
            // this is followed by an attempt for the proper steps
            // which is set as the attempt of the intermediate frame
            Frame onVerdict = inter.newFrame(locAttempt.onSuccess());
            // we had a location with property switches; this guarantees
            // that the success and failure locations coincide
            assert onVerdict.getLocation() == locAttempt.onFailure();
            StepAttempt interAttempt = new StepAttempt(onVerdict, onVerdict);
            for (SwitchStack sw : properCalls) {
                result.add(inter.createStep(sw));
            }
            inter.attempt = interAttempt;
        }
        return result;
    }

    /** Constructs a step from this frame, based on a given control location switch. */
    private Step createStep(SwitchStack sw) {
        SwitchStack targetStack = new SwitchStack();
        targetStack.addAll(getSwitchStack());
        targetStack.addAll(sw);
        Switch call = targetStack.pop();
        Frame target;
        // the following exception is wrong, if an explicit condition call occurs
        // whereas for other property calls, why bother?
        //        if (call.getCall().getRule().getRole().isProperty()) {
        //            // all properties should leave the control frame unchanged
        //            assert sw.size() == 1;
        //            target = this;
        //        } else {
        target = new Frame(getAut(), call.onFinish(), targetStack, null).normalise();
        //        }
        return new Step(this, sw, target);
    }

    /** Returns the successor frame, depending on a given policy value. */
    public Frame onPolicy(CheckPolicy policy) {
        Frame result = null;
        switch (policy) {
        case ERROR:
            result = onError();
            break;
        case REMOVE:
            result = onRemove();
            break;
        case SILENT:
            result = this;
            break;
        default:
            assert false;
        }
        return result;
    }

    /** Returns the error frame from this frame. */
    public Frame onError() {
        if (this.onError == null) {
            if (isError() || isRemoved()) {
                this.onError = this;
            } else {
                this.onError =
                    newFrame(Location.getSpecial(CheckPolicy.ERROR, getLocation().getTransience()));
            }
        }
        return this.onError;
    }

    private Frame onError;

    /** Returns the absence frame from this frame. */
    public Frame onRemove() {
        if (this.onRemove == null) {
            if (isRemoved()) {
                this.onRemove = this;
            } else {
                this.onRemove =
                    newFrame(Location.getSpecial(CheckPolicy.REMOVE, getLocation().getTransience()));
            }
        }
        return this.onRemove;
    }

    private Frame onRemove;

    /**
     * Indicates if this frame is inside a recipe.
     * This is the case if and only if the recipe has started
     * and not yet terminated.
     * A frame can only be inside a recipe if it is transient.
     * @see #getRecipe()
     * @see #isTransient()
     */
    public boolean isInternal() {
        return getSwitchStack().inRecipe();
    }

    /**
     * Returns the outer recipe to which this frame belongs, if any.
     * @return the recipe to this this frame belongs, or {@code null}
     * if it is not inside a recipe
     * @see #isInternal()
     */
    public Recipe getRecipe() {
        return getSwitchStack().getRecipe();
    }

    /**
     * Indicates if this frame is inside an atomic block.
     * Convenience method for <code>getTransience() > 0</code>
     */
    public boolean isTransient() {
        return getTransience() > 0;
    }

    @Override
    public int getTransience() {
        return getSwitchStack().getTransience() + getLocation().getTransience();
    }

    /** Indicates if this frame is nested inside a procedure. */
    public boolean isNested() {
        return !getSwitchStack().isEmpty();
    }

    @Override
    public boolean hasVars() {
        return getLocation().hasVars();
    }

    @Override
    public List<CtrlVar> getVars() {
        return getLocation().getVars();
    }

    /**
     * Constructs a frame for a given control location,
     * with the same prime frame and call stack as this frame.
     */
    private Frame newFrame(Location loc) {
        Frame result = new Frame(getAut(), loc, getSwitchStack(), this);
        return result.normalise();
    }

    /** Fixes this frame and returns its canonical representative. */
    public Frame normalise() {
        setFixed();
        return getAut().addFrame(this);
    }

    @Override
    public int hashCode() {
        assert isFixed();
        final int prime = 31;
        // use identity of prime frame as it has already been normalised
        int result = (isPrime() ? 1237 : System.identityHashCode(this.prime));
        result = prime * result + System.identityHashCode(this.pred);
        result = prime * result + this.location.hashCode();
        result = prime * result + this.pops.hashCode();
        result = prime * result + this.switchStack.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        assert isFixed();
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Frame)) {
            return false;
        }
        Frame other = (Frame) obj;
        if (isPrime() ? !other.isPrime() : this.prime != other.prime) {
            return false;
        }
        if (this.pred != other.pred) {
            return false;
        }
        if (!this.pops.equals(other.pops)) {
            return false;
        }
        if (!this.location.equals(other.location)) {
            return false;
        }
        if (!this.switchStack.equals(other.switchStack)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String result = getIdString();
        if (RICH_LABELS) {
            if (getTransience() > 0) {
                result += ", d" + getTransience();
            }
            if (isFinal()) {
                result += ", final";
            } else if (isDead()) {
                result += ", dead";
            }
            if (isPrime()) {
                result += "\nPrime";
            } else {
                result += "\nPrime: " + getPrime().getIdString();
                if (VERY_RICH_LABELS) {
                    result += "\nTried:";
                    for (CallStack tried : getPastAttempts()) {
                        result += " " + tried.toString();
                    }
                }
            }
        }
        return result;
    }

    /** Returns the concatenation of the call stack locations. */
    public String getIdString() {
        StringBuilder result = new StringBuilder();
        //        String callerName = null;
        //        for (Switch swit : getSwitchStack()) {
        //            if (callerName == null) {
        //                result.append("c");
        //            } else {
        //                result.append('/');
        //                result.append(callerName);
        //                result.append('.');
        //            }
        //            result.append(swit.onFinish().getNumber());
        //            callerName = swit.getCall().getUnit().getLastName();
        //        }
        //        if (callerName == null) {
        //            result.append("c");
        //        } else {
        //            result.append('/');
        //            result.append(callerName);
        //            result.append('.');
        //        }
        result.append("c");
        result.append(getNumber());
        return result.toString();
    }

    @Override
    public boolean setFixed() {
        return this.fixable.setFixed();
    }

    @Override
    public boolean isFixed() {
        return this.fixable.isFixed();
    }

    @Override
    public void testFixed(boolean fixed) {
        this.fixable.testFixed(fixed);
    }

    private final DefaultFixable fixable = new DefaultFixable();

    private final static boolean RICH_LABELS = false;
    private final static boolean VERY_RICH_LABELS = false;
}
