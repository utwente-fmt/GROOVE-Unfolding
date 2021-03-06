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
 * $Id: SmallCollection.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.util.collect;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Collection built either on a singleton element or on an inner collection.
 * Saves space with respect to an ordinary collection implementation if the
 * content is typically a singleton.
 * @author Arend Rensink
 * @version $Revision $
 */
public class SmallCollection<E> extends AbstractCollection<E> {
    /** Constructs an empty collection. */
    public SmallCollection() {
        // empty
    }

    /** Constructs a singleton collection. */
    public SmallCollection(E obj) {
        this.singleton = obj;
    }

    @Override
    public boolean add(E obj) {
        if (this.inner != null) {
            return this.inner.add(obj);
        } else if (this.singleton == null) {
            this.singleton = obj;
            return true;
        } else {
            this.inner = createCollection();
            this.inner.add(this.singleton);
            this.singleton = null;
            return this.inner.add(obj);
        }
    }

    @Override
    public void clear() {
        this.inner = null;
        this.singleton = null;
    }

    @Override
    public boolean contains(Object obj) {
        if (this.inner != null) {
            return this.inner.contains(obj);
        } else {
            return this.singleton != null && this.singleton.equals(obj);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.inner == null && this.singleton == null
            || this.inner.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        if (this.inner != null) {
            return this.inner.iterator();
        } else if (this.singleton == null) {
            return Collections.<E>emptyList().iterator();
        } else {
            return Collections.singleton(this.singleton).iterator();
        }
    }

    @Override
    public boolean remove(Object obj) {
        boolean result;
        if (this.inner == null) {
            result = this.singleton != null && this.singleton.equals(obj);
            if (result) {
                this.singleton = null;
            }
        } else {
            result = this.inner.remove(obj);
            if (result && this.inner.size() == 1) {
                this.singleton = this.inner.iterator().next();
                this.inner = null;
            }
        }
        return result;
    }

    @Override
    public int size() {
        if (this.inner == null) {
            return this.singleton == null ? 0 : 1;
        } else {
            return this.inner.size();
        }
    }

    /** Indicates is there is precisely one element in this collection. */
    public boolean isSingleton() {
        return this.singleton != null
            || (this.inner != null && this.inner.size() == 1);
    }

    /**
     * Returns the unique element in this collection, or <code>null</code> if
     * the collection is not a singleton.
     * @return the unique element in this collection, of <code>null</code>
     * @see #isSingleton()
     */
    public E getSingleton() {
        E result = this.singleton;
        if (result == null && this.inner != null && this.inner.size() == 1) {
            result = this.inner.iterator().next();
        }
        return result;
    }

    /** Factory method to create the inner (non-singular) collection. */
    protected Collection<E> createCollection() {
        return new ArrayList<E>();
    }

    /** The singleton element, if the collection is a singleton. */
    private E singleton;
    /** The inner (non-singular) collection. */
    private Collection<E> inner;
}
