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
 * $Id: VisualValue.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.gui.look;

import groove.gui.jgraph.JCell;

/**
 * Strategy to compute a new value for a refreshable {@link VisualKey}.
 * @author Arend Rensink
 * @version $Revision $
 */
public interface VisualValue<T> {
    /** Computes and returns a new value for the relevant key. */
    T get(JCell<?> cell);
}
