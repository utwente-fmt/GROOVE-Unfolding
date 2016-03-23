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
package groove.unfolding.dependency;

import groove.transform.Proof;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author s1018078
 * @version $Revision $
 */
public class Dependencies {

    private Set<Set<String>> stringDependencies = new LinkedHashSet<Set<String>>();
    private Set<Set<Proof>> proofDependencies = new LinkedHashSet<Set<Proof>>();
    private boolean conflict;

    public Set<Set<String>> getDependencies() {
        return this.stringDependencies;
    }

    public void setDependencies(Set<Set<String>> dependencies) {
        this.stringDependencies = dependencies;
    }

    public Set<Set<Proof>> getProofDependencies() {
        return this.proofDependencies;
    }

    public void setProofDependencies(Set<Set<Proof>> proofDependencies) {
        this.proofDependencies = proofDependencies;
    }

    public boolean hasConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

}
