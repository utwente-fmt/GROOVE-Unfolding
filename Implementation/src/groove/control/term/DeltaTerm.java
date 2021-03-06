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
 * $Id: DeltaTerm.java 5572 2014-10-13 07:19:49Z rensink $
 */
package groove.control.term;

import groove.util.collect.Pool;

/**
 * Deadlock.
 * @author Arend Rensink
 * @version $Revision $
 */
public class DeltaTerm extends Term {
    /**
     * Constructs a delta term.
     */
    public DeltaTerm(Pool<Term> pool, int depth) {
        super(pool, Term.Op.DELTA);
        this.depth = depth;
    }

    @Override
    protected DerivationAttempt computeAttempt(boolean nested) {
        return null;
    }

    @Override
    protected int computeDepth() {
        return this.depth;
    }

    private final int depth;

    @Override
    protected Type computeType() {
        return Type.DEAD;
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }

    @Override
    protected int computeHashCode() {
        int prime = 31;
        return prime * super.computeHashCode() + getTransience();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((DeltaTerm) obj).getTransience() == getTransience();
    }

    @Override
    public String toString() {
        String result = super.toString();
        if (getTransience() > 0) {
            result += "(" + getTransience() + ")";
        }
        return result;
    }
}
