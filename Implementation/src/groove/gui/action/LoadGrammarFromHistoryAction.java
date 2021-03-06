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
 * $Id: LoadGrammarFromHistoryAction.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.gui.action;

import groove.gui.Simulator;
import groove.io.store.SystemStore;

import java.io.IOException;

/**
 * Action that loads a new grammar from a history item of the simulator.
 * @author Arend Rensink
 * @version $Revision $
 */
public class LoadGrammarFromHistoryAction extends SimulatorAction {
    /**
     * Constructs an action that will load a grammar from a predefined
     * location.
     * @param location the location to load from; non-null
     * 
     */
    public LoadGrammarFromHistoryAction(Simulator simulator, String location,
            SystemStore store) {
        super(simulator, store.toString(), null);
        this.location = location;
        this.store = store;
    }

    @Override
    public void execute() {
        try {
            getActions().getLoadGrammarAction().load(this.store);
        } catch (IOException e) {
            showErrorDialog(e, "Can't load grammar: ");
        }
    }

    /** Returns the location that this action was initialised with. */
    public String getLocation() {
        return this.location;
    }

    /**
     * Two actions are considered equal if they load from the same
     * location.
     * @see #getLocation()
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LoadGrammarFromHistoryAction)
            && ((LoadGrammarFromHistoryAction) obj).getLocation().equals(
                getLocation());
    }

    /**
     * Returns the hash code of this action's location.
     * @see #getLocation()
     */
    @Override
    public int hashCode() {
        return getLocation().hashCode();
    }

    @Override
    public String toString() {
        return this.store.toString();
    }

    /** Location that this action loads from (non-null). */
    private final String location;
    /** System store associated with this action (non-null). */
    private final SystemStore store;
}