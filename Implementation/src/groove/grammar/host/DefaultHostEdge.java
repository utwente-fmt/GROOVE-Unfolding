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
 * $Id: DefaultHostEdge.java 5764 2015-12-14 22:07:04Z rensink $
 */
package groove.grammar.host;

import java.util.LinkedHashSet;
import java.util.Set;

import groove.grammar.AnchorKind;
import groove.grammar.type.TypeEdge;
import groove.grammar.type.TypeLabel;
import groove.graph.AEdge;
import groove.transform.Proof;
import groove.unfolding.prefix.EnrichedCondition;
import groove.unfolding.prefix.History;

/**
 * Class that implements the edges of a host graph.
 * @author Arend Rensink
 */
public class DefaultHostEdge extends AEdge<HostNode,TypeLabel> implements HostEdge {
    /** Constructor for a typed edge.
     * @param simple indicates if this is a simple or multi-edge.
     */
    protected DefaultHostEdge(HostNode source, TypeEdge type, HostNode target, int nr,
        boolean simple) {
        super(source, type.label(), target, nr);
        this.type = type;
        this.simple = simple;
        assert type != null;
    }

    // ------------------------------------------------------------------------
    // Overridden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean isSimple() {
        return this.simple;
    }

    @Override
    protected boolean isTypeEqual(Object obj) {
        return obj instanceof DefaultHostEdge;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DefaultHostEdge other = (DefaultHostEdge) obj;
        if (getType() != other.getType()) {
            return false;
        }
        return true;
    }

    @Override
    public TypeEdge getType() {
        return this.type;
    }

    @Override
    public AnchorKind getAnchorKind() {
        return AnchorKind.EDGE;
    }

    /** Flag indicating whether this is a simple or multi-edge. */
    private final boolean simple;
    /** Non-{@code null} type of this edge. */
    private final TypeEdge type;

    /**********************************************
     * This File has been Hijacked for Unfolding purposes defined below
     **********************************************/
    
    private Proof proof;
    private boolean created = false;

    public Proof getProof() {
        return this.proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

    public boolean isCreated() {
        return this.created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

	//--------------------------------------
    private Set<EnrichedCondition> generationHistories = new LinkedHashSet<EnrichedCondition>();
    
    private Set<EnrichedCondition> readingHistories = new LinkedHashSet<EnrichedCondition>();
    
    
    public Set<EnrichedCondition> getGenerationHistories() {
		return generationHistories;
	}

	public void addGenerationHistory(EnrichedCondition genHistory) {
		this.generationHistories.add(genHistory);
	}

	public Set<EnrichedCondition> getReadingHistories() {
		return readingHistories;
	}

	public void addReadingHistories(EnrichedCondition readingHistory) {
		this.readingHistories.add(readingHistory);
	}
    
}
