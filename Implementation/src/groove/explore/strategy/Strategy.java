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
 * $Id: Strategy.java 5759 2015-12-06 23:32:04Z rensink $
 */
package groove.explore.strategy;

import groove.explore.result.Acceptor;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.lts.Status.Flag;

/**
 * A strategy defines an order in which the states of a graph transition system
 * are to be explored. It can also determine which states are to be explored
 * because of the nature of the strategy (see for instance
 * {@link LinearStrategy}).
 * To use, call {@link #setGTS} and {@link #setAcceptor} and optionally {@link #setState}
 * then call {@link #play()}.
 */
public abstract class Strategy {
    /**
     * Constructs a new strategy,
     * under the assumption that the instantiated class
     * is itself an instance of {@link ExploreIterator}.
     */
    protected Strategy() {
        this.iterator = (ExploreIterator) this;
    }

    /**
     * Constructs a new strategy,
     * for a given exploration iterator.
     * @param iterator the exploration iterator to be used.
     */
    protected Strategy(ExploreIterator iterator) {
        this.iterator = iterator;
    }

    /**
     * Sets the GTS to be explored, in preparation
     * to a call of {@link #play()}.
     * Also sets the state to be explored to {@code null},
     * meaning that exploration will start at the start state of the GTS.
     */
    final public void setGTS(GTS gts) {
        this.gts = gts;
        this.startState = null;
    }

    /**
     * Sets the state to be explored, in preparation to a call of {@link #play()}.
     * It is assumed that the state (if not {@code null}) is already in the GTS.
     * @param state the start state for the exploration; if {@code null},
     * the GTS start state will be used
     */
    final public void setState(GraphState state) {
        this.startState = state;
    }

    /**
     * Adds an acceptor to the strategy.
     */
    final public void setAcceptor(Acceptor acceptor) {
        assert acceptor != null && !acceptor.isPrototype();
        this.acceptor = acceptor;
    }

    /**
     * Plays out this strategy, until a halting condition kicks in,
     * the thread is interrupted or the acceptor signals that exploration is done.
     */
    final public void play() {
        assert this.gts != null : "GTS not initialised";
        assert this.acceptor != null : "Acceptor not initialised";
        this.acceptor.prepare(this.gts);
        this.iterator.prepare(this.gts, this.startState, this.acceptor);
        collectKnownStates();
        this.interrupted = false;
        while (!this.acceptor.done() && this.iterator.hasNext() && !testInterrupted()) {
            this.lastState = this.iterator.doNext();
        }
        this.iterator.finish();
    }

    /**
     * Sets all states already in the state space to Flag.KNOWN.
     */
    private void collectKnownStates() {
        for (GraphState next : this.gts.nodeSet()) {
            next.setFlag(Flag.KNOWN, true);
        }
    }

    /** Signals if the last invocation of {@link #play} finished because the thread was interrupted. */
    final public boolean isInterrupted() {
        return this.interrupted;
    }

    /**
     * Tests if the thread has been interrupted, and stores the
     * result.
     */
    private boolean testInterrupted() {
        boolean result = this.interrupted;
        if (!result) {
            result = this.interrupted = Thread.currentThread().isInterrupted();
        }
        return result;
    }

    /** Returns the last state explored by the last invocation of {@link #play}.
     */
    final public GraphState getLastState() {
        return this.lastState;
    }

    /**
     * Returns a message recorded after exploration.
     * The message is non-{@code null} after {@link #play()} has returned.
     */
    final public String getMessage() {
        StringBuilder result = new StringBuilder();
        if (isInterrupted()) {
            result.append("Exploration interrupted. ");
        }
        result.append(this.acceptor.getMessage());
        return result.toString();
    }

    private final ExploreIterator iterator;
    /** Flag indicating that the last invocation of {@link #play} was interrupted. */
    private boolean interrupted;
    /** The graph transition system explored by the strategy. */
    private GTS gts;
    /**
     * Start state for exploration, set in the constructor.
     * If {@code null}, the GTS start state is selected at exploration time.
     */
    private GraphState startState;
    /** The acceptor to be used at the next exploration. */
    private Acceptor acceptor;
    /** The state returned by the last call of {@link ExploreIterator#doNext()}. */
    private GraphState lastState;
}
