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
 * $Id: ALabelEdge.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.graph;

import groove.util.line.Line;

/**
 * Abstract edge class that is its own label.
 * @author Arend Rensink
 * @version $Revision $
 */
public abstract class ALabelEdge<N extends Node> extends AEdge<N,ALabelEdge<N>> implements Label {
    /**
     * Constructs a new instance, for a given source and target node.
     */
    public ALabelEdge(N source, N target) {
        super(source, target);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    final public String text() {
        return toLine().toFlatString();
    }

    @Override
    final public Line toLine() {
        if (this.line == null) {
            this.line = computeLine();
        }
        return this.line;
    }

    private Line line;

    /** Callback method to compute the line returned by {@link #toLine()}. */
    abstract protected Line computeLine();

    /** In general, we do not expect labels to be reconstructable from a string. */
    @Override
    public String toParsableString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Label o) {
        int result = getRole().compareTo(o.getRole());
        if (result != 0) {
            return result;
        }
        return text().compareTo(o.text());
    }

    @Override
    public ALabelEdge<N> label() {
        return this;
    }

    @Override
    abstract public EdgeRole getRole();

    @Override
    protected int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + source().hashCode();
        result = prime * result + target().hashCode();
        result = prime * result + computeLabelHash();
        return result;
    }

    /** Computes the hash code for the label part of this edge. */
    abstract protected int computeLabelHash();

    @Override
    protected boolean isTypeEqual(Object obj) {
        return obj instanceof ALabelEdge;
    }

    /* Overwritten to avoid infinite recursion. */
    @Override
    abstract protected boolean isLabelEqual(Edge other);

    @Override
    protected String getLabelText() {
        return text();
    }
}
